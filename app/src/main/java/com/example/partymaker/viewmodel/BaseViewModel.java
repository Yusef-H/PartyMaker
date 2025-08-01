package com.example.partymaker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;

/**
 * Base ViewModel class that provides common functionality for all ViewModels in the application.
 *
 * <p>This class handles common UI states such as loading, error messages, success messages, and
 * provides utility methods for managing these states consistently across all ViewModels.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Loading state management
 *   <li>Error handling with typed error messages
 *   <li>Success and info message handling
 *   <li>Automatic cleanup in onCleared()
 * </ul>
 *
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 */
public abstract class BaseViewModel extends AndroidViewModel {

  /** Tag for logging - subclasses should override this */
  protected static final String TAG = "BaseViewModel";

  /** Default timeout for operations in milliseconds */
  protected static final long DEFAULT_TIMEOUT_MS = 10000L;

  // UI State LiveData
  protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  protected final MutableLiveData<String> successMessage = new MutableLiveData<>();
  protected final MutableLiveData<NetworkUtils.ErrorType> errorType = new MutableLiveData<>();
  protected final MutableLiveData<String> infoMessage = new MutableLiveData<>();

  /**
   * Constructor for BaseViewModel.
   *
   * @param application The application context for this ViewModel
   */
  public BaseViewModel(@NonNull Application application) {
    super(application);
  }

  /**
   * Returns the loading state as LiveData.
   *
   * @return LiveData<Boolean> that indicates whether an operation is in progress
   */
  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  /**
   * Returns error messages as LiveData.
   *
   * @return LiveData<String> containing error messages, null if no error
   */
  public LiveData<String> getErrorMessage() {
    return errorMessage;
  }

  /**
   * Returns success messages as LiveData.
   *
   * @return LiveData<String> containing success messages, null if no success message
   */
  public LiveData<String> getSuccessMessage() {
    return successMessage;
  }

  /**
   * Returns the error type as LiveData for more specific error handling.
   *
   * @return LiveData<NetworkUtils.ErrorType> containing the type of error that occurred
   */
  public LiveData<NetworkUtils.ErrorType> getErrorType() {
    return errorType;
  }

  /**
   * Returns informational messages as LiveData.
   *
   * @return LiveData<String> containing info messages, null if no info message
   */
  public LiveData<String> getInfoMessage() {
    return infoMessage;
  }

  /**
   * Sets the loading state.
   *
   * @param loading true if an operation is in progress, false otherwise
   */
  public void setLoading(boolean loading) {
    isLoading.setValue(loading);
  }

  /**
   * Sets an error message and clears the error type.
   *
   * @param error The error message to display, or null to clear
   */
  public void setError(@Nullable String error) {
    errorMessage.setValue(error);
    errorType.setValue(null);
  }

  /**
   * Sets an error type and automatically generates the corresponding error message.
   *
   * @param type The type of error that occurred
   */
  public void setError(@NonNull NetworkUtils.ErrorType type) {
    errorType.setValue(type);
    errorMessage.setValue(NetworkUtils.getErrorMessage(type));
  }

  /**
   * Sets both a custom error message and error type.
   *
   * @param error The custom error message to display
   * @param type The type of error that occurred
   */
  public void setError(@Nullable String error, @NonNull NetworkUtils.ErrorType type) {
    errorMessage.setValue(error);
    errorType.setValue(type);
  }

  /**
   * Sets a success message.
   *
   * @param message The success message to display, or null to clear
   */
  public void setSuccess(@Nullable String message) {
    successMessage.setValue(message);
  }

  /**
   * Sets an informational message.
   *
   * @param message The info message to display, or null to clear
   */
  public void setInfo(@Nullable String message) {
    infoMessage.setValue(message);
  }

  /** Clears all error-related messages and states. */
  public void clearError() {
    errorMessage.setValue(null);
    errorType.setValue(null);
  }

  /** Clears all messages (error, success, info) and error types. */
  public void clearMessages() {
    errorMessage.setValue(null);
    successMessage.setValue(null);
    infoMessage.setValue(null);
    errorType.setValue(null);
  }

  /**
   * Utility method to check if currently loading.
   *
   * @return true if loading, false otherwise
   */
  protected boolean isCurrentlyLoading() {
    Boolean loading = isLoading.getValue();
    return loading != null && loading;
  }

  /**
   * Utility method to safely execute operations only when not loading.
   *
   * @param operation The operation to execute
   */
  protected void executeIfNotLoading(@NonNull Runnable operation) {
    if (!isCurrentlyLoading()) {
      operation.run();
    }
  }

  /**
   * Called when the ViewModel is no longer used and will be destroyed. Cleans up all messages and
   * states.
   */
  @Override
  protected void onCleared() {
    super.onCleared();
    clearMessages();
    setLoading(false);
  }
}
