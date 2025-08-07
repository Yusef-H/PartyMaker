package com.example.partymaker.utils.ui.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.airbnb.lottie.LottieAnimationView;

/**
 * Custom refresh animation helper for PartyMaker
 * Creates party-themed pull-to-refresh animations
 */
public class CustomRefreshAnimationHelper {
    
    /**
     * Applies party-themed colors to SwipeRefreshLayout
     */
    public static void setupPartyRefreshLayout(@NonNull SwipeRefreshLayout refreshLayout) {
        refreshLayout.setColorSchemeColors(
            0xFF0E81D1, // PartyMaker blue
            0xFF1976D2, // Material blue
            0xFF2196F3, // Light blue
            0xFF00BCD4  // Cyan
        );
        
        // Set background color
        refreshLayout.setProgressBackgroundColorSchemeColor(0xFFF5F5F5);
        
        // Set size
        refreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        
        // Set distance to trigger refresh
        refreshLayout.setDistanceToTriggerSync(200);
        
        // Set progress view offset
        refreshLayout.setProgressViewOffset(false, -100, 200);
    }
    
    /**
     * Creates a custom refresh animation using Lottie
     */
    public static void createCustomRefreshAnimation(@NonNull View parentView, 
                                                   @NonNull LottieAnimationView refreshLottie,
                                                   boolean isRefreshing) {
        if (isRefreshing) {
            // Show and start party loading animation
            refreshLottie.setVisibility(View.VISIBLE);
            refreshLottie.setAnimation("party_loading.json");
            refreshLottie.setRepeatCount(-1); // Infinite loop
            refreshLottie.playAnimation();
            
            // Add gentle bounce to parent
            createRefreshBounceAnimation(parentView);
            
        } else {
            // Hide animation with smooth fade out
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(refreshLottie, "alpha", 1f, 0f);
            fadeOut.setDuration(300);
            fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    refreshLottie.setVisibility(View.GONE);
                    refreshLottie.pauseAnimation();
                    refreshLottie.setAlpha(1f); // Reset alpha for next time
                }
            });
            fadeOut.start();
        }
    }
    
    /**
     * Creates gentle bounce animation during refresh (disabled to prevent shake)
     */
    private static void createRefreshBounceAnimation(@NonNull View view) {
        // Disabled bounce animation to prevent shake after refresh
        // The animation was causing visual artifacts and uneven spacing
        // Just ensure the view stays at normal scale
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
    }
    
    /**
     * Stops refresh bounce animation
     */
    public static void stopRefreshBounceAnimation(@NonNull View view) {
        ValueAnimator animator = (ValueAnimator) view.getTag(com.example.partymaker.R.id.refresh_animator_tag);
        if (animator != null) {
            animator.cancel();
            
            // Smoothly return to normal scale
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1f);
            scaleX.setDuration(200);
            scaleY.setDuration(200);
            scaleX.start();
            scaleY.start();
            
            view.setTag(com.example.partymaker.R.id.refresh_animator_tag, null);
        }
    }
    
    /**
     * Creates pull-down visual feedback
     */
    public static void createPullDownFeedback(@NonNull View view, float pullDistance) {
        // Calculate scale based on pull distance
        float maxPull = 300f;
        float pullRatio = Math.min(pullDistance / maxPull, 1f);
        float scale = 1f + (pullRatio * 0.05f); // Max 5% scale increase
        
        view.setScaleX(scale);
        view.setScaleY(scale);
        
        // Add slight rotation for dynamic effect
        float rotation = pullRatio * 2f; // Max 2 degrees rotation
        view.setRotation(rotation);
    }
    
    /**
     * Resets pull-down feedback
     */
    public static void resetPullDownFeedback(@NonNull View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", view.getRotation(), 0f);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        rotation.setDuration(200);
        
        scaleX.start();
        scaleY.start();
        rotation.start();
    }
    
    /**
     * Creates success feedback after refresh completion
     */
    public static void createRefreshSuccessFeedback(@NonNull View view) {
        // Quick flash effect
        ObjectAnimator flash = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f);
        flash.setDuration(300);
        flash.setInterpolator(new AccelerateDecelerateInterpolator());
        flash.start();
        
        // Success scale bounce
        ObjectAnimator bounceX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f);
        bounceX.setDuration(400);
        bounceY.setDuration(400);
        bounceX.start();
        bounceY.start();
    }
    
    /**
     * Creates loading shimmer effect for list items during refresh
     */
    public static ValueAnimator createShimmerEffect(@NonNull View view) {
        ValueAnimator shimmerAnimator = ValueAnimator.ofFloat(-1f, 2f);
        shimmerAnimator.setDuration(1500);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setInterpolator(new LinearInterpolator());
        
        shimmerAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            
            // Create shimmer overlay effect
            float alpha = 0.3f + 0.4f * (float) Math.sin(progress * Math.PI);
            view.setAlpha(Math.max(0.3f, Math.min(1f, alpha)));
        });
        
        return shimmerAnimator;
    }
}