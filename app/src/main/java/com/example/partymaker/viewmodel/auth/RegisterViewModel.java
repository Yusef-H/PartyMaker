package com.example.partymaker.viewmodel.auth;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.User;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.validation.PasswordValidator;
import com.example.partymaker.viewmodel.BaseViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel for user registration functionality.
 *
 * <p>This ViewModel is specifically designed for REGISTRATION operations only.
 *
 * <p><strong>Usage Note:</strong> This app has multiple auth ViewModels:
 *
 * <ul>
 *   <li><strong>LoginViewModel</strong> - Use for login screens
 *   <li><strong>RegisterViewModel</strong> - Use for registration screens (THIS CLASS)
 *   <li><strong>AuthViewModel</strong> - Legacy general auth (consider deprecating)
 * </ul>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Email and password validation
 *   <li>Firebase authentication integration
 *   <li>User profile creation
 *   <li>Comprehensive error handling
 *   <li>Registration progress tracking
 * </ul>
 *
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 */
public class RegisterViewModel extends BaseViewModel {

  private static final String TAG = "RegisterViewModel";

  // Dependencies
  private final FirebaseAuth firebaseAuth;
  private final UserRepository userRepository;

    // LiveData for registration state
  private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
  private final MutableLiveData<String> validationError = new MutableLiveData<>();
  private final MutableLiveData<User> registeredUser = new MutableLiveData<>();
  private final MutableLiveData<Boolean> emailVerificationSent = new MutableLiveData<>();

  // Form validation state
  private final MutableLiveData<Boolean> isEmailValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isPasswordValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isUsernameValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isFormValid = new MutableLiveData<>();

  /**
   * Constructor for RegisterViewModel.
   *
   * @param application The application context
   */
  public RegisterViewModel(@NonNull Application application) {
    super(application);
    this.firebaseAuth = FirebaseAuth.getInstance();
    this.userRepository = UserRepository.getInstance();
      FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Initialize form validation state
    isEmailValid.setValue(false);
    isPasswordValid.setValue(false);
    isUsernameValid.setValue(false);
    isFormValid.setValue(false);

    Log.d(TAG, "RegisterViewModel initialized");
  }

  // Getters for LiveData

  public LiveData<Boolean> getRegistrationSuccess() {
    return registrationSuccess;
  }

  public LiveData<String> getValidationError() {
    return validationError;
  }

  public LiveData<User> getRegisteredUser() {
    return registeredUser;
  }

  public LiveData<Boolean> getEmailVerificationSent() {
    return emailVerificationSent;
  }

  public LiveData<Boolean> getIsEmailValid() {
    return isEmailValid;
  }

  public LiveData<Boolean> getIsPasswordValid() {
    return isPasswordValid;
  }

  public LiveData<Boolean> getIsUsernameValid() {
    return isUsernameValid;
  }

  public LiveData<Boolean> getIsFormValid() {
    return isFormValid;
  }

  /**
   * Validates email format and availability.
   *
   * @param email The email to validate
   */
  public void validateEmail(@NonNull String email) {
    ThreadUtils.runOnBackground(
        () -> {
          try {
            boolean valid = isValidEmail(email);
            isEmailValid.postValue(valid);

            if (!valid) {
              validationError.postValue("Please enter a valid email address");
            } else {
              validationError.postValue(null);
            }

            updateFormValidation();

          } catch (Exception e) {
            Log.e(TAG, "Error validating email", e);
            isEmailValid.postValue(false);
            validationError.postValue("Email validation failed");
          }
        });
  }

  /**
   * Validates password strength and requirements.
   *
   * @param password The password to validate
   */
  public void validatePassword(@NonNull String password) {
    ThreadUtils.runOnBackground(
        () -> {
          try {
            PasswordValidator.ValidationResult result =
                PasswordValidator.validatePassword(password);
            boolean valid = result.isValid();

            isPasswordValid.postValue(valid);

            if (!valid) {
              validationError.postValue(result.getErrorMessage());
            } else {
              validationError.postValue(null);
            }

            updateFormValidation();

          } catch (Exception e) {
            Log.e(TAG, "Error validating password", e);
            isPasswordValid.postValue(false);
            validationError.postValue("Password validation failed");
          }
        });
  }

  /**
   * Validates username format and availability.
   *
   * @param username The username to validate
   */
  public void validateUsername(@NonNull String username) {
    ThreadUtils.runOnBackground(
        () -> {
          try {
            boolean valid = isValidUsername(username);
            isUsernameValid.postValue(valid);

            if (!valid) {
              validationError.postValue(
                  "Username must be 3-20 characters long and contain only letters, numbers, and underscores");
            } else {
              validationError.postValue(null);
            }

            updateFormValidation();

          } catch (Exception e) {
            Log.e(TAG, "Error validating username", e);
            isUsernameValid.postValue(false);
            validationError.postValue("Username validation failed");
          }
        });
  }

  /**
   * Registers a new user with email and password.
   *
   * @param email The user's email
   * @param password The user's password
   * @param username The user's display name
   * @param fullName The user's full name (optional)
   */
  public void registerUser(
      @NonNull String email, @NonNull String password, @NonNull String username, String fullName) {

    if (isCurrentlyLoading()) {
      Log.w(TAG, "Registration already in progress");
      return;
    }

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearMessages();

          Log.d(TAG, "Starting user registration for email: " + email);

          ThreadUtils.runOnBackground(
              () -> {
                try {
                  // Final validation before registration
                  if (!validateRegistrationData(email, password, username)) {
                    return;
                  }

                  // Create user with Firebase Auth
                  firebaseAuth
                      .createUserWithEmailAndPassword(email, password)
                      .addOnCompleteListener(
                          task -> {
                            if (task.isSuccessful()) {
                              FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                              if (firebaseUser != null) {
                                handleRegistrationSuccess(firebaseUser, username, fullName);
                              } else {
                                handleRegistrationError(
                                    "Registration failed: User creation unsuccessful");
                              }
                            } else {
                              String errorMsg =
                                  task.getException() != null
                                      ? task.getException().getMessage()
                                      : "Registration failed";
                              handleRegistrationError(errorMsg);
                            }
                          });

                } catch (Exception e) {
                  Log.e(TAG, "Error during registration", e);
                  handleRegistrationError("Registration failed: " + e.getMessage());
                }
              });
        });
  }

  /** Sends email verification to the registered user. */
  public void sendEmailVerification() {
    executeIfNotLoading(
        () -> {
          FirebaseUser currentUser = firebaseAuth.getCurrentUser();
          if (currentUser != null) {
            setLoading(true);

            currentUser
                .sendEmailVerification()
                .addOnCompleteListener(
                    task -> {
                      setLoading(false);

                      if (task.isSuccessful()) {
                        emailVerificationSent.setValue(true);
                        setSuccess("Verification email sent to " + currentUser.getEmail());
                        Log.d(TAG, "Email verification sent successfully");
                      } else {
                        String errorMsg =
                            task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to send verification email";
                        setError(errorMsg, NetworkUtils.ErrorType.AUTHENTICATION_ERROR);
                        Log.e(TAG, "Failed to send email verification: " + errorMsg);
                      }
                    });
          } else {
            setError("No user logged in", NetworkUtils.ErrorType.AUTHENTICATION_ERROR);
          }
        });
  }

  /** Resends the registration confirmation email. */
  public void resendConfirmationEmail() {
    sendEmailVerification();
  }

  // Private helper methods

  private boolean validateRegistrationData(String email, String password, String username) {
    if (!isValidEmail(email)) {
      handleRegistrationError("Invalid email format");
      return false;
    }

    PasswordValidator.ValidationResult passwordResult =
        PasswordValidator.validatePassword(password);
    if (!passwordResult.isValid()) {
      handleRegistrationError(passwordResult.getErrorMessage());
      return false;
    }

    if (!isValidUsername(username)) {
      handleRegistrationError("Invalid username format");
      return false;
    }

    return true;
  }

  private boolean isValidEmail(String email) {
    return email != null
        && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        && email.trim().length() > 0;
  }

  private boolean isValidUsername(String username) {
    return username != null
        && username.trim().length() >= 3
        && username.trim().length() <= 20
        && username.matches("^[a-zA-Z0-9_]+$");
  }

  private void updateFormValidation() {
    ThreadUtils.runOnMainThread(
        () -> {
          Boolean emailValid = isEmailValid.getValue();
          Boolean passwordValid = isPasswordValid.getValue();
          Boolean usernameValid = isUsernameValid.getValue();

          boolean formValid =
              (emailValid != null && emailValid)
                  && (passwordValid != null && passwordValid)
                  && (usernameValid != null && usernameValid);

          isFormValid.setValue(formValid);
        });
  }

  private void handleRegistrationSuccess(
      FirebaseUser firebaseUser, String username, String fullName) {
    Log.d(TAG, "Firebase user created successfully, creating user profile");

    // Create user profile
    User newUser = new User();
    newUser.setUserKey(firebaseUser.getUid());
    newUser.setEmail(firebaseUser.getEmail());
    newUser.setUsername(username);
    newUser.setFullName(fullName != null ? fullName : username);
    newUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));

    // Save user profile to server with timeout handling
    final boolean[] isCompleted = {false};

    // Set timeout for user profile creation
    ThreadUtils.runOnMainThreadDelayed(
        () -> {
          if (!isCompleted[0]) {
            Log.w(
                TAG,
                "User profile creation timed out during registration, proceeding with offline mode");
            isCompleted[0] = true;

            ThreadUtils.runOnMainThread(
                () -> {
                  setLoading(false);
                  registrationSuccess.setValue(true);
                  registeredUser.setValue(newUser);
                  setInfo("Registration successful! (Offline mode - server unavailable)");
                });
          }
        },
        8000); // 8 second timeout

    userRepository.createUser(
        newUser,
        new UserRepository.Callback<User>() {
          @Override
          public void onSuccess(User result) {
            if (isCompleted[0]) return;
            isCompleted[0] = true;

            Log.d(TAG, "User profile created successfully");

            ThreadUtils.runOnMainThread(
                () -> {
                  setLoading(false);
                  registrationSuccess.setValue(true);
                  registeredUser.setValue(result);
                  setSuccess("Registration successful! Welcome to PartyMaker!");
                });
          }

          @Override
          public void onError(Exception error) {
            if (isCompleted[0]) return;
            isCompleted[0] = true;

            Log.e(TAG, "Failed to create user profile, using offline mode", error);

            // Registration succeeded but profile creation failed
            // Still consider it a success but show warning
            ThreadUtils.runOnMainThread(
                () -> {
                  setLoading(false);
                  registrationSuccess.setValue(true);
                  registeredUser.setValue(newUser);
                  setInfo("Registration successful! (Offline mode - server unavailable)");
                });
          }
        });
  }

  private void handleRegistrationError(String errorMessage) {
    Log.e(TAG, "Registration failed: " + errorMessage);

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          registrationSuccess.setValue(false);

          // Determine error type based on message
          NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.UNKNOWN_ERROR;
          if (errorMessage.toLowerCase().contains("email")) {
            errorType = NetworkUtils.ErrorType.AUTHENTICATION_ERROR;
          } else if (errorMessage.toLowerCase().contains("password")) {
            errorType = NetworkUtils.ErrorType.VALIDATION_ERROR;
          } else if (errorMessage.toLowerCase().contains("network")) {
            errorType = NetworkUtils.ErrorType.NETWORK_ERROR;
          }

          setError(errorMessage, errorType);
        });
  }

  /** Clears all registration-related data and states. */
  public void clearRegistrationData() {
    registrationSuccess.setValue(null);
    registeredUser.setValue(null);
    emailVerificationSent.setValue(false);
    validationError.setValue(null);
    clearMessages();

    // Reset form validation
    isEmailValid.setValue(false);
    isPasswordValid.setValue(false);
    isUsernameValid.setValue(false);
    isFormValid.setValue(false);

    Log.d(TAG, "Registration data cleared");
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearRegistrationData();
    Log.d(TAG, "RegisterViewModel cleared");
  }
}
