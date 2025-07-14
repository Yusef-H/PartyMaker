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
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
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
  private Object databaseRef;
  private ArrayList<Group> groupList = new ArrayList<>();
  private String UserKey;
  private ArrayAdapter<Group> groupAdapter;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (!initializeUser()) {
      return; // Exit if user initialization failed
    }

    initializeViews();
    setupActionBar();
    initializeDatabase();
    setupEventHandlers();
    setupFloatingChatButton();
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

  // Initializes the Firebase database reference and retrieves data.
  private void initializeDatabase() {
    try {
      // Server mode is always enabled
      Log.d(TAG, "Server mode enabled: true");

      // Use FirebaseAccessManager to get server client
      FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
      databaseRef = accessManager.getGroupsRef();
      retrieveGroupData();
      Log.d(TAG, "Database initialized successfully");

    } catch (Exception e) {
      Log.e(TAG, "Error initializing database", e);
      showError("Database connection failed");
    }
  }

  // Sets up all event handlers for UI components.
  private void setupEventHandlers() {
    if (lv1 != null) {
      lv1.setOnItemClickListener(
          (parent, view, position, id) -> {
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
            }
          });
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
      showError("Failed to open group");
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

  // Retrieves group data from Firebase database.
  private void retrieveGroupData() {
    Log.d(TAG, "Retrieving group data for user: " + UserKey);

    // Create FirebaseAccessManager instance
    FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
    databaseRef = accessManager.getGroupsRef();

    // Get user groups first
    if (databaseRef instanceof FirebaseServerClient) {
      FirebaseServerClient serverClient = (FirebaseServerClient) databaseRef;
      serverClient.getUserGroups(
          UserKey,
          new FirebaseServerClient.DataCallback<Map<String, Group>>() {
            @Override
            public void onSuccess(Map<String, Group> groups) {
              Log.d(TAG, "Successfully retrieved " + groups.size() + " user groups");
              if (groups != null && !groups.isEmpty()) {
                groupList.clear();
                groupList.addAll(groups.values());
                sortAndDisplayGroups();
              } else {
                Log.d(TAG, "No user groups found, retrieving all groups");
                getAllGroups();
              }
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Error retrieving user groups: " + errorMessage);
              getAllGroups();
            }
          });
    } else {
      Log.e(TAG, "Unknown database reference type");
      getAllGroups();
    }
  }

  private void getAllGroups() {
    Log.d(TAG, "Retrieving all groups");

    if (databaseRef instanceof FirebaseServerClient) {
      FirebaseServerClient serverClient = (FirebaseServerClient) databaseRef;
      serverClient.getGroups(
          new FirebaseServerClient.DataCallback<Map<String, Group>>() {
            @Override
            public void onSuccess(Map<String, Group> groups) {
              Log.d(TAG, "Successfully retrieved " + groups.size() + " groups");
              if (groups != null && !groups.isEmpty()) {
                groupList.clear();
                groupList.addAll(groups.values());
                sortAndDisplayGroups();
              } else {
                Log.d(TAG, "No groups found");
                showError("No groups found");
              }
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Error retrieving groups: " + errorMessage);
              showError("Error retrieving groups: " + errorMessage);
            }
          });
    } else {
      Log.e(TAG, "Unknown database reference type: " + databaseRef.getClass().getSimpleName());
      showError("Unsupported database mode");
    }
  }

  private void processGroups(Map<String, Group> groups) {
    // Clear existing data
    groupList.clear();

    // Add all groups to the list
    for (Map.Entry<String, Group> entry : groups.entrySet()) {
      Group group = entry.getValue();
      if (group.getGroupKey() == null) {
        group.setGroupKey(entry.getKey());
      }
      groupList.add(group);
    }

    // Sort groups by date (newest first)
    Collections.sort(
        groupList,
        (g1, g2) -> {
          try {
            Date date1 =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(g1.getCreatedAt());
            Date date2 =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(g2.getCreatedAt());
            return date2.compareTo(date1);
          } catch (Exception e) {
            return 0;
          }
        });

    // Update the UI
    runOnUiThread(
        () -> {
          if (groupAdapter != null) {
            // Use GroupAdapter instead of ArrayAdapter for proper display
            if (lv1.getAdapter() == null || !(lv1.getAdapter() instanceof GroupAdapter)) {
              groupAdapter = new GroupAdapter(MainActivity.this, 0, 0, groupList);
              lv1.setAdapter(groupAdapter);
            } else {
              groupAdapter.notifyDataSetChanged();
            }
          }

          // Show content
          if (lv1 != null) {
            lv1.setVisibility(View.VISIBLE);
          }

          // Show empty state if no groups
          if (groupList.isEmpty()) {
            showEmptyState();
          }
        });
  }

  // Show empty state when no groups are available
  private void showEmptyState() {
    runOnUiThread(
        () -> {
          if (lv1 != null) {
            // Just show a toast message since we don't have a dedicated empty state view
            Toast.makeText(this, "No groups found", Toast.LENGTH_LONG).show();
          }
        });
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

    try {
      if (itemId == R.id.idMenu) {
        navigateToActivity(MainActivity.class);
      } else if (itemId == R.id.idAddProfile) {
        navigateToActivity(CreateGroupActivity.class);
      } else if (itemId == R.id.idEditProfile) {
        navigateToActivity(EditProfileActivity.class);
      } else if (itemId == R.id.idPublicParties) {
        navigateToActivity(PublicGroupsActivity.class);
      } else if (itemId == R.id.idServerSettings) {
        navigateToActivity(ServerSettingsActivity.class);
      } else if (itemId == R.id.idLogout) {
        handleLogout();
      }
    } catch (Exception e) {
      Log.e(TAG, "Error handling menu selection", e);
      showError("Menu action failed");
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

  // Shows an error message to the user.
  private void showError(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
