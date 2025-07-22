package com.example.partymaker.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Process;
import android.util.Log;
import java.io.File;
import java.util.Locale;

/**
 * Utility class for managing memory. Provides methods for checking memory usage and clearing
 * caches.
 */
public class MemoryManager {
  private static final String TAG = "MemoryManager";
  private static final int LOW_MEMORY_THRESHOLD_PERCENTAGE = 15;

  /**
   * Checks if the device is running low on memory.
   *
   * @param context The context
   * @return true if the device is running low on memory, false otherwise
   */
  public static boolean isLowMemory(Context context) {
    ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    activityManager.getMemoryInfo(memoryInfo);

    // Calculate the percentage of available memory
    float percentAvailable = 100.0f * memoryInfo.availMem / memoryInfo.totalMem;

    // Log memory info
    Log.d(
        TAG,
        String.format(
            "Memory - Available: %.2f MB, Total: %.2f MB, %.1f%% free",
            memoryInfo.availMem / (1024.0f * 1024.0f),
            memoryInfo.totalMem / (1024.0f * 1024.0f),
            percentAvailable));

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

    String sb =
        "Memory Info:\n"
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

    return sb;
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
}
