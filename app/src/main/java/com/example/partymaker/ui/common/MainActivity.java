package com.example.partymaker.ui.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.GroupAdapter;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.chatbot.GptChatActivity;
import com.example.partymaker.ui.group.CreateGroupActivity;
import com.example.partymaker.ui.group.PartyMainActivity;
import com.example.partymaker.ui.group.PublicGroupsActivity;
import com.example.partymaker.ui.profile.EditProfileActivity;
import com.example.partymaker.ui.settings.ServerSettingsActivity;
import com.example.partymaker.utilities.AuthHelper;
import com.example.partymaker.utilities.BottomNavigationHelper;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.example.partymaker.viewmodel.GroupViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  // Constants
  private static final String TAG = "MainActivity";
  private static final String ACTION_BAR_START_COLOR = "#0E81D1";
  private static final String ACTION_BAR_END_COLOR = "#0E81D1";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;
  private static final String GROUPS_DB_PATH = "Groups";

  // UI Components
  private ListView lv1;
  private FloatingActionButton fabChat;

  // Data Components
  private GroupViewModel viewModel;
  private ArrayList<Group> groupList = new ArrayList<>();
  private String UserKey;
  private GroupAdapter groupAdapter;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      setContentView(R.layout.activity_main);

      // Force set server URL to Render
      forceSetServerUrl();

      if (!initializeUser()) {
        return; // Exit if user initialization failed
      }

      // Initialize ViewModel
      viewModel = new ViewModelProvider(this).get(GroupViewModel.class);
      
      initializeViews();
      setupActionBar();
      setupEventHandlers();
      setupFloatingChatButton();
      setupBottomNavigation();
      
      // Observe group data from ViewModel
      observeViewModel();
      
      // Show loading indicator
      showLoading(true);
      
      // Load groups for current user
      viewModel.loadUserGroups(UserKey, true);
    } catch (Exception e) {
      Log.e(TAG, "Fatal error in onCreate", e);
      showError("An unexpected error occurred. Please restart the app.");
    }
  }
  
  /**
   * Shows or hides a loading indicator
   * 
   * @param show True to show loading, false to hide
   */
  private void showLoading(boolean show) {
    try {
      // Simple implementation without dedicated loading view
      if (lv1 != null) {
        lv1.setVisibility(show ? View.GONE : View.VISIBLE);
      }
      
      // Show a toast if loading starts
      if (show) {
        Toast.makeText(this, "Loading groups...", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      Log.e(TAG, "Error toggling loading state", e);
    }
  }

  /**
   * Forces the server URL to be set to Render
   */
  private void forceSetServerUrl() {
    try {
      String renderUrl = "https://partymaker.onrender.com";
      androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
          .edit()
          .putString("server_url", renderUrl)
          .apply();
      Log.d(TAG, "Forced server URL to: " + renderUrl);
    } catch (Exception e) {
      Log.e(TAG, "Error setting server URL", e);
      // Non-critical error, continue execution
    }
  }

  /**
   * Initializes and validates the current user.
   *
   * @return true if user is valid and initialized, false otherwise
   */
  private boolean initializeUser() {
    try {
      String userEmail = AuthHelper.getCurrentUserEmail(this);
      if (userEmail != null) {
        UserKey = userEmail.replace('.', ' ');
        Log.d(TAG, "User initialized successfully: " + UserKey);
        return true;
      } else {
        Log.e(TAG, "User not logged in or email is null");
        showError("Authentication error. Please login again.");
        navigateToLogin();
        return false;
      }

    } catch (Exception e) {
      Log.e(TAG, "Error initializing user", e);
      showError("User initialization failed");
      navigateToLogin();
      return false;
    }
  }

  // Initializes all view components.
  private void initializeViews() {
    lv1 = findViewById(R.id.lv1);
    fabChat = findViewById(R.id.fabChat);

    if (lv1 == null) {
      Log.e(TAG, "Critical view lv1 not found");
      showError("UI initialization failed");
      finish();
      return;
    }

    // Initialize the adapter with the groupList
    groupAdapter = new GroupAdapter(MainActivity.this, 0, 0, groupList);
    lv1.setAdapter(groupAdapter);
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
        "<font color='%s'><b>%s</b></font>", MainActivity.ACTION_BAR_TITLE_COLOR, "My Parties");
  }

  // Configures action bar properties.
  private void configureActionBarProperties(ActionBar actionBar) {
    actionBar.setElevation(ACTION_BAR_ELEVATION);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(false);
  }

  // Observes LiveData from the ViewModel
  private void observeViewModel() {
    try {
      // Observe group list
      viewModel.getGroups().observe(this, groups -> {
        try {
          if (groups != null) {
            groupList.clear();
            groupList.addAll(groups);
            groupAdapter.notifyDataSetChanged();
            Log.d(TAG, "Group list updated with " + groups.size() + " groups");
            
            // Hide loading indicator
            showLoading(false);
            
            // Show empty state if needed
            if (groups.isEmpty()) {
              showEmptyState();
            }
          } else {
            Log.w(TAG, "Received null groups list from ViewModel");
            showEmptyState();
            showLoading(false);
          }
        } catch (Exception e) {
          Log.e(TAG, "Error processing groups data", e);
          showError("Error displaying groups");
          showLoading(false);
        }
      });
      
      // Observe loading state
      viewModel.getIsLoading().observe(this, isLoading -> {
        try {
          showLoading(isLoading);
        } catch (Exception e) {
          Log.e(TAG, "Error updating loading state", e);
        }
      });
      
      // Observe error messages
      viewModel.getErrorMessage().observe(this, errorMsg -> {
        try {
          if (errorMsg != null && !errorMsg.isEmpty()) {
            showError(errorMsg);
            viewModel.clearError(); // Clear the error after showing it
            showLoading(false);
          }
        } catch (Exception e) {
          Log.e(TAG, "Error displaying error message", e);
        }
      });
    } catch (Exception e) {
      Log.e(TAG, "Error setting up observers", e);
      showError("Error initializing data observers");
    }
  }

  // Sets up all event handlers for UI components.
  private void setupEventHandlers() {
    try {
      if (lv1 != null) {
        lv1.setOnItemClickListener(
            (parent, view, position, id) -> {
              try {
                if (isValidPosition(position)) {
                  Group selectedGroup = groupList.get(position);
                  Log.d(
                      TAG,
                      "Group clicked: "
                          + selectedGroup.getGroupName()
                          + ", key: "
                          + selectedGroup.getGroupKey()
                          + ", adminKey: "
                          + selectedGroup.getAdminKey()
                          + ", messageKeys size: "
                          + (selectedGroup.getMessageKeys() != null
                              ? selectedGroup.getMessageKeys().size()
                              : "null"));
                  navigateToGroupScreen(selectedGroup);
                } else {
                  Log.w(TAG, "Invalid position clicked: " + position);
                }
              } catch (Exception e) {
                Log.e(TAG, "Error handling item click", e);
                showError("Error opening group");
              }
            });
      }
    } catch (Exception e) {
      Log.e(TAG, "Error setting up event handlers", e);
    }
  }

  // Sets up the floating chat button with click and touch handlers.
  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingChatButton() {
    if (fabChat == null) {
      Log.w(TAG, "FloatingActionButton not found");
      return;
    }

    fabChat.setOnClickListener(view -> navigateToChat());
    fabChat.setOnTouchListener(Common::dragChatButtonOnTouch);
  }

  private void setupBottomNavigation() {
    BottomNavigationHelper.setupBottomNavigation(this, "myparties");
  }

  // Navigates to the chat activity.
  private void navigateToChat() {
    try {
      Intent intent = new Intent(MainActivity.this, GptChatActivity.class);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Error navigating to chat", e);
      showError("Failed to open chat");
    }
  }

  // Navigates to the group screen with all necessary data.
  private void navigateToGroupScreen(Group group) {
    if (group == null) {
      Log.e(TAG, "Cannot navigate to null group");
      return;
    }

    try {
      Log.d(
          TAG,
          "Attempting to navigate to PartyMainActivity for group: "
              + group.getGroupName()
              + " with key: "
              + group.getGroupKey());
      Intent intent = new Intent(getBaseContext(), PartyMainActivity.class);
      
      // Add GroupKey directly to intent
      intent.putExtra("GroupKey", group.getGroupKey());
      Log.d(TAG, "Added GroupKey to intent: " + group.getGroupKey());
      
      // Also add ExtrasMetadata for backward compatibility
      ExtrasMetadata extras = createExtrasFromGroup(group);
      Common.addExtrasToIntent(intent, extras);

      // Add UserKey to intent
      intent.putExtra("UserKey", UserKey);
      Log.d(TAG, "Added UserKey to intent: " + UserKey);

      Log.d(TAG, "Starting PartyMainActivity...");
      startActivity(intent);
      Log.d(TAG, "PartyMainActivity started successfully");

    } catch (Exception e) {
      Log.e(TAG, "Error navigating to group screen", e);
      showError("Failed to open group: " + e.getMessage());
    }
  }

  // Creates ExtrasMetadata object from Group data.
  private ExtrasMetadata createExtrasFromGroup(Group group) {
    return new ExtrasMetadata(
        group.getGroupName(),
        group.getGroupKey(),
        group.getGroupDays(),
        group.getGroupMonths(),
        group.getGroupYears(),
        group.getGroupHours(),
        group.getGroupLocation(),
        group.getAdminKey(),
        group.getCreatedAt(),
        group.getGroupPrice(),
        group.getGroupType(),
        group.isCanAdd(),
        group.getFriendKeys(),
        group.getComingKeys(),
        group.getMessageKeys());
  }

  // Shows an empty state when no groups are available
  private void showEmptyState() {
    try {
      runOnUiThread(
          () -> {
            if (lv1 != null && groupList != null && groupList.isEmpty()) {
              Toast.makeText(this, "No groups found", Toast.LENGTH_LONG).show();
            }
          });
    } catch (Exception e) {
      Log.e(TAG, "Error showing empty state", e);
    }
  }

  // Checks if the current user is a member of the given group.
  private boolean isUserInGroup(Group group) {
    HashMap<String, Object> userKeys = group.getFriendKeys();
    if (userKeys == null || UserKey == null) {
      return false;
    }

    return userKeys.containsKey(UserKey);
  }

  // Sorts groups by creation date and updates the adapter.
  private void sortAndDisplayGroups() {
    if (groupList != null) {
      // Filter out groups with null createdAt and sort the rest
      groupList.removeIf(group -> group.getCreatedAt() == null);
      if (!groupList.isEmpty()) {
        groupList.sort(Comparator.comparing(Group::getCreatedAt));
      }
      GroupAdapter allGroupsAdapter = new GroupAdapter(MainActivity.this, 0, 0, groupList);
      lv1.setAdapter(allGroupsAdapter);
    }
  }

  // Validates if the given position is within bounds.
  private boolean isValidPosition(int position) {
    return groupList != null && position >= 0 && position < groupList.size();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    Intent goToNextActivity;

    if (itemId == R.id.settings) {
      goToNextActivity = new Intent(getApplicationContext(), ServerSettingsActivity.class);
      startActivity(goToNextActivity);
    } else if (itemId == R.id.logout) {
      AuthHelper.clearAuthData(this);
      goToNextActivity = new Intent(getApplicationContext(), LoginActivity.class);
      startActivity(goToNextActivity);
      finish();
    }

    return true;
  }

  // Navigates to the specified activity class.
  private void navigateToActivity(Class<?> activityClass) {
    Intent intent = new Intent(getApplicationContext(), activityClass);
    startActivity(intent);
  }

  // Handles user logout process
  private void handleLogout() {
    try {
      AuthHelper.clearAuthData(this);
      navigateToLogin();
      Log.d(TAG, "User logged out successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error during logout", e);
      showError("Logout failed");
    }
  }

  // Navigates to the login activity and finishes current activity.
  private void navigateToLogin() {
    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
    startActivity(intent);
    finish();
  }

  /**
   * Shows an error message to the user.
   *
   * @param message The error message to display
   */
  private void showError(String message) {
    try {
      Toast.makeText(this, message, Toast.LENGTH_LONG).show();
      Log.e(TAG, "Error shown to user: " + message);
    } catch (Exception e) {
      Log.e(TAG, "Error showing error message", e);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  // Update the UI with the current group list
  private void updateUI() {
    if (groupList != null) {
      // Filter out groups with null createdAt and sort the rest
      groupList.removeIf(group -> group.getCreatedAt() == null);
      if (!groupList.isEmpty()) {
        groupList.sort(Comparator.comparing(Group::getCreatedAt));
      }
      GroupAdapter allGroupsAdapter = new GroupAdapter(MainActivity.this, 0, 0, groupList);
      lv1.setAdapter(allGroupsAdapter);
    }
  }
}
