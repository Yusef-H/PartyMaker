package com.example.partymaker.viewmodel.auth;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.User;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.BaseViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * ViewModel for user login functionality.
 * 
 * <p>Handles user authentication including email/password login, Google Sign-In,
 * remember me functionality, and proper session management.
 * 
 * <p>Features:
 * <ul>
 *   <li>Email/password authentication</li>
 *   <li>Google Sign-In integration</li>
 *   <li>Remember me functionality</li>
 *   <li>User session management</li>
 *   <li>Login state persistence</li>
 *   <li>Comprehensive error handling</li>
 * </ul>
 * 
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class LoginViewModel extends BaseViewModel {
    
    private static final String TAG = "LoginViewModel";
    private static final String PREFS_NAME = "PartyMakerPrefs";
    private static final String KEY_REMEMBER_EMAIL = "remember_email";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_USER_EXPLICITLY_LOGGED_IN = "user_explicitly_logged_in";
    
    // Dependencies
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final SharedPreferences preferences;
    
    // LiveData for login state
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<User> loggedInUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGoogleSignInAvailable = new MutableLiveData<>();
    private final MutableLiveData<String> savedEmail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> rememberMe = new MutableLiveData<>();
    
    // Form validation state
    private final MutableLiveData<Boolean> isEmailValid = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPasswordValid = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFormValid = new MutableLiveData<>();
    
    /**
     * Constructor for LoginViewModel.
     * 
     * @param application The application context
     */
    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = UserRepository.getInstance();
        this.preferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize form validation state
        isEmailValid.setValue(false);
        isPasswordValid.setValue(false);
        isFormValid.setValue(false);
        
        // Load saved login preferences
        loadSavedPreferences();
        
        // Check Google Sign-In availability
        checkGoogleSignInAvailability();
        
        Log.d(TAG, "LoginViewModel initialized");
    }
    
    // Getters for LiveData
    
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }
    
    public LiveData<User> getLoggedInUser() {
        return loggedInUser;
    }
    
    public LiveData<Boolean> getIsGoogleSignInAvailable() {
        return isGoogleSignInAvailable;
    }
    
    public LiveData<String> getSavedEmail() {
        return savedEmail;
    }
    
    public LiveData<Boolean> getRememberMe() {
        return rememberMe;
    }
    
    public LiveData<Boolean> getIsEmailValid() {
        return isEmailValid;
    }
    
    public LiveData<Boolean> getIsPasswordValid() {
        return isPasswordValid;
    }
    
    public LiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }
    
    /**
     * Validates email format.
     * 
     * @param email The email to validate
     */
    public void validateEmail(@NonNull String email) {
        boolean valid = isValidEmail(email);
        isEmailValid.setValue(valid);
        updateFormValidation();
    }
    
    /**
     * Validates password (non-empty for login).
     * 
     * @param password The password to validate
     */
    public void validatePassword(@NonNull String password) {
        boolean valid = password != null && password.trim().length() > 0;
        isPasswordValid.setValue(valid);
        updateFormValidation();
    }
    
    /**
     * Sets the remember me preference.
     * 
     * @param remember Whether to remember the user's email
     */
    public void setRememberMe(boolean remember) {
        rememberMe.setValue(remember);
        preferences.edit().putBoolean(KEY_REMEMBER_EMAIL, remember).apply();
        
        if (!remember) {
            // Clear saved email if remember me is disabled
            preferences.edit().remove(KEY_SAVED_EMAIL).apply();
            savedEmail.setValue(null);
        }
    }
    
    /**
     * Performs email/password login.
     * 
     * @param email The user's email
     * @param password The user's password
     */
    public void loginWithEmail(@NonNull String email, @NonNull String password) {
        if (isCurrentlyLoading()) {
            Log.w(TAG, "Login already in progress");
            return;
        }
        
        executeIfNotLoading(() -> {
            setLoading(true);
            clearMessages();
            
            Log.d(TAG, "Starting email/password login for: " + email);
            
            // Validate input
            if (!isValidEmail(email)) {
                handleLoginError("Please enter a valid email address");
                return;
            }
            
            if (password.trim().isEmpty()) {
                handleLoginError("Please enter your password");
                return;
            }
            
            // Perform Firebase authentication
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            handleLoginSuccess(firebaseUser, email);
                        } else {
                            handleLoginError("Login failed: Authentication unsuccessful");
                        }
                    } else {
                        String errorMsg = task.getException() != null ? 
                            task.getException().getMessage() : "Login failed";
                        handleLoginError(errorMsg);
                    }
                });
        });
    }
    
    /**
     * Performs Google Sign-In authentication.
     * 
     * @param idToken The Google ID token from the sign-in result
     */
    public void loginWithGoogle(@NonNull String idToken) {
        if (isCurrentlyLoading()) {
            Log.w(TAG, "Google login already in progress");
            return;
        }
        
        executeIfNotLoading(() -> {
            setLoading(true);
            clearMessages();
            
            Log.d(TAG, "Starting Google Sign-In authentication");
            
            // Create Firebase credential from Google token
            com.google.firebase.auth.AuthCredential credential = 
                GoogleAuthProvider.getCredential(idToken, null);
            
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            handleGoogleLoginSuccess(firebaseUser);
                        } else {
                            handleLoginError("Google Sign-In failed: Authentication unsuccessful");
                        }
                    } else {
                        String errorMsg = task.getException() != null ? 
                            task.getException().getMessage() : "Google Sign-In failed";
                        handleLoginError(errorMsg);
                    }
                });
        });
    }
    
    /**
     * Checks if user is already logged in.
     * 
     * @return true if user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        boolean isLoggedIn = currentUser != null;
        
        if (isLoggedIn) {
            Log.d(TAG, "User already logged in: " + currentUser.getEmail());
        }
        
        return isLoggedIn;
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return FirebaseUser if logged in, null otherwise
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        executeIfNotLoading(() -> {
            setLoading(true);
            
            try {
                // Clear user session
                AuthenticationManager.logout(getApplication());
                
                // Clear explicit login flag
                preferences.edit().putBoolean(KEY_USER_EXPLICITLY_LOGGED_IN, false).apply();
                
                // Clear ViewModel state
                loginSuccess.setValue(false);
                loggedInUser.setValue(null);
                
                setLoading(false);
                setSuccess("Logged out successfully");
                
                Log.d(TAG, "User logged out successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error during logout", e);
                setLoading(false);
                setError("Logout failed: " + e.getMessage());
            }
        });
    }
    
    // Private helper methods
    
    private void loadSavedPreferences() {
        boolean remember = preferences.getBoolean(KEY_REMEMBER_EMAIL, false);
        String email = preferences.getString(KEY_SAVED_EMAIL, null);
        
        rememberMe.setValue(remember);
        savedEmail.setValue(remember ? email : null);
        
        Log.d(TAG, "Loaded saved preferences - Remember: " + remember + ", Email: " + 
              (email != null ? "saved" : "none"));
    }
    
    private void checkGoogleSignInAvailability() {
        // Check if Google Play Services are available
        try {
            // This is a simplified check - in production you'd use GoogleApiAvailability
            isGoogleSignInAvailable.setValue(true);
            Log.d(TAG, "Google Sign-In is available");
        } catch (Exception e) {
            isGoogleSignInAvailable.setValue(false);
            Log.w(TAG, "Google Sign-In not available", e);
        }
    }
    
    private boolean isValidEmail(String email) {
        return email != null && 
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
               email.trim().length() > 0;
    }
    
    private void updateFormValidation() {
        ThreadUtils.runOnMainThread(() -> {
            Boolean emailValid = isEmailValid.getValue();
            Boolean passwordValid = isPasswordValid.getValue();
            
            boolean formValid = (emailValid != null && emailValid) &&
                              (passwordValid != null && passwordValid);
            
            isFormValid.setValue(formValid);
        });
    }
    
    private void handleLoginSuccess(FirebaseUser firebaseUser, String email) {
        Log.d(TAG, "Email/password login successful, loading user profile");
        
        // Save email if remember me is enabled
        Boolean remember = rememberMe.getValue();
        if (remember != null && remember) {
            preferences.edit().putString(KEY_SAVED_EMAIL, email).apply();
            savedEmail.setValue(email);
        }
        
        // Set explicit login flag
        preferences.edit().putBoolean(KEY_USER_EXPLICITLY_LOGGED_IN, true).apply();
        
        // Load user profile
        loadUserProfile(firebaseUser);
    }
    
    private void handleGoogleLoginSuccess(FirebaseUser firebaseUser) {
        Log.d(TAG, "Google Sign-In successful, checking user profile");
        
        // Set explicit login flag
        preferences.edit().putBoolean(KEY_USER_EXPLICITLY_LOGGED_IN, true).apply();
        
        // For Google Sign-In, we might need to create a user profile if it doesn't exist
        checkAndCreateUserProfile(firebaseUser);
    }
    
    private void loadUserProfile(FirebaseUser firebaseUser) {
        String userKey = firebaseUser.getUid();
        
        userRepository.getUser(userKey, new UserRepository.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "User profile loaded successfully");
                
                ThreadUtils.runOnMainThread(() -> {
                    setLoading(false);
                    loginSuccess.setValue(true);
                    loggedInUser.setValue(user);
                    setSuccess("Welcome back, " + user.getUsername() + "!");
                });
            }
            
            @Override
            public void onError(Exception error) {
                Log.w(TAG, "User profile not found, creating new profile", error);
                
                // Create user profile if it doesn't exist
                createUserProfileFromFirebase(firebaseUser);
            }
        });
    }
    
    private void checkAndCreateUserProfile(FirebaseUser firebaseUser) {
        String userKey = firebaseUser.getUid();
        
        userRepository.getUser(userKey, new UserRepository.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Existing user profile found");
                
                ThreadUtils.runOnMainThread(() -> {
                    setLoading(false);
                    loginSuccess.setValue(true);
                    loggedInUser.setValue(user);
                    setSuccess("Welcome back, " + user.getUsername() + "!");
                });
            }
            
            @Override
            public void onError(Exception error) {
                Log.d(TAG, "Creating new user profile for Google Sign-In user");
                createUserProfileFromFirebase(firebaseUser);
            }
        });
    }
    
    private void createUserProfileFromFirebase(FirebaseUser firebaseUser) {
        User newUser = new User();
        newUser.setUserKey(firebaseUser.getUid());
        newUser.setEmail(firebaseUser.getEmail());
        newUser.setUsername(firebaseUser.getDisplayName() != null ? 
                           firebaseUser.getDisplayName() : "User");
        newUser.setFullName(firebaseUser.getDisplayName());
        newUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        
        userRepository.createUser(newUser, new UserRepository.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "User profile created successfully");
                
                ThreadUtils.runOnMainThread(() -> {
                    setLoading(false);
                    loginSuccess.setValue(true);
                    loggedInUser.setValue(user);
                    setSuccess("Welcome to PartyMaker, " + user.getUsername() + "!");
                });
            }
            
            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Failed to create user profile", error);
                
                ThreadUtils.runOnMainThread(() -> {
                    setLoading(false);
                    loginSuccess.setValue(true);
                    loggedInUser.setValue(newUser);
                    setInfo("Login successful, but profile setup needs completion");
                });
            }
        });
    }
    
    private void handleLoginError(String errorMessage) {
        Log.e(TAG, "Login failed: " + errorMessage);
        
        ThreadUtils.runOnMainThread(() -> {
            setLoading(false);
            loginSuccess.setValue(false);
            
            // Determine error type based on message
            NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.AUTHENTICATION_ERROR;
            if (errorMessage.toLowerCase().contains("network")) {
                errorType = NetworkUtils.ErrorType.NETWORK_ERROR;
            } else if (errorMessage.toLowerCase().contains("email") || 
                      errorMessage.toLowerCase().contains("password")) {
                errorType = NetworkUtils.ErrorType.AUTHENTICATION_ERROR;
            }
            
            setError(errorMessage, errorType);
        });
    }
    
    /**
     * Clears all login-related data and states.
     */
    public void clearLoginData() {
        loginSuccess.setValue(null);
        loggedInUser.setValue(null);
        clearMessages();
        
        // Reset form validation but keep saved preferences
        isEmailValid.setValue(false);
        isPasswordValid.setValue(false);
        isFormValid.setValue(false);
        
        Log.d(TAG, "Login data cleared");
    }
    
    /**
     * Handles errors from external sources.
     * 
     * @param error The error to handle
     */
    public void handleError(Exception error) {
        String errorMessage = error.getMessage();
        if (errorMessage == null) {
            errorMessage = "An unexpected error occurred";
        }
        
        NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.UNKNOWN_ERROR;
        setError(errorMessage, errorType);
        
        Log.e(TAG, "External error handled", error);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        clearLoginData();
        Log.d(TAG, "LoginViewModel cleared");
    }
}