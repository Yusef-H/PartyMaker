package com.example.partymaker.utils.infrastructure.system;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Process;
import android.util.Log;

import com.example.partymaker.PartyApplication;
import com.example.partymaker.utils.media.FileManager;
import com.example.partymaker.utils.media.ImageOptimizationManager;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced memory manager with singleton pattern and advanced memory management capabilities.
 * Provides methods for checking memory usage, clearing caches, and performing memory cleanup.
 */
public final class MemoryManager {
  private static final String TAG = "MemoryManager";
  private static MemoryManager instance;
  private final Map<String, WeakReference<Object>> objectRegistry = new ConcurrentHashMap<>();

  // Memory threshold constants
  private static final int LOW_MEMORY_THRESHOLD_PERCENTAGE = 15;
  private static final float PERCENTAGE_MULTIPLIER = 100.0f;
  private static final int CRITICAL_MEMORY_THRESHOLD = 85;

  // Memory unit conversion constants
  private static final float BYTES_TO_MB = 1024.0f * 1024.0f;
  private static final long BYTES_TO_KB = 1024L;
  
  /**
   * Gets the singleton instance of MemoryManager.
   */
  public static MemoryManager getInstance() {
    if (instance == null) {
      synchronized (MemoryManager.class) {
        if (instance == null) {
          instance = new MemoryManager();
        }
      }
    }
    return instance;
  }

  /**
   * Checks if the device is running low on memory.
   *
   * @param context The context (cannot be null)
   * @return true if the device is running low on memory, false otherwise
   * @throws IllegalArgumentException if context is null
   */
  public static boolean isLowMemory(Context context) {
    validateNotNull(context);

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
   * Enhanced cleanup of unused resources with weak reference management.
   */
  public void clearUnusedResources() {
    // Clear weak references
    objectRegistry.entrySet().removeIf(entry -> entry.getValue().get() == null);
    
    // Clear Glide memory cache in background
    ThreadUtils.executeImageTask(() -> {
      Context context = getApplicationContext();
      if (context != null) {
        ImageOptimizationManager.clearMemoryCache(context);
      }
    });
    
    // Suggest garbage collection
    System.gc();
    
    Log.d(TAG, "Cleared unused resources. Active objects: " + objectRegistry.size());
  }
  
  /**
   * Clears image cache both memory and disk.
   */
  public void clearImageCache() {
    Context context = getApplicationContext();
    if (context == null) return;
    
    // Clear Glide disk cache in background
    ImageOptimizationManager.clearDiskCache(context);
    
    // Clear memory cache on main thread
    ImageOptimizationManager.clearMemoryCache(context);
  }
  
  /**
   * Emergency cleanup for critical memory situations.
   */
  public void emergencyCleanup() {
    Log.w(TAG, "Emergency memory cleanup triggered");
    
    // Clear all caches
    clearImageCache();
    clearUnusedResources();
    
    Context context = getApplicationContext();
    if (context != null) {
      // Clear file caches
      FileManager.clearCache(context);
    }
    
    // Clear system caches
    System.runFinalization();
    System.gc();
    
    // Log memory stats
    logMemoryStats();
  }
  
  /**
   * Registers an object for memory tracking.
   */
  public void registerObject(String key, Object obj) {
    objectRegistry.put(key, new WeakReference<>(obj));
  }
  
  /**
   * Unregisters an object from memory tracking.
   */
  public void unregisterObject(String key) {
    objectRegistry.remove(key);
  }
  
  /**
   * Logs current memory statistics and performs emergency cleanup if needed.
   */
  public void logMemoryStats() {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    long maxMemory = runtime.maxMemory();
    long availableMemory = maxMemory - usedMemory;
    
    Log.i(TAG, String.format("Memory Stats - Used: %dMB, Available: %dMB, Max: %dMB (%.1f%% used)",
        usedMemory / (1024 * 1024),
        availableMemory / (1024 * 1024),
        maxMemory / (1024 * 1024),
        (usedMemory * 100.0) / maxMemory));
        
    // Alert if memory is critical
    if ((usedMemory * 100.0) / maxMemory > CRITICAL_MEMORY_THRESHOLD) {
      Log.e(TAG, "CRITICAL MEMORY USAGE!");
      emergencyCleanup();
    }
  }

  /**
   * Performs memory cleanup when the app is running low on memory.
   *
   * @param context The context
   */
  public static void performMemoryCleanup(Context context) {
    Log.d(TAG, "Performing memory cleanup");

    // Use instance method for enhanced cleanup
    getInstance().emergencyCleanup();

    // Log memory after cleanup
    Log.d(TAG, "Memory after cleanup: " + FileManager.formatSize(getAppMemoryUsage()));
  }
  
  /**
   * Gets application context safely.
   */
  private Context getApplicationContext() {
    try {
      return PartyApplication.getInstance();
    } catch (Exception e) {
      Log.w(TAG, "Could not get application context", e);
      return null;
    }
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
   * @throws IllegalArgumentException if obj is null
   */
  private static void validateNotNull(Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException("Context cannot be null");
    }
  }

  // Private constructor for singleton pattern
  private MemoryManager() {
    // Initialize memory manager
  }
}
