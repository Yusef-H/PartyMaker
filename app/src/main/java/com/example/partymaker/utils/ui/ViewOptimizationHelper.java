package com.example.partymaker.utils.ui;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.utils.media.ImageOptimizationManager;

/**
 * Utility class for optimizing view performance and memory usage
 */
public class ViewOptimizationHelper {
    private static final String TAG = "ViewOptimization";
    
    // Optimize RecyclerView setup for better performance and reduce OpenGLRenderer operations
    public static void optimizeRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == null) {
            Log.w(TAG, "RecyclerView is null, cannot optimize");
            return;
        }
        
        try {
            // Fixed size optimization - prevents layout calculation on item changes
            recyclerView.setHasFixedSize(true);
            
            // Increase view cache size for better scroll performance
            recyclerView.setItemViewCacheSize(20);
            
            // Enable drawing cache for smoother scrolling
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            
            // Disable item animator for better performance during updates
            recyclerView.setItemAnimator(null);
            
            // Disable nested scrolling to reduce OpenGLRenderer operations
            recyclerView.setNestedScrollingEnabled(false);
            
            // Reduce overdraw by disabling clipToPadding when not needed
            recyclerView.setClipToPadding(false);
            
            // Setup layout manager optimizations
            LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext()) {
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false; // Disable for better scroll performance
                }
                
                @Override
                public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                    try {
                        super.onLayoutChildren(recycler, state);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, "Error in RecyclerView layout", e);
                    }
                }
            };
            
            // Enable prefetch for smoother scrolling (Android 5.0+)
            layoutManager.setItemPrefetchEnabled(true);
            layoutManager.setInitialPrefetchItemCount(4);
            
            recyclerView.setLayoutManager(layoutManager);
            
            // Shared RecyclerView pool for memory efficiency
            RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
            sharedPool.setMaxRecycledViews(0, 25); // Increase pool size for group items
            recyclerView.setRecycledViewPool(sharedPool);
            
            // Add scroll listener to optimize rendering during scroll
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                        // Reduce layer type during fast scroll to minimize GPU operations
                        recyclerView.setLayerType(View.LAYER_TYPE_NONE, null);
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        // Re-enable hardware acceleration when idle
                        recyclerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    }
                }
            });
            
            Log.d(TAG, "RecyclerView optimized successfully with OpenGLRenderer optimizations");
        } catch (Exception e) {
            Log.e(TAG, "Error optimizing RecyclerView", e);
        }
    }
    
    // Optimize individual RecyclerView child views
    private static void optimizeRecyclerViewChildren(RecyclerView recyclerView) {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            if (child instanceof ViewGroup) {
                optimizeViewGroup((ViewGroup) child);
            }
        }
    }
    
    // Optimize ImageView for memory and performance
    public static void optimizeImageView(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        
        try {
            // Set appropriate scale type for consistent sizing
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            
            // Use software layer for static images to reduce GPU operations
            // Hardware layer is only beneficial for animations
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            
            // Note: setFilterBitmap is not available in ImageView, it's for Bitmap drawing
            
            // No specific optimization needed here - let the image loading handle it
            // ImageOptimizationManager provides optimized loading methods
            
            Log.v(TAG, "ImageView optimized");
        } catch (Exception e) {
            Log.e(TAG, "Error optimizing ImageView", e);
        }
    }
    
    // Batch view optimizations for ViewGroup and children
    public static void optimizeViewGroup(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        
        try {
            // Optimize drawing performance
            viewGroup.setWillNotDraw(false);
            viewGroup.setClipChildren(true);
            viewGroup.setClipToPadding(true);
            
            // Recursively optimize all child views
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
            
            Log.v(TAG, "ViewGroup optimized with " + viewGroup.getChildCount() + " children");
        } catch (Exception e) {
            Log.e(TAG, "Error optimizing ViewGroup", e);
        }
    }
    
    // Setup view recycling for custom views
    public static void enableViewRecycling(View view, String tag) {
        if (view == null || tag == null) {
            return;
        }
        
        view.setTag(R.id.view_recycling_tag, tag);
        
        // Enable hardware acceleration for smooth animations
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        Log.v(TAG, "View recycling enabled for tag: " + tag);
    }
    
    // Memory leak prevention for views
    public static void clearViewReferences(View view) {
        if (view == null) {
            return;
        }
        
        try {
            // Clear ImageView references to prevent memory leaks
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                imageView.setImageDrawable(null);
                imageView.setImageBitmap(null);
                // Use ImageOptimizationManager for proper cleanup
                ImageOptimizationManager.clearImageView(imageView);
            }
            
            // Recursively clear child view references
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    clearViewReferences(viewGroup.getChildAt(i));
                }
            }
            
            // Clear any tags that might hold references
            view.setTag(null);
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing view references", e);
        }
    }
    
    // Inflate ViewStub safely with error handling
    public static View inflateViewStub(ViewStub viewStub) {
        if (viewStub == null) {
            Log.w(TAG, "ViewStub is null, cannot inflate");
            return null;
        }
        
        try {
            View inflatedView = viewStub.inflate();
            Log.d(TAG, "ViewStub inflated successfully: " + viewStub.getLayoutResource());
            return inflatedView;
        } catch (Exception e) {
            Log.e(TAG, "Error inflating ViewStub", e);
            return null;
        }
    }
    
    // Check if ViewStub has been inflated
    public static boolean isViewStubInflated(ViewStub viewStub) {
        return viewStub != null && viewStub.getParent() == null;
    }
    
    // Safe ViewStub inflation with callback
    public static void inflateViewStubAsync(ViewStub viewStub, ViewStubInflationCallback callback) {
        if (viewStub == null) {
            if (callback != null) {
                callback.onInflationFailed("ViewStub is null");
            }
            return;
        }
        
        // Inflate on main thread but with proper error handling
        try {
            View inflatedView = viewStub.inflate();
            if (callback != null) {
                callback.onInflationComplete(inflatedView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inflating ViewStub asynchronously", e);
            if (callback != null) {
                callback.onInflationFailed(e.getMessage());
            }
        }
    }
    
    // Callback interface for ViewStub inflation
    public interface ViewStubInflationCallback {
        void onInflationComplete(View inflatedView);
        void onInflationFailed(String error);
    }
    
    // Enable hardware acceleration for entire view hierarchy
    public static void enableHardwareAcceleration(View rootView) {
        if (rootView == null) {
            return;
        }
        
        try {
            rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            
            if (rootView instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) rootView;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);
                    child.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            }
            
            Log.d(TAG, "Hardware acceleration enabled for view hierarchy");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling hardware acceleration", e);
        }
    }
}