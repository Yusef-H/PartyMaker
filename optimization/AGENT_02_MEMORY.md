# AGENT 02 - Memory Management & Image Optimization

## 🎯 Mission: Memory Leak Prevention & Image Performance
**Estimated Time: 5-6 hours**
**Priority: CRITICAL**

---

## 📋 Tasks Overview

### Task 1: Enhanced Memory Management
**Time: 2 hours | Priority: CRITICAL**

#### Files to Modify:
- `app/src/main/java/com/example/partymaker/utils/infrastructure/MemoryManager.java` (enhance existing)
- All Activity files (lifecycle integration)

#### 1. Enhance Existing MemoryManager:
```java
public class MemoryManager {
    private static MemoryManager instance;
    private static final String TAG = "MemoryManager";
    private final Map<String, WeakReference<Object>> objectRegistry = new ConcurrentHashMap<>();
    
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
    
    // Enhanced cleanup methods
    public void clearUnusedResources() {
        // Clear weak references
        objectRegistry.entrySet().removeIf(entry -> entry.getValue().get() == null);
        
        // Clear Glide memory cache
        new Thread(() -> {
            Glide.get(getApplicationContext()).clearMemory();
        }).start();
        
        // Suggest garbage collection
        System.gc();
        
        Log.d(TAG, "Cleared unused resources. Active objects: " + objectRegistry.size());
    }
    
    public void clearImageCache() {
        // Clear Glide disk cache in background
        new Thread(() -> {
            Glide.get(getApplicationContext()).clearDiskCache();
        }).start();
        
        // Clear memory cache on main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Glide.get(getApplicationContext()).clearMemory();
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                Glide.get(getApplicationContext()).clearMemory();
            });
        }
    }
    
    public void emergencyCleanup() {
        Log.w(TAG, "Emergency memory cleanup triggered");
        
        // Clear all caches
        clearImageCache();
        clearUnusedResources();
        
        // Clear system caches
        System.runFinalization();
        System.gc();
        
        // Log memory stats
        logMemoryStats();
    }
    
    public void registerObject(String key, Object obj) {
        objectRegistry.put(key, new WeakReference<>(obj));
    }
    
    public void unregisterObject(String key) {
        objectRegistry.remove(key);
    }
    
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
        if ((usedMemory * 100.0) / maxMemory > 85) {
            Log.e(TAG, "CRITICAL MEMORY USAGE!");
            emergencyCleanup();
        }
    }
    
    private Context getApplicationContext() {
        // You'll need to store application context
        return PartyApplication.getInstance();
    }
}
```

#### 2. Create BaseActivity with Memory Management:
**Create:** `app/src/main/java/com/example/partymaker/ui/base/BaseActivity.java`
```java
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private String activityKey;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityKey = getClass().getSimpleName() + "_" + System.currentTimeMillis();
        MemoryManager.getInstance().registerObject(activityKey, this);
        MemoryManager.getInstance().logMemoryStats();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        MemoryManager.getInstance().clearUnusedResources();
    }
    
    @Override
    protected void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_RUNNING_MODERATE:
            case TRIM_MEMORY_RUNNING_LOW:
                MemoryManager.getInstance().clearImageCache();
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                MemoryManager.getInstance().emergencyCleanup();
                break;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MemoryManager.getInstance().unregisterObject(activityKey);
        
        // Clear ViewModels
        getViewModelStore().clear();
        
        // Clear any remaining references
        clearActivityReferences();
    }
    
    protected abstract void clearActivityReferences();
}
```

#### 3. Update Major Activities to extend BaseActivity:
**Files to modify:**
- `MainActivity.java`
- `ChatActivity.java`
- `PartyMainActivity.java`
- `CreateGroupActivity.java`

**Example for MainActivity:**
```java
public class MainActivity extends BaseActivity {
    private GroupAdapter adapter;
    private GroupViewModel viewModel;
    
    @Override
    protected void clearActivityReferences() {
        if (adapter != null) {
            adapter = null;
        }
        if (viewModel != null) {
            viewModel = null;
        }
        // Clear any other references
    }
}
```

---

### Task 2: Image Loading Optimization
**Time: 2-3 hours | Priority: CRITICAL**

#### Create New File:
- `app/src/main/java/com/example/partymaker/utils/media/ImageOptimizationManager.java`

#### Implementation:
```java
public class ImageOptimizationManager {
    private static final String TAG = "ImageOptimization";
    
    // Optimized request options
    private static final RequestOptions thumbnailOptions = new RequestOptions()
        .override(150, 150)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.placeholder_group)
        .error(R.drawable.error_placeholder)
        .skipMemoryCache(false)
        .format(DecodeFormat.PREFER_RGB_565); // Use less memory
    
    private static final RequestOptions profileOptions = new RequestOptions()
        .override(100, 100)
        .circleCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.placeholder_user)
        .error(R.drawable.error_user)
        .skipMemoryCache(false)
        .format(DecodeFormat.PREFER_RGB_565);
    
    private static final RequestOptions fullImageOptions = new RequestOptions()
        .override(800, 600)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.placeholder_image)
        .error(R.drawable.error_image)
        .skipMemoryCache(false)
        .format(DecodeFormat.PREFER_ARGB_8888); // Better quality for full images
    
    public static void loadGroupThumbnail(ImageView imageView, String url) {
        if (imageView == null || url == null || url.isEmpty()) return;
        
        Glide.with(imageView.getContext())
            .load(url)
            .apply(thumbnailOptions)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Log.w(TAG, "Failed to load group thumbnail: " + url, e);
                    return false;
                }
                
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "Loaded group thumbnail from: " + dataSource);
                    return false;
                }
            })
            .into(imageView);
    }
    
    public static void loadProfileImage(ImageView imageView, String url) {
        if (imageView == null || url == null || url.isEmpty()) return;
        
        Glide.with(imageView.getContext())
            .load(url)
            .apply(profileOptions)
            .into(imageView);
    }
    
    public static void loadFullImage(ImageView imageView, String url) {
        if (imageView == null || url == null || url.isEmpty()) return;
        
        Glide.with(imageView.getContext())
            .load(url)
            .apply(fullImageOptions)
            .into(imageView);
    }
    
    public static void preloadImage(Context context, String url) {
        if (url == null || url.isEmpty()) return;
        
        Glide.with(context)
            .load(url)
            .apply(thumbnailOptions)
            .preload();
    }
    
    public static void clearImageView(ImageView imageView) {
        if (imageView != null) {
            Glide.with(imageView.getContext()).clear(imageView);
        }
    }
    
    // Batch image preloading for lists
    public static void preloadImages(Context context, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        
        ThreadUtils.executeImageTask(() -> {
            for (String url : urls) {
                preloadImage(context, url);
            }
        });
    }
}
```

#### Update GlideImageLoader.java:
Replace existing implementation with calls to ImageOptimizationManager:
```java
public class GlideImageLoader {
    // Replace existing methods with:
    public static void loadImageIntoImageView(Context context, String imageUrl, ImageView imageView) {
        ImageOptimizationManager.loadGroupThumbnail(imageView, imageUrl);
    }
    
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        ImageOptimizationManager.loadProfileImage(imageView, imageUrl);
    }
}
```

---

### Task 3: Update All Image Loading Calls
**Time: 1-2 hours | Priority: HIGH**

#### Files to Update:
- All Adapter classes (`GroupAdapter.java`, `ChatRecyclerAdapter.java`, etc.)
- All Activity files that load images
- All Fragment files that load images

#### Search and Replace Pattern:
**Find:** `Glide.with(context).load(url).into(imageView)`
**Replace:** `ImageOptimizationManager.loadGroupThumbnail(imageView, url)`

#### Specific Updates:

**In GroupAdapter.java:**
```java
// Replace this:
Glide.with(context).load(group.getImageUrl()).into(holder.groupImage);

// With this:
ImageOptimizationManager.loadGroupThumbnail(holder.groupImage, group.getImageUrl());

// Also add in onViewRecycled:
@Override
public void onViewRecycled(@NonNull ViewHolder holder) {
    super.onViewRecycled(holder);
    ImageOptimizationManager.clearImageView(holder.groupImage);
}
```

**In ChatRecyclerAdapter.java:**
```java
// For profile images:
ImageOptimizationManager.loadProfileImage(holder.userAvatar, message.getUserImageUrl());

// For message images:
ImageOptimizationManager.loadFullImage(holder.messageImage, message.getImageUrl());
```

---

### Task 4: Memory Leak Prevention
**Time: 1 hour | Priority: HIGH**

#### Create Memory Leak Detection:
**Add to build.gradle (app):**
```gradle
debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
```

#### Update PartyApplication.java:
```java
public class PartyApplication extends Application {
    private static PartyApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize memory management
        MemoryManager.getInstance();
        
        // Setup memory monitoring in debug builds
        if (BuildConfig.DEBUG) {
            setupMemoryMonitoring();
        }
    }
    
    private void setupMemoryMonitoring() {
        // LeakCanary is automatically initialized
        
        // Additional memory monitoring
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                Log.d("MemoryMonitor", "Activity created: " + activity.getClass().getSimpleName());
                MemoryManager.getInstance().logMemoryStats();
            }
            
            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Log.d("MemoryMonitor", "Activity destroyed: " + activity.getClass().getSimpleName());
                MemoryManager.getInstance().logMemoryStats();
            }
            
            // Other lifecycle methods...
            @Override public void onActivityStarted(@NonNull Activity activity) {}
            @Override public void onActivityResumed(@NonNull Activity activity) {}
            @Override public void onActivityPaused(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
        });
    }
    
    public static PartyApplication getInstance() {
        return instance;
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        MemoryManager.getInstance().emergencyCleanup();
    }
}
```

---

## ✅ Testing Instructions

### Memory Tests:
1. **Memory Usage Test:**
```bash
# Monitor memory usage
adb shell dumpsys meminfo com.example.partymaker

# Check for memory leaks
adb shell am start com.example.partymaker/.ui.features.core.MainActivity
# Use app extensively, then check LeakCanary
```

1. **Image Loading Performance:**
   - Open activities with many images
   - Scroll rapidly through lists
   - Switch between activities
   - Check memory doesn't continuously grow

2. **Low Memory Simulation:**
```bash
# Simulate low memory condition
adb shell am send-trim-memory com.example.partymaker RUNNING_LOW
```

### Expected Results:
- Memory usage stable under 100MB
- No memory leaks in LeakCanary
- Images load smoothly without OOM errors
- App handles low memory conditions gracefully

---

## 🚨 Critical Points

1. **Test on Low-RAM Device**: Use 1GB RAM emulator
2. **Extended Usage Test**: Use app for 30+ minutes continuously
3. **Image Loading Test**: Load 100+ images in succession
4. **Memory Leak Check**: Use LeakCanary after every major operation

---

## 📊 Success Criteria

- [ ] Memory usage stable under 100MB during normal use
- [ ] Zero memory leaks detected by LeakCanary
- [ ] Images load without OutOfMemoryError
- [ ] App survives system memory pressure
- [ ] Memory cleanup works properly
- [ ] BaseActivity integration complete

---

**Agent 02 Priority:** Focus on Image Optimization first - biggest memory impact!
**Time Allocation:** Images (40%) → Memory Manager (35%) → Leak Prevention (25%)