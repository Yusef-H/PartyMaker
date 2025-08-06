package com.example.partymaker.ui.features.core;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.R;
import com.example.partymaker.ui.features.auth.IntroActivity;
import com.example.partymaker.ui.features.auth.LoginActivity;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.security.core.SecureConfigManager;
import com.example.partymaker.viewmodel.core.SplashViewModel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enterprise-level SplashActivity implementation with proper separation of concerns, error
 * handling, and resource management.
 *
 * <p>Responsibilities: - Display splash screen with animations - Initialize application
 * dependencies - Route user to appropriate destination based on authentication state
 *
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

  private static final class Config {
    static final int SPLASH_DELAY_MS = 3000;
    static final int DOT_ANIMATION_DELAY_MS = 500;
    static final int DOT_ANIMATION_STAGGER_MS = 200;
    static final int DOT_ANIMATION_DURATION_MS = 600;
    static final int DOT_ANIMATION_LOOP_MS = 1500;
    static final float DOT_SCALE_FACTOR = 1.3f;
    static final float DOT_ALPHA_MIN = 0.5f;
    static final float DOT_ALPHA_MAX = 1.0f;
    static final String LOG_TAG = "SplashActivity";
  }

  private ImageView logoImageView;
  private View[] loadingDots;
  private Handler mainHandler;
  private SplashViewModel splashViewModel;
  private final AtomicBoolean isDestroyed = new AtomicBoolean(false);

  private SecureConfigManager configManager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      setContentView(R.layout.activity_auth_splash);

      initializeDependencies();
      initializeViewModel();
      initializeViews();
      setupViewModelObservers();
      startSplashFlow();

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Critical error during splash initialization", e);
      handleInitializationError(e);
    }
  }

  private void initializeDependencies() {
    try {
      configManager = SecureConfigManager.getInstance(this);

      String serverUrl = configManager.getServerUrl();
      Log.d(Config.LOG_TAG, "Initialized with server URL configured");

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Failed to initialize dependencies", e);
      throw new RuntimeException("Critical dependency initialization failure", e);
    }
  }

  private void initializeViewModel() {
    splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
  }

  private void initializeViews() {
    logoImageView = findViewById(R.id.imgLogo);
    loadingDots =
        new View[] {findViewById(R.id.dot1), findViewById(R.id.dot2), findViewById(R.id.dot3)};
    mainHandler = new Handler(Looper.getMainLooper());

    validateViewsInitialized();
  }

  private void validateViewsInitialized() {
    if (logoImageView == null) {
      throw new IllegalStateException("Logo ImageView not found in layout");
    }

    for (int i = 0; i < loadingDots.length; i++) {
      if (loadingDots[i] == null) {
        throw new IllegalStateException("Loading dot " + (i + 1) + " not found in layout");
      }
    }
  }

  private void startSplashFlow() {
    if (isDestroyed.get()) {
      Log.w(Config.LOG_TAG, "Attempted to start splash flow after activity destruction");
      return;
    }

    try {
      animateLogo();
      startLoadingDotsAnimation();
      scheduleNavigation();

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Error during splash flow execution", e);
      handleSplashFlowError(e);
    }
  }

  private void animateLogo() {
    try {
      Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.tween);
      logoImageView.startAnimation(fadeIn);

    } catch (Exception e) {
      Log.w(Config.LOG_TAG, "Logo animation failed, continuing without animation", e);
    }
  }

  private void startLoadingDotsAnimation() {
    for (int i = 0; i < loadingDots.length; i++) {
      final int dotIndex = i;
      final int delay = Config.DOT_ANIMATION_DELAY_MS + (i * Config.DOT_ANIMATION_STAGGER_MS);

      ThreadUtils.runOnMainThreadDelayed(
          () -> {
            if (!isDestroyed.get()) {
              animateDot(loadingDots[dotIndex]);
            }
          },
          delay);
    }

    ThreadUtils.runOnMainThreadDelayed(
        () -> {
          if (!isDestroyed.get()) {
            startLoadingDotsAnimation();
          }
        },
        Config.DOT_ANIMATION_LOOP_MS);
  }

  private void animateDot(@NonNull View dot) {
    if (isDestroyed.get()) {
      return;
    }

    try {
      AnimatorSet animatorSet = createDotAnimatorSet(dot);
      animatorSet.start();

    } catch (Exception e) {
      Log.w(Config.LOG_TAG, "Dot animation failed for dot: " + dot.getId(), e);
    }
  }

  @NonNull
  private AnimatorSet createDotAnimatorSet(@NonNull View dot) {
    ObjectAnimator scaleX =
        ObjectAnimator.ofFloat(dot, "scaleX", 1.0f, Config.DOT_SCALE_FACTOR, 1.0f);
    ObjectAnimator scaleY =
        ObjectAnimator.ofFloat(dot, "scaleY", 1.0f, Config.DOT_SCALE_FACTOR, 1.0f);
    ObjectAnimator alpha =
        ObjectAnimator.ofFloat(
            dot, "alpha", Config.DOT_ALPHA_MIN, Config.DOT_ALPHA_MAX, Config.DOT_ALPHA_MIN);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY, alpha);
    animatorSet.setDuration(Config.DOT_ANIMATION_DURATION_MS);

    return animatorSet;
  }

  private void scheduleNavigation() {
    ThreadUtils.runOnMainThreadDelayed(
        () -> {
          if (!isDestroyed.get() && !isFinishing()) {
            splashViewModel.initialize();
          }
        },
        Config.SPLASH_DELAY_MS);
  }

  private void setupViewModelObservers() {
    splashViewModel.getNavigationDestination().observe(this, this::handleNavigationDestination);
  }

  private void handleNavigationDestination(
      @Nullable SplashViewModel.NavigationDestination destination) {
    if (destination == null || isFinishing() || isDestroyed.get()) {
      return;
    }

    try {
      Intent intent = createNavigationIntent(destination);
      startActivity(intent);
      finish();

    } catch (Exception e) {
      Log.e(Config.LOG_TAG, "Navigation failed for destination: " + destination, e);
      handleNavigationError(e);
    }
  }

  @NonNull
  private Intent createNavigationIntent(
      @NonNull SplashViewModel.NavigationDestination destination) {
    switch (destination) {
      case LOGIN:
        return new Intent(this, LoginActivity.class);
      case MAIN:
        return new Intent(this, MainActivity.class);
      case INTRO:
        return new Intent(this, IntroActivity.class);
      default:
        throw new IllegalArgumentException("Unknown navigation destination: " + destination);
    }
  }

  private void handleInitializationError(@NonNull Exception error) {
    Log.e(Config.LOG_TAG, "Initialization error - navigating to login", error);
    startActivity(new Intent(this, LoginActivity.class));
    finish();
  }

  private void handleSplashFlowError(@NonNull Exception error) {
    Log.e(Config.LOG_TAG, "Splash flow error - continuing with basic flow", error);
    scheduleNavigation();
  }

  private void handleNavigationError(@NonNull Exception error) {
    Log.e(Config.LOG_TAG, "Navigation error - falling back to login", error);
    startActivity(new Intent(this, LoginActivity.class));
    finish();
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
    if (mainHandler != null) {
      mainHandler.removeCallbacksAndMessages(null);
      mainHandler = null;
    }

    if (loadingDots != null) {
      for (View dot : loadingDots) {
        if (dot != null) {
          dot.clearAnimation();
        }
      }
      loadingDots = null;
    }

    if (logoImageView != null) {
      logoImageView.clearAnimation();
      logoImageView = null;
    }

    splashViewModel = null;
    configManager = null;
  }
}
