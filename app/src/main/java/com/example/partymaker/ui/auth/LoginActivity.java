package com.example.partymaker.ui.auth;

import static com.example.partymaker.utils.data.Constants.IS_CHECKED;
import static com.example.partymaker.utils.data.Constants.PREFS_NAME;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.utils.auth.AuthHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Activity for user login, including email/password and Google sign-in. Handles authentication,
 * navigation, and UI state.
 */
public class LoginActivity extends AppCompatActivity {
  /** Email input field. */
  private TextInputEditText etEmail;

  /** Password input field. */
  private TextInputEditText etPassword;

  /** About button. */
  private ImageButton btnAbout;

  /** Login button. */
  private MaterialButton btnLogin;

  /** Register button. */
  private MaterialButton btnPress;

  /** Reset password button. */
  private MaterialButton btnResetPass;

  /** Remember me checkbox. */
  private MaterialCheckBox cbRememberMe;

  /** Google sign-in button. */
  private SignInButton btnGoogleSignIn;

  /** Google sign-in client. */
  private GoogleSignInClient mGoogleSignInClient;

  /** Request code for Google sign-in. */
  private static final int RC_SIGN_IN = 9001;

  /** Firebase authentication instance. */
  private FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth_login);

    // Force set server URL to Render
    forceSetServerUrl();

    // Initialize Firebase Auth
    mAuth = FirebaseAuth.getInstance();

    // Add debug option to reset test user password
    resetTestUserPassword();

    // Check server connectivity
    checkServerConnectivity();

    // Configure Google Sign In
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // connection between XML and Java
    btnAbout = findViewById(R.id.btnAbout);
    etEmail = findViewById(R.id.etEmailL);
    etPassword = findViewById(R.id.etPasswordL);
    btnLogin = findViewById(R.id.btnLogin);
    btnPress = findViewById(R.id.btnPressL);
    cbRememberMe = findViewById(R.id.cbRememberMe);
    btnResetPass = findViewById(R.id.btnResetPass);
    btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

    // start animation on ImageButton btnAbout
    Animation myFadeInAnimation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in);
    btnAbout.startAnimation(myFadeInAnimation);

    eventHandler();
  }

  /** Forces the server URL to be set to Render */
  private void forceSetServerUrl() {
    String renderUrl = "https://partymaker.onrender.com";
    androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putString("server_url", renderUrl)
        .apply();
    Log.d("LoginActivity", "Forced server URL to: " + renderUrl);
  }

  /** Resets password for test user (1@1.com) to 123456 This is for debugging purposes only */
  private void resetTestUserPassword() {
    String testEmail = "1@1.com";
    String testPassword = "123456";

    // Try to sign in with email and password directly
    mAuth
        .signInWithEmailAndPassword(testEmail, testPassword)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d("LoginActivity", "Test user login successful");
              } else {
                Log.d("LoginActivity", "Test user login failed, attempting to create user");

                // If login fails, try to create the user
                mAuth
                    .createUserWithEmailAndPassword(testEmail, testPassword)
                    .addOnCompleteListener(
                        createTask -> {
                          if (createTask.isSuccessful()) {
                            Log.d("LoginActivity", "Test user created successfully");
                            // Create user in database using Map instead of User object
                            Map<String, Object> testUserData = new HashMap<>();
                            testUserData.put("username", "Test User");
                            testUserData.put("email", testEmail);
                            testUserData.put("userKey", java.util.UUID.randomUUID().toString());

                            DBRef.refUsers
                                .child(testEmail.replace('.', ' '))
                                .setValue(testUserData);
                          } else {
                            Log.d(
                                "LoginActivity",
                                "Test user creation failed: "
                                    + (createTask.getException() != null
                                        ? createTask.getException().getMessage()
                                        : "unknown error"));
                          }
                        });
              }
            });
  }

  /** Checks connectivity to the server */
  private void checkServerConnectivity() {
    new Thread(
            () -> {
              try {
                String serverUrl =
                    androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("server_url", "https://partymaker.onrender.com");

                Log.d("LoginActivity", "Checking server connectivity to: " + serverUrl);

                java.net.URL url = new java.net.URL(serverUrl + "/api/firebase/health");
                java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                Log.d("LoginActivity", "Server response code: " + responseCode);

                if (responseCode == 200) {
                  java.io.BufferedReader reader =
                      new java.io.BufferedReader(
                          new java.io.InputStreamReader(connection.getInputStream()));
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = reader.readLine()) != null) {
                    response.append(line);
                  }
                  reader.close();

                  Log.d("LoginActivity", "Server response: " + response);
                }
              } catch (Exception e) {
                Log.e("LoginActivity", "Error checking server connectivity", e);
              }
            })
        .start();
  }

  /** Handles all button click events and login logic. */
  private void eventHandler() {
    btnLogin.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            SignIn();
          }

          // connection between firebase and login button
          private void SignIn() {
            String email = Objects.requireNonNull(etEmail.getText()).toString();
            String password = Objects.requireNonNull(etPassword.getText()).toString();
            if (email.matches("") || password.matches("")) {
              Toast.makeText(LoginActivity.this, "input both to login", Toast.LENGTH_SHORT).show();
            } else {
              final ProgressDialog pd =
                  ProgressDialog.show(LoginActivity.this, "connecting", "please wait... ", true);
              pd.show();

              mAuth
                  .signInWithEmailAndPassword(email, password)
                  .addOnCompleteListener(
                      LoginActivity.this,
                      task -> {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                          // Set user session using AuthHelper
                          AuthHelper.setCurrentUserSession(LoginActivity.this, email);

                          SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                          SharedPreferences.Editor editor = settings.edit();
                          editor.putBoolean(IS_CHECKED, cbRememberMe.isChecked());
                          editor.apply();

                          Toast.makeText(
                                  LoginActivity.this, "Connected Successfully", Toast.LENGTH_SHORT)
                              .show();
                          Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                          btnAbout.clearAnimation();
                          startActivity(intent);
                          finish();
                        } else {
                          // Log the error details
                          Log.e("LoginActivity", "Authentication failed", task.getException());
                          String errorMessage =
                              task.getException() != null
                                  ? task.getException().getMessage()
                                  : "Unknown error";
                          Log.e("LoginActivity", "Error details: " + errorMessage);

                          Toast.makeText(
                                  LoginActivity.this,
                                  "Invalid Email or Password",
                                  Toast.LENGTH_SHORT)
                              .show();
                          btnResetPass.setVisibility(View.VISIBLE);
                        }
                      });
            }
          }
        });

    // Google Sign In button click listener
    btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

    // Press Here Onclick
    btnPress.setOnClickListener(
        view -> {
          // when click on "press here" it takes you to RegisterActivity
          Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
          btnAbout.clearAnimation();
          startActivity(i);
          overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        });
    btnResetPass.setOnClickListener(
        v -> {
          Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
          btnAbout.clearAnimation();
          startActivity(i);
        });
    btnAbout.setOnClickListener(
        v -> {
          Intent i = new Intent(LoginActivity.this, IntroActivity.class);
          startActivity(i);
        });
  }

  /** Initiates Google sign-in flow. */
  private void signInWithGoogle() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  /** Handles the result of Google sign-in intent. */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        GoogleSignInAccount account = task.getResult(ApiException.class);
        firebaseAuthWithGoogle(account.getIdToken());
      } catch (ApiException e) {
        Toast.makeText(
                LoginActivity.this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT)
            .show();
      }
    }
  }

  /**
   * Authenticates with Firebase using Google ID token.
   *
   * @param idToken the Google ID token
   */
  private void firebaseAuthWithGoogle(String idToken) {
    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
    mAuth
        .signInWithCredential(credential)
        .addOnCompleteListener(
            this,
            task -> {
              if (task.isSuccessful()) {
                // Sign in success
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                  String email = firebaseUser.getEmail();
                  String username = firebaseUser.getDisplayName();

                  // Use a Map instead of User object to avoid Firebase serialization issues
                  Map<String, Object> userData = new HashMap<>();
                  userData.put("username", username);
                  userData.put("email", email);
                  userData.put("userKey", java.util.UUID.randomUUID().toString());

                  // Replace dots with spaces in the email to make it a valid key in Firebase
                  assert email != null;
                  DBRef.refUsers.child(email.replace('.', ' ')).setValue(userData);
                }

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
              } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(LoginActivity.this, task.toString(), Toast.LENGTH_LONG).show();
              }
            });
  }
}
