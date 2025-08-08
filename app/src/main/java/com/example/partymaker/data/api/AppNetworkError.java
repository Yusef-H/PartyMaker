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
  
  // UI Constants
  private static final String TAG = "AppNetworkError";
  private static final int TOAST_DURATION_LONG = Toast.LENGTH_LONG;
  
  // Dialog Constants
  private static final String DIALOG_TITLE = "Server Connection Error";
  private static final String DIALOG_MESSAGE_SUFFIX = "\n\nWould you like to check your server settings?";
  private static final String BUTTON_TEXT_SETTINGS = "Server Settings";
  private static final String BUTTON_TEXT_CANCEL = "Cancel";
  
  // Prevent instantiation
  private AppNetworkError() {
    // Utility class
  }
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
      Toast.makeText(context, message, TOAST_DURATION_LONG).show();
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
        .setTitle(DIALOG_TITLE)
        .setMessage(message + DIALOG_MESSAGE_SUFFIX)
        .setPositiveButton(
            BUTTON_TEXT_SETTINGS,
            (dialog, which) -> navigateToServerSettings(context))
        .setNegativeButton(BUTTON_TEXT_CANCEL, null)
        .show();
  }
  
  /**
   * Navigates to server settings activity
   * 
   * @param context The context
   */
  private static void navigateToServerSettings(Context context) {
    try {
      Intent intent = new Intent(context, ServerSettingsActivity.class);
      context.startActivity(intent);
    } catch (Exception e) {
      android.util.Log.e(TAG, "Error navigating to server settings", e);
    }
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
    android.util.Log.e(TAG, "Network error: " + error + ", Type: " + errorType);
  }
}
