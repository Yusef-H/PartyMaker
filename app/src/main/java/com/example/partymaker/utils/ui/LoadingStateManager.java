package com.example.partymaker.utils.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.partymaker.R;

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

  private LoadingState currentState = LoadingState.CONTENT;
  private String currentLoadingMessage = "Loading...";

  public LoadingStateManager(
      @NonNull View contentView,
      @NonNull ProgressBar progressBar,
      @Nullable TextView loadingText,
      @Nullable View errorView) {
    this.contentView = contentView;
    this.progressBar = progressBar;
    this.loadingText = loadingText;
    this.errorView = errorView;
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
    animateViewTransition(progressBar, true);
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

    // Animate transitions
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
    EMPTY
  }

  /** Builder class for creating LoadingStateManager instances */
  public static class Builder {
    private View contentView;
    private ProgressBar progressBar;
    private TextView loadingText;
    private View errorView;

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

    public LoadingStateManager build() {
      if (contentView == null) {
        throw new IllegalStateException("Content view must be set");
      }
      if (progressBar == null) {
        throw new IllegalStateException("Progress bar must be set");
      }

      return new LoadingStateManager(contentView, progressBar, loadingText, errorView);
    }
  }

  /** Interface for handling state change events */
  public interface StateChangeListener {
    void onStateChanged(@NonNull LoadingState oldState, @NonNull LoadingState newState);
  }
}
