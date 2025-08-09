package com.example.partymaker.utils.ui.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;

/**
 * Utility class for creating smooth UI animations and transitions. Provides consistent animation
 * patterns across the PartyMaker app.
 */
public class UiAnimationHelper {

  // Animation duration constants
  private static final int SHORT_DURATION = 200;
  private static final int MEDIUM_DURATION = 300;
  private static final int LONG_DURATION = 500;

  /** Creates a smooth fade in animation for a view */
  public static void fadeIn(@NonNull View view) {
    fadeIn(view, MEDIUM_DURATION, null);
  }

  /** Creates a smooth fade in animation with callback */
  public static void fadeIn(@NonNull View view, int duration, Runnable onComplete) {
    view.setAlpha(0f);
    view.setVisibility(View.VISIBLE);

    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
    fadeIn.setDuration(duration);
    fadeIn.setInterpolator(new DecelerateInterpolator());

    if (onComplete != null) {
      fadeIn.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              onComplete.run();
            }
          });
    }

    fadeIn.start();
  }

  /** Creates a smooth fade out animation for a view */
  public static void fadeOut(@NonNull View view) {
    fadeOut(view, MEDIUM_DURATION, null);
  }

  /** Creates a smooth fade out animation with callback */
  public static void fadeOut(@NonNull View view, int duration, Runnable onComplete) {
    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
    fadeOut.setDuration(duration);
    fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());

    fadeOut.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setVisibility(View.GONE);
            if (onComplete != null) {
              onComplete.run();
            }
          }
        });

    fadeOut.start();
  }

  /** Creates a smooth scale animation for buttons and interactive elements */
  public static void scalePress(@NonNull View view) {
    ObjectAnimator scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
    scaleDown.setDuration(SHORT_DURATION);
    scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
    scaleDownY.setDuration(SHORT_DURATION);
    scaleDownY.setInterpolator(new AccelerateDecelerateInterpolator());

    scaleDown.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            // Scale back up
            ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
            scaleUp.setDuration(SHORT_DURATION);
            scaleUp.setInterpolator(new DecelerateInterpolator());

            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
            scaleUpY.setDuration(SHORT_DURATION);
            scaleUpY.setInterpolator(new DecelerateInterpolator());

            scaleUp.start();
            scaleUpY.start();
          }
        });

    scaleDown.start();
    scaleDownY.start();
  }

  /** Creates a smooth slide in from bottom animation */
  public static void slideInFromBottom(@NonNull View view) {
    view.setTranslationY(view.getHeight());
    view.setVisibility(View.VISIBLE);

    ObjectAnimator slideIn = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0f);
    slideIn.setDuration(MEDIUM_DURATION);
    slideIn.setInterpolator(new DecelerateInterpolator());
    slideIn.start();
  }

  /** Creates a smooth slide out to bottom animation */
  public static void slideOutToBottom(@NonNull View view, Runnable onComplete) {
    ObjectAnimator slideOut = ObjectAnimator.ofFloat(view, "translationY", 0f, view.getHeight());
    slideOut.setDuration(MEDIUM_DURATION);
    slideOut.setInterpolator(new AccelerateDecelerateInterpolator());

    slideOut.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setVisibility(View.GONE);
            view.setTranslationY(0f); // Reset for next use
            if (onComplete != null) {
              onComplete.run();
            }
          }
        });

    slideOut.start();
  }

  /** Creates a gentle bounce animation for success feedback */
  public static void bounceSuccess(@NonNull View view) {
    ObjectAnimator bounce = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
    bounce.setDuration(LONG_DURATION);
    bounce.setInterpolator(new DecelerateInterpolator());

    ObjectAnimator bounceY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);
    bounceY.setDuration(LONG_DURATION);
    bounceY.setInterpolator(new DecelerateInterpolator());

    bounce.start();
    bounceY.start();
  }

  /** Creates a shake animation for error feedback */
  public static void shakeError(@NonNull View view) {
    ObjectAnimator shake =
        ObjectAnimator.ofFloat(view, "translationX", 0, -25, 25, -25, 25, -15, 15, -5, 5, 0);
    shake.setDuration(LONG_DURATION);
    shake.setInterpolator(new DecelerateInterpolator());
    shake.start();
  }

  /** Creates a smooth transition between two views */
  public static void crossFade(@NonNull View fadeOut, @NonNull View fadeIn) {
    fadeOut(fadeOut, MEDIUM_DURATION, null);
    fadeIn(fadeIn, MEDIUM_DURATION, null);
  }

  /** Creates a pulsing animation for loading states */
  public static ValueAnimator createPulseAnimation(@NonNull View view) {
    ValueAnimator pulse = ValueAnimator.ofFloat(0.8f, 1.2f);
    pulse.setDuration(LONG_DURATION);
    pulse.setRepeatCount(ValueAnimator.INFINITE);
    pulse.setRepeatMode(ValueAnimator.REVERSE);
    pulse.setInterpolator(new AccelerateDecelerateInterpolator());

    pulse.addUpdateListener(
        animation -> {
          float scale = (float) animation.getAnimatedValue();
          view.setScaleX(scale);
          view.setScaleY(scale);
        });

    return pulse;
  }
}
