package com.example.partymaker.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.Process;
import android.util.Log;
import java.io.File;

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

    StringBuilder sb = new StringBuilder();
    sb.append("Memory Info:\n");
    sb.append("- Max: ").append(FileManager.formatSize(maxMemory)).append("\n");
    sb.append("- Total: ").append(FileManager.formatSize(totalMemory)).append("\n");
    sb.append("- Used: ")
        .append(FileManager.formatSize(usedMemory))
        .append(" (")
        .append(String.format("%.1f%%", 100.0f * usedMemory / maxMemory))
        .append(")\n");
    sb.append("- Free: ").append(FileManager.formatSize(freeMemory)).append("\n");

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      sb.append("- PSS Total: ")
          .append(FileManager.formatSize(memoryInfo.getTotalPss() * 1024L))
          .append("\n");
      sb.append("- Private Dirty: ")
          .append(FileManager.formatSize(memoryInfo.getTotalPrivateDirty() * 1024L))
          .append("\n");
      sb.append("- Shared Dirty: ")
          .append(FileManager.formatSize(memoryInfo.getTotalSharedDirty() * 1024L))
          .append("\n");
    }

    return sb.toString();
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
