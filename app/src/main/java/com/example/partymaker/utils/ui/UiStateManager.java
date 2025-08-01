package com.example.partymaker.utils.ui;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.example.partymaker.R;
import com.example.partymaker.data.api.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

/**
 * Utility class for managing UI states and user feedback. Provides standardized methods for showing
 * loading states, errors, success messages, and managing user interactions during different
 * application states.
 */
public class UiStateManager {

  /** Shows a loading state with optional message */
  public static void showLoading(@NonNull View containerView, @NonNull String message) {
    showSnackbar(containerView, message, Snackbar.LENGTH_INDEFINITE, SnackbarType.INFO);
  }

  /** Shows a loading state with default message */
  public static void showLoading(@NonNull View containerView) {
    showLoading(containerView, "Loading...");
  }

  /** Shows a success message */
  public static void showSuccess(@NonNull View containerView, @NonNull String message) {
    showSnackbar(containerView, message, Snackbar.LENGTH_SHORT, SnackbarType.SUCCESS);
  }

  /** Shows a success message with resource string */
  public static void showSuccess(
      @NonNull Context context, @NonNull View containerView, @StringRes int messageRes) {
    showSuccess(containerView, context.getString(messageRes));
  }

  /** Shows an error message with retry option */
  public static void showError(
      @NonNull View containerView, @NonNull String message, @NonNull Runnable retryAction) {
    Snackbar snackbar = Snackbar.make(containerView, message, Snackbar.LENGTH_LONG);
    snackbar.setAction("RETRY", v -> retryAction.run());
    snackbar.setActionTextColor(containerView.getContext().getColor(R.color.primaryBlue));
    snackbar.show();
  }

  /** Shows an error message without retry option */
  public static void showError(@NonNull View containerView, @NonNull String message) {
    showSnackbar(containerView, message, Snackbar.LENGTH_LONG, SnackbarType.ERROR);
  }

  /** Shows an error message based on error type with retry option */
  public static void showError(
      @NonNull View containerView,
      @NonNull NetworkUtils.ErrorType errorType,
      @NonNull Runnable retryAction) {
    String message = NetworkUtils.getErrorMessage(errorType);
    showError(containerView, message, retryAction);
  }

  /** Shows an error message based on error type without retry option */
  public static void showError(
      @NonNull View containerView, @NonNull NetworkUtils.ErrorType errorType) {
    String message = NetworkUtils.getErrorMessage(errorType);
    showError(containerView, message);
  }

  /** Shows a warning message */
  public static void showWarning(@NonNull View containerView, @NonNull String message) {
    showSnackbar(containerView, message, Snackbar.LENGTH_LONG, SnackbarType.WARNING);
  }

  /** Shows an info message */
  public static void showInfo(@NonNull View containerView, @NonNull String message) {
    showSnackbar(containerView, message, Snackbar.LENGTH_SHORT, SnackbarType.INFO);
  }

  /** Shows a confirmation message with action */
  public static void showConfirmation(
      @NonNull View containerView,
      @NonNull String message,
      @NonNull String actionText,
      @NonNull Runnable action) {
    Snackbar snackbar = Snackbar.make(containerView, message, Snackbar.LENGTH_LONG);
    snackbar.setAction(actionText, v -> action.run());
    snackbar.setActionTextColor(containerView.getContext().getColor(R.color.primaryBlue));
    snackbar.show();
  }

  /** Shows a network status message */
  public static void showNetworkStatus(@NonNull View containerView, boolean isOnline) {
    if (isOnline) {
      showInfo(containerView, "Network connection restored");
    } else {
      showWarning(containerView, "You're offline - some features may be limited");
    }
  }

  /** Shows a progress message with percentage */
  public static void showProgress(
      @NonNull View containerView, @NonNull String operation, int percentage) {
    String message = operation + " " + percentage + "%";
    showSnackbar(containerView, message, Snackbar.LENGTH_SHORT, SnackbarType.INFO);
  }

  /** Shows a simple toast message (for cases where Snackbar is not suitable) */
  public static void showToast(@NonNull Context context, @NonNull String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  /** Shows a long toast message */
  public static void showLongToast(@NonNull Context context, @NonNull String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

  /** Dismisses any active snackbars */
  public static void dismissActiveSnackbars(@NonNull View containerView) {
    // This is handled automatically by Snackbar when a new one is shown
    // But we can implement a registry if needed for manual dismissal
  }

  /** Internal method to show snackbar with different styles */
  private static void showSnackbar(
      @NonNull View containerView,
      @NonNull String message,
      int duration,
      @NonNull SnackbarType type) {
    Snackbar snackbar = Snackbar.make(containerView, message, duration);

    // Customize snackbar based on type
    switch (type) {
      case SUCCESS:
        snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.success_green));
        snackbar.setTextColor(containerView.getContext().getColor(R.color.white));
        break;
      case ERROR:
        snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.error_red));
        snackbar.setTextColor(containerView.getContext().getColor(R.color.white));
        break;
      case WARNING:
        snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.warning_orange));
        snackbar.setTextColor(containerView.getContext().getColor(R.color.white));
        break;
      case INFO:
      default:
        snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.primaryBlue));
        snackbar.setTextColor(containerView.getContext().getColor(R.color.white));
        break;
    }

    snackbar.show();
  }

  /** Enum for different types of snackbar messages */
  private enum SnackbarType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
  }

  /** Interface for handling user feedback events */
  public interface FeedbackListener {
    void onRetryRequested();

    void onDismissed();

    void onActionClicked();
  }

  /** Builder class for creating complex feedback messages */
  public static class FeedbackBuilder {
    private final View containerView;
    private String message;
    private String actionText;
    private Runnable actionCallback;
    private int duration = Snackbar.LENGTH_SHORT;
    private SnackbarType type = SnackbarType.INFO;

    public FeedbackBuilder(@NonNull View containerView) {
      this.containerView = containerView;
    }

    public FeedbackBuilder message(@NonNull String message) {
      this.message = message;
      return this;
    }

    public FeedbackBuilder action(@NonNull String actionText, @NonNull Runnable actionCallback) {
      this.actionText = actionText;
      this.actionCallback = actionCallback;
      return this;
    }

    public FeedbackBuilder duration(int duration) {
      this.duration = duration;
      return this;
    }

    public FeedbackBuilder asSuccess() {
      this.type = SnackbarType.SUCCESS;
      return this;
    }

    public FeedbackBuilder asError() {
      this.type = SnackbarType.ERROR;
      return this;
    }

    public FeedbackBuilder asWarning() {
      this.type = SnackbarType.WARNING;
      return this;
    }

    public FeedbackBuilder asInfo() {
      this.type = SnackbarType.INFO;
      return this;
    }

    public void show() {
      if (message == null) {
        throw new IllegalStateException("Message must be set before showing feedback");
      }

      Snackbar snackbar = Snackbar.make(containerView, message, duration);

      // Apply styling
      switch (type) {
        case SUCCESS:
          snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.success_green));
          break;
        case ERROR:
          snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.error_red));
          break;
        case WARNING:
          snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.warning_orange));
          break;
        case INFO:
        default:
          snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.primaryBlue));
          break;
      }

      snackbar.setTextColor(containerView.getContext().getColor(R.color.white));

      // Add action if specified
      if (actionText != null && actionCallback != null) {
        snackbar.setAction(actionText, v -> actionCallback.run());
        snackbar.setActionTextColor(containerView.getContext().getColor(R.color.white));
      }

      snackbar.show();
    }
  }
}
