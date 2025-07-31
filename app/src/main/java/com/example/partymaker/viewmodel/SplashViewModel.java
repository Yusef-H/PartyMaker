package com.example.partymaker.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.partymaker.utils.data.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel for SplashActivity handling navigation logic
 */
public class SplashViewModel extends BaseViewModel {
    
    private final MutableLiveData<NavigationDestination> navigationDestination = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isInitializationComplete = new MutableLiveData<>(false);
    
    private final FirebaseAuth auth;
    private final SharedPreferences sharedPreferences;
    
    public enum NavigationDestination {
        LOGIN,
        MAIN,
        INTRO
    }
    
    public SplashViewModel(@NonNull Application application) {
        super(application);
        auth = FirebaseAuth.getInstance();
        sharedPreferences = application.getSharedPreferences(Constants.PREFS_NAME, 0);
    }
    
    public LiveData<NavigationDestination> getNavigationDestination() {
        return navigationDestination;
    }
    
    public LiveData<Boolean> getIsInitializationComplete() {
        return isInitializationComplete;
    }
    
    public void initialize() {
        setLoading(true);
        
        // Simulate initialization delay
        new android.os.Handler().postDelayed(() -> {
            checkAuthenticationStatus();
            setLoading(false);
            isInitializationComplete.setValue(true);
        }, 2000);
    }
    
    private void checkAuthenticationStatus() {
        FirebaseUser currentUser = auth.getCurrentUser();
        boolean isChecked = sharedPreferences.getBoolean(Constants.IS_CHECKED, false);
        
        if (currentUser != null && isChecked) {
            // User is signed in and remember me was checked
            navigationDestination.setValue(NavigationDestination.MAIN);
        } else {
            // User needs to login
            navigationDestination.setValue(NavigationDestination.LOGIN);
        }
    }
    
    public void setFirstTimeUser(boolean isFirstTime) {
        if (isFirstTime) {
            navigationDestination.setValue(NavigationDestination.INTRO);
        }
    }
}