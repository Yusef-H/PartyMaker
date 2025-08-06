package com.example.partymaker.utils.infrastructure.async;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Modern replacement for deprecated AsyncTask using ExecutorService and Handler. Provides a clean
 * API for background operations with UI thread callbacks.
 */
public class AsyncTaskReplacement {
  private static final String TAG = "AsyncTaskReplacement";

  // Shared thread pool for background operations
  private static final ExecutorService BACKGROUND_EXECUTOR =
      Executors.newCachedThreadPool(
          r -> {
            Thread thread = new Thread(r, "AsyncTask-Background");
            thread.setDaemon(true);
            return thread;
          });

  // Handler for main thread operations
  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

  /** Interface for background operations */
  public interface BackgroundTask<T> {
    T doInBackground() throws Exception;
  }

  /** Interface for UI thread callbacks */
  public interface UICallback<T> {
    void onPreExecute();

    void onPostExecute(T result);

    void onError(Exception error);
  }

  /** Simple UI callback with default implementations */
  public abstract static class SimpleUICallback<T> implements UICallback<T> {
    @Override
    public void onPreExecute() {
      // Default empty implementation
    }

    @Override
    public void onError(Exception error) {
      Log.e(TAG, "Background task failed", error);
    }
  }

  /** Execute a background task with UI callbacks */
  public static <T> Future<?> execute(
      @NonNull BackgroundTask<T> backgroundTask, @NonNull UICallback<T> uiCallback) {

    // Run onPreExecute on main thread
    MAIN_HANDLER.post(uiCallback::onPreExecute);

    // Submit background task
    return BACKGROUND_EXECUTOR.submit(
        () -> {
          try {
            // Execute background operation
            T result = backgroundTask.doInBackground();

            // Post result to main thread
            MAIN_HANDLER.post(() -> uiCallback.onPostExecute(result));

          } catch (Exception e) {
            Log.e(TAG, "Background task execution failed", e);

            // Post error to main thread
            MAIN_HANDLER.post(() -> uiCallback.onError(e));
          }
        });
  }

  /** Execute a background task with simple result callback */
  public static <T> Future<?> execute(
      @NonNull BackgroundTask<T> backgroundTask, @NonNull ResultCallback<T> resultCallback) {

    return execute(
        backgroundTask,
            new SimpleUICallback<>() {
                @Override
                public void onPostExecute(T result) {
                    resultCallback.onResult(result);
                }

                @Override
                public void onError(Exception error) {
                    resultCallback.onError(error);
                }
            });
  }

  /** Execute a background task without UI callbacks */
  public static Future<?> execute(@NonNull Runnable backgroundTask) {
    return BACKGROUND_EXECUTOR.submit(backgroundTask);
  }

  /** Execute a callable background task */
  public static <T> Future<T> submit(@NonNull Callable<T> backgroundTask) {
    return BACKGROUND_EXECUTOR.submit(backgroundTask);
  }

  /** Simple callback interface for results */
  public interface ResultCallback<T> {
    void onResult(T result);

    void onError(Exception error);
  }

  /** Utility class for network operations (common AsyncTask use case) */
  public static class NetworkTask {

    /** Execute HTTP request in background */
    public static Future<?> executeRequest(
        @NonNull HttpRequestTask requestTask, @NonNull NetworkCallback networkCallback) {

      return execute(
          () -> {
            try {
              return requestTask.executeRequest();
            } catch (Exception e) {
              throw new NetworkException("Network request failed", e);
            }
          },
              new SimpleUICallback<>() {
                  @Override
                  public void onPostExecute(String result) {
                      networkCallback.onSuccess(result);
                  }

                  @Override
                  public void onError(Exception error) {
                      if (error instanceof NetworkException) {
                          networkCallback.onError((NetworkException) error);
                      } else {
                          networkCallback.onError(new NetworkException("Unexpected error", error));
                      }
                  }
              });
    }

    /** Interface for HTTP request implementations */
    public interface HttpRequestTask {
      String executeRequest() throws Exception;
    }

    /** Callback for network operations */
    public interface NetworkCallback {
      void onSuccess(String response);

      void onError(NetworkException error);
    }

    /** Network-specific exception */
    public static class NetworkException extends Exception {
      public NetworkException(String message) {
        super(message);
      }

      public NetworkException(String message, Throwable cause) {
        super(message, cause);
      }
    }
  }

  /** Utility class for database operations (common AsyncTask use case) */
  public static class DatabaseTask {

    /** Execute database operation in background */
    public static <T> Future<?> executeDbOperation(
        @NonNull DatabaseOperation<T> dbOperation, @NonNull DatabaseCallback<T> dbCallback) {

      return execute(
          () -> {
            try {
              return dbOperation.execute();
            } catch (Exception e) {
              throw new DatabaseException("Database operation failed", e);
            }
          },
              new SimpleUICallback<>() {
                  @Override
                  public void onPostExecute(T result) {
                      dbCallback.onSuccess(result);
                  }

                  @Override
                  public void onError(Exception error) {
                      if (error instanceof DatabaseException) {
                          dbCallback.onError((DatabaseException) error);
                      } else {
                          dbCallback.onError(new DatabaseException("Unexpected database error", error));
                      }
                  }
              });
    }

    /** Interface for database operations */
    public interface DatabaseOperation<T> {
      T execute() throws Exception;
    }

    /** Callback for database operations */
    public interface DatabaseCallback<T> {
      void onSuccess(T result);

      void onError(DatabaseException error);
    }

    /** Database-specific exception */
    public static class DatabaseException extends Exception {
      public DatabaseException(String message) {
        super(message);
      }

      public DatabaseException(String message, Throwable cause) {
        super(message, cause);
      }
    }
  }

  /** Shutdown the executor service (call this when application exits) */
  public static void shutdown() {
    try {
      BACKGROUND_EXECUTOR.shutdown();
      Log.d(TAG, "AsyncTask replacement executor shut down");
    } catch (Exception e) {
      Log.e(TAG, "Error shutting down executor", e);
    }
  }

  /** Check if executor is shut down */
  public static boolean isShutdown() {
    return BACKGROUND_EXECUTOR.isShutdown();
  }

  /** Get the number of active background tasks (for monitoring) */
  public static String getExecutorStatus() {
    if (BACKGROUND_EXECUTOR instanceof java.util.concurrent.ThreadPoolExecutor) {
      java.util.concurrent.ThreadPoolExecutor tpe =
          (java.util.concurrent.ThreadPoolExecutor) BACKGROUND_EXECUTOR;
      return String.format(
          Locale.US,
          "Active: %d, Pool: %d, Queue: %d, Completed: %d",
          tpe.getActiveCount(),
          tpe.getPoolSize(),
          tpe.getQueue().size(),
          tpe.getCompletedTaskCount());
    }
    return "Status unknown";
  }
}
