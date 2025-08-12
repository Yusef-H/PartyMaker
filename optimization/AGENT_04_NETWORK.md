# AGENT 04 - Network & API Optimization

## 🎯 Mission: Network Performance & Request Optimization
**Estimated Time: 4-5 hours**
**Priority: HIGH**

---

## 📋 Tasks Overview

### Task 1: Network Request Optimization
**Time: 2-3 hours | Priority: CRITICAL**

#### Files to Modify:
- `app/src/main/java/com/example/partymaker/data/api/FirebaseServerClient.java`
- Create new: `NetworkOptimizationManager.java`

#### 1. Create NetworkOptimizationManager:
**Create:** `app/src/main/java/com/example/partymaker/utils/infrastructure/NetworkOptimizationManager.java`

```java
public class NetworkOptimizationManager {
    private static final String TAG = "NetworkOptimization";
    private static final int CACHE_SIZE = 20 * 1024 * 1024; // 20MB cache
    private static OkHttpClient httpClient;
    private static final Map<String, CachedResponse> responseCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_ENTRIES = 200;
    
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
    
    // Request compression interceptor
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
    
    // Cache interceptor for GET requests
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
    
    // Network cache interceptor
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
    
    // Retry interceptor for failed requests
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
        
        private boolean shouldRetry(int responseCode) {
            return responseCode >= 500 || responseCode == 408 || responseCode == 429;
        }
    }
    
    // Logging interceptor for debugging
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
    
    // In-memory cache management
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
    
    private static void cleanupExpiredEntries() {
        responseCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        Log.d(TAG, "Cleaned up expired cache entries. Current size: " + responseCache.size());
    }
    
    // Clear all caches
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
    
    // Cached response wrapper
    private static class CachedResponse {
        final Response response;
        final long timestamp;
        final long ttl = 5 * 60 * 1000; // 5 minutes TTL
        
        CachedResponse(Response response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
    }
}
```

#### 2. Create RequestMetrics for monitoring:
```java
public class RequestMetrics {
    private static final String TAG = "RequestMetrics";
    private static final Map<String, List<Long>> requestTimes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> failureCount = new ConcurrentHashMap<>();
    
    public static void recordRequestTime(String endpoint, long duration) {
        // Extract endpoint from full URL
        String key = extractEndpoint(endpoint);
        
        requestTimes.computeIfAbsent(key, k -> new ArrayList<>()).add(duration);
        
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
    
    public static void recordFailure(String endpoint) {
        String key = extractEndpoint(endpoint);
        failureCount.put(key, failureCount.getOrDefault(key, 0) + 1);
    }
    
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
            
            Log.i(TAG, String.format("%s: avg=%.0fms, min=%dms, max=%dms, requests=%d, failures=%d",
                endpoint, avg, min, max, times.size(), failures));
        }
    }
    
    private static String extractEndpoint(String fullUrl) {
        try {
            return new URL(fullUrl).getPath();
        } catch (Exception e) {
            return fullUrl;
        }
    }
    
    public static double getAverageResponseTime(String endpoint) {
        List<Long> times = requestTimes.get(extractEndpoint(endpoint));
        if (times == null || times.isEmpty()) return 0;
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}
```

---

### Task 2: Update FirebaseServerClient
**Time: 1-2 hours | Priority: HIGH**

#### Update FirebaseServerClient.java:
```java
public class FirebaseServerClient {
    private static FirebaseServerClient instance;
    private OkHttpClient httpClient;
    private static final String TAG = "FirebaseServerClient";
    
    // Request deduplication
    private final Map<String, CompletableFuture<String>> ongoingRequests = new ConcurrentHashMap<>();
    
    private FirebaseServerClient(Context context) {
        this.httpClient = NetworkOptimizationManager.getOptimizedHttpClient(context);
    }
    
    public static synchronized FirebaseServerClient getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseServerClient(context.getApplicationContext());
        }
        return instance;
    }
    
    // Enhanced request method with deduplication
    private CompletableFuture<String> makeOptimizedRequest(String endpoint, String method, String jsonBody) {
        // Create request key for deduplication
        String requestKey = method + ":" + endpoint + ":" + (jsonBody != null ? jsonBody.hashCode() : 0);
        
        // Return existing request if in progress (for GET requests only)
        if ("GET".equals(method) && ongoingRequests.containsKey(requestKey)) {
            Log.d(TAG, "Returning existing request for: " + endpoint);
            return ongoingRequests.get(requestKey);
        }
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return executeHttpRequest(endpoint, method, jsonBody);
        }, ThreadUtils.getNetworkExecutor()).whenComplete((result, throwable) -> {
            ongoingRequests.remove(requestKey);
            
            if (throwable != null) {
                RequestMetrics.recordFailure(endpoint);
                Log.e(TAG, "Request failed: " + endpoint, throwable);
            }
        });
        
        // Store only GET requests for deduplication
        if ("GET".equals(method)) {
            ongoingRequests.put(requestKey, future);
        }
        
        return future;
    }
    
    private String executeHttpRequest(String endpoint, String method, String jsonBody) {
        long startTime = System.currentTimeMillis();
        
        try {
            Request.Builder requestBuilder = new Request.Builder()
                .url(SERVER_URL + endpoint)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PartyMaker-Android/1.0");
            
            // Add method and body
            if ("GET".equals(method)) {
                requestBuilder.get();
            } else if ("POST".equals(method)) {
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody != null ? jsonBody : "{}");
                requestBuilder.post(body);
            } else if ("PUT".equals(method)) {
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody != null ? jsonBody : "{}");
                requestBuilder.put(body);
            } else if ("DELETE".equals(method)) {
                requestBuilder.delete();
            }
            
            Request request = requestBuilder.build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                long duration = System.currentTimeMillis() - startTime;
                RequestMetrics.recordRequestTime(endpoint, duration);
                
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP " + response.code() + ": " + response.message());
                }
                
                ResponseBody responseBody = response.body();
                return responseBody != null ? responseBody.string() : "";
            }
            
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            RequestMetrics.recordRequestTime(endpoint, duration);
            RequestMetrics.recordFailure(endpoint);
            throw new RuntimeException("Network request failed: " + endpoint, e);
        }
    }
    
    // Batch request methods
    public CompletableFuture<List<String>> makeBatchRequests(List<BatchRequest> requests) {
        List<CompletableFuture<String>> futures = requests.stream()
            .map(req -> makeOptimizedRequest(req.endpoint, req.method, req.body))
            .collect(Collectors.toList());
            
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
    
    // Prefetch common data
    public void prefetchCommonData(String userId) {
        ThreadUtils.executeNetworkTask(() -> {
            try {
                // Prefetch user groups
                makeOptimizedRequest("/user/" + userId + "/groups", "GET", null);
                
                // Prefetch public groups
                makeOptimizedRequest("/groups/public?limit=20", "GET", null);
                
                Log.d(TAG, "Prefetched common data for user: " + userId);
            } catch (Exception e) {
                Log.w(TAG, "Failed to prefetch data", e);
            }
        });
    }
    
    // Batch request helper class
    public static class BatchRequest {
        final String endpoint;
        final String method;
        final String body;
        
        public BatchRequest(String endpoint, String method, String body) {
            this.endpoint = endpoint;
            this.method = method;
            this.body = body;
        }
    }
    
    // Clear all network state
    public void clearNetworkState() {
        ongoingRequests.clear();
        NetworkOptimizationManager.clearAllCaches();
    }
}
```

---

### Task 3: Network State Management
**Time: 1 hour | Priority: MEDIUM**

#### Update NetworkManager.java (if exists) or create:
```java
public class NetworkManager {
    private static NetworkManager instance;
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final Set<NetworkStateListener> listeners = ConcurrentHashMap.newKeySet();
    private boolean isOnline = false;
    
    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }
    
    private NetworkManager(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        registerNetworkCallback();
        updateNetworkState();
    }
    
    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }
        return instance;
    }
    
    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();\n            connectivityManager.registerNetworkCallback(\n                builder.build(),\n                new ConnectivityManager.NetworkCallback() {\n                    @Override\n                    public void onAvailable(@NonNull Network network) {\n                        super.onAvailable(network);\n                        onNetworkChanged(true);\n                    }\n                    \n                    @Override\n                    public void onLost(@NonNull Network network) {\n                        super.onLost(network);\n                        onNetworkChanged(false);\n                    }\n                });\n        }\n    }\n    \n    private void onNetworkChanged(boolean isOnline) {\n        if (this.isOnline != isOnline) {\n            this.isOnline = isOnline;\n            \n            if (isOnline) {\n                listeners.forEach(NetworkStateListener::onNetworkAvailable);\n                // Retry pending requests\n                retryPendingRequests();\n            } else {\n                listeners.forEach(NetworkStateListener::onNetworkLost);\n            }\n        }\n    }\n    \n    private void updateNetworkState() {\n        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {\n            Network activeNetwork = connectivityManager.getActiveNetwork();\n            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);\n            isOnline = networkCapabilities != null && \n                      networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&\n                      networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);\n        } else {\n            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();\n            isOnline = activeNetworkInfo != null && activeNetworkInfo.isConnected();\n        }\n    }\n    \n    public boolean isOnline() {\n        return isOnline;\n    }\n    \n    public void addNetworkStateListener(NetworkStateListener listener) {\n        listeners.add(listener);\n    }\n    \n    public void removeNetworkStateListener(NetworkStateListener listener) {\n        listeners.remove(listener);\n    }\n    \n    private void retryPendingRequests() {\n        // Implementation to retry failed requests when network becomes available\n        ThreadUtils.executeNetworkTask(() -> {\n            Log.d("NetworkManager", "Network available - retrying pending requests");\n            // This could trigger a retry mechanism in FirebaseServerClient\n        });\n    }\n    \n    public String getNetworkType() {\n        if (!isOnline) return "None";\n        \n        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {\n            Network activeNetwork = connectivityManager.getActiveNetwork();\n            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);\n            \n            if (capabilities != null) {\n                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {\n                    return "WiFi";\n                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {\n                    return "Mobile";\n                }\n            }\n        }\n        \n        return "Unknown";\n    }\n}
```

---

## ✅ Testing Instructions

### Network Performance Tests:

1. **Response Time Test:**
```bash
# Monitor network requests
adb logcat -s NetworkOptimization RequestMetrics

# Test with slow network
# Settings > Developer Options > Select USB Debugging > Networking > Simulate slow network
```

1. **Cache Effectiveness Test:**
   - Make same API call multiple times
   - Check logs for "Serving from memory cache"
   - Verify reduced network traffic

2. **Offline Handling Test:**
```bash
# Disable network
adb shell svc wifi disable
adb shell svc data disable

# Enable network
adb shell svc wifi enable  
adb shell svc data enable
```

### Expected Results:
- API requests under 3 seconds average
- Cache hit ratio > 30% for repeated requests
- Graceful offline handling
- Automatic retry on network restore

---

## 🚨 Critical Points

1. **Test on Slow Network**: Use Network Link Conditioner or similar
2. **Monitor Memory Usage**: HTTP cache can consume memory
3. **Test Offline Scenarios**: Ensure app doesn't crash without network
4. **Check Request Deduplication**: Verify duplicate requests are merged

---

## 📊 Success Criteria

- [ ] Average API response time under 3 seconds
- [ ] Cache hit ratio > 30% for repeated requests
- [ ] Zero network-related crashes
- [ ] Proper offline state handling
- [ ] Request deduplication working
- [ ] Network state changes handled correctly

---

**Agent 04 Priority:** Focus on NetworkOptimizationManager first - biggest network performance impact!
**Time Allocation:** Network Manager (50%) → FirebaseServerClient Updates (30%) → State Management (20%)