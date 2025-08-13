package com.example.partymaker.utils.infrastructure;

import android.util.Log;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Request metrics tracking class for monitoring network performance.
 * Tracks request times, failure rates, and provides performance statistics
 * for network optimization analysis.
 */
public class RequestMetrics {
    private static final String TAG = "RequestMetrics";
    private static final Map<String, List<Long>> requestTimes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> failureCount = new ConcurrentHashMap<>();
    private static final Map<String, Integer> requestCount = new ConcurrentHashMap<>();
    
    /**
     * Records the time taken for a network request.
     * 
     * @param endpoint The endpoint URL
     * @param duration The duration in milliseconds
     */
    public static void recordRequestTime(String endpoint, long duration) {
        // Extract endpoint from full URL
        String key = extractEndpoint(endpoint);
        
        // Record the request time
        requestTimes.computeIfAbsent(key, k -> new ArrayList<>()).add(duration);
        
        // Increment request count
        requestCount.put(key, requestCount.getOrDefault(key, 0) + 1);
        
        // Log slow requests
        if (duration > 5000) {
            Log.w(TAG, "SLOW REQUEST: " + key + " took " + duration + "ms");
        }
        
        // Keep only recent metrics (last 100 requests per endpoint)
        List<Long> times = requestTimes.get(key);
        if (times.size() > 100) {
            times.subList(0, times.size() - 100).clear();
        }
    }
    
    /**
     * Records a request failure for tracking failure rates.
     * 
     * @param endpoint The endpoint that failed
     */
    public static void recordFailure(String endpoint) {
        String key = extractEndpoint(endpoint);
        failureCount.put(key, failureCount.getOrDefault(key, 0) + 1);
        Log.w(TAG, "REQUEST FAILURE: " + key + " (total failures: " + failureCount.get(key) + ")");
    }
    
    /**
     * Prints comprehensive network performance statistics to the log.
     */
    public static void printNetworkStats() {
        Log.i(TAG, "=== Network Performance Stats ===");
        
        for (Map.Entry<String, List<Long>> entry : requestTimes.entrySet()) {
            String endpoint = entry.getKey();
            List<Long> times = entry.getValue();
            
            if (times.isEmpty()) continue;
            
            double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
            long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
            int failures = failureCount.getOrDefault(endpoint, 0);
            int requests = requestCount.getOrDefault(endpoint, 0);
            double failureRate = requests > 0 ? (failures * 100.0 / requests) : 0;
            
            Log.i(TAG, String.format("%s: avg=%.0fms, min=%dms, max=%dms, requests=%d, failures=%d (%.1f%%)",
                endpoint, avg, min, max, requests, failures, failureRate));
        }
        
        // Overall stats
        int totalRequests = requestCount.values().stream().mapToInt(Integer::intValue).sum();
        int totalFailures = failureCount.values().stream().mapToInt(Integer::intValue).sum();
        double overallFailureRate = totalRequests > 0 ? (totalFailures * 100.0 / totalRequests) : 0;
        
        Log.i(TAG, String.format("OVERALL: %d requests, %d failures (%.1f%% failure rate)", 
            totalRequests, totalFailures, overallFailureRate));
    }
    
    /**
     * Extracts the endpoint path from a full URL for categorization.
     * 
     * @param fullUrl The full URL
     * @return The endpoint path
     */
    private static String extractEndpoint(String fullUrl) {
        try {
            return new URL(fullUrl).getPath();
        } catch (Exception e) {
            // Fallback: try to extract path manually
            if (fullUrl.contains("/api/")) {
                int apiIndex = fullUrl.indexOf("/api/");
                return fullUrl.substring(apiIndex);
            }
            return fullUrl;
        }
    }
    
    /**
     * Gets the average response time for a specific endpoint.
     * 
     * @param endpoint The endpoint to check
     * @return Average response time in milliseconds, or 0 if no data
     */
    public static double getAverageResponseTime(String endpoint) {
        List<Long> times = requestTimes.get(extractEndpoint(endpoint));
        if (times == null || times.isEmpty()) return 0;
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
    
    /**
     * Gets the failure rate for a specific endpoint.
     * 
     * @param endpoint The endpoint to check
     * @return Failure rate as a percentage (0-100)
     */
    public static double getFailureRate(String endpoint) {
        String key = extractEndpoint(endpoint);
        int failures = failureCount.getOrDefault(key, 0);
        int requests = requestCount.getOrDefault(key, 0);
        return requests > 0 ? (failures * 100.0 / requests) : 0;
    }
    
    /**
     * Gets the total number of requests for a specific endpoint.
     * 
     * @param endpoint The endpoint to check
     * @return Total request count
     */
    public static int getRequestCount(String endpoint) {
        return requestCount.getOrDefault(extractEndpoint(endpoint), 0);
    }
    
    /**
     * Gets the total number of failures for a specific endpoint.
     * 
     * @param endpoint The endpoint to check
     * @return Total failure count
     */
    public static int getFailureCount(String endpoint) {
        return failureCount.getOrDefault(extractEndpoint(endpoint), 0);
    }
    
    /**
     * Checks if an endpoint is performing poorly based on metrics.
     * 
     * @param endpoint The endpoint to check
     * @return true if the endpoint has poor performance metrics
     */
    public static boolean isPoorPerformer(String endpoint) {
        double avgTime = getAverageResponseTime(endpoint);
        double failureRate = getFailureRate(endpoint);
        int requestCount = getRequestCount(endpoint);
        
        // Consider poor performance if:
        // - Average response time > 5 seconds
        // - Failure rate > 10%
        // - Has at least 5 requests for statistical significance
        return requestCount >= 5 && (avgTime > 5000 || failureRate > 10);
    }
    
    /**
     * Gets performance summary for all endpoints.
     * 
     * @return Map of endpoint to performance summary
     */
    public static Map<String, String> getPerformanceSummary() {
        Map<String, String> summary = new ConcurrentHashMap<>();
        
        for (String endpoint : requestCount.keySet()) {
            double avgTime = getAverageResponseTime(endpoint);
            double failureRate = getFailureRate(endpoint);
            int requests = getRequestCount(endpoint);
            
            String performance = String.format("avg=%.0fms, requests=%d, failures=%.1f%%", 
                avgTime, requests, failureRate);
            summary.put(endpoint, performance);
        }
        
        return summary;
    }
    
    /**
     * Clears all recorded metrics. Use with caution.
     */
    public static void clearAllMetrics() {
        requestTimes.clear();
        failureCount.clear();
        requestCount.clear();
        Log.i(TAG, "All request metrics cleared");
    }
    
    /**
     * Gets cache hit ratio if available from NetworkOptimizationManager.
     * 
     * @return Cache statistics string
     */
    public static String getCacheStats() {
        return NetworkOptimizationManager.getCacheStats();
    }
}