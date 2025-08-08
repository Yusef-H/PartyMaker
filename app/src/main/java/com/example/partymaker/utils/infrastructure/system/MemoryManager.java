package com.example.partymaker.utils.infrastructure.system;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Process;
import android.util.Log;
import com.example.partymaker.utils.media.FileManager;
import com.example.partymaker.utils.media.GlideImageLoader;
import java.io.File;
import java.util.Locale;

/**
 * Utility class for managing memory operations. Provides methods for checking memory usage,
 * clearing caches, and performing memory cleanup. All methods are static and thread-safe.
 */
public final class MemoryManager {
  private static final String TAG = "MemoryManager";

  // Memory threshold constants
  private static final int LOW_MEMORY_THRESHOLD_PERCENTAGE = 15;
  private static final float PERCENTAGE_MULTIPLIER = 100.0f;

  // Memory unit conversion constants
  private static final float BYTES_TO_MB = 1024.0f * 1024.0f;
  private static final long BYTES_TO_KB = 1024L;

  /**
   * Checks if the device is running low on memory.
   *
   * @param context The context (cannot be null)
   * @return true if the device is running low on memory, false otherwise
   * @throws IllegalArgumentException if context is null
   */
  public static boolean isLowMemory(Context context) {
    validateNotNull(context, "Context cannot be null");

    ActivityManager activityManager = getActivityManager(context);
    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(memoryInfo);

    float percentAvailable = PERCENTAGE_MULTIPLIER * memoryInfo.availMem / memoryInfo.totalMem;

    logMemoryInfo(memoryInfo, percentAvailable);

    return percentAvailable < LOW_MEMORY_THRESHOLD_PERCENTAGE || memoryInfo.lowMemory;
  }

  /**
   * Gets the current memory usage of the app.
   *
   * @return The memory usage in bytes
   */
  public static long getAppMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() - runtime.freeMemory();
  }

  /**
   * Gets detailed memory information.
   *
   * @return A string with detailed memory information
   */
  public static String getDetailedMemoryInfo() {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long usedMemory = totalMemory - freeMemory;

    Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
    Debug.getMemoryInfo(memoryInfo);

    return "Memory Info:\n"
        + "- Max: "
        + FileManager.formatSize(maxMemory)
        + "\n"
        + "- Total: "
        + FileManager.formatSize(totalMemory)
        + "\n"
        + "- Used: "
        + FileManager.formatSize(usedMemory)
        + " ("
        + String.format(Locale.ROOT, "%.1f%%", 100.0f * usedMemory / maxMemory)
        + ")\n"
        + "- Free: "
        + FileManager.formatSize(freeMemory)
        + "\n"
        + "- PSS Total: "
        + FileManager.formatSize(memoryInfo.getTotalPss() * 1024L)
        + "\n"
        + "- Private Dirty: "
        + FileManager.formatSize(memoryInfo.getTotalPrivateDirty() * 1024L)
        + "\n"
        + "- Shared Dirty: "
        + FileManager.formatSize(memoryInfo.getTotalSharedDirty() * 1024L)
        + "\n";
  }

  /**
   * Performs memory cleanup when the app is running low on memory.
   *
   * @param context The context
   */
  public static void performMemoryCleanup(Context context) {
    Log.d(TAG, "Performing memory cleanup");

    // Clear image caches
    GlideImageLoader.clearMemoryCache(context);

    // Clear file caches
    FileManager.clearCache(context);

    // Suggest garbage collection
    System.gc();

    // Log memory after cleanup
    Log.d(TAG, "Memory after cleanup: " + FileManager.formatSize(getAppMemoryUsage()));
  }

  /**
   * Gets the size of the app's cache directory.
   *
   * @param context The context
   * @return The size of the cache directory in bytes
   */
  public static long getCacheSize(Context context) {
    File cacheDir = context.getCacheDir();
    return FileManager.getSize(cacheDir);
  }

  /**
   * Gets the process ID of the current process.
   *
   * @return The process ID
   */
  public static int getProcessId() {
    return Process.myPid();
  }

  // Private helper methods

  /**
   * Gets the ActivityManager from the context.
   *
   * @param context The context to get the service from
   * @return The ActivityManager instance
   */
  private static ActivityManager getActivityManager(Context context) {
    return (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
  }

  /**
   * Logs memory information.
   *
   * @param memoryInfo The memory info object
   * @param percentAvailable The percentage of available memory
   */
  private static void logMemoryInfo(ActivityManager.MemoryInfo memoryInfo, float percentAvailable) {
    Log.d(
        TAG,
        String.format(
            Locale.ROOT,
            "Memory - Available: %.2f MB, Total: %.2f MB, %.1f%% free",
            memoryInfo.availMem / BYTES_TO_MB,
            memoryInfo.totalMem / BYTES_TO_MB,
            percentAvailable));
  }

  /**
   * Validates that an object is not null.
   *
   * @param obj The object to validate
   * @param message The error message if validation fails
   * @throws IllegalArgumentException if obj is null
   */
  private static void validateNotNull(Object obj, String message) {
    if (obj == null) {
      throw new IllegalArgumentException(message);
    }
  }

  // Private constructor to prevent instantiation
  private MemoryManager() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
