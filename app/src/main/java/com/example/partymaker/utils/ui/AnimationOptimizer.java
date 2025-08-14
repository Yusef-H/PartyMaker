package com.example.partymaker.utils.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Optimized animation utilities with caching and performance optimizations
 */
public class AnimationOptimizer {
    private static final String TAG = "AnimationOptimizer";
    private static final Map<String, Animation> animationCache = new ConcurrentHashMap<>();
    private static final Map<String, Animator> propertyAnimatorCache = new ConcurrentHashMap<>();
    
    // Animation duration constants
    private static final long FAST_ANIMATION_DURATION = 150;
    private static final long MEDIUM_ANIMATION_DURATION = 300;
    private static final long SLOW_ANIMATION_DURATION = 500;
    
    // Create optimized fade in animation
    public static Animation createFadeIn(long duration) {
        String key = "fade_in_" + duration;
        
        return animationCache.computeIfAbsent(key, k -> {
            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(duration);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setFillAfter(true);
            return animation;
        });
    }
    
    public static Animation createFadeOut(long duration) {
        String key = "fade_out_" + duration;
        
        return animationCache.computeIfAbsent(key, k -> {
            AlphaAnimation animation = new AlphaAnimation(1f, 0f);
            animation.setDuration(duration);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setFillAfter(true);
            return animation;
        });
    }
    
    // Optimized slide animations with caching
    public static Animation createSlideIn(int direction, long duration) {
        String key = "slide_in_" + direction + "_" + duration;
        
        return animationCache.computeIfAbsent(key, k -> {
            TranslateAnimation animation;
            switch (direction) {
                case Gravity.LEFT:
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, -1f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f);
                    break;
                case Gravity.RIGHT:
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 1f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f);
                    break;
                case Gravity.TOP:
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, -1f,
                        Animation.RELATIVE_TO_SELF, 0f);
                    break;
                default:
                    animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 1f,
                        Animation.RELATIVE_TO_SELF, 0f);
                    break;
            }
            animation.setDuration(duration);
            animation.setInterpolator(new OvershootInterpolator(1.2f));
            animation.setFillAfter(true);
            return animation;
        });
    }
    
    // Property animator versions for better performance
    public static Animator createPropertyFadeIn(View view, long duration) {
        String key = "prop_fade_in_" + duration;
        
        Animator animator = propertyAnimatorCache.get(key);
        if (animator != null) {
            animator.setTarget(view);
            return animator;
        }
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeIn.setDuration(duration);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        
        propertyAnimatorCache.put(key, fadeIn);
        return fadeIn;
    }
    
    public static Animator createPropertySlideUp(View view, long duration) {
        String key = "prop_slide_up_" + duration;
        
        Animator animator = propertyAnimatorCache.get(key);
        if (animator != null) {
            animator.setTarget(view);
            return animator;
        }
        
        float startY = view.getHeight();
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", startY, 0f);
        slideUp.setDuration(duration);
        slideUp.setInterpolator(new OvershootInterpolator(1.1f));
        
        propertyAnimatorCache.put(key, slideUp);
        return slideUp;
    }
    
    // Staggered animation for RecyclerView items - DISABLED for smooth scrolling
    public static void applyStaggeredAnimation(RecyclerView recyclerView) {
        // Animation disabled to prevent unwanted scroll effects
        Log.d(TAG, "Staggered animation disabled for better scroll performance");
    }
    
    // Combined animation for better performance
    public static void animateViewEntrance(View view, long delay) {
        if (view == null || shouldDisableAnimations(view.getContext())) {
            return;
        }
        
        // Use AnimatorSet for coordinated animations
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        
        animatorSet.playTogether(fadeIn, scaleX, scaleY);
        animatorSet.setDuration(MEDIUM_ANIMATION_DURATION);
        animatorSet.setStartDelay(delay);
        animatorSet.setInterpolator(new OvershootInterpolator(1.1f));
        
        // Set initial values
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        
        animatorSet.start();
    }
    
    // Optimize animations for RecyclerView items
    public static void optimizeRecyclerViewAnimations(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }
        
        // Disable item animations for better scroll performance
        recyclerView.setItemAnimator(null);
        
        // No entrance animations to prevent scroll jank
        // Items will appear immediately without animation
        
        Log.d(TAG, "RecyclerView animations disabled for smooth scrolling");
    }
    
    // Shimmer effect animation for loading states
    public static ValueAnimator createShimmerAnimation(View view) {
        if (view == null) {
            return null;
        }
        
        ValueAnimator shimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        shimmerAnimator.setDuration(1500);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        
        shimmerAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            float alpha = 0.3f + (animatedValue * 0.4f); // Animate between 0.3 and 0.7 alpha
            view.setAlpha(alpha);
        });
        
        return shimmerAnimator;
    }
    
    // Clear animation cache to free memory
    public static void clearAnimationCache() {
        animationCache.clear();
        propertyAnimatorCache.clear();
        Log.d(TAG, "Animation cache cleared");
    }
    
    // Check if animations should be disabled (accessibility)
    public static boolean shouldDisableAnimations(Context context) {
        if (context == null) {
            return false;
        }
        
        try {
            float animationScale = Settings.Global.getFloat(
                context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            );
            
            return animationScale == 0f;
        } catch (Exception e) {
            Log.w(TAG, "Could not check animation scale setting", e);
            return false;
        }
    }
    
    // Bounce animation for interactive elements
    public static void animateBounce(View view) {
        if (view == null || shouldDisableAnimations(view.getContext())) {
            return;
        }
        
        AnimatorSet bounceSet = new AnimatorSet();
        
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f);
        
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);
        
        bounceSet.play(scaleDownX).with(scaleDownY);
        bounceSet.play(scaleUpX).with(scaleUpY).after(scaleDownX);
        
        bounceSet.start();
    }
    
    // Pulse animation for notifications or highlights
    public static ValueAnimator createPulseAnimation(View view) {
        if (view == null) {
            return null;
        }
        
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1.1f, 1f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        pulseAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            view.setScaleX(animatedValue);
            view.setScaleY(animatedValue);
        });
        
        return pulseAnimator;
    }
}