package com.example.partymaker.utils.infrastructure.network;

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
public final class NetworkErrorHandler {

  // HTTP Status Code Constants
  private static final String HTTP_UNAUTHORIZED = "401";
  private static final String HTTP_FORBIDDEN = "403";
  private static final String HTTP_NOT_FOUND = "404";
  private static final String HTTP_SERVER_ERROR = "500";

  // Error Message Keywords
  private static final String KEYWORD_UNAUTHORIZED = "unauthorized";
  private static final String KEYWORD_FORBIDDEN = "forbidden";
  private static final String KEYWORD_NOT_FOUND = "not found";
  private static final String KEYWORD_SERVER = "server";

  // Retry Delay Constants
  private static final long BASE_RETRY_DELAY_MS = 1000L;
  private static final long NETWORK_RETRY_MULTIPLIER = 3L;
  private static final int EXPONENTIAL_BASE = 2;

  private NetworkErrorHandler() {
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
      if (isClientErrorMessage(message)) {
        return NetworkUtils.ErrorType.CLIENT_ERROR;
      } else if (isServerErrorMessage(message)) {
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

  /** Checks if the error message indicates a client error. */
  private static boolean isClientErrorMessage(String message) {
    return message.contains(KEYWORD_UNAUTHORIZED)
        || message.contains(HTTP_UNAUTHORIZED)
        || message.contains(KEYWORD_FORBIDDEN)
        || message.contains(HTTP_FORBIDDEN)
        || message.contains(KEYWORD_NOT_FOUND)
        || message.contains(HTTP_NOT_FOUND);
  }

  /** Checks if the error message indicates a server error. */
  private static boolean isServerErrorMessage(String message) {
    return message.contains(KEYWORD_SERVER) || message.contains(HTTP_SERVER_ERROR);
  }

  /**
   * Gets the appropriate retry delay for a given error type and attempt count.
   *
   * @param errorType The type of error
   * @param attemptCount The current attempt count (0-based)
   * @return Delay in milliseconds before next retry
   */
  public static long getRetryDelay(NetworkUtils.ErrorType errorType, int attemptCount) {
    switch (errorType) {
      case TIMEOUT:
        return calculateExponentialBackoff(attemptCount);
      case NO_NETWORK:
        return calculateNetworkRetryDelay(attemptCount);
      case SERVER_ERROR:
        return calculateLinearBackoff(attemptCount);
      case CLIENT_ERROR:
      case UNKNOWN:
      default:
        return BASE_RETRY_DELAY_MS;
    }
  }

  /** Calculates exponential backoff delay for timeout errors. */
  private static long calculateExponentialBackoff(int attemptCount) {
    return BASE_RETRY_DELAY_MS * (long) Math.pow(EXPONENTIAL_BASE, attemptCount);
  }

  /** Calculates retry delay for network connectivity issues. */
  private static long calculateNetworkRetryDelay(int attemptCount) {
    return BASE_RETRY_DELAY_MS * NETWORK_RETRY_MULTIPLIER * (attemptCount + 1);
  }

  /** Calculates linear backoff delay for server errors. */
  private static long calculateLinearBackoff(int attemptCount) {
    return BASE_RETRY_DELAY_MS * (attemptCount + 1);
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
