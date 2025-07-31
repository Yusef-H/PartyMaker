package com.example.partymaker.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utils.auth.SecureAuthHelper;
import com.example.partymaker.utils.security.PasswordValidator;
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

public class AuthViewModel extends BaseViewModel {
  private static final String TAG = "AuthViewModel";
  private static final int RC_SIGN_IN = 9001;

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
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                getApplication().getString(com.example.partymaker.R.string.default_web_client_id))
            .requestEmail()
            .build();

    googleSignInClient = GoogleSignIn.getClient(getApplication(), gso);
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
      setError("Email is required");
      return;
    }

    if (password == null || password.trim().isEmpty()) {
      setError("Password is required");
      return;
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      setError("Please enter a valid email address");
      return;
    }

    Log.d(TAG, "Attempting to login with email: " + email);
    setLoading(true);
    clearMessages();

    auth.signInWithEmailAndPassword(email.trim(), password)
        .addOnCompleteListener(
            task -> {
              ThreadUtils.runOnMainThread(
                  () -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                      Log.d(TAG, "Email login successful");
                      FirebaseUser user = auth.getCurrentUser();
                      if (user != null) {
                        currentUser.setValue(user);
                        isAuthenticated.setValue(true);
                        setSuccess("Login successful");
                      }
                    } else {
                      Log.e(TAG, "Email login failed", task.getException());
                      String error =
                          task.getException() != null
                              ? task.getException().getMessage()
                              : "Login failed";
                      setError(error);
                    }
                  });
            });
  }

  public void registerWithEmail(
      String email, String password, String confirmPassword, String username) {
    if (email == null || email.trim().isEmpty()) {
      setError("Email is required");
      return;
    }

    if (password == null || password.trim().isEmpty()) {
      setError("Password is required");
      return;
    }

    if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
      setError("Please confirm your password");
      return;
    }

    if (username == null || username.trim().isEmpty()) {
      setError("Username is required");
      return;
    }

    if (!password.equals(confirmPassword)) {
      setError("Passwords do not match");
      return;
    }

    // Enhanced password validation
    PasswordValidator.ValidationResult passwordValidation = PasswordValidator.validate(password);
    if (!passwordValidation.isValid) {
      setError(String.join(". ", passwordValidation.errors));
      return;
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      setError("Please enter a valid email address");
      return;
    }

    Log.d(TAG, "Attempting to register with email: " + email);
    setLoading(true);
    clearMessages();

    auth.createUserWithEmailAndPassword(email.trim(), password)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "Email registration successful");
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                  createUserProfile(user, username.trim());
                }
              } else {
                setLoading(false);
                Log.e(TAG, "Email registration failed", task.getException());
                String error =
                    task.getException() != null
                        ? task.getException().getMessage()
                        : "Registration failed";
                setError(error);
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
      setLoading(false);
      setError("Google sign in failed: " + e.getMessage());
    }
  }

  private void firebaseAuthWithGoogle(String idToken) {
    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

    auth.signInWithCredential(credential)
        .addOnCompleteListener(
            task -> {
              setLoading(false);
              if (task.isSuccessful()) {
                Log.d(TAG, "Firebase authentication with Google successful");
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                  currentUser.setValue(user);
                  isAuthenticated.setValue(true);

                  String username =
                      user.getDisplayName() != null ? user.getDisplayName() : "Google User";
                  checkAndCreateUserProfile(user, username);
                }
              } else {
                Log.e(TAG, "Firebase authentication with Google failed", task.getException());
                String error =
                    task.getException() != null
                        ? task.getException().getMessage()
                        : "Google authentication failed";
                setError(error);
              }
            });
  }

  private void checkAndCreateUserProfile(FirebaseUser firebaseUser, String username) {
    String userKey = Objects.requireNonNull(firebaseUser.getEmail()).replace('.', ' ');

    // First check if user already exists
    DBRef.refUsers
        .child(userKey)
        .get()
        .addOnCompleteListener(
            task -> {
              setLoading(false);
              if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                  // User already exists, just sign in
                  Log.d(TAG, "Existing user signed in with Google");
                  currentUser.setValue(firebaseUser);
                  isAuthenticated.setValue(true);
                  setSuccess("Login successful");
                } else {
                  // New user, create profile
                  Log.d(TAG, "New user, creating profile");
                  createNewUserProfile(firebaseUser, username);
                }
              } else {
                Log.e(TAG, "Failed to check if user exists", task.getException());
                setError("Failed to verify user profile");
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

    DBRef.refUsers
        .child(userKey)
        .setValue(userMap)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "New user profile created successfully");
                currentUser.setValue(firebaseUser);
                isAuthenticated.setValue(true);
                setSuccess("Account created successfully");
              } else {
                Log.e(TAG, "Failed to create user profile", task.getException());
                setError("Failed to create user profile");
              }
            });
  }

  private void createUserProfile(FirebaseUser firebaseUser, String username) {
    // This method is used for email registration - always creates new profile
    createNewUserProfile(firebaseUser, username);
  }

  public void resetPassword(String email) {
    if (email == null || email.trim().isEmpty()) {
      setError("Email is required");
      return;
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      setError("Please enter a valid email address");
      return;
    }

    Log.d(TAG, "Sending password reset email to: " + email);
    setLoading(true);
    clearMessages();

    auth.sendPasswordResetEmail(email.trim())
        .addOnCompleteListener(
            task -> {
              setLoading(false);
              if (task.isSuccessful()) {
                Log.d(TAG, "Password reset email sent successfully");
                isPasswordResetSent.setValue(true);
                setSuccess("Password reset email sent");
              } else {
                Log.e(TAG, "Failed to send password reset email", task.getException());
                String error =
                    task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to send reset email";
                setError(error);
              }
            });
  }

  public void signOut(Context context) {
    Log.d(TAG, "Signing out user");

    auth.signOut();

    if (googleSignInClient != null) {
      googleSignInClient.signOut();
    }

    SecureAuthHelper.clearAuthData(context);

    currentUser.setValue(null);
    isAuthenticated.setValue(false);
    setSuccess("Signed out successfully");
  }

  public void clearPasswordResetStatus() {
    isPasswordResetSent.setValue(false);
  }

  public boolean isValidEmail(String email) {
    return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
  }

  public boolean isValidPassword(String password) {
    if (password == null) return false;
    PasswordValidator.ValidationResult result = PasswordValidator.validate(password);
    return result.isValid;
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
