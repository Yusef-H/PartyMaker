package com.example.partymaker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** ViewModel for IntroActivity handling intro/onboarding state */
public class IntroViewModel extends BaseViewModel {

  private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);
  private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> shouldNavigateToLogin = new MutableLiveData<>(false);

  private final int totalPages;

  public IntroViewModel(@NonNull Application application) {
    super(application);
    this.totalPages = 3; // Number of intro slides
  }

  public LiveData<Integer> getCurrentPage() {
    return currentPage;
  }

  public LiveData<Boolean> getIsLastPage() {
    return isLastPage;
  }

  public LiveData<Boolean> getShouldNavigateToLogin() {
    return shouldNavigateToLogin;
  }

  public void setCurrentPage(int page) {
    currentPage.setValue(page);
    isLastPage.setValue(page == totalPages - 1);
  }

  public void onNextClicked() {
    Integer current = currentPage.getValue();
    if (current != null) {
      if (current < totalPages - 1) {
        currentPage.setValue(current + 1);
        isLastPage.setValue(current + 1 == totalPages - 1);
      } else {
        shouldNavigateToLogin.setValue(true);
      }
    }
  }

  public void onSkipClicked() {
    shouldNavigateToLogin.setValue(true);
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void resetNavigation() {
    shouldNavigateToLogin.setValue(false);
  }
}
