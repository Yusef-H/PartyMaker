package com.example.partymaker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** Base ViewModel class that provides common functionality for all ViewModels */
public abstract class BaseViewModel extends AndroidViewModel {

  protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  protected final MutableLiveData<String> successMessage = new MutableLiveData<>();

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

  public void setLoading(boolean loading) {
    isLoading.setValue(loading);
  }

  public void setError(String error) {
    errorMessage.setValue(error);
  }

  public void setSuccess(String message) {
    successMessage.setValue(message);
  }

  public void clearMessages() {
    errorMessage.setValue(null);
    successMessage.setValue(null);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearMessages();
  }
}
