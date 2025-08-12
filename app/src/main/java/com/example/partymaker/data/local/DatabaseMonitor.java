package com.example.partymaker.data.local;

import android.content.Context;
import android.util.Log;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import java.io.File;

/**
 * Database performance monitoring utility for tracking query performance,
 * database size, and providing optimization insights.
 */
public class DatabaseMonitor {
    private static final String TAG = "DatabaseMonitor";
    private static final long SLOW_QUERY_THRESHOLD_MS = 500; // Queries over 500ms are considered slow
    private static final long DATABASE_SIZE_WARNING_MB = 50; // Warn when database exceeds 50MB
    
    /**
     * Logs query performance with timing information
     *
     * @param queryName The name/description of the query
     * @param startTime The start time in milliseconds
     */
    public static void logQueryPerformance(String queryName, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > SLOW_QUERY_THRESHOLD_MS) {
            Log.w(TAG, "SLOW QUERY: " + queryName + " took " + duration + "ms - Consider optimization");
        } else if (duration > 200) {
            Log.i(TAG, "MODERATE QUERY: " + queryName + " took " + duration + "ms");
        } else {
            Log.d(TAG, "FAST QUERY: " + queryName + " took " + duration + "ms");
        }
        
        // Log additional warnings for extremely slow queries
        if (duration > 2000) { // Over 2 seconds
            Log.e(TAG, "EXTREMELY SLOW QUERY: " + queryName + " took " + duration + "ms - URGENT optimization needed");
        }
    }
    
    /**
     * Logs comprehensive database statistics including table sizes and file size
     *
     * @param context The application context
     */
    public static void logDatabaseStats(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot log database stats");
            return;
        }
        
        ThreadUtils.executeDatabaseTask(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                logTableStatistics(db);
                logDatabaseFileSize(context);
                logPerformanceRecommendations(context);
            } catch (Exception e) {
                Log.e(TAG, "Error logging database stats", e);
            }
        });
    }
    
    /**
     * Logs statistics for each table in the database
     *
     * @param db The database instance
     */
    private static void logTableStatistics(AppDatabase db) {
        try {
            // Groups table stats
            int publicGroupCount = db.groupDao().getPublicGroupsCount();
            Log.i(TAG, "Database Stats - Public Groups: " + publicGroupCount);
            
            // Note: We can't easily get total group count without a user ID for getUserPrivateGroupsCount
            // This would need to be enhanced based on the actual data structure
            
            // Chat messages would need similar implementation
            Log.i(TAG, "Database table statistics logged");
            
        } catch (Exception e) {
            Log.w(TAG, "Could not retrieve all table statistics", e);
        }
    }
    
    /**
     * Logs database file size and provides warnings if too large
     *
     * @param context The application context
     */
    private static void logDatabaseFileSize(Context context) {
        String databaseName = "partymaker_database";
        File dbFile = context.getDatabasePath(databaseName);
        
        if (dbFile.exists()) {
            long sizeInBytes = dbFile.length();
            long sizeInMB = sizeInBytes / (1024 * 1024);
            
            Log.i(TAG, "Database file size: " + sizeInMB + " MB (" + sizeInBytes + " bytes)");
            
            if (sizeInMB > DATABASE_SIZE_WARNING_MB) {
                Log.w(TAG, "DATABASE SIZE WARNING: Database is " + sizeInMB + " MB, consider cleanup or archiving");
            }
            
            // Log WAL and SHM file sizes too
            logWalFileSize(context, databaseName);
            
        } else {
            Log.w(TAG, "Database file does not exist at: " + dbFile.getAbsolutePath());
        }
    }
    
    /**
     * Logs WAL (Write-Ahead Log) file size
     *
     * @param context The application context
     * @param databaseName The database name
     */
    private static void logWalFileSize(Context context, String databaseName) {
        File walFile = new File(context.getDatabasePath(databaseName).getAbsolutePath() + "-wal");
        File shmFile = new File(context.getDatabasePath(databaseName).getAbsolutePath() + "-shm");
        
        if (walFile.exists()) {
            long walSizeMB = walFile.length() / (1024 * 1024);
            Log.d(TAG, "WAL file size: " + walSizeMB + " MB");
            
            if (walSizeMB > 10) { // WAL files over 10MB might indicate checkpointing issues
                Log.w(TAG, "WAL file is large (" + walSizeMB + " MB) - consider manual checkpoint");
            }
        }
        
        if (shmFile.exists()) {
            long shmSizeMB = shmFile.length() / (1024 * 1024);
            Log.d(TAG, "SHM file size: " + shmSizeMB + " MB");
        }
    }
    
    /**
     * Provides performance recommendations based on current database state
     *
     * @param context The application context
     */
    private static void logPerformanceRecommendations(Context context) {
        File dbFile = context.getDatabasePath("partymaker_database");
        
        if (dbFile.exists()) {
            long sizeInMB = dbFile.length() / (1024 * 1024);
            
            Log.i(TAG, "=== DATABASE PERFORMANCE RECOMMENDATIONS ===");
            
            if (sizeInMB > 100) {
                Log.i(TAG, "RECOMMENDATION: Database is large (" + sizeInMB + " MB)");
                Log.i(TAG, "- Consider implementing data archiving");
                Log.i(TAG, "- Review and cleanup old data regularly");
                Log.i(TAG, "- Consider database compression");
            }
            
            if (sizeInMB > 20) {
                Log.i(TAG, "RECOMMENDATION: Consider enabling VACUUM for optimization");
                Log.i(TAG, "- Run VACUUM periodically to reclaim space");
                Log.i(TAG, "- Monitor index usage with EXPLAIN QUERY PLAN");
            }
            
            Log.i(TAG, "GENERAL RECOMMENDATIONS:");
            Log.i(TAG, "- Use pagination for large result sets");
            Log.i(TAG, "- Implement proper indexing on frequently queried columns");
            Log.i(TAG, "- Consider using Room's @RawQuery for complex operations");
            Log.i(TAG, "- Monitor query performance regularly");
            Log.i(TAG, "=== END RECOMMENDATIONS ===");
        }
    }
    
    /**
     * Logs memory usage information related to the database
     */
    public static void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // Maximum memory available to the app
        long totalMemory = runtime.totalMemory(); // Total memory currently allocated
        long freeMemory = runtime.freeMemory(); // Free memory in currently allocated space
        long usedMemory = totalMemory - freeMemory;
        
        Log.i(TAG, "=== MEMORY USAGE STATS ===");
        Log.i(TAG, "Max Memory: " + (maxMemory / (1024 * 1024)) + " MB");
        Log.i(TAG, "Used Memory: " + (usedMemory / (1024 * 1024)) + " MB");
        Log.i(TAG, "Free Memory: " + (freeMemory / (1024 * 1024)) + " MB");
        Log.i(TAG, "Memory Usage: " + ((usedMemory * 100) / maxMemory) + "%");
        
        if ((usedMemory * 100) / maxMemory > 80) {
            Log.w(TAG, "HIGH MEMORY USAGE WARNING: Consider cache cleanup or optimization");
        }
        
        Log.i(TAG, "=== END MEMORY STATS ===");
    }
    
    /**
     * Starts a performance timing session
     *
     * @param operationName The name of the operation being timed
     * @return The start time in milliseconds
     */
    public static long startTiming(String operationName) {
        long startTime = System.currentTimeMillis();
        Log.d(TAG, "TIMING START: " + operationName);
        return startTime;
    }
    
    /**
     * Ends a performance timing session and logs the results
     *
     * @param operationName The name of the operation
     * @param startTime The start time from startTiming()
     */
    public static void endTiming(String operationName, long startTime) {
        logQueryPerformance(operationName, startTime);
    }
    
    /**
     * Logs database configuration information
     *
     * @param context The application context
     */
    public static void logDatabaseConfiguration(Context context) {
        Log.i(TAG, "=== DATABASE CONFIGURATION ===");
        Log.i(TAG, "Database Name: partymaker_database");
        Log.i(TAG, "Journal Mode: WAL (Write-Ahead Logging)");
        Log.i(TAG, "Synchronous Mode: NORMAL");
        Log.i(TAG, "Cache Size: 10000 pages (~40MB)");
        Log.i(TAG, "Temp Store: MEMORY");
        Log.i(TAG, "Foreign Keys: ENABLED");
        
        File dbFile = context.getDatabasePath("partymaker_database");
        if (dbFile.exists()) {
            Log.i(TAG, "Database Path: " + dbFile.getAbsolutePath());
        }
        
        Log.i(TAG, "=== END CONFIGURATION ===");
    }
}