package com.example.partymaker.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utils.auth.AuthHelper;
import com.example.partymaker.utils.system.ThreadUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AuthViewModel";
    private static final int RC_SIGN_IN = 9001;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>(false);
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRegistrationMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isPasswordResetSent = new MutableLiveData<>(false);

    private final FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        auth = FirebaseAuth.getInstance();
        
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            currentUser.setValue(user);
            isAuthenticated.setValue(true);
        }
        
        setupGoogleSignIn();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getApplication().getString(com.example.partymaker.R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(getApplication(), gso);
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

    public LiveData<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsRegistrationMode() {
        return isRegistrationMode;
    }

    public LiveData<Boolean> getIsPasswordResetSent() {
        return isPasswordResetSent;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void setRegistrationMode(boolean isRegistration) {
        isRegistrationMode.setValue(isRegistration);
    }

    public void loginWithEmail(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Please enter a valid email address");
            return;
        }

        Log.d(TAG, "Attempting to login with email: " + email);
        isLoading.setValue(true);
        clearMessages();

        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    ThreadUtils.runOnMainThread(() -> {
                        isLoading.setValue(false);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email login successful");
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                currentUser.setValue(user);
                                isAuthenticated.setValue(true);
                                successMessage.setValue("Login successful");
                            }
                        } else {
                            Log.e(TAG, "Email login failed", task.getException());
                            String error = task.getException() != null ? 
                                    task.getException().getMessage() : "Login failed";
                            errorMessage.setValue(error);
                        }
                    });
                });
    }

    public void registerWithEmail(String email, String password, String confirmPassword, String username) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            errorMessage.setValue("Please confirm your password");
            return;
        }

        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("Username is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Please enter a valid email address");
            return;
        }

        Log.d(TAG, "Attempting to register with email: " + email);
        isLoading.setValue(true);
        clearMessages();

        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email registration successful");
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            createUserProfile(user, username.trim());
                        }
                    } else {
                        isLoading.setValue(false);
                        Log.e(TAG, "Email registration failed", task.getException());
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Registration failed";
                        errorMessage.setValue(error);
                    }
                });
    }

    public void signInWithGoogle(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google sign in successful, authenticating with Firebase");
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed", e);
            isLoading.setValue(false);
            errorMessage.setValue("Google sign in failed: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication with Google successful");
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            currentUser.setValue(user);
                            isAuthenticated.setValue(true);
                            
                            String username = user.getDisplayName() != null ? 
                                    user.getDisplayName() : "Google User";
                            checkAndCreateUserProfile(user, username);
                        }
                    } else {
                        Log.e(TAG, "Firebase authentication with Google failed", task.getException());
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Google authentication failed";
                        errorMessage.setValue(error);
                    }
                });
    }

    private void checkAndCreateUserProfile(FirebaseUser firebaseUser, String username) {
        String userKey = Objects.requireNonNull(firebaseUser.getEmail()).replace('.', ' ');
        
        // First check if user already exists
        DBRef.refUsers.child(userKey).get()
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // User already exists, just sign in
                            Log.d(TAG, "Existing user signed in with Google");
                            currentUser.setValue(firebaseUser);
                            isAuthenticated.setValue(true);
                            successMessage.setValue("Login successful");
                        } else {
                            // New user, create profile
                            Log.d(TAG, "New user, creating profile");
                            createNewUserProfile(firebaseUser, username);
                        }
                    } else {
                        Log.e(TAG, "Failed to check if user exists", task.getException());
                        errorMessage.setValue("Failed to verify user profile");
                    }
                });
    }

    private void createNewUserProfile(FirebaseUser firebaseUser, String username) {
        String userKey = Objects.requireNonNull(firebaseUser.getEmail()).replace('.', ' ');
        
        User user = new User();
        user.setUserKey(userKey);
        user.setEmail(firebaseUser.getEmail());
        user.setUsername(username);
        
        if (firebaseUser.getPhotoUrl() != null) {
            user.setProfileImageUrl(firebaseUser.getPhotoUrl().toString());
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        if (user.getProfileImageUrl() != null) {
            userMap.put("profileImageUrl", user.getProfileImageUrl());
        }

        DBRef.refUsers.child(userKey).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "New user profile created successfully");
                        currentUser.setValue(firebaseUser);
                        isAuthenticated.setValue(true);
                        successMessage.setValue("Account created successfully");
                    } else {
                        Log.e(TAG, "Failed to create user profile", task.getException());
                        errorMessage.setValue("Failed to create user profile");
                    }
                });
    }
    
    private void createUserProfile(FirebaseUser firebaseUser, String username) {
        // This method is used for email registration - always creates new profile
        createNewUserProfile(firebaseUser, username);
    }

    public void resetPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Please enter a valid email address");
            return;
        }

        Log.d(TAG, "Sending password reset email to: " + email);
        isLoading.setValue(true);
        clearMessages();

        auth.sendPasswordResetEmail(email.trim())
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent successfully");
                        isPasswordResetSent.setValue(true);
                        successMessage.setValue("Password reset email sent");
                    } else {
                        Log.e(TAG, "Failed to send password reset email", task.getException());
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Failed to send reset email";
                        errorMessage.setValue(error);
                    }
                });
    }

    public void signOut(Context context) {
        Log.d(TAG, "Signing out user");
        
        auth.signOut();
        
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
        
        AuthHelper.clearAuthData(context);
        
        currentUser.setValue(null);
        isAuthenticated.setValue(false);
        successMessage.setValue("Signed out successfully");
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }

    public void clearPasswordResetStatus() {
        isPasswordResetSent.setValue(false);
    }

    public boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean isValidUsername(String username) {
        return username != null && username.trim().length() >= 2;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AuthViewModel cleared");
    }
}