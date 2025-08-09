package com.example.partymaker.ui.features.auth;

import static com.example.partymaker.utils.core.AppConstants.Preferences.IS_CHECKED;
import static com.example.partymaker.utils.core.AppConstants.Preferences.PREFS_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.R;
import com.example.partymaker.data.api.NetworkManager;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.ui.components.UiStateManager;
import com.example.partymaker.viewmodel.auth.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enterprise-level LoginActivity implementation with proper separation of concerns, comprehensive
 * error handling, input validation, and resource management.
 *
 * <p>Responsibilities: - Handle user authentication (email/password and Google Sign-In) - Validate
 * user input with real-time feedback - Manage UI state transitions and loading states - Navigate to
 * appropriate screens based on authentication result - Maintain user session preferences
 *
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 */
public class LoginActivity extends AppCompatActivity {

  private static final class Config {
    static final String LOG_TAG = "LoginActivity";
    static final int GOOGLE_SIGN_IN_REQUEST_CODE = 9001;
    static final String DEFAULT_SERVER_URL = "https://partymaker.onrender.com";
    static final String SERVER_HEALTH_ENDPOINT = "/api/firebase/health";
    static final int SERVER_TIMEOUT_MS = 3000;
    static final int SUCCESS_NAVIGATION_DELAY_MS = 800;
    static final String PREFS_EXPLICIT_LOGIN = "user_explicitly_logged_in";
    static final String PREFS_PARTY_MAKER = "PartyMakerPrefs";
  }

  // UI Components
  private TextInputEditText emailEditText;
  private TextInputEditText passwordEditText;
  private ImageButton aboutButton;
  private MaterialButton loginButton;
  private MaterialButton registerButton;
  private MaterialButton resetPasswordButton;
  private MaterialCheckBox rememberMeCheckbox;
  private SignInButton googleSignInButton;
  private ProgressBar progressBar;
  private View rootView;

  // Dependencies
  private FirebaseAuth firebaseAuth;
  private AuthViewModel authViewModel;
  private NetworkManager networkManager;

  // State Management
  private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
  private final AtomicBoolean isInitialized = new AtomicBoolean(false);

  // Validation and Input Management
  private InputValidator inputValidator;
  private AnimationManager animationManager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      setContentView(R.layout.activity_auth_login);

      initializeDependencies();
      initializeComponents();
      configureActionBar();
      bindViewReferences();
      setupEventHandlers();
      setupViewModelObservers();
      setupInputValidation();
      startInitialAnimations();

      performInitialSetup();

      isInitialized.set(true);
      Log.d(Config.LOG_TAG, "LoginActivity initialized successfully");

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Critical error during login initialization", e);
      handleInitializationError(e);
    }
  }

  private void initializeDependencies() {
    try {
      firebaseAuth = FirebaseAuth.getInstance();
      authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
      networkManager = NetworkManager.getInstance();
      inputValidator = new InputValidator();
      animationManager = new AnimationManager(this);

      Log.d(Config.LOG_TAG, "Dependencies initialized successfully");

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Failed to initialize dependencies", e);
      throw new RuntimeException("Critical dependency initialization failure", e);
    }
  }

  private void initializeComponents() {
    rootView = findViewById(android.R.id.content);
    setupLoadingStateManager();
  }

  private void configureActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
  }

  private void bindViewReferences() {
    aboutButton = findViewById(R.id.btnAbout);
    emailEditText = findViewById(R.id.etEmailL);
    passwordEditText = findViewById(R.id.etPasswordL);
    loginButton = findViewById(R.id.btnLogin);
    registerButton = findViewById(R.id.btnPressL);
    rememberMeCheckbox = findViewById(R.id.cbRememberMe);
    resetPasswordButton = findViewById(R.id.btnResetPass);
    googleSignInButton = findViewById(R.id.btnGoogleSignIn);
    progressBar = findViewById(R.id.progressBar);

    validateViewReferences();
  }

  private void validateViewReferences() {
    if (emailEditText == null || passwordEditText == null || loginButton == null) {
      throw new IllegalStateException("Critical UI components not found in layout");
    }
  }

  private void setupLoadingStateManager() {
    if (progressBar == null) {
      progressBar = new ProgressBar(this);
      progressBar.setVisibility(View.GONE);
    }
    // Direct progress bar management to avoid interference with LoadingStateManager
  }

  private void setupViewModelObservers() {
    authViewModel.getIsLoading().observe(this, this::handleLoadingState);
    authViewModel.getErrorMessage().observe(this, this::handleErrorMessage);
    authViewModel.getSuccessMessage().observe(this, this::handleSuccessMessage);
    authViewModel.getIsAuthenticated().observe(this, this::handleAuthenticationSuccess);
  }

  private void handleLoadingState(@Nullable Boolean isLoading) {
    boolean loading = Boolean.TRUE.equals(isLoading);

    loginButton.setEnabled(!loading);
    googleSignInButton.setEnabled(!loading);

    if (progressBar != null) {
      progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
  }

  private void handleErrorMessage(@Nullable String errorMessage) {
    if (errorMessage != null && !errorMessage.isEmpty()) {
      UiStateManager.showError(rootView, errorMessage, this::clearErrorAndRetry);
      resetPasswordButton.setVisibility(View.VISIBLE);
    }
  }

  private void handleSuccessMessage(@Nullable String successMessage) {
    if (successMessage != null && !successMessage.isEmpty()) {
      UiStateManager.showSuccess(rootView, successMessage);
    }
  }

  private void handleAuthenticationSuccess(@Nullable Boolean isAuthenticated) {
    if (Boolean.TRUE.equals(isAuthenticated)) {
      // Success message is already shown by AuthViewModel through handleSuccessMessage()
      // Just handle navigation after a brief delay
      ThreadUtils.runOnMainThreadDelayed(
          () -> {
            saveUserPreferences();
            navigateToMainActivity();
          },
          800);
    }
  }

  private void clearErrorAndRetry() {
    authViewModel.clearMessages();
  }

  private void saveUserPreferences() {
    try {
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      settings.edit().putBoolean(IS_CHECKED, rememberMeCheckbox.isChecked()).apply();

      SharedPreferences prefs =
          getSharedPreferences(Config.PREFS_PARTY_MAKER, Context.MODE_PRIVATE);
      prefs.edit().putBoolean(Config.PREFS_EXPLICIT_LOGIN, true).apply();

    } catch (Exception e) {
      Log.w(Config.LOG_TAG, "Error saving user preferences", e);
    }
  }

  private void navigateToMainActivity() {
    ThreadUtils.runOnMainThreadDelayed(
        () -> {
          if (!isDestroyed.get() && !isFinishing()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            animationManager.clearAnimations();
            startActivity(intent);
            finish();
          }
        },
        Config.SUCCESS_NAVIGATION_DELAY_MS);
  }

  private void forceSetServerUrl() {
    try {
      androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
          .edit()
          .putString("server_url", Config.DEFAULT_SERVER_URL)
          .apply();
      Log.d(Config.LOG_TAG, "Server URL configured: " + Config.DEFAULT_SERVER_URL);

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Failed to set server URL", e);
    }
  }

  private void setupDebugTestUser() {
    // Debug test user setup removed for security
  }

  private void createOrLoginTestUser(@NonNull String email, @NonNull String password) {
    firebaseAuth
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(Config.LOG_TAG, "Debug test user login successful");
              } else {
                createTestUser(email, password);
              }
            });
  }

  private void createTestUser(@NonNull String email, @NonNull String password) {
    firebaseAuth
        .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            createTask -> {
              if (createTask.isSuccessful()) {
                Log.d(Config.LOG_TAG, "Debug test user created successfully");
                createTestUserInDatabase(email);
              } else {
                Log.w(
                    Config.LOG_TAG,
                    "Debug test user creation failed: "
                        + (createTask.getException() != null
                            ? createTask.getException().getMessage()
                            : "unknown error"));
              }
            });
  }

  private void createTestUserInDatabase(@NonNull String email) {
    try {
      Map<String, Object> testUserData = new HashMap<>();
      testUserData.put("username", "Test User");
      testUserData.put("email", email);
      testUserData.put("userKey", java.util.UUID.randomUUID().toString());

      DBRef.refUsers.child(email.replace('.', ' ')).setValue(testUserData);

    } catch (Exception e) {
      Log.w(Config.LOG_TAG, "Error creating test user in database", e);
    }
  }

  private void checkServerConnectivity() {
    ThreadUtils.runInBackground(
        () -> {
          try {
            String serverUrl = getConfiguredServerUrl();
            String healthEndpoint = serverUrl + Config.SERVER_HEALTH_ENDPOINT;

            Log.d(Config.LOG_TAG, "Checking server connectivity to: " + serverUrl);

            boolean isReachable =
                networkManager.isServerReachable(healthEndpoint, Config.SERVER_TIMEOUT_MS);

            ThreadUtils.runOnMainThread(() -> handleConnectivityResult(isReachable));

          } catch (Exception e) {
            Log.e(Config.LOG_TAG, "Error checking server connectivity", e);
            ThreadUtils.runOnMainThread(() -> handleConnectivityError(e));
          }
        });
  }

  @NonNull
  private String getConfiguredServerUrl() {
    return androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        .getString("server_url", Config.DEFAULT_SERVER_URL);
  }

  private void handleConnectivityResult(boolean isReachable) {
    if (isReachable) {
      Log.d(Config.LOG_TAG, "Server connectivity confirmed");
    } else {
      Log.w(Config.LOG_TAG, "Server unreachable - offline mode may be used");
    }
  }

  private void handleConnectivityError(@NonNull Exception error) {
    Log.w(Config.LOG_TAG, "Connectivity check failed - continuing with offline capability", error);
  }

  private void setupEventHandlers() {
    loginButton.setOnClickListener(this::handleLoginClick);
    googleSignInButton.setOnClickListener(this::handleGoogleSignInClick);
    registerButton.setOnClickListener(this::handleRegisterClick);
    resetPasswordButton.setOnClickListener(this::handleResetPasswordClick);
    aboutButton.setOnClickListener(this::handleAboutClick);
  }

  private void handleLoginClick(@NonNull View view) {
    try {
      LoginCredentials credentials = extractCredentials();

      if (inputValidator.validateLoginInput(credentials, emailEditText, passwordEditText)) {
        performEmailLogin(credentials);
      }

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Error during login attempt", e);
      UiStateManager.showError(rootView, "Login failed. Please try again.");
    }
  }

  @NonNull
  private LoginCredentials extractCredentials() {
    String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
    String password =
        passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
    return new LoginCredentials(email, password);
  }

  private void performEmailLogin(@NonNull LoginCredentials credentials) {
    authViewModel.loginWithEmail(credentials.email, credentials.password);
    // Session management is handled automatically by AuthenticationManager
  }

  private void handleGoogleSignInClick(@NonNull View view) {
    try {
      GoogleSignInClient googleClient = authViewModel.getGoogleSignInClient();
      if (googleClient != null) {
        Intent signInIntent = googleClient.getSignInIntent();
        startActivityForResult(signInIntent, Config.GOOGLE_SIGN_IN_REQUEST_CODE);
      } else {
        Log.w(Config.LOG_TAG, "Google Sign-In client not available");
        UiStateManager.showError(rootView, "Google Sign-In temporarily unavailable");
      }
    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Error initiating Google Sign-In", e);
      UiStateManager.showError(rootView, "Google Sign-In failed. Please try again.");
    }
  }

  private void handleRegisterClick(@NonNull View view) {
    navigateToActivity(RegisterActivity.class, true);
  }

  private void handleResetPasswordClick(@NonNull View view) {
    navigateToActivity(ResetPasswordActivity.class, true);
  }

  private void handleAboutClick(@NonNull View view) {
    navigateToActivity(IntroActivity.class, false);
  }

  private void navigateToActivity(@NonNull Class<?> activityClass, boolean withTransition) {
    try {
      Intent intent = new Intent(this, activityClass);
      animationManager.clearAnimations();
      startActivity(intent);

      if (withTransition) {
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
      }

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Error navigating to " + activityClass.getSimpleName(), e);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == Config.GOOGLE_SIGN_IN_REQUEST_CODE) {
      try {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        authViewModel.signInWithGoogle(task);

      } catch (Exception e) {
        Log.e(Config.LOG_TAG, "Error processing Google Sign-In result", e);
        UiStateManager.showError(rootView, "Google Sign-In processing failed");
      }
    }
  }

  private void setupInputValidation() {
    emailEditText.addTextChangedListener(new EmailTextWatcher());
    passwordEditText.addTextChangedListener(new PasswordTextWatcher());
  }

  private class EmailTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      emailEditText.setError(null);

      String email = s.toString().trim();
      if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        emailEditText.setError("Please enter a valid email address (e.g., name@example.com)");
      }
    }

    @Override
    public void afterTextChanged(Editable s) {
      authViewModel.clearMessages();
    }
  }

  private class PasswordTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      passwordEditText.setError(null);
    }

    @Override
    public void afterTextChanged(Editable s) {
      authViewModel.clearMessages();
    }
  }

  private void clearPreviousAuthState() {
    try {
      Log.d(Config.LOG_TAG, "Clearing previous authentication state");

      if (authViewModel != null) {
        authViewModel.clearMessages();
      }

      Log.d(Config.LOG_TAG, "Authentication state cleared successfully");

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Error clearing authentication state", e);
    }
  }

  private void performInitialSetup() {
    forceSetServerUrl();
    clearPreviousAuthState();
    setupDebugTestUser();
    checkServerConnectivity();
    restoreCheckboxState();
  }

  private void restoreCheckboxState() {
    try {
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      boolean isChecked = settings.getBoolean(IS_CHECKED, false);
      rememberMeCheckbox.setChecked(isChecked);
      Log.d(Config.LOG_TAG, "Restored checkbox state: " + isChecked);
    } catch (Exception e) {
      Log.w(Config.LOG_TAG, "Error restoring checkbox state", e);
    }
  }

  private void startInitialAnimations() {
    animationManager.startFadeInAnimation(aboutButton);
  }

  private void handleInitializationError(@NonNull Exception error) {
    Log.e(Config.LOG_TAG, "Critical initialization error - showing fallback UI", error);
    UiStateManager.showError(rootView, "App initialization failed. Please restart the app.");
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
    if (animationManager != null) {
      animationManager.clearAnimations();
      animationManager = null;
    }

    emailEditText = null;
    passwordEditText = null;
    authViewModel = null;
    networkManager = null;
    inputValidator = null;
  }

  // Helper Classes
  private static class LoginCredentials {
    final String email;
    final String password;

    LoginCredentials(@NonNull String email, @NonNull String password) {
      this.email = email;
      this.password = password;
    }
  }

  private static class InputValidator {
    boolean validateLoginInput(
        @NonNull LoginCredentials credentials,
        @NonNull TextInputEditText emailField,
        @NonNull TextInputEditText passwordField) {
      if (credentials.email.isEmpty()) {
        emailField.setError("Please enter your email");
        emailField.requestFocus();
        return false;
      }

      if (credentials.password.isEmpty()) {
        passwordField.setError("Please enter your password");
        passwordField.requestFocus();
        return false;
      }

      return true;
    }
  }

  private static class AnimationManager {
    private final Context context;
    private ImageButton currentAnimatedButton;

    AnimationManager(@NonNull Context context) {
      this.context = context;
    }

    void startFadeInAnimation(@NonNull ImageButton button) {
      try {
        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        button.startAnimation(fadeIn);
        currentAnimatedButton = button;

      } catch (Exception e) {
        Log.w("AnimationManager", "Animation failed", e);
      }
    }

    void clearAnimations() {
      if (currentAnimatedButton != null) {
        currentAnimatedButton.clearAnimation();
        currentAnimatedButton = null;
      }
    }
  }
}
