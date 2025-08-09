package com.example.partymaker.viewmodel.auth;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.BaseViewModel;
import com.google.firebase.auth.FirebaseAuth;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ViewModel for password reset functionality.
 *
 * <p>Handles password reset operations including email validation, sending reset emails, and
 * providing user feedback.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Email validation for reset requests
 *   <li>Password reset email sending
 *   <li>Reset request tracking
 *   <li>Comprehensive error handling
 *   <li>Rate limiting awareness
 * </ul>
 *
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class ResetPasswordViewModel extends BaseViewModel {

  private static final String TAG = "ResetPasswordViewModel";
  private static final long RESET_COOLDOWN_MS = 60000; // 1 minute cooldown between requests

  // Dependencies
  private final FirebaseAuth firebaseAuth;

  // LiveData for reset state
  private final MutableLiveData<Boolean> resetEmailSent = new MutableLiveData<>();
  private final MutableLiveData<String> targetEmail = new MutableLiveData<>();
  private final MutableLiveData<Boolean> canSendReset = new MutableLiveData<>();
  private final MutableLiveData<Long> nextResetAllowedTime = new MutableLiveData<>();

  // Form validation state
  private final MutableLiveData<Boolean> isEmailValid = new MutableLiveData<>();

  // Internal state
  private long lastResetRequestTime = 0;
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> countdownTask;

  /**
   * Constructor for ResetPasswordViewModel.
   *
   * @param application The application context
   */
  public ResetPasswordViewModel(@NonNull Application application) {
    super(application);
    this.firebaseAuth = FirebaseAuth.getInstance();

    // Initialize state
    isEmailValid.setValue(false);
    resetEmailSent.setValue(false);
    canSendReset.setValue(true);

    Log.d(TAG, "ResetPasswordViewModel initialized");
  }

  // Getters for LiveData

  public LiveData<Boolean> getResetEmailSent() {
    return resetEmailSent;
  }

  public LiveData<String> getTargetEmail() {
    return targetEmail;
  }

  public LiveData<Boolean> getCanSendReset() {
    return canSendReset;
  }

  public LiveData<Long> getNextResetAllowedTime() {
    return nextResetAllowedTime;
  }

  public LiveData<Boolean> getIsEmailValid() {
    return isEmailValid;
  }

  public LiveData<Boolean> getResetSuccess() {
    return resetEmailSent;
  }

  /**
   * Validates email format for password reset.
   *
   * @param email The email to validate
   */
  public void validateEmail(@NonNull String email) {
    boolean valid = isValidEmail(email);
    isEmailValid.setValue(valid);

    if (valid) {
      targetEmail.setValue(email.trim().toLowerCase());
    } else {
      targetEmail.setValue(null);
    }
  }

  /**
   * Sends a password reset email to the specified address.
   *
   * @param email The email address to send the reset link to
   */
  public void resetPassword(@NonNull String email) {
    sendPasswordResetEmail(email);
  }

  /**
   * Sends a password reset email to the specified address.
   *
   * @param email The email address to send the reset link to
   */
  public void sendPasswordResetEmail(@NonNull String email) {
    if (isCurrentlyLoading()) {
      Log.w(TAG, "Password reset already in progress");
      return;
    }

    executeIfNotLoading(
        () -> {
          // Check if we can send a reset email (rate limiting)
          if (!canSendResetEmail()) {
            long timeRemaining =
                (lastResetRequestTime + RESET_COOLDOWN_MS) - System.currentTimeMillis();
            setError(
                "Please wait "
                    + (timeRemaining / 1000)
                    + " seconds before requesting another reset",
                NetworkUtils.ErrorType.RATE_LIMIT_ERROR);
            return;
          }

          setLoading(true);
          clearMessages();

          Log.d(TAG, "Sending password reset email to: " + email);

          // Validate email format
          if (!isValidEmail(email)) {
            handleResetError("Please enter a valid email address");
            return;
          }

          // Send password reset email via Firebase
          firebaseAuth
              .sendPasswordResetEmail(email.trim().toLowerCase())
              .addOnCompleteListener(
                  task -> {
                    if (task.isSuccessful()) {
                      handleResetSuccess(email);
                    } else {
                      String errorMsg =
                          task.getException() != null
                              ? task.getException().getMessage()
                              : "Failed to send reset email";
                      handleResetError(errorMsg);
                    }
                  });
        });
  }

  /** Resends the password reset email to the same address. */
  public void resendPasswordResetEmail() {
    String email = targetEmail.getValue();
    if (email != null && !email.isEmpty()) {
      sendPasswordResetEmail(email);
    } else {
      setError("No email address specified for reset", NetworkUtils.ErrorType.VALIDATION_ERROR);
    }
  }

  /**
   * Checks if the user can send a password reset email (rate limiting).
   *
   * @return true if reset email can be sent, false if still in cooldown period
   */
  public boolean canSendResetEmail() {
    long currentTime = System.currentTimeMillis();
    boolean canSend = (currentTime - lastResetRequestTime) > RESET_COOLDOWN_MS;

    canSendReset.setValue(canSend);

    if (!canSend) {
      long nextAllowedTime = lastResetRequestTime + RESET_COOLDOWN_MS;
      nextResetAllowedTime.setValue(nextAllowedTime);
    } else {
      nextResetAllowedTime.setValue(null);
    }

    return canSend;
  }

  /**
   * Gets the remaining time in seconds before another reset email can be sent.
   *
   * @return remaining seconds, or 0 if reset can be sent immediately
   */
  public long getRemainingCooldownSeconds() {
    if (canSendResetEmail()) {
      return 0;
    }

    long currentTime = System.currentTimeMillis();
    long timeRemaining = (lastResetRequestTime + RESET_COOLDOWN_MS) - currentTime;

    return Math.max(0, timeRemaining / 1000);
  }

  /** Starts a countdown for when the next reset email can be sent. */
  public void startCooldownCountdown() {
    if (!canSendResetEmail()) {
      // Cancel any existing countdown
      if (countdownTask != null && !countdownTask.isDone()) {
        countdownTask.cancel(false);
      }

      // Create scheduler if needed
      if (scheduler == null || scheduler.isShutdown()) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
      }

      // Schedule periodic updates every second
      countdownTask =
          scheduler.scheduleAtFixedRate(
              () -> {
                ThreadUtils.runOnMainThread(
                    () -> {
                      boolean canSend = canSendResetEmail();
                      canSendReset.setValue(canSend);

                      if (canSend) {
                        setInfo("You can now request another password reset");
                        // Cancel the task once we can send again
                        if (countdownTask != null) {
                          countdownTask.cancel(false);
                        }
                      }
                    });
              },
              0, // Initial delay
              1, // Period
              TimeUnit.SECONDS);
    }
  }

  // Private helper methods

  private boolean isValidEmail(String email) {
    return email != null
        && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        && !email.trim().isEmpty();
  }

  private void handleResetSuccess(String email) {
    Log.d(TAG, "Password reset email sent successfully to: " + email);

    // Update rate limiting
    lastResetRequestTime = System.currentTimeMillis();

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          resetEmailSent.setValue(true);
          targetEmail.setValue(email.trim().toLowerCase());
          setSuccess("Password reset email sent to " + email);

          // Update rate limiting UI
          canSendReset.setValue(false);
          nextResetAllowedTime.setValue(lastResetRequestTime + RESET_COOLDOWN_MS);

          // Start countdown for next allowed reset
          startCooldownCountdown();
        });
  }

  private void handleResetError(String errorMessage) {
    Log.e(TAG, "Password reset failed: " + errorMessage);

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          resetEmailSent.setValue(false);

          // Determine error type based on message
          NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.UNKNOWN_ERROR;
          if (errorMessage.toLowerCase().contains("email")
              || errorMessage.toLowerCase().contains("user")) {
            errorType = NetworkUtils.ErrorType.AUTHENTICATION_ERROR;
          } else if (errorMessage.toLowerCase().contains("network")) {
            errorType = NetworkUtils.ErrorType.NETWORK_ERROR;
          } else if (errorMessage.toLowerCase().contains("too many")) {
            errorType = NetworkUtils.ErrorType.RATE_LIMIT_ERROR;
            // Update rate limiting even on error
            lastResetRequestTime = System.currentTimeMillis();
            canSendReset.setValue(false);
            nextResetAllowedTime.setValue(lastResetRequestTime + RESET_COOLDOWN_MS);
            startCooldownCountdown();
          }

          setError(errorMessage, errorType);
        });
  }

  /** Clears all reset-related data and states. */
  public void clearResetData() {
    resetEmailSent.setValue(false);
    targetEmail.setValue(null);
    isEmailValid.setValue(false);
    clearMessages();

    // Don't reset rate limiting data - it should persist

    Log.d(TAG, "Reset data cleared");
  }

  /** Resets the rate limiting (for testing or administrative purposes). */
  public void resetRateLimit() {
    lastResetRequestTime = 0;
    canSendReset.setValue(true);
    nextResetAllowedTime.setValue(null);

    Log.d(TAG, "Rate limiting reset");
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearResetData();
    // Cleanup scheduler
    if (countdownTask != null) {
      countdownTask.cancel(false);
    }
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
    Log.d(TAG, "ResetPasswordViewModel cleared");
  }
}
