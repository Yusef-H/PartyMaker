package com.example.partymaker.data.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for network operations. Provides methods for checking network connectivity,
 * handling retries, and categorizing errors.
 */
public class NetworkUtils {
  private static final String TAG = "NetworkUtils";
  private static final int DEFAULT_MAX_RETRIES = 3;
  private static final int DEFAULT_RETRY_DELAY_MS = 1000;
  private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
  private static final int DEFAULT_READ_TIMEOUT = 15000;

  private static final ExecutorService executor = Executors.newCachedThreadPool();
  private static final Handler mainHandler = new Handler(Looper.getMainLooper());

  /**
   * Checks if the device has an active network connection
   *
   * @param context Application context
   * @return true if connected, false otherwise
   */
  public static boolean isNetworkAvailable(Context context) {
    if (context == null) {
      return false;
    }

    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) {
      return false;
    }

    NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
    return capabilities != null
        && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
  }

  /**
   * Executes a network operation with automatic retries
   *
   * @param operation The operation to execute
   * @param callback Callback for success/failure
   * @param <T> The return type of the operation
   */
  public static <T> void executeWithRetry(Callable<T> operation, RetryCallback<T> callback) {
    executeWithRetry(operation, callback, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MS);
  }

  /**
   * Executes a network operation with automatic retries and custom parameters
   *
   * @param operation The operation to execute
   * @param callback Callback for success/failure
   * @param maxRetries Maximum number of retry attempts
   * @param retryDelayMs Delay between retries in milliseconds
   * @param <T> The return type of the operation
   */
  public static <T> void executeWithRetry(
      Callable<T> operation, RetryCallback<T> callback, int maxRetries, int retryDelayMs) {

    AtomicInteger attempts = new AtomicInteger(0);

    executor.execute(
        () -> {
          try {
            T result =
                executeWithRetryInternal(operation, callback, attempts, maxRetries, retryDelayMs);
            mainHandler.post(() -> callback.onSuccess(result));
          } catch (Exception e) {
            Log.e(TAG, "Operation failed after " + attempts.get() + " attempts", e);
            ErrorType errorType = categorizeError(e);
            mainHandler.post(() -> callback.onFailure(errorType, e.getMessage()));
          }
        });
  }

  /** Internal method to handle retry logic */
  private static <T> T executeWithRetryInternal(
      Callable<T> operation,
      RetryCallback<T> callback,
      AtomicInteger attempts,
      int maxRetries,
      int retryDelayMs)
      throws Exception {

    while (true) {
      int currentAttempt = attempts.incrementAndGet();

      try {
        T result = operation.call();
        if (result != null) {
          return result;
        } else {
          throw new IOException("Operation returned null result");
        }
      } catch (Exception e) {
        Log.w(TAG, "Attempt " + currentAttempt + " failed: " + e.getMessage());

        if (currentAttempt >= maxRetries) {
          throw e;
        }

        // Notify about retry
        mainHandler.post(() -> callback.onRetry(currentAttempt, e));

        // Exponential backoff
        long delay = retryDelayMs * (long) Math.pow(2, currentAttempt - 1);
        Thread.sleep(delay);
      }
    }
  }

  /**
   * Categorizes an exception into an error type
   *
   * @param e The exception to categorize
   * @return The error type
   */
  public static ErrorType categorizeError(Exception e) {
    if (e instanceof UnknownHostException) {
      return ErrorType.NO_NETWORK;
    } else if (e instanceof SocketTimeoutException) {
      return ErrorType.TIMEOUT;
    } else if (e instanceof IOException) {
      String message = e.getMessage();
      if (message != null) {
        if (message.contains("4")) {
          return ErrorType.CLIENT_ERROR;
        } else if (message.contains("5")) {
          return ErrorType.SERVER_ERROR;
        }
      }
    }

    return ErrorType.UNKNOWN;
  }

  /**
   * Gets a user-friendly error message based on the error type
   *
   * @param errorType The error type
   * @return A user-friendly error message
   */
  public static String getErrorMessage(ErrorType errorType) {
    switch (errorType) {
      case NO_NETWORK:
        return "No internet connection. Please check your network settings and try again.";
      case TIMEOUT:
        return "Connection timed out. The server might be busy, please try again later.";
      case SERVER_ERROR:
        return "Server error. Our team has been notified and is working on a fix.";
      case CLIENT_ERROR:
        return "Something went wrong with your request. Please try again.";
      case UNKNOWN:
      default:
        return "An unexpected error occurred. Please try again later.";
    }
  }

  /**
   * Tests connectivity to a specific URL
   *
   * @param urlString The URL to test
   * @return true if the URL is reachable, false otherwise
   */
  public static boolean testConnection(String urlString) {
    HttpURLConnection connection = null;
    try {
      URL url = new URL(urlString);
      connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
      connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
      connection.setRequestMethod("HEAD");
      int responseCode = connection.getResponseCode();
      return responseCode == HttpURLConnection.HTTP_OK;
    } catch (Exception e) {
      Log.e(TAG, "Error testing connection to " + urlString, e);
      return false;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /** Cancels all pending operations */
  public static void cancelAllOperations() {
    executor.shutdownNow();
    try {
      if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
        Log.w(TAG, "Executor did not terminate in the specified time.");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** Enum representing different types of network errors */
  public enum ErrorType {
    NO_NETWORK,
    TIMEOUT,
    SERVER_ERROR,
    CLIENT_ERROR,
    UNKNOWN
  }

  /** Interface for retry callbacks */
  public interface RetryCallback<T> {
    void onSuccess(T result);

    void onFailure(ErrorType errorType, String errorMessage);

    void onRetry(int attemptCount, Exception e);
  }
}
