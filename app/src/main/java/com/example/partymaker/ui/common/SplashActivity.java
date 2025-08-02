package com.example.partymaker.ui.common;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.R;
import com.example.partymaker.ui.auth.IntroActivity;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.security.core.SecureConfigManager;
import com.example.partymaker.utils.system.ThreadUtils;
import com.example.partymaker.viewmodel.SplashViewModel;

/**
 * SplashActivity displays the initial splash screen with animations, then navigates the user to the
 * appropriate screen (Main or Login).
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

  private static final int SPLASH_DELAY = 3000; // Duration to stay on splash screen (ms)

  private ImageView imgLogo;
  private View dot1, dot2, dot3;
  private Handler handler;
  private SplashViewModel viewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth_splash);

    // Initialize secure configuration
    initializeSecureConfig();

    // Initialize ViewModel
    viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
    setupViewModelObservers();

    initializeViews();
    startSplashFlow();
  }

  /** Initialize secure configuration */
  private void initializeSecureConfig() {
    try {
      SecureConfigManager config = SecureConfigManager.getInstance(this);
      // Server URL is now managed through SecureConfig
      String serverUrl = config.getServerUrl();
      android.util.Log.d("SplashActivity", "Server URL: " + serverUrl);
    } catch (Exception e) {
      android.util.Log.e("SplashActivity", "Failed to initialize secure config", e);
    }
  }

  // Initialize views and handler
  private void initializeViews() {
    imgLogo = findViewById(R.id.imgLogo);
    dot1 = findViewById(R.id.dot1);
    dot2 = findViewById(R.id.dot2);
    dot3 = findViewById(R.id.dot3);
    handler = new Handler(Looper.getMainLooper());
  }

  // Starts splash screen animations and navigation logic
  private void startSplashFlow() {
    animateLogo();
    animateLoadingDots();
    scheduleNextScreen();
  }

  // Applies fade-in animation to the logo
  private void animateLogo() {
    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.tween);
    imgLogo.startAnimation(fadeIn);
  }

  // Animates loading dots in a wave effect
  private void animateLoadingDots() {
    ThreadUtils.runOnMainThreadDelayed(() -> animateDot(dot1), 500);
    ThreadUtils.runOnMainThreadDelayed(() -> animateDot(dot2), 700);
    ThreadUtils.runOnMainThreadDelayed(() -> animateDot(dot3), 900);

    // Repeats the dot animation loop every 1.5 seconds
    ThreadUtils.runOnMainThreadDelayed(this::animateLoadingDots, 1500);
  }

  // Single dot animation (scale + alpha)
  private void animateDot(View dot) {
    if (dot == null) return;

    ObjectAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1.0f, 1.3f, 1.0f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1.0f, 1.3f, 1.0f);
    ObjectAnimator alpha = ObjectAnimator.ofFloat(dot, "alpha", 0.5f, 1.0f, 0.5f);

    scaleX.setDuration(600);
    scaleY.setDuration(600);
    alpha.setDuration(600);

    scaleX.start();
    scaleY.start();
    alpha.start();
  }

  // Schedules the transition to the next activity after the splash delay
  private void scheduleNextScreen() {
    ThreadUtils.runOnMainThreadDelayed(() -> viewModel.initialize(), SPLASH_DELAY);
  }

  /** Sets up observers for ViewModel LiveData */
  private void setupViewModelObservers() {
    viewModel
        .getNavigationDestination()
        .observe(
            this,
            destination -> {
              if (destination != null && !isFinishing()) {
                switch (destination) {
                  case LOGIN:
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    break;
                  case MAIN:
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
                  case INTRO:
                    startActivity(new Intent(this, IntroActivity.class));
                    finish();
                    break;
                }
              }
            });
  }

  // Checks if user is authenticated and 'Remember Me' is checked with session validation
  private boolean shouldNavigateToMain(boolean rememberMeChecked) {
    if (!rememberMeChecked) {
      return false;
    }

    // Check if session is valid
    if (!AuthenticationManager.isSessionValid(this)) {
      // Clear expired session
      AuthenticationManager.clearAuthData(this);
      return false;
    }

    // Check if user is authenticated
    if (AuthenticationManager.isUserAuthenticated(this)) {
      // Refresh session for continued use
      AuthenticationManager.refreshSession(this);
      return true;
    }

    return false;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (handler != null) {
      handler.removeCallbacksAndMessages(null); // Prevent memory leaks
    }
  }
}
