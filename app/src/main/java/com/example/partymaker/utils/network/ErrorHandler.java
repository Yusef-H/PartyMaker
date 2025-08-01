package com.example.partymaker.utils.network;

import android.content.Context;
import com.example.partymaker.data.api.NetworkUtils;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;

/**
 * Centralized error handling utility for network operations and API calls. Provides consistent
 * error categorization and user-friendly error messages.
 */
public final class ErrorHandler {

  private ErrorHandler() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Converts exceptions to appropriate ErrorType for consistent error handling.
   *
   * @param exception The exception to categorize
   * @return The appropriate ErrorType
   */
  public static NetworkUtils.ErrorType categorizeException(Exception exception) {
    if (exception instanceof UnknownHostException) {
      return NetworkUtils.ErrorType.NO_NETWORK;
    } else if (exception instanceof SocketTimeoutException) {
      return NetworkUtils.ErrorType.TIMEOUT;
    } else if (exception instanceof ConnectException) {
      return NetworkUtils.ErrorType.SERVER_ERROR;
    } else if (exception instanceof SSLException) {
      return NetworkUtils.ErrorType.SERVER_ERROR; // Treat SSL errors as server errors
    } else if (exception.getMessage() != null) {
      String message = exception.getMessage().toLowerCase();
      if (message.contains("unauthorized")
          || message.contains("401")
          || message.contains("forbidden")
          || message.contains("403")
          || message.contains("not found")
          || message.contains("404")) {
        return NetworkUtils.ErrorType.CLIENT_ERROR;
      } else if (message.contains("server") || message.contains("500")) {
        return NetworkUtils.ErrorType.SERVER_ERROR;
      }
    }
    return NetworkUtils.ErrorType.UNKNOWN;
  }

  /**
   * Gets a user-friendly error message based on the error type and context.
   *
   * @param errorType The type of error
   * @param context Application context for string resources (can be null)
   * @return User-friendly error message
   */
  public static String getUserFriendlyMessage(NetworkUtils.ErrorType errorType, Context context) {
    // For now, fall back to NetworkUtils implementation
    // In a full implementation, you would use context.getString() with resource IDs
    return NetworkUtils.getErrorMessage(errorType);
  }

  /**
   * Determines if an error is recoverable (retry-able).
   *
   * @param errorType The type of error
   * @return true if the operation can be retried, false otherwise
   */
  public static boolean isRecoverable(NetworkUtils.ErrorType errorType) {
    switch (errorType) {
      case TIMEOUT:
      case NO_NETWORK:
      case SERVER_ERROR:
        return true;
      case CLIENT_ERROR:
      case UNKNOWN:
      default:
        return false;
    }
  }

  /**
   * Gets the appropriate retry delay for a given error type and attempt count.
   *
   * @param errorType The type of error
   * @param attemptCount The current attempt count (0-based)
   * @return Delay in milliseconds before next retry
   */
  public static long getRetryDelay(NetworkUtils.ErrorType errorType, int attemptCount) {
    long baseDelay = 1000L; // 1 second

    switch (errorType) {
      case TIMEOUT:
        // Exponential backoff for timeouts
        return baseDelay * (long) Math.pow(2, attemptCount);
      case NO_NETWORK:
        // Longer delay for network issues
        return baseDelay * 3L * (attemptCount + 1);
      case SERVER_ERROR:
        // Linear backoff for server issues
        return baseDelay * (attemptCount + 1);
      case CLIENT_ERROR:
      case UNKNOWN:
      default:
        return baseDelay;
    }
  }

  /**
   * Logs error information for debugging purposes.
   *
   * @param tag Log tag
   * @param operation The operation that failed
   * @param errorType The type of error
   * @param originalException The original exception (can be null)
   */
  public static void logError(
      String tag, String operation, NetworkUtils.ErrorType errorType, Exception originalException) {
    String message =
        String.format("Operation '%s' failed with error type: %s", operation, errorType);

    if (originalException != null) {
      android.util.Log.e(tag, message, originalException);
    } else {
      android.util.Log.e(tag, message);
    }
  }
}
