package com.example.partymaker.ui.features.auth;

import static com.example.partymaker.utils.core.IntentExtrasManager.hideViews;
import static com.example.partymaker.utils.core.IntentExtrasManager.showViews;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import com.example.partymaker.ui.base.BaseActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.R;
import com.example.partymaker.viewmodel.auth.ResetPasswordViewModel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enterprise-level ResetPasswordActivity implementation with proper validation, error handling,
 * theme management, and resource cleanup.
 *
 * <p>Responsibilities: - Handle password reset functionality via email - Provide real-time email
 * validation with user feedback - Manage light/dark theme switching with visual feedback - Display
 * contextual help and instructions - Ensure proper error handling and user experience
 *
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 */
public class ResetPasswordActivity extends BaseActivity {

  private static final class Config {
    static final String LOG_TAG = "ResetPasswordActivity";
    static final String LOADING_TEXT = "Sending...";
    static final String DEFAULT_TEXT = "Reset Password";
    static final String SUCCESS_MESSAGE = "Password reset email sent! Check your inbox.";
    static final String EMAIL_REQUIRED_ERROR = "Please enter your email address";
    static final String INVALID_EMAIL_ERROR = "Please enter a valid email address";

    // Color filter matrix for image inversion
    static final float[] NEGATIVE_FILTER = {
      -1.0f, 0, 0, 0, 255, 0, -1.0f, 0, 0, 255, 0, 0, -1.0f, 0, 255, 0, 0, 0, 1.0f, 0
    };
  }

  // UI Components - Interactive Elements
  private Button lightThemeButton;
  private Button darkThemeButton;
  private Button resetPasswordButton;
  private Button helpButton;
  private Button hideInstructionsButton;
  private EditText emailInputField;

  // UI Components - Display Elements
  private RelativeLayout resetLayout;
  private TextView forgotPasswordTextView;
  private TextView helpTextView;
  private TextView instructionsTextView;
  private TextView hideInstructionsTextView;
  private ImageView cakeImageView;

  // Dependencies and State Management
  private ResetPasswordViewModel resetPasswordViewModel;
  private final AtomicBoolean isDestroyed = new AtomicBoolean(false);

  // Managers
  private ThemeManager themeManager;
  private EmailValidator emailValidator;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      setContentView(R.layout.activity_auth_reset);

      initializeDependencies();
      configureActionBar();
      bindViewReferences();
      setupViewModelObservers();
      setupEventHandlers();
      setupInputValidation();

      Log.d(Config.LOG_TAG, "ResetPasswordActivity initialized successfully");

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Critical error during reset password initialization", e);
      handleInitializationError(e);
    }
  }

  private void initializeDependencies() {
    try {
      resetPasswordViewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);
      themeManager = new ThemeManager();
      emailValidator = new EmailValidator();

      Log.d(Config.LOG_TAG, "Dependencies initialized successfully");

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Failed to initialize dependencies", e);
      throw new RuntimeException("Critical dependency initialization failure", e);
    }
  }

  private void configureActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
  }

  private void bindViewReferences() {
    // Interactive elements
    darkThemeButton = findViewById(R.id.btnBlack);
    lightThemeButton = findViewById(R.id.btnWhite);
    helpButton = findViewById(R.id.btnHelp);
    resetPasswordButton = findViewById(R.id.btnReset);
    hideInstructionsButton = findViewById(R.id.btnHide);
    emailInputField = findViewById(R.id.etInputEmail);

    // Display elements
    forgotPasswordTextView = findViewById(R.id.tvForgotPass);
    helpTextView = findViewById(R.id.tvHelp);
    instructionsTextView = findViewById(R.id.tvInstructions);
    hideInstructionsTextView = findViewById(R.id.tvHide);
    resetLayout = findViewById(R.id.rltReset);
    cakeImageView = findViewById(R.id.imgWhiteCake);

    validateCriticalViews();
  }

  private void validateCriticalViews() {
    if (emailInputField == null || resetPasswordButton == null || resetLayout == null) {
      throw new IllegalStateException("Critical UI components not found in layout");
    }
  }

  private void setupViewModelObservers() {
    resetPasswordViewModel.getIsLoading().observe(this, this::handleLoadingState);
    resetPasswordViewModel.getErrorMessage().observe(this, this::handleErrorMessage);
    resetPasswordViewModel.getSuccessMessage().observe(this, this::handleSuccessMessage);
    resetPasswordViewModel.getResetSuccess().observe(this, this::handleResetSuccess);
    resetPasswordViewModel.getIsEmailValid().observe(this, this::handleEmailValidation);
  }

  private void handleLoadingState(@Nullable Boolean isLoading) {
    boolean loading = Boolean.TRUE.equals(isLoading);

    resetPasswordButton.setEnabled(!loading);
    resetPasswordButton.setText(loading ? Config.LOADING_TEXT : Config.DEFAULT_TEXT);
  }

  private void handleErrorMessage(@Nullable String errorMessage) {
    if (errorMessage != null && !errorMessage.isEmpty()) {
      Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
  }

  private void handleSuccessMessage(@Nullable String successMessage) {
    if (successMessage != null && !successMessage.isEmpty()) {
      Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
    }
  }

  private void handleResetSuccess(@Nullable Boolean resetSent) {
    if (Boolean.TRUE.equals(resetSent)) {
      Toast.makeText(this, Config.SUCCESS_MESSAGE, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void handleEmailValidation(@Nullable Boolean isValid) {
    String currentEmail = emailInputField.getText().toString();

    if (Boolean.FALSE.equals(isValid) && !currentEmail.isEmpty()) {
      emailInputField.setError(Config.INVALID_EMAIL_ERROR);
    } else {
      emailInputField.setError(null);
    }
  }

  private void setupEventHandlers() {
    resetPasswordButton.setOnClickListener(this::handleResetPasswordClick);
    lightThemeButton.setOnClickListener(this::handleLightThemeClick);
    darkThemeButton.setOnClickListener(this::handleDarkThemeClick);
    helpButton.setOnClickListener(this::handleHelpClick);
    hideInstructionsButton.setOnClickListener(this::handleHideInstructionsClick);
  }

  private void setupInputValidation() {
    emailInputField.addTextChangedListener(new EmailValidationTextWatcher());
  }

  private void handleResetPasswordClick(@NonNull View view) {
    try {
      String email = emailInputField.getText().toString().trim();

      if (emailValidator.validateEmail(email, emailInputField)) {
        resetPasswordViewModel.resetPassword(email);
      }

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Error during password reset attempt", e);
      Toast.makeText(this, "Password reset failed. Please try again.", Toast.LENGTH_SHORT).show();
    }
  }

  private void handleLightThemeClick(@NonNull View view) {
    themeManager.switchToLightTheme();
  }

  private void handleDarkThemeClick(@NonNull View view) {
    themeManager.switchToDarkTheme();
  }

  private void handleHelpClick(@NonNull View view) {
    showViews(instructionsTextView, hideInstructionsTextView, hideInstructionsButton);
    hideViews(helpTextView, helpButton);
  }

  private void handleHideInstructionsClick(@NonNull View view) {
    hideViews(instructionsTextView, hideInstructionsTextView, hideInstructionsButton);
    showViews(helpTextView, helpButton);
  }

  private void handleInitializationError(@NonNull Exception error) {
    Log.e(Config.LOG_TAG, "Critical initialization error - showing fallback UI", error);
    Toast.makeText(this, "App initialization failed. Please restart the app.", Toast.LENGTH_LONG)
        .show();
  }

  @Override
  protected void onDestroy() {
    isDestroyed.set(true);

    try {
      cleanupResources();
    } catch (Exception e) {
      Log.w(Config.LOG_TAG, "Error during resource cleanup", e);
    }

    super.onDestroy();
  }

  private void cleanupResources() {
    themeManager = null;
    emailValidator = null;
    resetPasswordViewModel = null;
  }

  @Override
  protected void clearActivityReferences() {
    // Clear UI components
    lightThemeButton = null;
    darkThemeButton = null;
    resetPasswordButton = null;
    helpButton = null;
    hideInstructionsButton = null;
    emailInputField = null;
    resetLayout = null;
    forgotPasswordTextView = null;
    helpTextView = null;
    instructionsTextView = null;
    hideInstructionsTextView = null;
    cakeImageView = null;

    // Clear dependencies and managers
    resetPasswordViewModel = null;
    themeManager = null;
    emailValidator = null;
  }

  // Inner Classes and Text Watcher
  private class EmailValidationTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      emailInputField.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {
      if (resetPasswordViewModel != null) {
        resetPasswordViewModel.clearMessages();
      }
    }
  }

  // Helper Classes
  private static class EmailValidator {
    boolean validateEmail(@NonNull String email, @NonNull EditText emailField) {
      if (email.isEmpty()) {
        emailField.setError(Config.EMAIL_REQUIRED_ERROR);
        emailField.requestFocus();
        return false;
      }

      if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        emailField.setError(Config.INVALID_EMAIL_ERROR);
        emailField.requestFocus();
        return false;
      }

      return true;
    }
  }

  private class ThemeManager {
    void switchToLightTheme() {
      try {
        applyImageColorFilter();
        resetLayout.setBackgroundColor(Color.WHITE);

        applyTextColors(Color.BLACK);
        applyEditTextColors(Color.BLACK);
        applyButtonTextColors(Color.BLACK);

        hideViews(lightThemeButton);
        showViews(darkThemeButton);

        Log.d(Config.LOG_TAG, "Switched to light theme");

      } catch (Exception e) {
        Log.w(Config.LOG_TAG, "Error switching to light theme", e);
      }
    }

    void switchToDarkTheme() {
      try {
        applyImageColorFilter();
        resetLayout.setBackgroundColor(Color.BLACK);

        applyTextColors(Color.WHITE);
        applyEditTextColors(Color.WHITE);
        applyButtonTextColors(Color.WHITE);

        hideViews(darkThemeButton);
        showViews(lightThemeButton);

        Log.d(Config.LOG_TAG, "Switched to dark theme");

      } catch (Exception e) {
        Log.w(Config.LOG_TAG, "Error switching to dark theme", e);
      }
    }

    private void applyImageColorFilter() {
      if (cakeImageView != null) {
        cakeImageView.setColorFilter(new ColorMatrixColorFilter(Config.NEGATIVE_FILTER));
      }
    }

    private void applyTextColors(int color) {
      if (forgotPasswordTextView != null) forgotPasswordTextView.setTextColor(color);
      if (helpTextView != null) helpTextView.setTextColor(color);
      if (instructionsTextView != null) instructionsTextView.setTextColor(color);
      if (hideInstructionsTextView != null) hideInstructionsTextView.setTextColor(color);
    }

    private void applyEditTextColors(int color) {
      if (emailInputField != null) {
        emailInputField.setHintTextColor(color);
        emailInputField.setBackgroundTintList(ColorStateList.valueOf(color));
        emailInputField.setTextColor(color);
      }
    }

    private void applyButtonTextColors(int color) {
      if (resetPasswordButton != null) resetPasswordButton.setTextColor(color);
      if (helpButton != null) helpButton.setTextColor(color);
      if (hideInstructionsButton != null) hideInstructionsButton.setTextColor(color);
    }
  }
}
