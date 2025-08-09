package com.example.partymaker.utils.ui.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Professional button animation helper for PartyMaker Creates delightful micro-interactions and
 * smooth animations for all interactive elements
 */
public class ButtonAnimationHelper {

  private static final int PRESS_DURATION = 150;
  private static final int RELEASE_DURATION = 200;
  private static final float PRESS_SCALE = 0.95f;
  private static final float ELEVATION_PRESSED = 8f;
  private static final float ELEVATION_NORMAL = 4f;

  /**
   * Applies professional press animation to any view Creates smooth scale and elevation changes
   * with haptic feedback
   */
  public static void applyPressAnimation(@NonNull View view) {
    applyPressAnimation(view, true);
  }

  /** Applies press animation with optional haptic feedback */
  public static void applyPressAnimation(@NonNull View view, boolean enableHaptics) {
    view.setOnTouchListener(
        (v, event) -> {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              animatePress(v, enableHaptics);
              break;
            case MotionEvent.ACTION_UP:
              animateRelease(v);
              v.performClick();
              break;
            case MotionEvent.ACTION_CANCEL:
              animateRelease(v);
              break;
          }
          return false; // Allow other touch events to continue
        });
  }

  /** Creates elegant press animation with scale and elevation */
  private static void animatePress(@NonNull View view, boolean enableHaptics) {
    // Scale animation
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, PRESS_SCALE);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, PRESS_SCALE);

    // Elevation animation for cards and buttons
    ObjectAnimator elevation = null;
    if (view instanceof CardView) {
      elevation =
          ObjectAnimator.ofFloat(
              view, "cardElevation", ((CardView) view).getCardElevation(), ELEVATION_PRESSED);
    } else if (view instanceof MaterialButton || view instanceof FloatingActionButton) {
      elevation = ObjectAnimator.ofFloat(view, "elevation", view.getElevation(), ELEVATION_PRESSED);
    }

    AnimatorSet animatorSet = new AnimatorSet();
    if (elevation != null) {
      animatorSet.playTogether(scaleX, scaleY, elevation);
    } else {
      animatorSet.playTogether(scaleX, scaleY);
    }

    animatorSet.setDuration(PRESS_DURATION);
    animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
    animatorSet.start();

    // Haptic feedback
    if (enableHaptics) {
      performHapticFeedback(view);
    }
  }

  /** Creates smooth release animation returning to normal state */
  private static void animateRelease(@NonNull View view) {
    // Scale animation back to normal
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1f);

    // Elevation animation back to normal
    ObjectAnimator elevation = null;
    if (view instanceof CardView) {
      elevation =
          ObjectAnimator.ofFloat(
              view, "cardElevation", ((CardView) view).getCardElevation(), ELEVATION_NORMAL);
    } else if (view instanceof MaterialButton || view instanceof FloatingActionButton) {
      elevation = ObjectAnimator.ofFloat(view, "elevation", view.getElevation(), ELEVATION_NORMAL);
    }

    AnimatorSet animatorSet = new AnimatorSet();
    if (elevation != null) {
      animatorSet.playTogether(scaleX, scaleY, elevation);
    } else {
      animatorSet.playTogether(scaleX, scaleY);
    }

    animatorSet.setDuration(RELEASE_DURATION);
    animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
    animatorSet.start();
  }

  /** Applies bounce animation for success states */
  public static void applySuccessBounce(@NonNull View view) {
    ObjectAnimator bounceX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f);
    ObjectAnimator bounceY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f);

    AnimatorSet bounceSet = new AnimatorSet();
    bounceSet.playTogether(bounceX, bounceY);
    bounceSet.setDuration(400);
    bounceSet.setInterpolator(new OvershootInterpolator(2f));
    bounceSet.start();

    performHapticFeedback(view);
  }

  /** Applies shake animation for error states */
  public static void applyErrorShake(@NonNull View view) {
    ObjectAnimator shake =
        ObjectAnimator.ofFloat(view, "translationX", 0, -15, 15, -10, 10, -5, 5, 0);
    shake.setDuration(500);
    shake.setInterpolator(new DecelerateInterpolator());
    shake.start();

    // Error haptic feedback - stronger vibration
    performErrorHapticFeedback(view);
  }

  /** Creates pulsing animation for attention-grabbing elements */
  public static ValueAnimator applyPulseAnimation(@NonNull View view) {
    ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1.08f, 1f);
    pulseAnimator.setDuration(1000);
    pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
    pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

    pulseAnimator.addUpdateListener(
        animation -> {
          float scale = (float) animation.getAnimatedValue();
          view.setScaleX(scale);
          view.setScaleY(scale);
        });

    pulseAnimator.start();
    return pulseAnimator;
  }

  /** Applies morphing animation for FAB state changes */
  public static void applyFabMorphAnimation(@NonNull FloatingActionButton fab, int newIcon) {
    // Scale down
    ObjectAnimator scaleDown = ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0f);
    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0f);

    AnimatorSet scaleDownSet = new AnimatorSet();
    scaleDownSet.playTogether(scaleDown, scaleDownY);
    scaleDownSet.setDuration(150);

    scaleDownSet.addListener(
        new android.animation.AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(android.animation.Animator animation) {
            // Change icon
            fab.setImageResource(newIcon);

            // Scale back up
            ObjectAnimator scaleUp = ObjectAnimator.ofFloat(fab, "scaleX", 0f, 1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(fab, "scaleY", 0f, 1f);

            AnimatorSet scaleUpSet = new AnimatorSet();
            scaleUpSet.playTogether(scaleUp, scaleUpY);
            scaleUpSet.setDuration(200);
            scaleUpSet.setInterpolator(new OvershootInterpolator(1.5f));
            scaleUpSet.start();
          }
        });

    scaleDownSet.start();
  }

  /** Creates ripple effect for custom views that don't have built-in ripples */
  public static void applyCustomRipple(@NonNull View view, int rippleColor) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      RippleDrawable rippleDrawable =
          new RippleDrawable(
              android.content.res.ColorStateList.valueOf(rippleColor), view.getBackground(), null);
      view.setBackground(rippleDrawable);
    }
  }

  /** Applies entrance animation for views appearing on screen */
  public static void applyEntranceAnimation(@NonNull View view, long delay) {
    view.setAlpha(0f);
    view.setScaleX(0.8f);
    view.setScaleY(0.8f);
    view.setTranslationY(50f);

    view.animate()
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .translationY(0f)
        .setDuration(300)
        .setStartDelay(delay)
        .setInterpolator(new OvershootInterpolator(1.1f))
        .start();
  }

  /** Performs subtle haptic feedback for normal interactions */
  private static void performHapticFeedback(@NonNull View view) {
    try {
      Context context = view.getContext();
      Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

      if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
          vibrator.vibrate(10);
        }
      }
    } catch (SecurityException e) {
      // Silently fail if vibrate permission is not granted
      // The animation will still work without haptic feedback
    } catch (Exception e) {
      // Silently fail for any other vibration-related issues
    }
  }

  /** Performs stronger haptic feedback for error states */
  private static void performErrorHapticFeedback(@NonNull View view) {
    Context context = view.getContext();
    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

    if (vibrator != null && vibrator.hasVibrator()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(new long[] {0, 100, 50, 100}, -1));
      } else {
        vibrator.vibrate(new long[] {0, 100, 50, 100}, -1);
      }
    }
  }

  /** Quick method to apply professional animations to common button types */
  public static void enhanceButton(@NonNull View button) {
    applyPressAnimation(button);
    applyCustomRipple(button, 0x30FFFFFF); // Semi-transparent white ripple
  }

  /** Quick method to enhance card views with press animations */
  public static void enhanceCard(@NonNull CardView card) {
    applyPressAnimation(card);
    card.setCardElevation(ELEVATION_NORMAL);
  }

  /** Applies stagger animation to a list of views (for RecyclerView items) */
  public static void applyStaggerAnimation(@NonNull View[] views) {
    for (int i = 0; i < views.length; i++) {
      applyEntranceAnimation(views[i], i * 50L); // 50ms delay between each item
    }
  }
}
