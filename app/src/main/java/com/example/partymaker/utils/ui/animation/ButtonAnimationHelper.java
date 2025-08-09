package com.example.partymaker.utils.ui.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.RippleDrawable;
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

  // Animation duration constants
  private static final int PRESS_DURATION_MS = 150;
  private static final int RELEASE_DURATION_MS = 200;
  private static final int BOUNCE_DURATION_MS = 400;
  private static final int SHAKE_DURATION_MS = 500;
  private static final int PULSE_DURATION_MS = 1000;
  private static final int FAB_MORPH_SCALE_DOWN_MS = 150;
  private static final int FAB_MORPH_SCALE_UP_MS = 200;
  private static final int ENTRANCE_ANIMATION_MS = 300;
  private static final int STAGGER_DELAY_MS = 50;

  // Scale constants
  private static final float PRESS_SCALE = 0.95f;
  private static final float BOUNCE_SCALE_MAX = 1.15f;
  private static final float PULSE_SCALE_MAX = 1.08f;
  private static final float ENTRANCE_SCALE_MIN = 0.8f;

  // Elevation constants
  private static final float ELEVATION_PRESSED = 8f;
  private static final float ELEVATION_NORMAL = 4f;

  // Animation values
  private static final float SHAKE_TRANSLATION = 15f;
  private static final float ENTRANCE_TRANSLATION_Y = 50f;
  private static final int VIBRATION_DURATION_MS = 10;
  private static final int ERROR_VIBRATION_DURATION_MS = 100;
  private static final int ERROR_VIBRATION_PAUSE_MS = 50;
  private static final int RIPPLE_COLOR = 0x30FFFFFF;
  private static final float OVERSHOOT_TENSION = 1.2f;
  private static final float FAB_OVERSHOOT_TENSION = 1.5f;
  private static final float ENTRANCE_OVERSHOOT_TENSION = 1.1f;
  private static final float BOUNCE_OVERSHOOT_TENSION = 2f;

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

    animatorSet.setDuration(PRESS_DURATION_MS);
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

    animatorSet.setDuration(RELEASE_DURATION_MS);
    animatorSet.setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION));
    animatorSet.start();
  }

  /** Applies bounce animation for success states */
  public static void applySuccessBounce(@NonNull View view) {
    ObjectAnimator bounceX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f);
    ObjectAnimator bounceY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f);

    AnimatorSet bounceSet = new AnimatorSet();
    bounceSet.playTogether(bounceX, bounceY);
    bounceSet.setDuration(BOUNCE_DURATION_MS);
    bounceSet.setInterpolator(new OvershootInterpolator(BOUNCE_OVERSHOOT_TENSION));
    bounceSet.start();

    performHapticFeedback(view);
  }

  /** Applies shake animation for error states */
  public static void applyErrorShake(@NonNull View view) {
    ObjectAnimator shake =
        ObjectAnimator.ofFloat(
            view,
            "translationX",
            0,
            -SHAKE_TRANSLATION,
            SHAKE_TRANSLATION,
            -SHAKE_TRANSLATION * 0.67f,
            SHAKE_TRANSLATION * 0.67f,
            -SHAKE_TRANSLATION * 0.33f,
            SHAKE_TRANSLATION * 0.33f,
            0);
    shake.setDuration(SHAKE_DURATION_MS);
    shake.setInterpolator(new DecelerateInterpolator());
    shake.start();

    // Error haptic feedback - stronger vibration
    performErrorHapticFeedback(view);
  }

  /** Creates pulsing animation for attention-grabbing elements */
  public static ValueAnimator applyPulseAnimation(@NonNull View view) {
    ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, PULSE_SCALE_MAX, 1f);
    pulseAnimator.setDuration(PULSE_DURATION_MS);
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
    scaleDownSet.setDuration(FAB_MORPH_SCALE_DOWN_MS);

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
            scaleUpSet.setDuration(FAB_MORPH_SCALE_UP_MS);
            scaleUpSet.setInterpolator(new OvershootInterpolator(FAB_OVERSHOOT_TENSION));
            scaleUpSet.start();
          }
        });

    scaleDownSet.start();
  }

  /** Creates ripple effect for custom views that don't have built-in ripples */
  public static void applyCustomRipple(@NonNull View view, int rippleColor) {
    RippleDrawable rippleDrawable =
        new RippleDrawable(
            android.content.res.ColorStateList.valueOf(rippleColor), view.getBackground(), null);
    view.setBackground(rippleDrawable);
  }

  /** Applies entrance animation for views appearing on screen */
  public static void applyEntranceAnimation(@NonNull View view, long delay) {
    view.setAlpha(0f);
    view.setScaleX(ENTRANCE_SCALE_MIN);
    view.setScaleY(ENTRANCE_SCALE_MIN);
    view.setTranslationY(ENTRANCE_TRANSLATION_Y);

    view.animate()
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .translationY(0f)
        .setDuration(ENTRANCE_ANIMATION_MS)
        .setStartDelay(delay)
        .setInterpolator(new OvershootInterpolator(ENTRANCE_OVERSHOOT_TENSION))
        .start();
  }

  /** Performs subtle haptic feedback for normal interactions */
  private static void performHapticFeedback(@NonNull View view) {
    try {
      Context context = view.getContext();
      Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

      if (vibrator != null && vibrator.hasVibrator()) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
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
      vibrator.vibrate(
          VibrationEffect.createWaveform(
              new long[] {
                0,
                ERROR_VIBRATION_DURATION_MS,
                ERROR_VIBRATION_PAUSE_MS,
                ERROR_VIBRATION_DURATION_MS
              },
              -1));
    }
  }

  /** Quick method to apply professional animations to common button types */
  public static void enhanceButton(@NonNull View button) {
    applyPressAnimation(button);
    applyCustomRipple(button, RIPPLE_COLOR);
  }

  /** Quick method to enhance card views with press animations */
  public static void enhanceCard(@NonNull CardView card) {
    applyPressAnimation(card);
    card.setCardElevation(ELEVATION_NORMAL);
  }

  /** Applies stagger animation to a list of views (for RecyclerView items) */
  public static void applyStaggerAnimation(@NonNull View[] views) {
    for (int i = 0; i < views.length; i++) {
      applyEntranceAnimation(views[i], i * (long) STAGGER_DELAY_MS);
    }
  }
}
