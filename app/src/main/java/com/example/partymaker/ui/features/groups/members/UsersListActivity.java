package com.example.partymaker.ui.features.groups.members;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import com.example.partymaker.ui.base.BaseActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.UserAdapter;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersListActivity extends BaseActivity {
  private static final String TAG = "UsersListActivity";

  // UI constants
  private static final String ACTIVITY_TITLE = "All users";
  private static final String ACTION_BAR_COLOR = "#0081d1";
  private static final String LOADING_MESSAGE = "Loading users...";
  private static final String NO_USERS_MESSAGE = "No users found";
  private static final String ADD_FRIEND_TITLE = "Add Friend";
  private static final String ADD_TO_COMING_TITLE = "Add to Coming List";

  // Static context accessor
  public static Context contextOfApplication;

  // UI Components
  private ListView userListView;
  private FirebaseServerClient serverClient;
  private ArrayList<User> usersList;

  // Group data
  private String groupKey;
  private HashMap<String, Object> friendKeys;
  private HashMap<String, Object> comingKeys;

  public static Context getContextOfApplication() {
    return contextOfApplication;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_party_friends);
    contextOfApplication = getApplicationContext();

    // Initialize server client
    serverClient = FirebaseServerClient.getInstance();

    // Get group data from intent
    initializeGroupData();

    setupActionBar();
    userListView = findViewById(R.id.lv);

    // Get users reference using FirebaseAccessManager
    FirebaseAccessManager accessManager = new FirebaseAccessManager(this);

    loadUsersData();
    setupEventHandlers();
  }

  private void initializeGroupData() {
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
    if (extras != null) {
      groupKey = extras.getGroupKey();
      String adminKey = extras.getAdminKey();
      friendKeys = extras.getFriendKeys();
      comingKeys = extras.getComingKeys();

      Log.d(TAG, "Received GroupKey: " + groupKey);
      Log.d(TAG, "Received AdminKey: " + adminKey);
      logGroupDataInfo();
    } else {
      Log.e(TAG, "No extras received from intent");
    }
  }

  private void logGroupDataInfo() {
    if (friendKeys != null) {
      Log.d(TAG, "Received FriendKeys: " + friendKeys.size());
    } else {
      Log.d(TAG, "FriendKeys is null");
    }
  }

  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(ACTIVITY_TITLE);
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(ACTION_BAR_COLOR)));
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setupEventHandlers() {
    userListView.setOnItemClickListener(
        (parent, view, position, id) -> {
          User selectedUser = usersList.get(position);

          if (!isValidUserSelection(selectedUser)) {
            return;
          }

          showAddUserConfirmation(selectedUser);
        });
  }

  private boolean isValidUserSelection(User selectedUser) {
    if (selectedUser == null || selectedUser.getEmail() == null) {
      Toast.makeText(this, "Invalid user selected", Toast.LENGTH_SHORT).show();
      return false;
    }

    if (groupKey == null || groupKey.isEmpty()) {
      Toast.makeText(this, "No group data available", Toast.LENGTH_SHORT).show();
      return false;
    }

    return true;
  }

  private void showAddUserConfirmation(User selectedUser) {
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle(ADD_FRIEND_TITLE)
        .setMessage("Add " + selectedUser.getEmail() + " to the group?")
        .setPositiveButton("Add", (dialog, which) -> addUserToGroup(selectedUser))
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void addUserToGroup(User user) {
    if (user == null || user.getEmail() == null) {
      Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show();
      return;
    }

    // Format email for Firebase key
    final String userEmail = user.getEmail().replace('.', ' ');

    // Try to get userKey from user object first
    String userKey = user.getUserKey();

    // If userKey is null or empty, try to get it from email
    if (userKey.isEmpty()) {
      try {
        // Get current user key for comparison
        String currentUserKey = AuthenticationManager.getCurrentUserKey(this);

        // If this is the current user, use their key
        if (user.getEmail().equals(AuthenticationManager.getCurrentUserEmail(this))) {
          userKey = currentUserKey;
          Log.d(TAG, "Using current user key: " + userKey);
        } else {
          // Otherwise use email as key (common pattern in Firebase)
          userKey = userEmail;
          Log.d(TAG, "Using email as key: " + userKey);
        }
      } catch (Exception e) {
        Log.e(TAG, "Error getting user key", e);
        // Use email as fallback
        userKey = userEmail;
      }
    }

    // Ensure we have a valid user key
    if (userKey.isEmpty()) {
      Toast.makeText(this, "Could not determine user key", Toast.LENGTH_SHORT).show();
      return;
    }

    final String finalUserKey = userKey;
    Log.d(TAG, "Adding user to group: " + userEmail + " with key: " + finalUserKey);

    // Check if user is already in the group
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null) {
              Toast.makeText(UsersListActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
              return;
            }

            Log.d(TAG, "Group found: " + group.getGroupName());

            boolean alreadyInGroup = false;

            // Check if user is already in FriendKeys
            if (group.getFriendKeys() != null) {
              for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
                Log.d(TAG, "Checking friend key: " + entry.getKey() + " -> " + entry.getValue());
                if (entry.getKey().equals(userEmail)
                    || (entry.getValue() != null
                        && entry.getValue().toString().equals(finalUserKey))) {
                  alreadyInGroup = true;
                  break;
                }
              }
            }

            if (alreadyInGroup) {
              Toast.makeText(UsersListActivity.this, "User already in group", Toast.LENGTH_SHORT)
                  .show();
              return;
            }

            Log.d(TAG, "Updating FriendKeys at path: Groups/" + groupKey + "/FriendKeys");
            Log.d(TAG, "Adding: " + userEmail + " -> " + finalUserKey);

            // Try direct Firebase update first
            try {
              // Update directly in Firebase
              HashMap<String, Object> directUpdates = new HashMap<>();
              if (friendKeys == null) {
                friendKeys = new HashMap<>();
              }
              friendKeys.put(userEmail, finalUserKey);
              directUpdates.put("FriendKeys", friendKeys);

              serverClient.updateGroup(
                  groupKey,
                  directUpdates,
                  new FirebaseServerClient.OperationCallback() {
                    @Override
                    public void onSuccess() {
                      Toast.makeText(
                              UsersListActivity.this,
                              "Friend successfully added",
                              Toast.LENGTH_SHORT)
                          .show();

                      // Ask if user wants to add to coming list
                      new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this)
                          .setTitle(ADD_TO_COMING_TITLE)
                          .setMessage("Add " + user.getEmail() + " to the coming list?")
                          .setPositiveButton(
                              "Yes", (dialog, which) -> addUserToComingList(userEmail))
                          .setNegativeButton("No", null)
                          .show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                      Log.e(TAG, "Error adding friend: " + errorMessage);
                      Toast.makeText(
                              UsersListActivity.this,
                              "Error adding friend: " + errorMessage,
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            } catch (Exception e) {
              Log.e(TAG, "Error updating group", e);
              Toast.makeText(
                      UsersListActivity.this,
                      "Error adding friend: " + e.getMessage(),
                      Toast.LENGTH_SHORT)
                  .show();
            }
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error loading group: " + errorMessage);
            Toast.makeText(
                    UsersListActivity.this,
                    "Error loading group: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void addUserToComingList(String userEmail) {
    if (userEmail == null || userEmail.isEmpty()) {
      return;
    }

    // Update directly in Firebase
    HashMap<String, Object> directUpdates = new HashMap<>();
    if (comingKeys == null) {
      comingKeys = new HashMap<>();
    }
    comingKeys.put(userEmail, "true");
    directUpdates.put("ComingKeys", comingKeys);

    serverClient.updateGroup(
        groupKey,
        directUpdates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Toast.makeText(UsersListActivity.this, "Added to Coming List", Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error adding to coming list: " + errorMessage);
            Toast.makeText(
                    UsersListActivity.this,
                    "Error adding to coming list: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void loadUsersData() {
    // Show loading indicator
    Toast.makeText(this, LOADING_MESSAGE, Toast.LENGTH_SHORT).show();

    // Always use server client for consistency
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, User> data) {
            usersList = new ArrayList<>(data.values());
            UserAdapter adapter = new UserAdapter(UsersListActivity.this, 0, 0, usersList);
            userListView.setAdapter(adapter);

            if (usersList.isEmpty()) {
              Toast.makeText(UsersListActivity.this, NO_USERS_MESSAGE, Toast.LENGTH_SHORT).show();
            }
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Server error: " + errorMessage);
            Toast.makeText(
                    UsersListActivity.this,
                    "Failed to load users: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void clearActivityReferences() {
    // Clear UI components
    userListView = null;

    // Clear data objects
    serverClient = null;
    usersList = null;
    friendKeys = null;
    comingKeys = null;

    // Clear static context reference
    contextOfApplication = null;
  }
}
