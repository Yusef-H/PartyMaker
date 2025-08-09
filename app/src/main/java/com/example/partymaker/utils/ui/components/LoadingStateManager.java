package com.example.partymaker.utils.ui.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieAnimationView;

/**
 * Manager class for handling loading states in a consistent and user-friendly way. Provides smooth
 * transitions between loading, content, and error states.
 */
public class LoadingStateManager {

  // Animation constants
  private static final int FADE_ANIMATION_DURATION_MS = 300;
  private static final int CELEBRATION_AUTO_TRANSITION_DELAY_MS = 1000;

  // Default messages
  private static final String DEFAULT_LOADING_MESSAGE = "Loading...";
  private static final String DEFAULT_ERROR_MESSAGE = "An error occurred";
  private static final String DEFAULT_SUCCESS_MESSAGE = "Success!";
  private static final String DEFAULT_CELEBRATION_MESSAGE = "Congratulations!";
  private static final String DEFAULT_EMPTY_MESSAGE = "No parties found";
  private static final String DEFAULT_SYNC_MESSAGE = "Syncing...";

  // Lottie animation file names
  private static final String LOTTIE_SUCCESS_ANIMATION = "success_checkmark.json";
  private static final String LOTTIE_ERROR_ANIMATION = "error_warning.json";
  private static final String LOTTIE_CELEBRATION_ANIMATION = "party_celebration.json";
  private static final String LOTTIE_EMPTY_ANIMATION = "empty_no_parties.json";
  private static final String LOTTIE_NETWORK_SYNC_ANIMATION = "network_sync.json";
  private static final String LOTTIE_USER_SWITCH_ANIMATION = "user_switch.json";

  // Lottie repeat counts
  private static final int LOTTIE_PLAY_ONCE = 0;
  private static final int LOTTIE_PLAY_TWICE = 1;
  private static final int LOTTIE_LOOP_INDEFINITELY = -1;

  private final View contentView;
  private final ProgressBar progressBar;
  private final TextView loadingText;
  private final View errorView;
  private final LottieAnimationView lottieAnimation;

  private LoadingState currentState = LoadingState.CONTENT;
  private String currentLoadingMessage = "Loading...";

  public LoadingStateManager(
      @NonNull View contentView,
      @NonNull ProgressBar progressBar,
      @Nullable TextView loadingText,
      @Nullable View errorView) {
    this(contentView, progressBar, loadingText, errorView, null);
  }

  public LoadingStateManager(
      @NonNull View contentView,
      @NonNull ProgressBar progressBar,
      @Nullable TextView loadingText,
      @Nullable View errorView,
      @Nullable LottieAnimationView lottieAnimation) {
    this.contentView = contentView;
    this.progressBar = progressBar;
    this.loadingText = loadingText;
    this.errorView = errorView;
    this.lottieAnimation = lottieAnimation;
  }

  /** Shows loading state with default message */
  public void showLoading() {
    showLoading(DEFAULT_LOADING_MESSAGE);
  }

  /** Shows loading state with custom message */
  public void showLoading(@NonNull String message) {
    if (currentState == LoadingState.LOADING) {
      // Just update the message if already loading
      updateLoadingMessage(message);
      return;
    }

    currentState = LoadingState.LOADING;
    currentLoadingMessage = message;

    // Update loading message
    updateLoadingMessage(message);

    // Animate transitions
    animateViewTransition(contentView, false);
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }

    // Use Lottie animation if available, otherwise fall back to ProgressBar
    if (lottieAnimation != null) {
      animateViewTransition(progressBar, false);
      animateViewTransition(lottieAnimation, true);
      lottieAnimation.playAnimation();
    } else {
      animateViewTransition(progressBar, true);
    }

    if (loadingText != null) {
      animateViewTransition(loadingText, true);
    }
  }

  /** Shows content state (hides loading and error) */
  public void showContent() {
    if (currentState == LoadingState.CONTENT) {
      return;
    }

    currentState = LoadingState.CONTENT;

    // Animate transitions - stop and hide loading animations
    if (lottieAnimation != null) {
      lottieAnimation.pauseAnimation();
      animateViewTransition(lottieAnimation, false);
    }
    animateViewTransition(progressBar, false);

    if (loadingText != null) {
      animateViewTransition(loadingText, false);
    }
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }
    animateViewTransition(contentView, true);
  }

  /** Shows error state with default error view */
  public void showError() {
    showError(DEFAULT_ERROR_MESSAGE);
  }

  /** Shows error state with custom message */
  public void showError(@NonNull String errorMessage) {
    if (currentState == LoadingState.ERROR) {
      return;
    }

    currentState = LoadingState.ERROR;

    // Update error message if error view has a TextView
    if (errorView instanceof ViewGroup) {
      TextView errorText = findTextViewInViewGroup((ViewGroup) errorView);
      if (errorText != null) {
        errorText.setText(errorMessage);
      }
    }

    // Animate transitions
    animateViewTransition(progressBar, false);
    if (loadingText != null) {
      animateViewTransition(loadingText, false);
    }
    animateViewTransition(contentView, false);
    if (errorView != null) {
      animateViewTransition(errorView, true);
    }
  }

  /** Shows empty state (content visible but empty) */
  public void showEmpty() {
    currentState = LoadingState.EMPTY;

    // Hide loading and error views
    animateViewTransition(progressBar, false);
    if (loadingText != null) {
      animateViewTransition(loadingText, false);
    }
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }

    // Show content (which should display empty state)
    animateViewTransition(contentView, true);
  }

  /** Updates the loading message without changing state */
  public void updateLoadingMessage(@NonNull String message) {
    currentLoadingMessage = message;
    if (loadingText != null && currentState == LoadingState.LOADING) {
      loadingText.setText(message);
    }
  }

  /** Gets the current loading state */
  @NonNull
  public LoadingState getCurrentState() {
    return currentState;
  }

  /** Checks if currently loading */
  public boolean isLoading() {
    return currentState == LoadingState.LOADING;
  }

  /** Checks if currently showing content */
  public boolean isShowingContent() {
    return currentState == LoadingState.CONTENT;
  }

  /** Checks if currently showing error */
  public boolean isShowingError() {
    return currentState == LoadingState.ERROR;
  }

  /** Checks if currently showing success */
  public boolean isShowingSuccess() {
    return currentState == LoadingState.SUCCESS;
  }

  /** Checks if currently showing celebration */
  public boolean isShowingCelebration() {
    return currentState == LoadingState.CELEBRATION;
  }

  /** Checks if currently syncing */
  public boolean isNetworkSyncing() {
    return currentState == LoadingState.NETWORK_SYNC;
  }

  /** Helper method to find a TextView within a ViewGroup */
  @Nullable
  private TextView findTextViewInViewGroup(@NonNull ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);
      if (child instanceof TextView) {
        return (TextView) child;
      } else if (child instanceof ViewGroup) {
        TextView foundTextView = findTextViewInViewGroup((ViewGroup) child);
        if (foundTextView != null) {
          return foundTextView;
        }
      }
    }
    return null;
  }

  /** Animates view visibility changes */
  private void animateViewTransition(@NonNull View view, boolean show) {
    if (show && view.getVisibility() == View.VISIBLE) {
      return; // Already visible
    }
    if (!show && view.getVisibility() == View.GONE) {
      return; // Already hidden
    }

    if (show) {
      // Show with fade in
      view.setAlpha(0f);
      view.setVisibility(View.VISIBLE);

      ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
      fadeIn.setDuration(FADE_ANIMATION_DURATION_MS);
      fadeIn.start();
    } else {
      // Hide with fade out
      ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
      fadeOut.setDuration(FADE_ANIMATION_DURATION_MS);
      fadeOut.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              view.setVisibility(View.GONE);
            }
          });
      fadeOut.start();
    }
  }

  /** Enum representing different loading states */
  public enum LoadingState {
    LOADING,
    CONTENT,
    ERROR,
    EMPTY,
    SUCCESS,
    CELEBRATION,
    NETWORK_SYNC
  }

  /** Sets custom Lottie animation for loading state */
  public void setLottieAnimation(@Nullable String animationName) {
    if (lottieAnimation != null && animationName != null) {
      lottieAnimation.setAnimation(animationName);
    }
  }

  /** Sets custom Lottie animation from raw resource */
  public void setLottieAnimation(int rawResId) {
    if (lottieAnimation != null) {
      lottieAnimation.setAnimation(rawResId);
    }
  }

  /** Shows loading state with custom Lottie animation */
  public void showLoadingWithAnimation(@NonNull String message, @Nullable String animationName) {
    setLottieAnimation(animationName);
    showLoading(message);
  }

  /** Shows success state with checkmark animation */
  public void showSuccess() {
    showSuccess(DEFAULT_SUCCESS_MESSAGE);
  }

  /** Shows success state with custom message */
  public void showSuccess(@NonNull String message) {
    currentState = LoadingState.SUCCESS;
    currentLoadingMessage = message;

    // Hide content and progress bar
    animateViewTransition(contentView, false);
    animateViewTransition(progressBar, false);
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }

    // Show success animation
    if (lottieAnimation != null) {
      lottieAnimation.setAnimation(LOTTIE_SUCCESS_ANIMATION);
      lottieAnimation.setRepeatCount(LOTTIE_PLAY_ONCE);
      animateViewTransition(lottieAnimation, true);
      lottieAnimation.playAnimation();
    }

    // Update and show text
    updateLoadingMessage(message);
    if (loadingText != null) {
      animateViewTransition(loadingText, true);
    }
  }

  /** Shows error state with warning animation */
  public void showErrorWithAnimation() {
    showErrorWithAnimation(DEFAULT_ERROR_MESSAGE);
  }

  /** Shows error state with warning animation and custom message */
  public void showErrorWithAnimation(@NonNull String errorMessage) {
    currentState = LoadingState.ERROR;
    currentLoadingMessage = errorMessage;

    // Hide other views
    animateViewTransition(contentView, false);
    animateViewTransition(progressBar, false);

    // Show error animation
    if (lottieAnimation != null) {
      lottieAnimation.setAnimation(LOTTIE_ERROR_ANIMATION);
      lottieAnimation.setRepeatCount(LOTTIE_PLAY_ONCE);
      animateViewTransition(lottieAnimation, true);
      lottieAnimation.playAnimation();
    }

    // Update and show text
    updateLoadingMessage(errorMessage);
    if (loadingText != null) {
      animateViewTransition(loadingText, true);
    }

    // Show error view if available
    if (errorView != null) {
      animateViewTransition(errorView, true);
    }
  }

  /** Shows celebration animation for achievements */
  public void showCelebration() {
    showCelebration(DEFAULT_CELEBRATION_MESSAGE);
  }

  /** Shows celebration animation with custom message */
  public void showCelebration(@NonNull String message) {
    currentState = LoadingState.CELEBRATION;
    currentLoadingMessage = message;

    // Hide other views
    animateViewTransition(progressBar, false);
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }

    // Show celebration animation
    if (lottieAnimation != null) {
      lottieAnimation.setAnimation(LOTTIE_CELEBRATION_ANIMATION);
      lottieAnimation.setRepeatCount(LOTTIE_PLAY_TWICE);
      animateViewTransition(lottieAnimation, true);
      lottieAnimation.playAnimation();
    }

    // Update and show text
    updateLoadingMessage(message);
    if (loadingText != null) {
      animateViewTransition(loadingText, true);
    }

    // Show content after celebration (delayed)
    if (lottieAnimation != null) {
      lottieAnimation.addAnimatorListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              // Auto-transition to content after celebration
              android.os.Handler handler =
                  new android.os.Handler(android.os.Looper.getMainLooper());
              handler.postDelayed(() -> showContent(), CELEBRATION_AUTO_TRANSITION_DELAY_MS);
            }
          });
    }
  }

  /** Shows empty state with sad face animation */
  public void showEmptyWithAnimation() {
    showEmptyWithAnimation(DEFAULT_EMPTY_MESSAGE);
  }

  /** Shows empty state with animation and custom message */
  public void showEmptyWithAnimation(@NonNull String message) {
    currentState = LoadingState.EMPTY;
    currentLoadingMessage = message;

    // Hide loading and error views
    animateViewTransition(progressBar, false);
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }

    // Show empty animation
    if (lottieAnimation != null) {
      lottieAnimation.setAnimation(LOTTIE_EMPTY_ANIMATION);
      lottieAnimation.setRepeatCount(LOTTIE_LOOP_INDEFINITELY);
      animateViewTransition(lottieAnimation, true);
      lottieAnimation.playAnimation();
    }

    // Update and show text
    updateLoadingMessage(message);
    if (loadingText != null) {
      animateViewTransition(loadingText, true);
    }

    // Show content view (which should handle empty state display)
    animateViewTransition(contentView, true);
  }

  /** Shows network sync animation for Firebase operations */
  public void showNetworkSync() {
    showNetworkSync(DEFAULT_SYNC_MESSAGE);
  }

  /** Shows network sync animation with custom message */
  public void showNetworkSync(@NonNull String message) {
    currentState = LoadingState.NETWORK_SYNC;
    currentLoadingMessage = message;

    // Hide other views
    animateViewTransition(contentView, false);
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }

    // Show network sync animation
    if (lottieAnimation != null) {
      animateViewTransition(progressBar, false);
      lottieAnimation.setAnimation(LOTTIE_NETWORK_SYNC_ANIMATION);
      lottieAnimation.setRepeatCount(LOTTIE_LOOP_INDEFINITELY);
      animateViewTransition(lottieAnimation, true);
      lottieAnimation.playAnimation();
    } else {
      animateViewTransition(progressBar, true);
    }

    // Update and show text
    updateLoadingMessage(message);
    if (loadingText != null) {
      animateViewTransition(loadingText, true);
    }
  }

  /** Shows user switch animation */
  public void showUserSwitch(@NonNull String message) {
    currentState = LoadingState.LOADING; // Treat as loading variant
    currentLoadingMessage = message;

    // Hide other views
    animateViewTransition(contentView, false);
    if (errorView != null) {
      animateViewTransition(errorView, false);
    }

    // Show user switch animation
    if (lottieAnimation != null) {
      animateViewTransition(progressBar, false);
      lottieAnimation.setAnimation(LOTTIE_USER_SWITCH_ANIMATION);
      lottieAnimation.setRepeatCount(LOTTIE_PLAY_ONCE);
      animateViewTransition(lottieAnimation, true);
      lottieAnimation.playAnimation();
    } else {
      animateViewTransition(progressBar, true);
    }

    // Update and show text
    updateLoadingMessage(message);
    if (loadingText != null) {
      animateViewTransition(loadingText, true);
    }
  }

  /** Builder class for creating LoadingStateManager instances */
  public static class Builder {
    private View contentView;
    private ProgressBar progressBar;
    private TextView loadingText;
    private View errorView;
    private LottieAnimationView lottieAnimation;

    public Builder contentView(@NonNull View contentView) {
      this.contentView = contentView;
      return this;
    }

    public Builder progressBar(@NonNull ProgressBar progressBar) {
      this.progressBar = progressBar;
      return this;
    }

    public Builder loadingText(@Nullable TextView loadingText) {
      this.loadingText = loadingText;
      return this;
    }

    public Builder errorView(@Nullable View errorView) {
      this.errorView = errorView;
      return this;
    }

    public Builder lottieAnimation(@Nullable LottieAnimationView lottieAnimation) {
      this.lottieAnimation = lottieAnimation;
      return this;
    }

    public LoadingStateManager build() {
      if (contentView == null) {
        throw new IllegalStateException("Content view must be set");
      }
      if (progressBar == null) {
        throw new IllegalStateException("Progress bar must be set");
      }

      return new LoadingStateManager(
          contentView, progressBar, loadingText, errorView, lottieAnimation);
    }
  }

  /** Interface for handling state change events */
  public interface StateChangeListener {
    void onStateChanged(@NonNull LoadingState oldState, @NonNull LoadingState newState);
  }
}
