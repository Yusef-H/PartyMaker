package com.example.partymaker.utils.ui.feedback;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.example.partymaker.R;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.api.Result;
import com.example.partymaker.utils.ui.components.UiStateManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * Utility class for providing user feedback through various UI components. Includes dialogs,
 * snackbars, toasts, and confirmation messages.
 */
public class UserFeedbackManager {

  /** Shows a confirmation dialog with custom title and message */
  public static void showConfirmationDialog(
      @NonNull Context context,
      @NonNull String title,
      @NonNull String message,
      @NonNull String positiveButtonText,
      @NonNull String negativeButtonText,
      @NonNull Runnable onConfirm,
      @NonNull Runnable onCancel) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(
            positiveButtonText,
            (dialog, which) -> {
              dialog.dismiss();
              onConfirm.run();
            })
        .setNegativeButton(
            negativeButtonText,
            (dialog, which) -> {
              dialog.dismiss();
              onCancel.run();
            })
        .setCancelable(false)
        .show();
  }

  /** Shows a simple confirmation dialog with default buttons */
  public static void showConfirmationDialog(
      @NonNull Context context,
      @NonNull String title,
      @NonNull String message,
      @NonNull Runnable onConfirm) {
    showConfirmationDialog(context, title, message, "Confirm", "Cancel", onConfirm, () -> {});
  }

  /** Shows a destructive action confirmation dialog */
  public static void showDestructiveConfirmationDialog(
      @NonNull Context context,
      @NonNull String title,
      @NonNull String message,
      @NonNull String destructiveActionText,
      @NonNull Runnable onConfirm) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(
            destructiveActionText,
            (dialog, which) -> {
              dialog.dismiss();
              onConfirm.run();
            })
        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
        .setCancelable(true)
        .show();
  }

  /** Shows an information dialog with single OK button */
  public static void showInfoDialog(
      @NonNull Context context, @NonNull String title, @NonNull String message) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
        .setCancelable(true)
        .show();
  }

  /** Shows an error dialog with details */
  public static void showErrorDialog(
      @NonNull Context context,
      @NonNull String title,
      @NonNull String message,
      @NonNull Runnable onRetry) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(
            "Retry",
            (dialog, which) -> {
              dialog.dismiss();
              onRetry.run();
            })
        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
        .setCancelable(true)
        .show();
  }

  /** Shows an error dialog without retry option */
  public static void showErrorDialog(@NonNull Context context, @NonNull String message) {
    showInfoDialog(context, "Error", message);
  }

  /** Shows a network error dialog based on error type */
  public static void showNetworkErrorDialog(
      @NonNull Context context,
      @NonNull NetworkUtils.ErrorType errorType,
      @NonNull Runnable onRetry) {
    String title = "Connection Error";
    String message = NetworkUtils.getErrorMessage(errorType);

    if (errorType == NetworkUtils.ErrorType.NO_NETWORK) {
      title = "No Internet Connection";
    } else if (errorType == NetworkUtils.ErrorType.SERVER_ERROR) {
      title = "Server Error";
    } else if (errorType == NetworkUtils.ErrorType.TIMEOUT) {
      title = "Connection Timeout";
    }

    showErrorDialog(context, title, message, onRetry);
  }

  /** Shows feedback based on Result object */
  public static <T> void handleResult(
      @NonNull Context context,
      @NonNull View containerView,
      @NonNull Result<T> result,
      @NonNull ResultHandler<T> handler) {
    if (result.isLoading()) {
      handler.onLoading();
    } else if (result.isSuccess()) {
      handler.onSuccess(result.getData());
    } else if (result.isError()) {
      if (result.getErrorType() != null) {
        UiStateManager.showError(containerView, result.getErrorType(), handler::onRetryRequested);
      } else {
        UiStateManager.showError(containerView, result.getError(), handler::onRetryRequested);
      }
      handler.onError(result.getError(), result.getErrorType());
    }
  }

  /** Shows a success message with optional action */
  public static void showSuccessWithAction(
      @NonNull View containerView,
      @NonNull String message,
      @NonNull String actionText,
      @NonNull Runnable action) {
    Snackbar snackbar = Snackbar.make(containerView, message, Snackbar.LENGTH_LONG);
    snackbar.setAction(actionText, v -> action.run());
    snackbar.setBackgroundTint(containerView.getContext().getColor(R.color.success_green));
    snackbar.setTextColor(containerView.getContext().getColor(R.color.white));
    snackbar.setActionTextColor(containerView.getContext().getColor(R.color.white));
    snackbar.show();
  }

  /** Shows a progress toast with percentage */
  public static void showProgressToast(
      @NonNull Context context, @NonNull String operation, int percentage) {
    String message = operation + " " + percentage + "%";
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  /** Shows a custom choice dialog with multiple options */
  public static void showChoiceDialog(
      @NonNull Context context,
      @NonNull String title,
      @NonNull String[] choices,
      @NonNull ChoiceCallback callback) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setItems(
            choices,
            (dialog, which) -> {
              dialog.dismiss();
              callback.onChoice(which, choices[which]);
            })
        .setCancelable(true)
        .show();
  }

  /** Shows a single choice dialog with radio buttons */
  public static void showSingleChoiceDialog(
      @NonNull Context context,
      @NonNull String title,
      @NonNull String[] choices,
      int selectedIndex,
      @NonNull SingleChoiceCallback callback) {
    final int[] selectedItem = {selectedIndex};

    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setSingleChoiceItems(choices, selectedIndex, (dialog, which) -> selectedItem[0] = which)
        .setPositiveButton(
            "OK",
            (dialog, which) -> {
              dialog.dismiss();
              callback.onChoice(selectedItem[0], choices[selectedItem[0]]);
            })
        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
        .setCancelable(true)
        .show();
  }

  /** Shows a feedback form dialog */
  public static void showFeedbackDialog(
      @NonNull Context context, @NonNull FeedbackCallback callback) {
    // This would typically show a custom dialog with rating and comments
    // For now, showing a simple input dialog
    showInfoDialog(
        context,
        "Feedback",
        "Thank you for using PartyMaker! " + "Your feedback helps us improve the app.");
  }

  /** Shows an offline notification */
  public static void showOfflineNotification(@NonNull View containerView) {
    UiStateManager.showWarning(containerView, "You're offline. Some features may be limited.");
  }

  /** Shows an online notification */
  public static void showOnlineNotification(@NonNull View containerView) {
    UiStateManager.showInfo(containerView, "Connection restored");
  }

  /** Interface for handling Result objects */
  public interface ResultHandler<T> {
    void onLoading();

    void onSuccess(T data);

    void onError(String errorMessage, NetworkUtils.ErrorType errorType);

    void onRetryRequested();
  }

  /** Interface for choice dialogs */
  public interface ChoiceCallback {
    void onChoice(int index, String choice);
  }

  /** Interface for single choice dialogs */
  public interface SingleChoiceCallback {
    void onChoice(int index, String choice);
  }

  /** Interface for feedback dialogs */
  public interface FeedbackCallback {
    void onFeedbackSubmitted(String feedback, int rating);

    void onFeedbackCancelled();
  }

  /** Shows a loading dialog that can be dismissed */
  public static AlertDialog showLoadingDialog(@NonNull Context context, @NonNull String message) {
    AlertDialog dialog =
        new MaterialAlertDialogBuilder(context)
            .setTitle("Please wait")
            .setMessage(message)
            .setCancelable(false)
            .create();
    dialog.show();
    return dialog;
  }

  /** Dismisses a dialog safely */
  public static void dismissDialog(@NonNull AlertDialog dialog) {
    if (dialog.isShowing()) {
      dialog.dismiss();
    }
  }
}
