package com.example.partymaker.utils.ui;

import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.utils.media.ImageOptimizationManager;

/**
 * Utility class for optimizing RecyclerView scrolling performance.
 * Reduces jank, improves smoothness, and minimizes frame drops during scroll.
 */
public class RecyclerViewScrollOptimizer {
    private static final String TAG = "RecyclerViewScrollOptimizer";
    
    // Scroll state constants
    private static final int FLING_VELOCITY_THRESHOLD = 4000;
    private static final int PREFETCH_DISTANCE = 2;
    
    /**
     * Applies comprehensive scroll optimizations to a RecyclerView
     * @param recyclerView The RecyclerView to optimize
     */
    public static void optimizeScrolling(@NonNull RecyclerView recyclerView) {
        try {
            // Add optimized scroll listener
            recyclerView.addOnScrollListener(new OptimizedScrollListener());
            
            // Setup fling optimization
            optimizeFlingBehavior(recyclerView);
            
            // Setup prefetch optimization
            optimizePrefetchBehavior(recyclerView);
            
            // Setup edge effect optimization
            optimizeEdgeEffects(recyclerView);
            
            Log.d(TAG, "RecyclerView scroll optimizations applied");
        } catch (Exception e) {
            Log.e(TAG, "Error applying scroll optimizations", e);
        }
    }
    
    /**
     * Optimizes fling behavior for smoother scrolling
     */
    private static void optimizeFlingBehavior(@NonNull RecyclerView recyclerView) {
        try {
            recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
                @Override
                public boolean onFling(int velocityX, int velocityY) {
                    // Reduce fling velocity for smoother scrolling
                    if (Math.abs(velocityY) > FLING_VELOCITY_THRESHOLD) {
                        int reducedVelocity = (int) (velocityY * 0.7);
                        recyclerView.fling(velocityX, reducedVelocity);
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error optimizing fling behavior", e);
        }
    }
    
    /**
     * Optimizes prefetch behavior for smoother scrolling
     */
    private static void optimizePrefetchBehavior(@NonNull RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            linearLayoutManager.setItemPrefetchEnabled(true);
            linearLayoutManager.setInitialPrefetchItemCount(PREFETCH_DISTANCE);
        }
    }
    
    /**
     * Optimizes edge effects to reduce overdraw
     */
    private static void optimizeEdgeEffects(@NonNull RecyclerView recyclerView) {
        // Disable overscroll effects to reduce GPU operations
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        
        // Disable edge glow effects
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(false);
    }
    
    /**
     * Custom scroll listener that optimizes rendering during scroll
     */
    private static class OptimizedScrollListener extends RecyclerView.OnScrollListener {
        private boolean isScrolling = false;
        private static final int SCROLL_THRESHOLD = 10;
        
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            
            switch (newState) {
                case RecyclerView.SCROLL_STATE_DRAGGING:
                case RecyclerView.SCROLL_STATE_SETTLING:
                    if (!isScrolling) {
                        isScrolling = true;
                        onScrollStart(recyclerView);
                    }
                    break;
                    
                case RecyclerView.SCROLL_STATE_IDLE:
                    if (isScrolling) {
                        isScrolling = false;
                        onScrollStop(recyclerView);
                    }
                    break;
            }
        }
        
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            
            // Pause image loading during fast scroll
            if (Math.abs(dy) > SCROLL_THRESHOLD) {
                ImageOptimizationManager.pauseImageLoading();
            }
        }
        
        /**
         * Called when scrolling starts
         */
        private void onScrollStart(@NonNull RecyclerView recyclerView) {
            try {
                // Skip layer type changes - causes visual issues
                
                // Pause complex animations if animator exists
                RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
                if (animator != null) {
                    animator.setMoveDuration(0);
                    animator.setAddDuration(0);
                    animator.setRemoveDuration(0);
                    animator.setChangeDuration(0);
                }
                
                Log.v(TAG, "Scroll optimizations activated");
            } catch (Exception e) {
                Log.e(TAG, "Error in onScrollStart", e);
            }
        }
        
        /**
         * Called when scrolling stops
         */
        private void onScrollStop(@NonNull RecyclerView recyclerView) {
            try {
                // Skip layer type changes - causes visual issues
                
                // Resume image loading
                ImageOptimizationManager.resumeImageLoading();
                
                // Restore animations if animator exists
                RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
                if (animator != null) {
                    animator.setMoveDuration(250);
                    animator.setAddDuration(250);
                    animator.setRemoveDuration(250);
                    animator.setChangeDuration(250);
                }
                
                Log.v(TAG, "Scroll optimizations deactivated");
            } catch (Exception e) {
                Log.e(TAG, "Error in onScrollStop", e);
            }
        }
    }
    
    /**
     * Applies smooth scroll optimization for programmatic scrolling
     */
    public static void smoothScrollToPosition(@NonNull RecyclerView recyclerView, int position) {
        try {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null) {
                RecyclerView.SmoothScroller smoothScroller = 
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }
                        
                        @Override
                        protected float calculateSpeedPerPixel(android.util.DisplayMetrics displayMetrics) {
                            // Reduce scroll speed for smoother animation
                            return 100f / displayMetrics.densityDpi;
                        }
                    };
                    
                smoothScroller.setTargetPosition(position);
                layoutManager.startSmoothScroll(smoothScroller);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in smooth scroll", e);
            // Fallback to regular scroll
            recyclerView.scrollToPosition(position);
        }
    }
}