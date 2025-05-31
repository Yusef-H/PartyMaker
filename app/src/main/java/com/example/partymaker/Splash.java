package com.example.partymaker;

import static com.example.partymaker.utilities.Constants.IS_CHECKED;
import static com.example.partymaker.utilities.Constants.PREFS_NAME;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.data.DBref;

public class Splash extends AppCompatActivity {

  private static final int SPLASH_DELAY = 3000; // 3 seconds
  private ImageView imgLogo;
  private Handler handler;
  private View dot1, dot2, dot3; // Loading dots

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    initializeComponents();
    startSplashSequence();
  }

  private void initializeComponents() {
    imgLogo = findViewById(R.id.imgLogo);
    dot1 = findViewById(R.id.dot1);
    dot2 = findViewById(R.id.dot2);
    dot3 = findViewById(R.id.dot3);
    handler = new Handler(Looper.getMainLooper());
  }

  private void startSplashSequence() {
    startLogoAnimation();
    animateLoadingDots();
    scheduleNextActivity();
  }

  private void startLogoAnimation() {
    Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.tween);
    imgLogo.startAnimation(fadeInAnimation);
  }

  private void animateLoadingDots() {
    // Start dot animations with delays for wave effect
    handler.postDelayed(() -> animateDot(dot1), 500);
    handler.postDelayed(() -> animateDot(dot2), 700);
    handler.postDelayed(() -> animateDot(dot3), 900);

    // Repeat the animation every 1.5 seconds
    handler.postDelayed(this::repeatDotAnimation, 1500);
  }

  private void animateDot(View dot) {
    if (dot != null) {
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
  }

  private void repeatDotAnimation() {
    if (!isFinishing()) {
      animateLoadingDots();
    }
  }

  private void scheduleNextActivity() {
    handler.postDelayed(this::navigateToNextActivity, SPLASH_DELAY);
  }

  private void navigateToNextActivity() {
    if (isFinishing()) {
      return; // If activity already closed, stop
    }

    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    boolean isChecked = settings.getBoolean(IS_CHECKED, false);

    Class<?> targetActivity = shouldNavigateToMain(isChecked) ? MainActivity.class : Login.class;

    Intent intent = new Intent(this, targetActivity);
    startActivity(intent);
    finish();
  }

  private boolean shouldNavigateToMain(boolean isRememberMeChecked) {
    return DBref.Auth.getCurrentUser() != null && isRememberMeChecked;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (handler != null) {
      handler.removeCallbacksAndMessages(null); // Clear callbacks to prevent memory leaks
    }
  }
}
