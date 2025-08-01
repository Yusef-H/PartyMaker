package com.example.partymaker.ui.features.auth;

import static com.example.partymaker.utils.core.IntentExtrasManager.hideViews;
import static com.example.partymaker.utils.core.IntentExtrasManager.showViews;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.R;
import com.example.partymaker.viewmodel.auth.AuthViewModel;

/**
 * Activity for resetting user password via email. Handles UI theme switching and password reset
 * logic.
 */
public class ResetPasswordActivity extends AppCompatActivity {
  /** Auth ViewModel */
  private AuthViewModel authViewModel;

  /** White theme button. */
  private Button btnWhite;

  /** Black theme button. */
  private Button btnBlack;

  /** Reset password button. */
  private Button btnReset;

  /** Help button. */
  private Button btnHelp;

  /** Hide instructions button. */
  private Button btnHide;

  /** Reset layout. */
  private RelativeLayout rltReset;

  /** Forgot password text. */
  private TextView tvForgotPass;

  /** Help text. */
  private TextView tvHelp;

  /** Instructions text. */
  private TextView tvInstructions;

  /** Hide instructions text. */
  private TextView tvHide;

  /** Email input field. */
  private EditText etInputEmail;

  /** Cake image view. */
  private ImageView imgWhiteCake;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth_reset);

    // this 3 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.hide();

    // Initialize ViewModel
    authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    setupViewModelObservers();

    btnBlack = findViewById(R.id.btnBlack);
    btnWhite = findViewById(R.id.btnWhite);
    btnHelp = findViewById(R.id.btnHelp);
    btnReset = findViewById(R.id.btnReset);
    btnHide = findViewById(R.id.btnHide);
    etInputEmail = findViewById(R.id.etInputEmail);
    tvForgotPass = findViewById(R.id.tvForgotPass);
    tvHelp = findViewById(R.id.tvHelp);
    tvInstructions = findViewById(R.id.tvInstructions);
    tvHide = findViewById(R.id.tvHide);
    rltReset = findViewById(R.id.rltReset);
    imgWhiteCake = findViewById(R.id.imgWhiteCake);

    eventHandler();
  }

  /** Sets up observers for AuthViewModel LiveData */
  private void setupViewModelObservers() {
    authViewModel
        .getIsLoading()
        .observe(
            this,
            isLoading -> {
              btnReset.setEnabled(!isLoading);
            });

    authViewModel
        .getErrorMessage()
        .observe(
            this,
            errorMessage -> {
              if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
              }
            });

    authViewModel
        .getSuccessMessage()
        .observe(
            this,
            successMessage -> {
              if (successMessage != null) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
              }
            });

    authViewModel
        .getIsPasswordResetSent()
        .observe(
            this,
            isResetSent -> {
              if (isResetSent) {
                finish();
              }
            });
  }

  /** Sends a password reset email to the user. */
  private void ResetPass() {
    final String email = etInputEmail.getText().toString();
    authViewModel.resetPassword(email);
  }

  /** Handles all button click events and UI theme switching. */
  private void eventHandler() {
    btnReset.setOnClickListener(v -> ResetPass());

    btnWhite.setOnClickListener(v -> switchToLightMode());

    btnBlack.setOnClickListener(v -> switchToDarkMode());

    btnHelp.setOnClickListener(
        v -> {
          showViews(tvInstructions, tvHide, btnHide);
          hideViews(tvHelp, btnHelp);
        });

    btnHide.setOnClickListener(
        v -> {
          hideViews(tvInstructions, tvHide, btnHide);
          showViews(tvHelp, btnHelp);
        });
  }

  private void switchToLightMode() {
    applyColorFilterToImage();
    rltReset.setBackgroundColor(Color.WHITE);

    setTextColors(Color.BLACK);
    setEditTextColors(Color.BLACK);
    setButtonTextColors(Color.BLACK);

    hideViews(btnWhite);
    showViews(btnBlack);
  }

  private void switchToDarkMode() {
    applyColorFilterToImage();
    rltReset.setBackgroundColor(Color.BLACK);

    setTextColors(Color.WHITE);
    setEditTextColors(Color.WHITE);
    setButtonTextColors(Color.WHITE);

    hideViews(btnBlack);
    showViews(btnWhite);
  }

  private void applyColorFilterToImage() {
    final float[] NEGATIVE = {
      -1.0f, 0, 0, 0, 255, 0, -1.0f, 0, 0, 255, 0, 0, -1.0f, 0, 255, 0, 0, 0, 1.0f, 0
    };
    imgWhiteCake.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
  }

  private void setTextColors(int color) {
    tvForgotPass.setTextColor(color);
    tvHelp.setTextColor(color);
    tvInstructions.setTextColor(color);
    tvHide.setTextColor(color);
  }

  private void setEditTextColors(int color) {
    etInputEmail.setHintTextColor(color);
    etInputEmail.setBackgroundTintList(ColorStateList.valueOf(color));
    etInputEmail.setTextColor(color);
  }

  private void setButtonTextColors(int color) {
    btnReset.setTextColor(color);
    btnHelp.setTextColor(color);
    btnHide.setTextColor(color);
  }
}
