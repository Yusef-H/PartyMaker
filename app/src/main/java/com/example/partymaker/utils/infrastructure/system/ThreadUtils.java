package com.example.partymaker.utils.infrastructure.system;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Utility class for thread management. Provides executors for different types of tasks. */
public class ThreadUtils {
  private static final String TAG = "ThreadUtils";

  // CPU and thread pool configuration
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int MIN_CORE_THREADS = 2;
  private static final int MAX_CORE_THREADS = 4;
  private static final int CORE_POOL_SIZE =
      Math.max(MIN_CORE_THREADS, Math.min(CPU_COUNT - 1, MAX_CORE_THREADS));
  private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
  private static final int KEEP_ALIVE_SECONDS = 30;
  private static final int QUEUE_CAPACITY = 128;
  private static final int NETWORK_THREAD_DIVISOR = 2;

  // Thread priority constants
  private static final int BACKGROUND_THREAD_PRIORITY = Thread.MIN_PRIORITY;
  private static final int LIGHTWEIGHT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

  // Thread factories
  private static final ThreadFactory BACKGROUND_THREAD_FACTORY =
      new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
          Thread thread = new Thread(r, "PartyMaker-bg-" + mCount.getAndIncrement());
          thread.setPriority(BACKGROUND_THREAD_PRIORITY);
          return thread;
        }
      };

  private static final ThreadFactory NETWORK_THREAD_FACTORY =
      new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
          return new Thread(r, "PartyMaker-net-" + mCount.getAndIncrement());
        }
      };

  // Executors
  private static final Executor MAIN_THREAD_EXECUTOR = new MainThreadExecutor();

  private static final ThreadPoolExecutor BACKGROUND_EXECUTOR =
      new ThreadPoolExecutor(
          CORE_POOL_SIZE,
          MAXIMUM_POOL_SIZE,
          KEEP_ALIVE_SECONDS,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(QUEUE_CAPACITY),
          BACKGROUND_THREAD_FACTORY,
          new ThreadPoolExecutor.DiscardOldestPolicy());

  private static final Executor NETWORK_EXECUTOR =
      Executors.newFixedThreadPool(
          Math.max(MIN_CORE_THREADS, CPU_COUNT / NETWORK_THREAD_DIVISOR), NETWORK_THREAD_FACTORY);

  private static final Executor LIGHTWEIGHT_EXECUTOR =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread thread = new Thread(r, "PartyMaker-lightweight");
            thread.setPriority(LIGHTWEIGHT_THREAD_PRIORITY);
            return thread;
          });

  static {
    BACKGROUND_EXECUTOR.allowCoreThreadTimeOut(true);
  }

  /**
   * Gets the main thread executor.
   *
   * @return The main thread executor
   */
  public static Executor getMainThreadExecutor() {
    return MAIN_THREAD_EXECUTOR;
  }

  /**
   * Gets the background executor.
   *
   * @return The background executor
   */
  public static Executor getBackgroundExecutor() {
    return BACKGROUND_EXECUTOR;
  }

  /**
   * Gets the network executor.
   *
   * @return The network executor
   */
  public static Executor getNetworkExecutor() {
    return NETWORK_EXECUTOR;
  }

  /**
   * Gets the lightweight executor.
   *
   * @return The lightweight executor
   */
  public static Executor getLightweightExecutor() {
    return LIGHTWEIGHT_EXECUTOR;
  }

  /**
   * Runs a task on the main thread.
   *
   * @param runnable The task to run
   */
  public static void runOnMainThread(Runnable runnable) {
    if (isMainThread()) {
      runnable.run();
    } else {
      MAIN_THREAD_EXECUTOR.execute(runnable);
    }
  }

  /**
   * Runs a task on the main thread with a delay.
   *
   * @param runnable The task to run
   * @param delayMillis The delay in milliseconds
   */
  public static void runOnMainThreadDelayed(Runnable runnable, long delayMillis) {
    new Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis);
  }

  /**
   * Runs a task on a background thread.
   *
   * @param runnable The task to run
   */
  public static void runInBackground(Runnable runnable) {
    BACKGROUND_EXECUTOR.execute(runnable);
  }

  /**
   * Runs a task on the network thread.
   *
   * @param runnable The task to run
   */
  public static void runOnNetworkThread(Runnable runnable) {
    NETWORK_EXECUTOR.execute(runnable);
  }

  /**
   * Checks if the current thread is the main thread.
   *
   * @return true if the current thread is the main thread, false otherwise
   */
  public static boolean isMainThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

  /**
   * Sleeps the current thread for the specified time.
   *
   * @param milliseconds Time to sleep in milliseconds
   */
  public static void sleep(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** An executor that runs tasks on the main thread. */
  private static class MainThreadExecutor implements Executor {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(@NonNull Runnable command) {
      mHandler.post(command);
    }
  }
}
