package com.example.partymaker.utils.infrastructure;

import android.content.Context;
import android.util.Log;
import com.example.partymaker.BuildConfig;
// BufferedSink is from okio
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Network optimization manager for PartyMaker application.
 * Provides HTTP client optimizations including caching, compression, retry logic,
 * and request deduplication for improved network performance.
 */
public class NetworkOptimizationManager {
    private static final String TAG = "NetworkOptimization";
    private static final int CACHE_SIZE = 20 * 1024 * 1024; // 20MB cache
    private static OkHttpClient httpClient;
    private static final Map<String, CachedResponse> responseCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_ENTRIES = 200;
    
    /**
     * Gets the optimized HTTP client with caching, compression, and retry logic.
     * 
     * @param context The application context
     * @return Optimized OkHttpClient instance
     */
    public static OkHttpClient getOptimizedHttpClient(Context context) {
        if (httpClient == null) {
            synchronized (NetworkOptimizationManager.class) {
                if (httpClient == null) {
                    httpClient = createOptimizedClient(context);
                }
            }
        }
        return httpClient;
    }
    
    /**
     * Creates an optimized HTTP client with all performance enhancements.
     */
    private static OkHttpClient createOptimizedClient(Context context) {
        File cacheDir = new File(context.getCacheDir(), "http-cache");
        Cache cache = new Cache(cacheDir, CACHE_SIZE);
        
        return new OkHttpClient.Builder()
            .cache(cache)
            .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES)) // Reuse connections
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(new RequestCompressionInterceptor())
            .addInterceptor(new CacheInterceptor())
            .addInterceptor(new LoggingInterceptor())
            .addNetworkInterceptor(new NetworkCacheInterceptor())
            .addInterceptor(new RetryInterceptor())
            .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1)) // HTTP/2 support
            .build();
    }
    
    /**
     * Request compression interceptor to reduce payload size.
     */
    private static class RequestCompressionInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            
            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
                return chain.proceed(originalRequest);
            }
            
            Request compressedRequest = originalRequest.newBuilder()
                .header("Content-Encoding", "gzip")
                .method(originalRequest.method(), gzip(originalRequest.body()))
                .build();
                
            return chain.proceed(compressedRequest);
        }
        
        /**
         * Creates gzipped request body.
         */
        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return body.contentType();
                }
                
                @Override
                public long contentLength() {
                    return -1; // Unknown compressed length
                }
                
                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }
    
    /**
     * Cache interceptor for GET requests with in-memory caching.
     */
    private static class CacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            
            // Add cache headers for GET requests
            if (request.method().equals("GET")) {
                String cacheKey = request.url().toString();
                
                // Check in-memory cache first
                CachedResponse cached = responseCache.get(cacheKey);
                if (cached != null && !cached.isExpired()) {
                    Log.d(TAG, "Serving from memory cache: " + cacheKey);
                    return cached.response;
                }
                
                // Modify request for HTTP caching
                request = request.newBuilder()
                    .addHeader("Cache-Control", "public, max-age=300") // 5 minutes
                    .build();
            }
            
            Response response = chain.proceed(request);
            
            // Cache successful GET responses
            if (request.method().equals("GET") && response.isSuccessful()) {
                cacheInMemory(request.url().toString(), response);
            }
            
            return response;
        }
    }
    
    /**
     * Network cache interceptor to add cache headers to responses.
     */
    private static class NetworkCacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            
            // Add cache headers for successful responses
            if (response.isSuccessful()) {
                return response.newBuilder()
                    .header("Cache-Control", "public, max-age=300") // 5 minutes
                    .removeHeader("Pragma") // Remove pragma to enable caching
                    .build();
            }
            
            return response;
        }
    }
    
    /**
     * Retry interceptor for failed requests with exponential backoff.
     */
    private static class RetryInterceptor implements Interceptor {
        private static final int MAX_RETRIES = 3;
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException lastException = null;
            
            for (int retryCount = 0; retryCount < MAX_RETRIES; retryCount++) {
                try {
                    response = chain.proceed(request);
                    
                    if (response.isSuccessful() || !shouldRetry(response.code())) {
                        return response;
                    }
                    
                    // Close response body before retry
                    if (response.body() != null) {
                        response.body().close();
                    }
                    
                    // Exponential backoff
                    Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                    
                } catch (IOException e) {
                    lastException = e;
                    Log.w(TAG, "Request failed, retry " + (retryCount + 1) + "/" + MAX_RETRIES, e);
                    
                    if (retryCount < MAX_RETRIES - 1) {
                        try {
                            Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (response != null) {
                return response;
            } else if (lastException != null) {
                throw lastException;
            } else {
                throw new IOException("Request failed after " + MAX_RETRIES + " retries");
            }
        }
        
        /**
         * Determines if a response code should trigger a retry.
         */
        private boolean shouldRetry(int responseCode) {
            return responseCode >= 500 || responseCode == 408 || responseCode == 429;
        }
    }
    
    /**
     * Logging interceptor for debugging and performance monitoring.
     */
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.currentTimeMillis();
            
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Request: " + request.method() + " " + request.url());
            }
            
            Response response = chain.proceed(request);
            long duration = System.currentTimeMillis() - startTime;
            
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Response: " + response.code() + " " + request.url() + " (" + duration + "ms)");
            }
            
            // Log slow requests
            if (duration > 3000) {
                Log.w(TAG, "SLOW REQUEST: " + request.url() + " took " + duration + "ms");
            }
            
            // Track request metrics
            RequestMetrics.recordRequestTime(request.url().toString(), duration);
            
            return response;
        }
    }
    
    /**
     * Caches response in memory for fast retrieval.
     */
    private static void cacheInMemory(String url, Response response) {
        try {
            // Don't cache if already at max capacity
            if (responseCache.size() >= MAX_CACHE_ENTRIES) {
                cleanupExpiredEntries();
                if (responseCache.size() >= MAX_CACHE_ENTRIES) {
                    return; // Still full after cleanup
                }
            }
            
            // Clone response for caching
            String bodyString = response.body().string();
            Response cachedResponse = response.newBuilder()
                .body(ResponseBody.create(response.body().contentType(), bodyString))
                .build();
                
            responseCache.put(url, new CachedResponse(cachedResponse, System.currentTimeMillis()));
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to cache response", e);
        }
    }
    
    /**
     * Cleans up expired cache entries to free memory.
     */
    private static void cleanupExpiredEntries() {
        responseCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        Log.d(TAG, "Cleaned up expired cache entries. Current size: " + responseCache.size());
    }
    
    /**
     * Clears all caches (both in-memory and HTTP cache).
     */
    public static void clearAllCaches() {
        responseCache.clear();
        if (httpClient != null && httpClient.cache() != null) {
            try {
                httpClient.cache().evictAll();
            } catch (IOException e) {
                Log.e(TAG, "Failed to clear HTTP cache", e);
            }
        }
        Log.i(TAG, "Cleared all network caches");
    }
    
    /**
     * Gets cache statistics for monitoring.
     * 
     * @return String containing cache statistics
     */
    public static String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("In-memory cache size: ").append(responseCache.size()).append("/").append(MAX_CACHE_ENTRIES);
        
        if (httpClient != null && httpClient.cache() != null) {
            try {
                Cache cache = httpClient.cache();
                stats.append(", HTTP cache size: ").append(cache.size()).append(" bytes");
                stats.append(", Hit count: ").append(cache.hitCount());
                stats.append(", Request count: ").append(cache.requestCount());
            } catch (IOException e) {
                stats.append(", HTTP cache: error reading stats");
            }
        }
        
        return stats.toString();
    }
    
    /**
     * Wrapper class for cached responses with TTL.
     */
    private static class CachedResponse {
        final Response response;
        final long timestamp;
        final long ttl = 5 * 60 * 1000; // 5 minutes TTL
        
        CachedResponse(Response response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }
        
        /**
         * Checks if the cached response has expired.
         */
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
    }
}