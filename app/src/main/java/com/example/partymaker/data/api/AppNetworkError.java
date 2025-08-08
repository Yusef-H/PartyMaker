package com.example.partymaker.data.api;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.partymaker.ui.features.auxiliary.settings.ServerSettingsActivity;

/**
 * Class for handling application-specific network errors. Provides methods for displaying
 * appropriate error messages and taking recovery actions.
 */
public class AppNetworkError {
  /**
   * Shows an appropriate error message based on the error type
   *
   * @param context The context
   * @param errorType The error type
   * @param isServerError Whether the error is related to the server connection
   */
  public static void showErrorMessage(
      Context context, NetworkUtils.ErrorType errorType, boolean isServerError) {
    if (context == null) return;

    String message = NetworkUtils.getErrorMessage(errorType);

    // For server connection errors, offer to go to server settings
    if (isServerError
        && (errorType == NetworkUtils.ErrorType.NO_NETWORK
            || errorType == NetworkUtils.ErrorType.SERVER_ERROR
            || errorType == NetworkUtils.ErrorType.TIMEOUT)) {
      showServerErrorDialog(context, message);
    } else {
      Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Shows a dialog for server connection errors with option to go to server settings
   *
   * @param context The context
   * @param message The error message
   */
  private static void showServerErrorDialog(Context context, String message) {
    new AlertDialog.Builder(context)
        .setTitle("Server Connection Error")
        .setMessage(message + "\n\nWould you like to check your server settings?")
        .setPositiveButton(
            "Server Settings",
            (dialog, which) -> {
              // Navigate to server settings
              Intent intent = new Intent(context, ServerSettingsActivity.class);
              context.startActivity(intent);
            })
        .setNegativeButton("Cancel", null)
        .show();
  }

  /**
   * Handles a network error by showing an appropriate message and taking recovery actions
   *
   * @param context The context
   * @param error The error message
   * @param errorType The error type
   * @param isServerError Whether the error is related to the server connection
   */
  public static void handleNetworkError(
      Context context, String error, NetworkUtils.ErrorType errorType, boolean isServerError) {
    if (context == null) return;

    showErrorMessage(context, errorType, isServerError);

    // Log the error
    android.util.Log.e("AppNetworkError", "Network error: " + error + ", Type: " + errorType);
  }
}
