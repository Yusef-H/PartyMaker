# AGENT 01 - Performance Optimization

## 🎯 Mission: Core Performance Improvements
**Estimated Time: 6-8 hours**
**Priority: CRITICAL**

---

## 📋 Tasks Overview

### Task 1: RecyclerView DiffUtil Implementation
**Time: 2-3 hours | Priority: CRITICAL**

#### Files to Modify:
- `app/src/main/java/com/example/partymaker/ui/adapters/GroupAdapter.java`
- `app/src/main/java/com/example/partymaker/ui/adapters/ChatRecyclerAdapter.java`

#### Implementation Steps:

1. **Update GroupAdapter.java:**
```java
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<Group> currentList = new ArrayList<>();
    
    public void updateGroups(List<Group> newGroups) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return currentList.get(oldItemPosition).getId().equals(
                    newGroups.get(newItemPosition).getId());
            }
            
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Group oldGroup = currentList.get(oldItemPosition);
                Group newGroup = newGroups.get(newItemPosition);
                return oldGroup.equals(newGroup) && 
                       oldGroup.getLastMessage().equals(newGroup.getLastMessage());
            }
            
            @Override
            public int getOldListSize() { return currentList.size(); }
            @Override
            public int getNewListSize() { return newGroups.size(); }
        });
        
        currentList.clear();
        currentList.addAll(newGroups);
        result.dispatchUpdatesTo(this);
    }
    
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Clear Glide requests to prevent memory leaks
        if (holder.groupImage != null) {
            Glide.with(holder.itemView.getContext()).clear(holder.groupImage);
        }
    }
}
```

1. **Update ChatRecyclerAdapter.java:**
```java
public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> currentMessages = new ArrayList<>();
    
    public void updateMessages(List<ChatMessage> newMessages) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MessageDiffCallback(currentMessages, newMessages));
        currentMessages.clear();
        currentMessages.addAll(newMessages);
        result.dispatchUpdatesTo(this);
    }
    
    private static class MessageDiffCallback extends DiffUtil.Callback {
        private List<ChatMessage> oldList;
        private List<ChatMessage> newList;
        
        MessageDiffCallback(List<ChatMessage> oldList, List<ChatMessage> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getMessageId().equals(
                newList.get(newItemPosition).getMessageId());
        }
        
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ChatMessage oldMsg = oldList.get(oldItemPosition);
            ChatMessage newMsg = newList.get(newItemPosition);
            return oldMsg.equals(newMsg) && 
                   oldMsg.getTimestamp() == newMsg.getTimestamp();
        }
        
        @Override
        public int getOldListSize() { return oldList.size(); }
        @Override
        public int getNewListSize() { return newList.size(); }
    }
}
```

1. **Update ViewModels to use new adapter methods:**
   - Find all calls to `adapter.notifyDataSetChanged()`
   - Replace with `adapter.updateGroups(newList)` or `adapter.updateMessages(newList)`

#### Files to Update:
- `MainActivity.java` 
- `ChatActivity.java`
- `PublicGroupsActivity.java`

---

### Task 2: Thread Management Enhancement
**Time: 2-3 hours | Priority: HIGH**

#### File to Modify:
- `app/src/main/java/com/example/partymaker/utils/infrastructure/ThreadUtils.java`

#### Implementation:
```java
public class ThreadUtils {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    
    // Separate thread pools for different operations
    private static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(2);
    private static final ExecutorService networkExecutor = Executors.newFixedThreadPool(3);
    private static final ExecutorService imageExecutor = Executors.newFixedThreadPool(1);
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    
    public static void executeDatabaseTask(Runnable task) {
        databaseExecutor.execute(task);
    }
    
    public static void executeNetworkTask(Runnable task) {
        networkExecutor.execute(task);
    }
    
    public static void executeImageTask(Runnable task) {
        imageExecutor.execute(task);
    }
    
    public static void scheduleTask(Runnable task, long delay, TimeUnit unit) {
        scheduledExecutor.schedule(task, delay, unit);
    }
    
    public static void shutdown() {
        databaseExecutor.shutdown();
        networkExecutor.shutdown();
        imageExecutor.shutdown();
        scheduledExecutor.shutdown();
    }
}
```

#### Files to Update:
- Replace all `ThreadUtils.runInBackground()` calls with appropriate specific methods
- Update `PartyApplication.java` to call `ThreadUtils.shutdown()` in `onTerminate()`

---

### Task 3: RecyclerView Performance Tuning
**Time: 1-2 hours | Priority: HIGH**

#### Files to Modify:
- `MainActivity.java`
- `ChatActivity.java`
- `PublicGroupsActivity.java`
- All activities with RecyclerView

#### Implementation for each Activity:
```java
private void setupRecyclerViewOptimizations() {
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemViewCacheSize(20);
    recyclerView.setDrawingCacheEnabled(true);
    recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    
    LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false; // Better scroll performance
        }
    };
    recyclerView.setLayoutManager(layoutManager);
    
    // Shared RecyclerView pool
    RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
    sharedPool.setMaxRecycledViews(0, 20);
    recyclerView.setRecycledViewPool(sharedPool);
}
```

#### Call in onCreate() of each Activity:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    setupRecyclerView();
    setupRecyclerViewOptimizations(); // Add this line
}
```

---

### Task 4: Performance Monitoring Setup
**Time: 1-2 hours | Priority: MEDIUM**

#### Create New File:
- `app/src/main/java/com/example/partymaker/utils/infrastructure/PerformanceMonitor.java`

#### Implementation:
```java
public class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";
    private static final Map<String, Long> timingMap = new ConcurrentHashMap<>();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    
    public static void startTiming(String operation) {
        if (!DEBUG) return;
        timingMap.put(operation, System.currentTimeMillis());
        Log.d(TAG, "Started: " + operation);
    }
    
    public static void endTiming(String operation) {
        if (!DEBUG) return;
        Long startTime = timingMap.remove(operation);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            Log.d(TAG, operation + " took: " + duration + "ms");
            
            if (duration > 1000) {
                Log.w(TAG, "SLOW OPERATION: " + operation + " took " + duration + "ms");
            }
        }
    }
    
    public static void trackMemoryUsage(String location) {
        if (!DEBUG) return;
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        
        Log.d(TAG, String.format("%s - Memory: %d/%d MB (%.1f%%)", 
            location,
            usedMemory / (1024 * 1024),
            maxMemory / (1024 * 1024),
            (usedMemory * 100.0) / maxMemory));
            
        if ((usedMemory * 100.0) / maxMemory > 80) {
            Log.w(TAG, "HIGH MEMORY USAGE at " + location);
            System.gc();
        }
    }
}
```

#### Add monitoring to key Activities:
```java
// In onCreate(), onResume(), onPause() of major Activities
@Override
protected void onCreate(Bundle savedInstanceState) {
    PerformanceMonitor.startTiming("MainActivity.onCreate");
    super.onCreate(savedInstanceState);
    // ... existing code ...
    PerformanceMonitor.endTiming("MainActivity.onCreate");
    PerformanceMonitor.trackMemoryUsage("MainActivity.onCreate.end");
}
```

---

## ✅ Testing Instructions

### Performance Tests:
1. **Launch Time Test:**
```bash
adb shell am start -W com.example.partymaker/.ui.features.core.MainActivity
```

1. **Memory Usage Test:**
```bash
adb shell dumpsys meminfo com.example.partymaker
```

1. **Scroll Performance Test:**
   - Open app with many groups
   - Scroll rapidly up/down
   - Check for frame drops in GPU profiler

### Expected Results:
- **App Launch**: <2 seconds
- **List Scrolling**: 60 FPS
- **Memory Usage**: <100MB during normal use
- **No ANRs**: All operations under 5 seconds

---

## 🚨 Critical Points

1. **Test on Low-End Device**: Use emulator with limited RAM
2. **Check Memory Leaks**: Run for extended periods
3. **Verify Thread Safety**: Ensure no race conditions
4. **Monitor ANRs**: Watch for Application Not Responding

---

## 📊 Success Criteria

- [ ] RecyclerView scrolling at 60 FPS
- [ ] App launch under 2 seconds
- [ ] Memory usage stable under 100MB
- [ ] No memory leaks in LeakCanary
- [ ] All performance monitoring logs working
- [ ] Thread pools properly configured

---

**Agent 01 Priority:** Complete RecyclerView DiffUtil first - biggest performance impact!
**Time Allocation:** RecyclerView (50%) → Threads (30%) → Monitoring (20%)