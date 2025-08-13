package com.example.partymaker.utils.infrastructure;

import android.util.Log;
import com.example.partymaker.BuildConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance monitoring utility for tracking operation timing and memory usage.
 * Only active in debug builds to avoid performance overhead in production.
 */
public class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";
    private static final Map<String, Long> timingMap = new ConcurrentHashMap<>();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    
    // Performance thresholds
    private static final long SLOW_OPERATION_THRESHOLD_MS = 1000;
    private static final long MEMORY_WARNING_THRESHOLD_PERCENT = 80;
    private static final long CRITICAL_MEMORY_THRESHOLD_PERCENT = 90;
    
    // Memory constants
    private static final long MB_TO_BYTES = 1024 * 1024;
    private static final String MEMORY_FORMAT = "%s - Memory: %d/%d MB (%.1f%%)";
    
    /**
     * Starts timing an operation.
     * 
     * @param operation The operation name to track
     */
    public static void startTiming(String operation) {
        if (!DEBUG) return;
        
        timingMap.put(operation, System.currentTimeMillis());
        Log.d(TAG, "Started: " + operation);
    }
    
    /**
     * Ends timing an operation and logs the duration.
     * 
     * @param operation The operation name to stop tracking
     */
    public static void endTiming(String operation) {
        if (!DEBUG) return;
        
        Long startTime = timingMap.remove(operation);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            Log.d(TAG, operation + " took: " + duration + "ms");
            
            if (duration > SLOW_OPERATION_THRESHOLD_MS) {
                Log.w(TAG, "SLOW OPERATION: " + operation + " took " + duration + "ms");
            }
        } else {
            Log.w(TAG, "Attempted to end timing for operation that was not started: " + operation);
        }
    }
    
    /**
     * Tracks current memory usage at a specific location.
     * 
     * @param location The location identifier for logging
     */
    public static void trackMemoryUsage(String location) {
        if (!DEBUG) return;
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryPercent = (usedMemory * 100.0) / maxMemory;
        
        Log.d(TAG, String.format(MEMORY_FORMAT,
                location,
                usedMemory / MB_TO_BYTES,
                maxMemory / MB_TO_BYTES,
                memoryPercent));
        
        // Log warnings for high memory usage
        if (memoryPercent > CRITICAL_MEMORY_THRESHOLD_PERCENT) {
            Log.e(TAG, "CRITICAL MEMORY USAGE at " + location + " (" + String.format("%.1f", memoryPercent) + "%)");
            // Force garbage collection for critical memory situations
            System.gc();
        } else if (memoryPercent > MEMORY_WARNING_THRESHOLD_PERCENT) {
            Log.w(TAG, "HIGH MEMORY USAGE at " + location + " (" + String.format("%.1f", memoryPercent) + "%)");
        }
    }
    
    /**
     * Tracks memory usage with additional heap information.
     * 
     * @param location The location identifier for logging
     */
    public static void trackDetailedMemoryUsage(String location) {
        if (!DEBUG) return;
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        long availableMemory = maxMemory - usedMemory;
        
        double memoryPercent = (usedMemory * 100.0) / maxMemory;
        
        Log.d(TAG, String.format("%s - Detailed Memory Stats:", location));
        Log.d(TAG, String.format("  Used: %d MB (%.1f%%)", usedMemory / MB_TO_BYTES, memoryPercent));
        Log.d(TAG, String.format("  Available: %d MB", availableMemory / MB_TO_BYTES));
        Log.d(TAG, String.format("  Total Heap: %d MB", totalMemory / MB_TO_BYTES));
        Log.d(TAG, String.format("  Max Heap: %d MB", maxMemory / MB_TO_BYTES));
        
        if (memoryPercent > MEMORY_WARNING_THRESHOLD_PERCENT) {
            Log.w(TAG, "Memory usage is high at " + location);
        }
    }
    
    /**
     * Measures and logs the execution time of a Runnable operation.
     * 
     * @param operation The operation name
     * @param runnable The operation to execute and measure
     */
    public static void measureOperation(String operation, Runnable runnable) {
        if (!DEBUG) {
            runnable.run();
            return;
        }
        
        startTiming(operation);
        try {
            runnable.run();
        } finally {
            endTiming(operation);
        }
    }
    
    /**
     * Logs current thread information for debugging.
     * 
     * @param location The location identifier
     */
    public static void logThreadInfo(String location) {
        if (!DEBUG) return;
        
        Thread currentThread = Thread.currentThread();
        Log.d(TAG, String.format("%s - Thread: %s [%s], Priority: %d",
                location,
                currentThread.getName(),
                currentThread.getState(),
                currentThread.getPriority()));
    }
    
    /**
     * Clears all active timing operations. Useful for cleanup.
     */
    public static void clearTimings() {
        if (!DEBUG) return;
        
        int clearedCount = timingMap.size();
        timingMap.clear();
        Log.d(TAG, "Cleared " + clearedCount + " active timing operations");
    }
    
    /**
     * Gets the count of currently active timing operations.
     * 
     * @return The number of active timings
     */
    public static int getActiveTimingCount() {
        return timingMap.size();
    }
    
    /**
     * Logs a performance benchmark result.
     * 
     * @param benchmarkName The benchmark name
     * @param operationCount The number of operations performed
     * @param totalTimeMs The total time taken in milliseconds
     */
    public static void logBenchmark(String benchmarkName, int operationCount, long totalTimeMs) {
        if (!DEBUG) return;
        
        double averageTimeMs = (double) totalTimeMs / operationCount;
        double operationsPerSecond = (operationCount * 1000.0) / totalTimeMs;
        
        Log.i(TAG, String.format("BENCHMARK: %s", benchmarkName));
        Log.i(TAG, String.format("  Operations: %d", operationCount));
        Log.i(TAG, String.format("  Total Time: %d ms", totalTimeMs));
        Log.i(TAG, String.format("  Average: %.2f ms/op", averageTimeMs));
        Log.i(TAG, String.format("  Throughput: %.2f ops/sec", operationsPerSecond));
    }
}