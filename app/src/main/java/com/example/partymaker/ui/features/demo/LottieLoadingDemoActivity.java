package com.example.partymaker.ui.features.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.example.partymaker.R;
import com.example.partymaker.utils.ui.components.LoadingStateManager;

/**
 * Demo Activity showing how to use LoadingStateManager with Lottie animations. This demonstrates
 * the enhanced loading experience with professional animations.
 */
public class LottieLoadingDemoActivity extends AppCompatActivity {

  // Animation timing constants
  private static final long DEMO_LOADING_DELAY_MS = 3000L;
  private static final long DEMO_CREATION_DELAY_MS = 2500L;
  private static final long DEMO_ERROR_AUTO_HIDE_MS = 3000L;
  private static final long DEMO_INITIALIZATION_DELAY_MS = 2000L;
  
  private LoadingStateManager loadingStateManager;
  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loading_lottie);

    handler = new Handler(Looper.getMainLooper());

    setupLoadingStateManager();
    setupDemoButtons();

    // Start with a demo loading sequence
    demonstrateLoadingStates();
  }

  private void setupLoadingStateManager() {
    // Find views
    View contentContainer = findViewById(R.id.content_container);
    ProgressBar progressBarFallback = findViewById(R.id.progress_bar_fallback);
    TextView loadingText = findViewById(R.id.loading_text);
    View errorContainer = findViewById(R.id.error_container);
    LottieAnimationView lottieAnimation = findViewById(R.id.lottie_loading);

    // Create LoadingStateManager with Lottie support
    loadingStateManager =
        new LoadingStateManager.Builder()
            .contentView(contentContainer)
            .progressBar(progressBarFallback)
            .loadingText(loadingText)
            .errorView(errorContainer)
            .lottieAnimation(lottieAnimation)
            .build();
  }

  private void setupDemoButtons() {
    // Add demo buttons to content container
    View contentContainer = findViewById(R.id.content_container);
    if (contentContainer instanceof android.widget.LinearLayout) {
      android.widget.LinearLayout layout = (android.widget.LinearLayout) contentContainer;

      // Clear existing content
      layout.removeAllViews();

      // Add demo buttons
      Button loadingButton = new Button(this);
      loadingButton.setText("Show Loading");
      loadingButton.setOnClickListener(
          v -> {
            loadingStateManager.showLoading("Fetching party data...");

            // Simulate network delay
            handler.postDelayed(() -> loadingStateManager.showContent(), DEMO_LOADING_DELAY_MS);
          });

      Button customAnimationButton = new Button(this);
      customAnimationButton.setText("Custom Animation");
      customAnimationButton.setOnClickListener(
          v -> {
            // You can set different animations for different operations
            loadingStateManager.showLoadingWithAnimation("Creating party...", null);

            // Simulate creation process
            handler.postDelayed(() -> loadingStateManager.showContent(), DEMO_CREATION_DELAY_MS);
          });

      Button errorButton = new Button(this);
      errorButton.setText("Show Error");
      errorButton.setOnClickListener(
          v -> {
            loadingStateManager.showError("Failed to load party data");

            // Auto-hide error after demo period
            handler.postDelayed(() -> loadingStateManager.showContent(), DEMO_ERROR_AUTO_HIDE_MS);
          });

      layout.addView(loadingButton);
      layout.addView(customAnimationButton);
      layout.addView(errorButton);
    }
  }

  private void demonstrateLoadingStates() {
    // Show initial loading
    loadingStateManager.showLoading("Initializing PartyMaker...");

    // Simulate app initialization
    handler.postDelayed(
        () -> {
          loadingStateManager.showContent();
        },
        DEMO_INITIALIZATION_DELAY_MS);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
    }
  }
}
