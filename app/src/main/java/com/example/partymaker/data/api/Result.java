package com.example.partymaker.data.api;

/**
 * A generic wrapper class that holds a result with data, error information, or loading state.
 * This follows the Result pattern commonly used in Android architecture for handling 
 * success, error, and loading states in a type-safe manner.
 *
 * @param <T> Type of the result data
 */
public class Result<T> {
  
  // State fields
  private T data;
  private String error;
  private NetworkUtils.ErrorType errorType;
  private boolean isLoading;

  // Private constructor for static factory methods
  private Result() {}

  /**
   * Creates a success result with data
   *
   * @param data The successful data result
   * @param <T> Type of the data
   * @return A success result containing the data
   */
  public static <T> Result<T> success(T data) {
    Result<T> result = new Result<>();
    result.data = data;
    result.error = null;
    result.errorType = null;
    result.isLoading = false;
    return result;
  }

  /**
   * Creates an error result with a message
   *
   * @param errorMessage The error message
   * @param <T> Type of the data (not used for error)
   * @return An error result with UNKNOWN error type
   */
  public static <T> Result<T> error(String errorMessage) {
    Result<T> result = new Result<>();
    result.data = null;
    result.error = errorMessage;
    result.errorType = NetworkUtils.ErrorType.UNKNOWN;
    result.isLoading = false;
    return result;
  }

  /**
   * Creates an error result with a message and error type
   *
   * @param errorMessage The error message
   * @param errorType The type of error
   * @param <T> Type of the data (not used for error)
   * @return An error result with specified error type
   */
  public static <T> Result<T> error(String errorMessage, NetworkUtils.ErrorType errorType) {
    Result<T> result = new Result<>();
    result.data = null;
    result.error = errorMessage;
    result.errorType = errorType;
    result.isLoading = false;
    return result;
  }

  /**
   * Creates a loading result
   *
   * @param <T> Type of the data (not used for loading)
   * @return A loading result with no data or errors
   */
  public static <T> Result<T> loading() {
    Result<T> result = new Result<>();
    result.data = null;
    result.error = null;
    result.errorType = null;
    result.isLoading = true;
    return result;
  }

  /**
   * Checks if the result is successful
   *
   * @return true if successful, false otherwise
   */
  public boolean isSuccess() {
    return data != null && error == null && !isLoading;
  }

  /**
   * Checks if the result is an error
   *
   * @return true if error, false otherwise
   */
  public boolean isError() {
    return error != null && !isLoading;
  }

  /**
   * Checks if the result is loading
   *
   * @return true if loading, false otherwise
   */
  public boolean isLoading() {
    return isLoading;
  }

  /**
   * Gets the data from the result
   *
   * @return The data, or null if not successful
   */
  public T getData() {
    return data;
  }

  /**
   * Gets the error message from the result
   *
   * @return The error message, or null if not an error
   */
  public String getError() {
    return error;
  }

  /**
   * Gets the error type from the result
   *
   * @return The error type, or null if not an error
   */
  public NetworkUtils.ErrorType getErrorType() {
    return errorType;
  }

  /**
   * Gets a user-friendly error message based on the error type
   *
   * @return A user-friendly error message
   */
  public String getUserFriendlyError() {
    if (!isError()) {
      return null;
    }

    if (errorType != null) {
      return NetworkUtils.getErrorMessage(errorType);
    }

    return error;
  }
}
