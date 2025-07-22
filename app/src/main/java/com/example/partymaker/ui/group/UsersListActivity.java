package com.example.partymaker.ui.group;

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
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.UserAdapter;
import com.example.partymaker.utilities.AuthHelper;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersListActivity extends AppCompatActivity {
  private static final String TAG = "UsersListActivity";
  private ListView lv;
  public static Context contextOfApplication;
  private FirebaseServerClient serverClient;
  private ArrayList<User> usersList;

  // Group data
  private String GroupKey;
  private HashMap<String, Object> FriendKeys;
  private HashMap<String, Object> ComingKeys;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_party_friends);
    contextOfApplication = getApplicationContext();

    // Initialize server client
    serverClient = FirebaseServerClient.getInstance();

    // Get group data from intent
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras != null) {
      GroupKey = extras.getGroupKey();
      String adminKey = extras.getAdminKey();
      FriendKeys = extras.getFriendKeys();
      ComingKeys = extras.getComingKeys();

      Log.d(TAG, "Received GroupKey: " + GroupKey);
      Log.d(TAG, "Received AdminKey: " + adminKey);
      if (FriendKeys != null) {
        Log.d(TAG, "Received FriendKeys: " + FriendKeys.size());
      } else {
        Log.d(TAG, "FriendKeys is null");
      }
    } else {
      Log.e(TAG, "No extras received from intent");
    }

    // Actionbar settings
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("All users");
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    lv = findViewById(R.id.lv);

    // Get users reference using FirebaseAccessManager
    FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
    Object usersRef = accessManager.getUsersRef();

    ShowData();
    setupEventHandlers();
  }

  private void setupEventHandlers() {
    lv.setOnItemClickListener(
        (parent, view, position, id) -> {
          User selectedUser = usersList.get(position);

          if (selectedUser == null || selectedUser.getEmail() == null) {
            Toast.makeText(this, "Invalid user selected", Toast.LENGTH_SHORT).show();
            return;
          }

          // Check if group data is available
          if (GroupKey == null || GroupKey.isEmpty()) {
            Toast.makeText(this, "No group data available", Toast.LENGTH_SHORT).show();
            return;
          }

          // Confirm adding the user
          new androidx.appcompat.app.AlertDialog.Builder(this)
              .setTitle("Add Friend")
              .setMessage("Add " + selectedUser.getEmail() + " to the group?")
              .setPositiveButton(
                  "Add",
                  (dialog, which) -> {
                    addUserToGroup(selectedUser);
                  })
              .setNegativeButton("Cancel", null)
              .show();
        });
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
        String currentUserKey = AuthHelper.getCurrentUserKey(this);

        // If this is the current user, use their key
        if (user.getEmail().equals(AuthHelper.getCurrentUserEmail(this))) {
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
        GroupKey,
        new FirebaseServerClient.DataCallback<Group>() {
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

            Log.d(TAG, "Updating FriendKeys at path: Groups/" + GroupKey + "/FriendKeys");
            Log.d(TAG, "Adding: " + userEmail + " -> " + finalUserKey);

            // Try direct Firebase update first
            try {
              // Update directly in Firebase
              HashMap<String, Object> directUpdates = new HashMap<>();
              if (FriendKeys == null) {
                FriendKeys = new HashMap<>();
              }
              FriendKeys.put(userEmail, finalUserKey);
              directUpdates.put("FriendKeys", FriendKeys);

              serverClient.updateGroup(
                  GroupKey,
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
                          .setTitle("Add to Coming List")
                          .setMessage("Add " + user.getEmail() + " to the coming list?")
                          .setPositiveButton(
                              "Yes",
                              (dialog, which) -> {
                                addUserToComingList(userEmail);
                              })
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
    if (ComingKeys == null) {
      ComingKeys = new HashMap<>();
    }
    ComingKeys.put(userEmail, "true");
    directUpdates.put("ComingKeys", ComingKeys);

    serverClient.updateGroup(
        GroupKey,
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

  private void ShowData() {
    // Show loading indicator
    Toast.makeText(this, "Loading users...", Toast.LENGTH_SHORT).show();

    // Always use server client for consistency
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<Map<String, User>>() {
          @Override
          public void onSuccess(Map<String, User> data) {
            usersList = new ArrayList<>(data.values());
            UserAdapter adapter = new UserAdapter(UsersListActivity.this, 0, 0, usersList);
            lv.setAdapter(adapter);

            if (usersList.isEmpty()) {
              Toast.makeText(UsersListActivity.this, "No users found", Toast.LENGTH_SHORT).show();
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

  public static Context getContextOfApplication() {
    return contextOfApplication;
  }
}
