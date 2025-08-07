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

  private static final int ANIMATION_DURATION = 300;

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
    showLoading("Loading...");
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
    showError("An error occurred");
  }

  /** Shows error state with custom message */
  public void showError(@NonNull String errorMessage) {
    if (currentState == LoadingState.ERROR) {
      return;
    }

    currentState = LoadingState.ERROR;

    // Update error message if error view has a TextView
    if (errorView instanceof ViewGroup) {
      TextView errorText = null; // Optional error text view
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
      fadeIn.setDuration(ANIMATION_DURATION);
      fadeIn.start();
    } else {
      // Hide with fade out
      ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
      fadeOut.setDuration(ANIMATION_DURATION);
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
    showSuccess("Success!");
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
      lottieAnimation.setAnimation("success_checkmark.json");
      lottieAnimation.setRepeatCount(0); // Play once
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
    showErrorWithAnimation("Error occurred");
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
      lottieAnimation.setAnimation("error_warning.json");
      lottieAnimation.setRepeatCount(0); // Play once
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
    showCelebration("Congratulations!");
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
      lottieAnimation.setAnimation("party_celebration.json");
      lottieAnimation.setRepeatCount(1); // Play twice for effect
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
      lottieAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          // Auto-transition to content after celebration
          android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
          handler.postDelayed(() -> showContent(), 1000);
        }
      });
    }
  }

  /** Shows empty state with sad face animation */
  public void showEmptyWithAnimation() {
    showEmptyWithAnimation("No parties found");
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
      lottieAnimation.setAnimation("empty_no_parties.json");
      lottieAnimation.setRepeatCount(-1); // Loop indefinitely
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
    showNetworkSync("Syncing...");
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
      lottieAnimation.setAnimation("network_sync.json");
      lottieAnimation.setRepeatCount(-1); // Loop while syncing
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
      lottieAnimation.setAnimation("user_switch.json");
      lottieAnimation.setRepeatCount(0); // Play once
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

      return new LoadingStateManager(contentView, progressBar, loadingText, errorView, lottieAnimation);
    }
  }

  /** Interface for handling state change events */
  public interface StateChangeListener {
    void onStateChanged(@NonNull LoadingState oldState, @NonNull LoadingState newState);
  }
}
