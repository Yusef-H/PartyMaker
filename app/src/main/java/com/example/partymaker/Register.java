package com.example.partymaker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import java.util.Objects;

public class Register extends AppCompatActivity {
  private TextInputLayout tilEmail, tilUsername, tilPassword;
  private TextInputEditText etEmail, etUsername, etPassword;
  private MaterialButton btnRegister, btnPress;
  private static final String CHANNEL_ID = "registration_channel";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    // Hide action bar for cleaner look
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // Initialize UI components
    initializeViews();

    // Create notification channel
    createNotificationChannel();

    // Set up event handlers
    setupEventHandlers();
  }

  private void initializeViews() {
    // TextInputLayouts for validation
    tilEmail = findViewById(R.id.tilEmail);
    tilUsername = findViewById(R.id.tilUsername);
    tilPassword = findViewById(R.id.tilPassword);

    // TextInputEditTexts
    etEmail = findViewById(R.id.etEmailR);
    etUsername = findViewById(R.id.etUsername);
    etPassword = findViewById(R.id.etPasswordR);

    // Buttons
    btnRegister = findViewById(R.id.btnRegister);
    btnPress = findViewById(R.id.btnPressR);
  }

  private void createNotificationChannel() {
    CharSequence name = "Registration Notifications";
    String description = "Notifications for successful registration";
    int importance = NotificationManager.IMPORTANCE_DEFAULT;
    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
    channel.setDescription(description);

    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);
  }

  private void setupEventHandlers() {
    // Register button click
    btnRegister.setOnClickListener(
        view -> {
          if (validateInputs()) {
            signUp();
          }
        });

    // Login link click
    btnPress.setOnClickListener(
        view -> {
          Intent intent = new Intent(Register.this, Login.class);
          startActivity(intent);
          finish(); // Close current activity
        });
  }

  private boolean validateInputs() {
    boolean isValid = true;

    // Reset errors
    tilEmail.setError(null);
    tilUsername.setError(null);
    tilPassword.setError(null);

    String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
    String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
    String password = Objects.requireNonNull(etPassword.getText()).toString();

    // Email validation
    if (TextUtils.isEmpty(email)) {
      tilEmail.setError("נדרש אימייל");
      isValid = false;
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      tilEmail.setError("כתובת אימייל לא תקינה");
      isValid = false;
    }

    // Username validation
    if (TextUtils.isEmpty(username)) {
      tilUsername.setError("נדרש שם משתמש");
      isValid = false;
    } else if (username.length() < 3) {
      tilUsername.setError("שם המשתמש חייב להכיל לפחות 3 תווים");
      isValid = false;
    } else if (username.length() > 20) {
      tilUsername.setError("שם המשתמש לא יכול להכיל יותר מ-20 תווים");
      isValid = false;
    }

    // Password validation
    if (TextUtils.isEmpty(password)) {
      tilPassword.setError("נדרשת סיסמה");
      isValid = false;
    } else if (password.length() < 6) {
      tilPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
      isValid = false;
    }

    return isValid;
  }

  private void signUp() {
    // Disable button during registration
    btnRegister.setEnabled(false);
    btnRegister.setText("נרשם...");

    final String email = Objects.requireNonNull(etEmail.getText()).toString().trim().toLowerCase();
    final String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
    String password = Objects.requireNonNull(etPassword.getText()).toString();

    DBref.Auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            Register.this,
            task -> {
              // Re-enable button
              btnRegister.setEnabled(true);
              btnRegister.setText(getString(R.string.register));

              if (task.isSuccessful()) {
                // Send success notification
                sendSuccessNotification(username);

                // Save user data to database
                User user = new User(email, username);
                DBref.refUsers.child(email.replace('.', ' ')).setValue(user);

                // Show success message
                Toast.makeText(Register.this, "הרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();

                // Navigate to login screen
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();

              } else {
                handleRegistrationError(task.getException());
              }
            });
  }

  private void handleRegistrationError(Exception exception) {
    String errorMessage;

    if (exception instanceof FirebaseAuthUserCollisionException) {
      errorMessage = "האימייל כבר קיים במערכת. נסה להתחבר.";
      tilEmail.setError(errorMessage);
    } else if (exception instanceof FirebaseAuthWeakPasswordException) {
      errorMessage = "הסיסמה חלשה מדי. בחר סיסמה חזקה יותר (לפחות 6 תווים).";
      tilPassword.setError(errorMessage);
    } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
      errorMessage = "כתובת האימייל אינה תקינה.";
      tilEmail.setError(errorMessage);
    } else if (exception instanceof FirebaseAuthException) {
      String errorCode = ((FirebaseAuthException) exception).getErrorCode();
      errorMessage = "שגיאה: " + errorCode;
    } else {
      errorMessage = "שגיאת הרשמה לא ידועה.";
    }

    Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
  }

  private void sendSuccessNotification(String username) {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.cake)
            .setContentTitle("ההרשמה בוצעה בהצלחה!")
            .setContentText("ברוך הבא " + username + "! תתחיל ליצור מסיבות מדהימות")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notificationManager.notify(1, builder.build());
  }
}
