# AGENT 05 - UI & Layout Optimization

## 🎯 Mission: UI Performance & Layout Optimization
**Estimated Time: 4-5 hours**
**Priority: MEDIUM-HIGH**

---

## 📋 Tasks Overview

### Task 1: Layout Optimization
**Time: 2-3 hours | Priority: HIGH**

#### Files to Modify:
- All XML layout files in `app/src/main/res/layout/`
- Focus on: `activity_main.xml`, `item_group.xml`, `activity_chat.xml`

#### 1. Convert Nested Layouts to ConstraintLayout:

**Update activity_main.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.features.core.MainActivity">

    <!-- Toolbar -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:background="?attr/colorPrimary"
        android:elevation="4dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:title="@string/app_name"
        app:titleTextColor="?attr/colorOnPrimary" />

    <!-- Search View (Hidden by default) -->
    <androidx.appcompat.widget.SearchView
        android:id="@+id/search_view"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/toolbar"
        tools:visibility="visible" />

    <!-- SwipeRefreshLayout with RecyclerView -->
    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipe_refresh"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toTopOf="@id/fab_create_group"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/search_view">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recycler_view_groups"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false"
            android:paddingBottom="16dp"
            android:scrollbars="vertical"
            tools:itemCount="5"
            tools:listitem="@layout/item_group" />

    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>

    <!-- Empty State (Using ViewStub for performance) -->
    <ViewStub
        android:id="@+id/stub_empty_state"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout="@layout/layout_empty_groups"
        android:inflatedId="@+id/empty_state_layout"
        app:layout_constraintBottom_toTopOf="@id/fab_create_group"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/search_view" />

    <!-- Loading State (Using ViewStub) -->
    <ViewStub
        android:id="@+id/stub_loading_state"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout="@layout/layout_loading_shimmer"
        android:inflatedId="@+id/loading_state_layout"
        app:layout_constraintBottom_toTopOf="@id/fab_create_group"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/search_view" />

    <!-- Floating Action Button -->
    <com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
        android:id="@+id/fab_create_group"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="16dp"
        android:layout_marginBottom="16dp"
        android:text="@string/create_group"
        app:icon="@drawable/ic_add"
        app:layout_behavior="com.google.android.material.behavior.HideBottomViewOnScrollBehavior"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Update item_group.xml (RecyclerView item):**
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginVertical="4dp"
    android:background="?attr/selectableItemBackground"
    android:clickable="true"
    android:focusable="true"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp"
    app:strokeWidth="0dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <!-- Group Image -->
        <com.google.android.material.imageview.ShapeableImageView
            android:id="@+id/image_group"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:scaleType="centerCrop"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:shapeAppearanceOverlay="@style/ShapeAppearanceOverlay.App.CornerSize12dp"
            tools:src="@drawable/placeholder_group" />

        <!-- Group Name -->
        <TextView
            android:id="@+id/text_group_name"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_marginEnd="8dp"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="?attr/textAppearanceSubtitle1"
            android:textStyle="bold"
            app:layout_constraintEnd_toStartOf="@id/text_unread_count"
            app:layout_constraintStart_toEndOf="@id/image_group"
            app:layout_constraintTop_toTopOf="@id/image_group"
            tools:text="Group Name" />

        <!-- Last Message -->
        <TextView
            android:id="@+id/text_last_message"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_marginTop="4dp"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="?attr/textAppearanceCaption"
            android:textColor="?attr/colorOnSurfaceVariant"
            app:layout_constraintEnd_toEndOf="@id/text_group_name"
            app:layout_constraintStart_toEndOf="@id/image_group"
            app:layout_constraintTop_toBottomOf="@id/text_group_name"
            tools:text="Last message preview..." />

        <!-- Timestamp -->
        <TextView
            android:id="@+id/text_timestamp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="2dp"
            android:textAppearance="?attr/textAppearanceCaption"
            android:textColor="?attr/colorOnSurfaceVariant"
            android:textSize="10sp"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="@id/image_group"
            tools:text="2:30 PM" />

        <!-- Unread Count Badge -->
        <TextView
            android:id="@+id/text_unread_count"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginEnd="8dp"
            android:background="@drawable/bg_unread_count"
            android:minWidth="20dp"
            android:paddingHorizontal="6dp"
            android:paddingVertical="2dp"
            android:textAppearance="?attr/textAppearanceCaption"
            android:textColor="?attr/colorOnPrimary"
            android:textSize="10sp"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="@id/image_group"
            app:layout_constraintEnd_toStartOf="@id/text_timestamp"
            tools:text="3"
            tools:visibility="visible" />

    </androidx.constraintlayout.widget.ConstraintLayout>

</com.google.android.material.card.MaterialCardView>
```

#### 2. Create Optimized Empty State Layout:
**Create:** `app/src/main/res/layout/layout_empty_groups.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="32dp">

    <!-- Lottie Animation -->
    <com.airbnb.lottie.LottieAnimationView
        android:id="@+id/lottie_empty"
        android:layout_width="200dp"
        android:layout_height="200dp"
        app:layout_constraintBottom_toTopOf="@id/text_empty_title"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_chainStyle="packed"
        app:lottie_autoPlay="true"
        app:lottie_fileName="empty_no_parties.json"
        app:lottie_loop="true" />

    <!-- Empty State Title -->
    <TextView
        android:id="@+id/text_empty_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:gravity="center"
        android:text="@string/no_groups_title"
        android:textAppearance="?attr/textAppearanceHeadline6"
        app:layout_constraintBottom_toTopOf="@id/text_empty_subtitle"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/lottie_empty" />

    <!-- Empty State Subtitle -->
    <TextView
        android:id="@+id/text_empty_subtitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:gravity="center"
        android:text="@string/no_groups_subtitle"
        android:textAppearance="?attr/textAppearanceBody2"
        android:textColor="?attr/colorOnSurfaceVariant"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/text_empty_title" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

#### 3. Create Loading Shimmer Layout:
**Create:** `app/src/main/res/layout/layout_loading_shimmer.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res/auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:paddingHorizontal="16dp"
    android:paddingVertical="8dp">

    <com.facebook.shimmer.ShimmerFrameLayout
        android:id="@+id/shimmer_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:shimmer_auto_start="true"
        app:shimmer_base_color="?attr/colorSurfaceVariant"
        app:shimmer_colored="false"
        app:shimmer_highlight_color="?attr/colorSurface">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Repeat this item for shimmer effect -->
            <include 
                layout="@layout/item_group_shimmer"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
            
            <include layout="@layout/item_group_shimmer" />
            <include layout="@layout/item_group_shimmer" />
            <include layout="@layout/item_group_shimmer" />
            <include layout="@layout/item_group_shimmer" />

        </LinearLayout>

    </com.facebook.shimmer.ShimmerFrameLayout>

</LinearLayout>
```

**Create:** `app/src/main/res/layout/item_group_shimmer.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginVertical="4dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <!-- Shimmer Image Placeholder -->
        <View
            android:id="@+id/shimmer_image"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:background="@drawable/shimmer_placeholder_rounded"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <!-- Shimmer Title Placeholder -->
        <View
            android:id="@+id/shimmer_title"
            android:layout_width="0dp"
            android:layout_height="16dp"
            android:layout_marginStart="12dp"
            android:layout_marginEnd="60dp"
            android:background="@drawable/shimmer_placeholder"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@id/shimmer_image"
            app:layout_constraintTop_toTopOf="@id/shimmer_image" />

        <!-- Shimmer Subtitle Placeholder -->
        <View
            android:id="@+id/shimmer_subtitle"
            android:layout_width="0dp"
            android:layout_height="12dp"
            android:layout_marginStart="12dp"
            android:layout_marginTop="8dp"
            android:layout_marginEnd="40dp"
            android:background="@drawable/shimmer_placeholder"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@id/shimmer_image"
            app:layout_constraintTop_toBottomOf="@id/shimmer_title" />

    </androidx.constraintlayout.widget.ConstraintLayout>

</com.google.android.material.card.MaterialCardView>
```

---

### Task 2: Overdraw Reduction
**Time: 1 hour | Priority: MEDIUM**

#### Create Drawable Resources:
**Create:** `app/src/main/res/drawable/shimmer_placeholder.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="?attr/colorSurfaceVariant" />
    <corners android:radius="4dp" />
</shape>
```

**Create:** `app/src/main/res/drawable/shimmer_placeholder_rounded.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="?attr/colorSurfaceVariant" />
    <corners android:radius="12dp" />
</shape>
```

**Create:** `app/src/main/res/drawable/bg_unread_count.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="?attr/colorPrimary" />
    <corners android:radius="10dp" />
</shape>
```

#### Overdraw Optimization Rules:
1. **Remove Unnecessary Backgrounds:**
   - Search for `android:background="@android:color/white"` and remove if parent has background
   - Use `android:background="@null"` where appropriate

2. **Use Window Background:**
   - Set window background in theme instead of root layout background

**Update themes.xml:**
```xml
<style name="AppTheme" parent="Theme.Material3.DynamicColors.Light">
    <item name="android:windowBackground">@color/surface</item>
    <item name="colorPrimary">@color/primary</item>
    <item name="colorSurface">@color/surface</item>
    <!-- Remove redundant backgrounds from layouts -->
</style>
```

---

### Task 3: View Optimization
**Time: 1 hour | Priority: MEDIUM**

#### Create ViewOptimizationHelper:
**Create:** `app/src/main/java/com/example/partymaker/utils/ui/ViewOptimizationHelper.java`
```java
public class ViewOptimizationHelper {
    private static final String TAG = "ViewOptimization";
    
    // Optimize RecyclerView setup
    public static void optimizeRecyclerView(RecyclerView recyclerView) {
        // Fixed size optimization
        recyclerView.setHasFixedSize(true);
        
        // Increase view cache size
        recyclerView.setItemViewCacheSize(20);
        
        // Enable drawing cache
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        // Disable item animator for better performance
        recyclerView.setItemAnimator(null);
        
        // Setup layout manager optimizations
        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false; // Disable for better scroll performance
            }
        };
        
        recyclerView.setLayoutManager(layoutManager);
        
        Log.d(TAG, "RecyclerView optimized");
    }
    
    // Optimize ImageView for memory
    public static void optimizeImageView(ImageView imageView) {
        // Set appropriate scale type
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Enable hardware acceleration
        imageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        Log.d(TAG, "ImageView optimized");
    }
    
    // Batch view optimizations
    public static void optimizeViewGroup(ViewGroup viewGroup) {
        // Optimize all child ImageViews
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            
            if (child instanceof ImageView) {
                optimizeImageView((ImageView) child);
            } else if (child instanceof RecyclerView) {
                optimizeRecyclerView((RecyclerView) child);
            } else if (child instanceof ViewGroup) {
                optimizeViewGroup((ViewGroup) child);
            }
        }
    }
    
    // Setup view recycling for custom views
    public static void enableViewRecycling(View view, String tag) {
        view.setTag(R.id.view_recycling_tag, tag);
    }
    
    // Memory leak prevention for views
    public static void clearViewReferences(View view) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setImageDrawable(null);
            imageView.setImageBitmap(null);
        }
        
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                clearViewReferences(viewGroup.getChildAt(i));
            }
        }
    }
}
```

#### Update MainActivity with optimizations:
```java
public class MainActivity extends BaseActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupViews();
        optimizeViews(); // Add this
    }
    
    private void optimizeViews() {
        // Optimize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_groups);
        ViewOptimizationHelper.optimizeRecyclerView(recyclerView);
        
        // Optimize root view group
        ViewGroup rootView = findViewById(android.R.id.content);
        ViewOptimizationHelper.optimizeViewGroup(rootView);
        
        // Enable hardware acceleration for smooth animations
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );
    }
    
    @Override
    protected void onDestroy() {
        // Clear view references to prevent memory leaks
        ViewGroup rootView = findViewById(android.R.id.content);
        ViewOptimizationHelper.clearViewReferences(rootView);
        
        super.onDestroy();
    }
}
```

---

### Task 4: Animation Performance
**Time: 1 hour | Priority: LOW-MEDIUM**

#### Create AnimationOptimizer:
**Create:** `app/src/main/java/com/example/partymaker/utils/ui/AnimationOptimizer.java`
```java
public class AnimationOptimizer {
    private static final String TAG = "AnimationOptimizer";
    private static final Map<String, Animation> animationCache = new ConcurrentHashMap<>();
    
    // Create optimized fade animation
    public static Animation createFadeIn(long duration) {
        String key = "fade_in_" + duration;
        
        return animationCache.computeIfAbsent(key, k -> {
            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(duration);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            return animation;
        });
    }
    
    public static Animation createFadeOut(long duration) {
        String key = "fade_out_" + duration;
        
        return animationCache.computeIfAbsent(key, k -> {
            AlphaAnimation animation = new AlphaAnimation(1f, 0f);
            animation.setDuration(duration);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            return animation;
        });
    }
    
    // Optimized slide animations
    public static Animation createSlideIn(int direction, long duration) {
        String key = "slide_in_" + direction + "_" + duration;
        
        return animationCache.computeIfAbsent(key, k -> {
            TranslateAnimation animation;
            switch (direction) {
                case Gravity.LEFT:
                    animation = new TranslateAnimation(-100, 0, 0, 0);
                    break;
                case Gravity.RIGHT:
                    animation = new TranslateAnimation(100, 0, 0, 0);
                    break;
                case Gravity.TOP:
                    animation = new TranslateAnimation(0, 0, -100, 0);
                    break;
                default:
                    animation = new TranslateAnimation(0, 0, 100, 0);
                    break;
            }
            animation.setDuration(duration);
            animation.setInterpolator(new OvershootInterpolator());
            return animation;
        });
    }
    
    // Apply staggered animation to RecyclerView items
    public static void applyStaggeredAnimation(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Only animate if scrolling down
                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                        
                        for (int i = firstVisibleItem; i <= lastVisibleItem; i++) {
                            View itemView = layoutManager.findViewByPosition(i);
                            if (itemView != null && itemView.getAnimation() == null) {
                                Animation animation = createSlideIn(Gravity.BOTTOM, 300);
                                animation.setStartOffset((i - firstVisibleItem) * 50);
                                itemView.startAnimation(animation);
                            }
                        }
                    }
                }
            }
        });
    }
    
    // Clear animation cache to free memory
    public static void clearAnimationCache() {
        animationCache.clear();
        Log.d(TAG, "Animation cache cleared");
    }
    
    // Check if animations should be disabled (accessibility)
    public static boolean shouldDisableAnimations(Context context) {
        float animationScale = Settings.Global.getFloat(
            context.getContentResolver(),
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        );
        
        return animationScale == 0f;
    }
}
```

---

## ✅ Testing Instructions

### UI Performance Tests:

1. **Layout Hierarchy Test:**
```bash
# Use Layout Inspector in Android Studio
# Check hierarchy depth - should be max 3-4 levels

# Use systrace for frame analysis
adb shell atrace -t 10 -b 8192 -a com.example.partymaker gfx input view
```

1. **Overdraw Test:**
```bash
# Enable overdraw debugging
adb shell setprop debug.hwui.overdraw show

# Check for minimal red areas (4x overdraw)
```

1. **Animation Performance:**
```bash
# Enable GPU rendering profile
adb shell setprop debug.hwui.profile visual_bars

# Check for bars staying below 16ms line
```

### Expected Results:
- Layout hierarchy depth < 4 levels
- Minimal overdraw (mostly green/blue areas)
- Smooth 60fps animations
- RecyclerView scroll without frame drops

---

## 🚨 Critical Points

1. **Test on Low-End Device**: Use low-spec emulator
2. **Check Memory Usage**: Ensure view optimizations don't increase memory
3. **Accessibility**: Verify animations can be disabled
4. **Orientation Changes**: Test layout performance on rotation

---

## 📊 Success Criteria

- [ ] Layout hierarchy optimized (max 3-4 levels)
- [ ] Overdraw minimized (mostly green areas)
- [ ] RecyclerView scrolling at 60fps
- [ ] Animation performance smooth
- [ ] ViewStub implementation working
- [ ] Memory usage stable with UI optimizations

---

**Agent 05 Priority:** Focus on Layout Optimization first - biggest UI performance impact!
**Time Allocation:** Layout Conversion (40%) → Overdraw Reduction (25%) → View Optimization (20%) → Animations (15%)