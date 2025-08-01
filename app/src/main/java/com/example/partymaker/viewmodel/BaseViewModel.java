package com.example.partymaker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;

/** Base ViewModel class that provides common functionality for all ViewModels */
public abstract class BaseViewModel extends AndroidViewModel {

  protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  protected final MutableLiveData<String> successMessage = new MutableLiveData<>();
  protected final MutableLiveData<NetworkUtils.ErrorType> errorType = new MutableLiveData<>();
  protected final MutableLiveData<String> infoMessage = new MutableLiveData<>();

  public BaseViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  public LiveData<String> getErrorMessage() {
    return errorMessage;
  }

  public LiveData<String> getSuccessMessage() {
    return successMessage;
  }

  public LiveData<NetworkUtils.ErrorType> getErrorType() {
    return errorType;
  }

  public LiveData<String> getInfoMessage() {
    return infoMessage;
  }

  public void setLoading(boolean loading) {
    isLoading.setValue(loading);
  }

  public void setError(String error) {
    errorMessage.setValue(error);
    errorType.setValue(null);
  }

  public void setError(NetworkUtils.ErrorType type) {
    errorType.setValue(type);
    errorMessage.setValue(NetworkUtils.getErrorMessage(type));
  }

  public void setError(String error, NetworkUtils.ErrorType type) {
    errorMessage.setValue(error);
    errorType.setValue(type);
  }

  public void setSuccess(String message) {
    successMessage.setValue(message);
  }

  public void setInfo(String message) {
    infoMessage.setValue(message);
  }

  public void clearError() {
    errorMessage.setValue(null);
    errorType.setValue(null);
  }

  public void clearMessages() {
    errorMessage.setValue(null);
    successMessage.setValue(null);
    infoMessage.setValue(null);
    errorType.setValue(null);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearMessages();
  }
}
