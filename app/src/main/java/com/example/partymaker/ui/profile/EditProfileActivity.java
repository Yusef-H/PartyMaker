package com.example.partymaker.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.R;
import com.example.partymaker.data.api.AppNetworkError;
import com.example.partymaker.data.api.ConnectivityManager;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.settings.ServerSettingsActivity;
import com.example.partymaker.utilities.AuthHelper;
import com.example.partymaker.utilities.BottomNavigationHelper;
import com.example.partymaker.viewmodel.UserViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

  private ImageView imgProfile;
  private EditText etUsername;
  private Button btnSaveProfile;
  private ProgressBar progressBar;
  private UserViewModel userViewModel;
  private View rootLayout;

  private final ActivityResultLauncher<String> imagePickerLauncher =
      registerForActivityResult(
          new ActivityResultContracts.GetContent(), this::uploadImageToFirebase);
  private static final String TAG = "EditProfileActivity";
  private static final String ACTION_BAR_START_COLOR = "#0E81D1";
  private static final String ACTION_BAR_END_COLOR = "#0E81D1";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_edit_profile);

    // Initialize ViewModel
    userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    userViewModel.setAppContext(getApplicationContext());

    // Hide action bar to remove black bar at top
    androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    setupActionBar();
    initViews();
    setupObservers();
    setListeners();
    setupBottomNavigation();

    // Load current user data with force refresh
    loadUserData();
  }

  // Sets up the action bar with custom gradient background and styling.
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      Log.w(TAG, "ActionBar not available");
      return;
    }

    try {
      GradientDrawable gradient = createActionBarGradient();
      actionBar.setBackgroundDrawable(gradient);

      String styledTitle = createStyledTitle();
      actionBar.setTitle(Html.fromHtml(styledTitle, Html.FROM_HTML_MODE_LEGACY));

      configureActionBarProperties(actionBar);

      Log.d(TAG, "ActionBar setup completed");

    } catch (Exception e) {
      Log.e(TAG, "Error setting up ActionBar", e);
    }
  }

  // Creates a gradient drawable for the action bar background.
  private GradientDrawable createActionBarGradient() {
    GradientDrawable gradient = new GradientDrawable();
    gradient.setShape(GradientDrawable.RECTANGLE);
    gradient.setColors(
        new int[] {
          Color.parseColor(ACTION_BAR_START_COLOR), Color.parseColor(ACTION_BAR_END_COLOR)
        });
    gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
    return gradient;
  }

  // Creates a styled HTML title string.
  private String createStyledTitle() {
    return String.format(
        "<font color='%s'><b>%s</b></font>",
        EditProfileActivity.ACTION_BAR_TITLE_COLOR, "Edit Profile");
  }

  // Configures action bar properties.
  private void configureActionBarProperties(ActionBar actionBar) {
    actionBar.setElevation(ACTION_BAR_ELEVATION);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(false);
  }

  private void initViews() {
    imgProfile = findViewById(R.id.imgProfile);
    etUsername = findViewById(R.id.etUsername);
    btnSaveProfile = findViewById(R.id.btnSaveProfile);
    progressBar = findViewById(R.id.progressBar);
    rootLayout = findViewById(R.id.rootLayout);
  }

  private void setupObservers() {
    // Observe current user
    userViewModel
        .getCurrentUser()
        .observe(
            this,
            user -> {
              if (user != null) {
                updateUI(user);
              }
            });

    // Observe loading state
    userViewModel
        .getIsLoading()
        .observe(
            this,
            isLoading -> {
              progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
              btnSaveProfile.setEnabled(!isLoading);
            });

    // Observe error messages
    userViewModel
        .getErrorMessage()
        .observe(
            this,
            error -> {
              if (error != null && !error.isEmpty()) {
                showError(error);
                userViewModel.clearError();
              }
            });

    // Observe success messages
    userViewModel
        .getSuccessMessage()
        .observe(
            this,
            success -> {
              if (success != null && !success.isEmpty()) {
                showSuccess(success);
                userViewModel.clearSuccess();
              }
            });

    // Observe network error type
    userViewModel
        .getNetworkErrorType()
        .observe(
            this,
            errorType -> {
              if (errorType != null) {
                handleNetworkError(errorType);
              }
            });

    // Observe network availability
    ConnectivityManager.getInstance()
        .getNetworkAvailability()
        .observe(
            this,
            isAvailable -> {
              // Only show network restored message if:
              // 1. Network is available now
              // 2. We're not in the onResume method
              // 3. We previously had a network outage
              if (isAvailable && !isResuming && wasOffline) {
                showNetworkRestored();
                wasOffline = false;
              } else if (!isAvailable) {
                // Mark that we were offline
                wasOffline = true;
              }
            });
  }

  private void setListeners() {
    imgProfile.setOnClickListener(
        v -> {
          // Check network availability before launching image picker
          if (Boolean.FALSE.equals(
              ConnectivityManager.getInstance().getNetworkAvailability().getValue())) {
            AppNetworkError.showErrorMessage(
                this, NetworkUtils.ErrorType.NO_NETWORK, "Network not available", false);
            return;
          }

          imagePickerLauncher.launch("image/*");
        });

    btnSaveProfile.setOnClickListener(v -> saveUserProfile());
  }

  private void loadUserData() {
    // Show loading indicator
    progressBar.setVisibility(View.VISIBLE);

    // Check if we're offline
    boolean isOffline =
        Boolean.FALSE.equals(ConnectivityManager.getInstance().getNetworkAvailability().getValue());

    // If offline, don't show error messages
    if (isOffline) {
      // Try to load from local cache
      try {
        String userKey = AuthHelper.getCurrentUserKey(this);
        if (!userKey.isEmpty()) {
          // Load profile image from cache if available
          loadProfileImageFromStorage(userKey);

          // Try to get cached user data
          User cachedUser = userViewModel.getCurrentUser().getValue();
          if (cachedUser != null) {
            updateUI(cachedUser);
          }
        }
      } catch (AuthHelper.AuthException e) {
        Log.e(TAG, "Authentication error in offline mode", e);
        // Don't show error in offline mode
      }
      progressBar.setVisibility(View.GONE);
      return;
    }

    // Force refresh from server to ensure we have the latest data
    userViewModel.loadCurrentUser(this, true);

    // Direct load from Firebase as backup
    try {
      String userKey = AuthHelper.getCurrentUserKey(this);
      if (!userKey.isEmpty()) {
        DBRef.refUsers
            .child(userKey)
            .get()
            .addOnSuccessListener(
                dataSnapshot -> {
                  if (dataSnapshot.exists()) {
                    // Use Map instead of User.class to avoid Firebase serialization issues
                    Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                    if (userData != null) {
                      // Update UI with user data
                      String username = "";
                      // Check for both username and userName fields
                      if (userData.containsKey("username")) {
                        username = (String) userData.get("username");
                      } else if (userData.containsKey("userName")) {
                        username = (String) userData.get("userName");
                      }

                      if (username != null && !username.isEmpty()) {
                        etUsername.setText(username);
                      }

                      // Load profile image
                      loadProfileImageFromStorage(userKey);
                    }
                  }
                })
            .addOnFailureListener(
                e -> Log.e(TAG, "Error loading user data directly", e));
      }
    } catch (AuthHelper.AuthException e) {
      Log.e(TAG, "Authentication error", e);
      showError("Authentication error: " + e.getMessage());
      progressBar.setVisibility(View.GONE);
    } catch (Exception e) {
      Log.e(TAG, "Error getting current user key", e);
      showError("Error: " + e.getMessage());
      progressBar.setVisibility(View.GONE);
    }
  }

  private void updateUI(User user) {
    if (user == null) {
      Log.e(TAG, "Cannot update UI: user is null");
      return;
    }

    Log.d(TAG, "Updating UI with user: " + user.getUsername() + ", key: " + user.getUserKey());

    // Update username field
    if (user.getUsername() != null) {
      etUsername.setText(user.getUsername());
    }

    // Load profile image
    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
      Log.d(TAG, "Loading profile image from URL: " + user.getProfileImageUrl());
      Picasso.get()
          .load(user.getProfileImageUrl())
          .placeholder(R.drawable.ic_profile)
          .error(R.drawable.ic_profile)
          .into(imgProfile);
    } else {
      Log.d(TAG, "No profile image URL, loading from storage for key: " + user.getUserKey());
      loadProfileImageFromStorage(user.getUserKey());
    }
  }

  private void saveUserProfile() {
    String username = etUsername.getText().toString().trim();

    if (username.isEmpty()) {
      etUsername.setError("Username cannot be empty");
      return;
    }

    // Show loading indicator
    progressBar.setVisibility(View.VISIBLE);

    // Force refresh network status before checking
    ConnectivityManager.getInstance().refreshNetworkStatus();

    // Add a small delay to allow the network status to update
    new Handler()
        .postDelayed(
            () -> {
              // Check network availability again after refresh
              boolean isNetworkAvailable =
                  ConnectivityManager.getInstance().getNetworkAvailability().getValue() != null
                      && ConnectivityManager.getInstance().getNetworkAvailability().getValue();

              if (!isNetworkAvailable) {
                Log.e(TAG, "Network not available when trying to save profile");
                progressBar.setVisibility(View.GONE);

                // Show a more helpful error message
                Snackbar.make(
                        findViewById(android.R.id.content),
                        "Cannot save profile while offline. Please check your internet connection.",
                        Snackbar.LENGTH_LONG)
                    .show();
                return;
              }

              // Create updates map
              Map<String, Object> updates = new HashMap<>();
              updates.put("username", username);

              // Update the user profile
              userViewModel.updateCurrentUser(updates);
            },
            500); // 500ms delay to allow network status to update
  }

  private void loadProfileImageFromStorage(String userKey) {
    Log.d(TAG, "Loading profile image from storage for key: " + userKey);

    // Force refresh network status
    ConnectivityManager.getInstance().refreshNetworkStatus();

    // Try to load from local cache first
    try {
      // Check if we have a locally cached image
      String localCachePath = getFilesDir() + "/profile_" + userKey + ".jpg";
      java.io.File localFile = new java.io.File(localCachePath);

      if (localFile.exists()) {
        // Load from local cache
        Picasso.get()
            .load(localFile)
            .placeholder(R.drawable.default_profile_image)
            .error(R.drawable.default_profile_image)
            .into(imgProfile);
        Log.d(TAG, "Loaded profile image from local cache");
        return; // Exit early if we loaded from cache
      }

      // No local cache, try to load from Firebase Storage
      boolean isNetworkAvailable =
          ConnectivityManager.getInstance().getNetworkAvailability().getValue() != null
              && ConnectivityManager.getInstance().getNetworkAvailability().getValue();
      if (!isNetworkAvailable) {
        // If offline, just use the default image without showing error
        imgProfile.setImageResource(R.drawable.default_profile_image);
        Log.d(TAG, "Using default profile image (offline mode)");
        return;
      }

      // Try multiple possible paths for the image
      tryLoadImageFromPath(userKey, localCachePath, 0);

    } catch (Exception e) {
      Log.e(TAG, "Error loading profile image", e);
      imgProfile.setImageResource(R.drawable.default_profile_image);
    }
  }

  /**
   * Try loading the profile image from different paths
   *
   * @param userKey The user key
   * @param localCachePath The local cache path
   * @param pathIndex The index of the path to try
   */
  private void tryLoadImageFromPath(String userKey, String localCachePath, int pathIndex) {
    // Define possible paths
    String[] paths =
        new String[] {
          // Path 1: UsersImageProfile/Users/[userKey] (without extension)
          "UsersImageProfile/Users/" + userKey,
          // Path 2: UsersImageProfile/Users/[userKey].jpg
          "UsersImageProfile/Users/" + userKey + ".jpg",
          // Path 3: Users/[userKey] (without extension)
          "Users/" + userKey,
          // Path 4: Users/[userKey].jpg
          "Users/" + userKey + ".jpg",
          // Path 5: [userKey] (root level, without extension)
          userKey,
          // Path 6: [userKey].jpg (root level)
          userKey + ".jpg"
        };

    // If we've tried all paths, give up
    if (pathIndex >= paths.length) {
      Log.w(TAG, "All image paths failed, using default image");
      imgProfile.setImageResource(R.drawable.default_profile_image);
      return;
    }

    String currentPath = paths[pathIndex];
    Log.d(TAG, "Trying to load image from path: " + currentPath);

    FirebaseStorage.getInstance()
        .getReference()
        .child(currentPath)
        .getDownloadUrl()
        .addOnSuccessListener(
            uri -> {
              Log.d(TAG, "Successfully got download URL from path: " + currentPath);
              // Load with Picasso
              Picasso.get()
                  .load(uri)
                  .placeholder(R.drawable.default_profile_image)
                  .error(R.drawable.default_profile_image)
                  .into(imgProfile);

              // Save to local cache for offline use
              downloadAndSaveImage(uri.toString(), localCachePath);

              Log.d(TAG, "Loaded profile image from Firebase Storage");
            })
        .addOnFailureListener(
            e -> {
              Log.w(
                  TAG,
                  "Failed to load image from path: " + currentPath + ", error: " + e.getMessage());
              // Try the next path
              tryLoadImageFromPath(userKey, localCachePath, pathIndex + 1);
            });
  }

  /**
   * Downloads and saves an image to local storage for offline use
   *
   * @param imageUrl The URL of the image to download
   * @param localPath The local path to save the image to
   */
  private void downloadAndSaveImage(String imageUrl, String localPath) {
    new Thread(
            () -> {
              try {
                // Download the image
                java.net.URL url = new java.net.URL(imageUrl);
                java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                java.io.InputStream input = connection.getInputStream();

                // Save to local file
                java.io.FileOutputStream output = new java.io.FileOutputStream(localPath);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                  output.write(buffer, 0, bytesRead);
                }
                output.close();
                input.close();

                Log.d(TAG, "Profile image saved to local cache: " + localPath);
              } catch (Exception e) {
                Log.e(TAG, "Error saving image to local cache", e);
              }
            })
        .start();
  }

  private void uploadImageToFirebase(Uri uri) {
    if (uri == null) return;

    // Check network availability
    boolean isNetworkAvailable =
        ConnectivityManager.getInstance().getNetworkAvailability().getValue() != null
            && ConnectivityManager.getInstance().getNetworkAvailability().getValue();
    if (!isNetworkAvailable) {
      AppNetworkError.showErrorMessage(
          this,
          NetworkUtils.ErrorType.NO_NETWORK,
          "Network not available. Cannot upload image.",
          false);
      return;
    }

    // Show a loading indicator
    progressBar.setVisibility(View.VISIBLE);

    String userKey;
    try {
      // Get user key directly from AuthHelper instead of relying on ViewModel
      userKey = AuthHelper.getCurrentUserKey(this);
      if (userKey.isEmpty()) {
        showError("Authentication error. Please login again.");
        progressBar.setVisibility(View.GONE);
        return;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error getting current user key", e);
      showError("Authentication error: " + e.getMessage());
      progressBar.setVisibility(View.GONE);
      return;
    }

    Log.d(TAG, "Uploading image for user key: " + userKey);

    // Set the image immediately for better UX
    imgProfile.setImageURI(uri);

    // Use the correct path for the image - without extension
    String imagePath = "UsersImageProfile/Users/" + userKey;
    Log.d(TAG, "Uploading to Firebase Storage at path: " + imagePath);

    FirebaseStorage.getInstance()
        .getReference()
        .child(imagePath)
        .putFile(uri)
        .addOnSuccessListener(
            taskSnapshot -> {
              Log.d(TAG, "Image uploaded successfully to path: " + imagePath);

              // Get the download URL and update the user profile
              FirebaseStorage.getInstance()
                  .getReference()
                  .child(imagePath)
                  .getDownloadUrl()
                  .addOnSuccessListener(
                      downloadUri -> {
                        Log.d(
                            TAG,
                            "Successfully uploaded image and got download URL: " + downloadUri);

                        // Update the profile image URL in the database
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImageUrl", downloadUri.toString());

                        // Update directly in Firebase
                        DBRef.refUsers
                            .child(userKey)
                            .updateChildren(updates)
                            .addOnSuccessListener(
                                aVoid -> {
                                  Log.d(TAG, "Profile image URL updated in database");
                                  progressBar.setVisibility(View.GONE);
                                  Toast.makeText(
                                          this, "Profile picture updated", Toast.LENGTH_SHORT)
                                      .show();

                                  // Also update in ViewModel to keep UI in sync
                                  userViewModel.updateCurrentUser(updates);

                                  // Save to local cache for offline use
                                  String localCachePath =
                                      getFilesDir() + "/profile_" + userKey + ".jpg";
                                  downloadAndSaveImage(downloadUri.toString(), localCachePath);
                                })
                            .addOnFailureListener(
                                e -> {
                                  Log.e(TAG, "Error updating profile image URL", e);
                                  progressBar.setVisibility(View.GONE);
                                  showError("Error updating profile: " + e.getMessage());
                                });
                      })
                  .addOnFailureListener(
                      e -> {
                        Log.e(TAG, "Error getting download URL: " + e.getMessage(), e);
                        progressBar.setVisibility(View.GONE);
                        showError("Error getting download URL: " + e.getMessage());
                      });
            })
        .addOnFailureListener(
            e -> {
              Log.e(TAG, "Error uploading image: " + e.getMessage(), e);
              progressBar.setVisibility(View.GONE);
              showError("Error uploading image: " + e.getMessage());

              // Try alternative path if the first one fails
              tryAlternativePath(uri, userKey);
            });
  }

  /**
   * Try uploading the image to an alternative path
   *
   * @param uri The image URI
   * @param userKey The user key
   */
  private void tryAlternativePath(Uri uri, String userKey) {
    // Try a simpler path without extension
    String alternativePath = "Users/" + userKey;
    Log.d(TAG, "Trying alternative upload path: " + alternativePath);

    // Show loading indicator again
    progressBar.setVisibility(View.VISIBLE);

    FirebaseStorage.getInstance()
        .getReference()
        .child(alternativePath)
        .putFile(uri)
        .addOnSuccessListener(
            taskSnapshot -> {
              Log.d(TAG, "Image uploaded successfully to alternative path: " + alternativePath);

              // Get the download URL and update the user profile
              FirebaseStorage.getInstance()
                  .getReference()
                  .child(alternativePath)
                  .getDownloadUrl()
                  .addOnSuccessListener(
                      downloadUri -> {
                        Log.d(
                            TAG,
                            "Successfully got download URL from alternative path: " + downloadUri);

                        // Update the profile image URL in the database
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImageUrl", downloadUri.toString());

                        // Update directly in Firebase
                        DBRef.refUsers
                            .child(userKey)
                            .updateChildren(updates)
                            .addOnSuccessListener(
                                aVoid -> {
                                  Log.d(TAG, "Profile image URL updated in database");
                                  progressBar.setVisibility(View.GONE);
                                  Toast.makeText(
                                          this, "Profile picture updated", Toast.LENGTH_SHORT)
                                      .show();

                                  // Also update in ViewModel to keep UI in sync
                                  userViewModel.updateCurrentUser(updates);

                                  // Save to local cache for offline use
                                  String localCachePath =
                                      getFilesDir() + "/profile_" + userKey + ".jpg";
                                  downloadAndSaveImage(downloadUri.toString(), localCachePath);
                                })
                            .addOnFailureListener(
                                e -> {
                                  Log.e(TAG, "Error updating profile image URL", e);
                                  progressBar.setVisibility(View.GONE);
                                  showError("Error updating profile: " + e.getMessage());
                                });
                      })
                  .addOnFailureListener(
                      e -> {
                        Log.e(
                            TAG,
                            "Error getting download URL from alternative path: " + e.getMessage(),
                            e);
                        progressBar.setVisibility(View.GONE);
                        showError("Error getting download URL: " + e.getMessage());
                      });
            })
        .addOnFailureListener(
            e -> {
              Log.e(TAG, "Error uploading image to alternative path: " + e.getMessage(), e);
              progressBar.setVisibility(View.GONE);
              showError("Error uploading image: " + e.getMessage());
            });
  }

  private void setupBottomNavigation() {
    BottomNavigationHelper.setupBottomNavigation(this);
  }

  private void showError(String message) {
    if (rootLayout != null) {
      Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
    } else {
      Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
  }

  private void showSuccess(String message) {
    if (rootLayout != null) {
      Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
  }

  private void handleNetworkError(NetworkUtils.ErrorType errorType) {
    if (errorType == NetworkUtils.ErrorType.NO_NETWORK) {
      // Don't show error toast for no network, just show a small indicator
      // Use a snackbar instead of a toast to be less intrusive
      View view = findViewById(android.R.id.content);
      if (view != null) {
        Snackbar snackbar =
            Snackbar.make(view, "Offline mode - limited functionality", Snackbar.LENGTH_SHORT);
        snackbar.show();
      }

      // Disable save button in offline mode
      btnSaveProfile.setEnabled(false);
    } else {
      // For other errors, use the standard error handler but with minimal UI
      AppNetworkError.handleNetworkError(this, null, errorType, true);
    }
  }

  private void showNetworkRestored() {
    // Only show network restored message if we're not in onResume
    // to avoid showing it every time the activity resumes
    if (rootLayout != null && !isResuming) {
      Snackbar.make(rootLayout, "Network connection restored", Snackbar.LENGTH_SHORT).show();
    }
  }

  // Flag to track if we're in onResume
  private boolean isResuming = false;

  // Flag to track if we were previously offline
  private boolean wasOffline = false;

  @Override
  protected void onResume() {
    super.onResume();

    isResuming = true;

    // Force refresh network status when activity resumes
    ConnectivityManager.getInstance().refreshNetworkStatus();

    // Add a small delay to allow the network status to update
    new Handler()
        .postDelayed(
            () -> {
              // Check if we need to reload user data based on network availability
              boolean isNetworkAvailable =
                  ConnectivityManager.getInstance().getNetworkAvailability().getValue() != null
                      && ConnectivityManager.getInstance().getNetworkAvailability().getValue();

              if (isNetworkAvailable) {
                Log.d(TAG, "Network is available, refreshing user data");
                loadUserData();
              }

              isResuming = false;
            },
            1000);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      Intent intent = new Intent(EditProfileActivity.this, ServerSettingsActivity.class);
      startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.logout) {
      AuthHelper.logout(this);
      Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
      startActivity(intent);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
