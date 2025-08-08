package com.example.partymaker.ui.features.groups.members;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.UserAdapter;
import com.example.partymaker.ui.features.groups.management.AdminOptionsActivity;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsRemoveActivity extends AppCompatActivity {
  private static final String TAG = "FriendsRemoveActivity";
  
  // UI constants
  private static final String ACTIVITY_TITLE = "Remove Friends";
  private static final String ACTION_BAR_COLOR = "#0081d1";
  private static final int ACTION_BAR_ELEVATION = 4;
  private static final String LOADING_MESSAGE = "Loading friends...";
  private static final String NO_FRIENDS_MESSAGE = "No friends in this group";
  private static final String REMOVE_CONFIRMATION_TITLE = "Remove Friend";
  private static final String SUCCESS_MESSAGE = "Friend removed successfully";
  private static final String EMAIL_REQUIRED_MESSAGE = "Please enter an email address";
  private static final String EMAIL_NOT_FOUND_MESSAGE = "Email not found";
  private static final String USER_NOT_IN_GROUP_MESSAGE = "User is not in this group";
  
  // Data collections
  private final ArrayList<User> usersList = new ArrayList<>();
  private final ArrayList<String> userKeys = new ArrayList<>();
  
  // UI Components
  private Button deleteButton;
  private EditText emailInput;
  private UserAdapter adapter;
  private FirebaseServerClient serverClient;
  
  // Group data
  private HashMap<String, Object> friendKeys, comingKeys, messageKeys;
  private String groupKey, currentFriend, groupName, groupDay, groupMonth, groupYear;
  private String groupHour, groupLocation, adminKey, createdAt, groupPrice;
  private int groupType;
  private boolean canAdd;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_friends_remove);

    // Set up the toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setupActionBar();

    // Initialize the server client
    serverClient = FirebaseServerClient.getInstance();

    Intent intent = getIntent();
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(intent);
    if (extras == null) {
      Toast.makeText(this, "Error loading group data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    extractGroupData(extras);

    // Initialize UI elements
    deleteButton = findViewById(R.id.btnDeleteFriend);
    emailInput = findViewById(R.id.etDeleteEmail);

    ListView listView = findViewById(R.id.friends_remove_list);
    adapter = new UserAdapter(this, R.layout.item_user, R.id.tvUserListUsername, usersList);
    listView.setAdapter(adapter);

    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          User selectedUser = usersList.get(position);
          String selectedKey = userKeys.get(position);
          showRemoveConfirmation(selectedUser, selectedKey);
        });

    setupEventHandlers();
    loadFriends();
  }

  private void extractGroupData(ExtrasMetadata extras) {
    groupName = extras.getGroupName();
    groupKey = extras.getGroupKey();
    groupDay = extras.getGroupDays();
    groupMonth = extras.getGroupMonths();
    groupYear = extras.getGroupYears();
    groupHour = extras.getGroupHours();
    groupLocation = extras.getGroupLocation();
    adminKey = extras.getAdminKey();
    createdAt = extras.getCreatedAt();
    groupPrice = extras.getGroupPrice();
    groupType = extras.getGroupType();
    canAdd = extras.isCanAdd();
    friendKeys = extras.getFriendKeys();
    comingKeys = extras.getComingKeys();
    messageKeys = extras.getMessageKeys();
  }
  
  private void showRemoveConfirmation(User selectedUser, String selectedKey) {
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
    builder.setTitle(REMOVE_CONFIRMATION_TITLE);
    builder.setMessage("Are you sure you want to remove " + selectedUser.getEmail() + "?");
    builder.setPositiveButton("Yes", (dialog, which) -> removeFriend(selectedKey));
    builder.setNegativeButton("No", null);
    builder.show();
  }
  
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(ACTIVITY_TITLE);
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(ACTION_BAR_COLOR)));
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setElevation(ACTION_BAR_ELEVATION);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      navigateBack();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void navigateBack() {
    Intent intent = new Intent(this, AdminOptionsActivity.class);
    ExtrasMetadata extras = createExtrasMetadata();
    IntentExtrasManager.addExtrasToIntent(intent, extras);
    startActivity(intent);
    finish(); // Close this activity
  }

  @Override
  public void onBackPressed() {
    // Call super first
    super.onBackPressed();
    navigateBack();
  }

  private ExtrasMetadata createExtrasMetadata() {
    return new ExtrasMetadata(
        groupName, groupKey, groupDay, groupMonth, groupYear, groupHour,
        groupLocation, adminKey, createdAt, groupPrice, groupType, canAdd,
        friendKeys, comingKeys, messageKeys);
  }
  
  private void setupEventHandlers() {
    deleteButton.setOnClickListener(v -> handleDeleteButtonClick());
  }
  
  private void handleDeleteButtonClick() {
    String emailText = emailInput.getText().toString().trim();
    if (!emailText.isEmpty()) {
      currentFriend = emailText.replace('.', ' ');
      loadAndProcessUsers();
    } else {
      Toast.makeText(this, EMAIL_REQUIRED_MESSAGE, Toast.LENGTH_SHORT).show();
    }
  }
  
  private void loadAndProcessUsers() {
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, User> data) {
            processServerUserData(data);
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    FriendsRemoveActivity.this,
                    "Server error: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void processServerUserData(Map<String, User> userData) {
    boolean userFound = false;
    String friendKey = null;

    // Find the user by email
    for (Map.Entry<String, User> entry : userData.entrySet()) {
      User user = entry.getValue();
      if (user != null && user.getEmail() != null) {
        String userEmail = user.getEmail().replace('.', ' ');
        if (userEmail.equals(currentFriend)) {
          userFound = true;
          friendKey = entry.getKey();
          break;
        }
      }
    }

    if (!userFound) {
      Toast.makeText(FriendsRemoveActivity.this, EMAIL_NOT_FOUND_MESSAGE, Toast.LENGTH_SHORT).show();
      return;
    }

    // Check if user is in the group
    final String finalFriendKey = friendKey;
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null) {
              Toast.makeText(FriendsRemoveActivity.this, "Group not found", Toast.LENGTH_SHORT)
                  .show();
              return;
            }

            boolean isInGroup = false;

            // Check if user is in FriendKeys
            if (group.getFriendKeys() != null) {
              for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
                if (entry.getValue().equals(finalFriendKey)) {
                  isInGroup = true;
                  break;
                }
              }
            }

            if (!isInGroup) {
              Toast.makeText(FriendsRemoveActivity.this, USER_NOT_IN_GROUP_MESSAGE, Toast.LENGTH_SHORT)
                  .show();
              return;
            }

            // Remove friend using the new method
            removeFriend(finalFriendKey);
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    FriendsRemoveActivity.this,
                    "Error loading group: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void loadFriends() {
    // Clear existing data
    usersList.clear();
    userKeys.clear();

    // Show loading message
    Toast.makeText(this, LOADING_MESSAGE, Toast.LENGTH_SHORT).show();

    // Get the group data
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null || group.getFriendKeys() == null || group.getFriendKeys().isEmpty()) {
              Toast.makeText(FriendsRemoveActivity.this, NO_FRIENDS_MESSAGE, Toast.LENGTH_SHORT)
                  .show();
              adapter.notifyDataSetChanged();
              return;
            }

            // Update local data
            friendKeys = new HashMap<>(group.getFriendKeys());

            // Load each friend's data
            for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
              String friendKey = entry.getValue().toString();
              serverClient.getUser(
                  friendKey,
                  new FirebaseServerClient.DataCallback<>() {
                    @Override
                    public void onSuccess(User user) {
                      if (user != null) {
                        usersList.add(user);
                        userKeys.add(friendKey);
                        adapter.notifyDataSetChanged();
                      }
                    }

                    @Override
                    public void onError(String errorMessage) {
                      Log.e(TAG, "Error loading user: " + errorMessage);
                    }
                  });
            }
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    FriendsRemoveActivity.this,
                    "Error loading group: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void removeFriend(String friendKey) {
    // Get the latest group data
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null) {
              Toast.makeText(FriendsRemoveActivity.this, "Group not found", Toast.LENGTH_SHORT)
                  .show();
              return;
            }

            // Find the key in FriendKeys that corresponds to this friend
            String keyToRemove = null;
            if (group.getFriendKeys() != null) {
              for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
                if (entry.getValue().equals(friendKey)) {
                  keyToRemove = entry.getKey();
                  break;
                }
              }
            }

            if (keyToRemove == null) {
              Toast.makeText(
                      FriendsRemoveActivity.this, "Friend not found in group", Toast.LENGTH_SHORT)
                  .show();
              return;
            }

            final String finalKeyToRemove = keyToRemove;
            Log.d(TAG, "Removing friend with key: " + finalKeyToRemove);

            // Create modified copies of the maps
            HashMap<String, Object> updatedFriendKeys = new HashMap<>();
            if (group.getFriendKeys() != null) {
              updatedFriendKeys.putAll(group.getFriendKeys());
              updatedFriendKeys.remove(finalKeyToRemove);
            }

            HashMap<String, Object> updatedComingKeys = new HashMap<>();
            if (group.getComingKeys() != null) {
              updatedComingKeys.putAll(group.getComingKeys());
              // Remove from ComingKeys if present
              updatedComingKeys.remove(finalKeyToRemove);
            }

            // Create updates map for the entire group
            HashMap<String, Object> groupUpdates = new HashMap<>();
            groupUpdates.put("FriendKeys", updatedFriendKeys);
            groupUpdates.put("ComingKeys", updatedComingKeys);

            // Update the group with both changes at once
            serverClient.updateGroup(
                groupKey,
                groupUpdates,
                new FirebaseServerClient.OperationCallback() {
                  @Override
                  public void onSuccess() {
                    Log.d(TAG, "Friend removed successfully from both FriendKeys and ComingKeys");

                    // Update local data
                    if (friendKeys != null) {
                      friendKeys = updatedFriendKeys;
                    }

                    if (comingKeys != null) {
                      comingKeys = updatedComingKeys;
                    }

                    Toast.makeText(FriendsRemoveActivity.this, SUCCESS_MESSAGE, Toast.LENGTH_SHORT)
                        .show();

                    // Clear the email field
                    emailInput.setText("");

                    // Refresh the list
                    loadFriends();
                  }

                  @Override
                  public void onError(String errorMessage) {
                    Log.e(TAG, "Error removing friend: " + errorMessage);
                    Toast.makeText(
                            FriendsRemoveActivity.this,
                            "Error removing friend: " + errorMessage,
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error loading group: " + errorMessage);
            Toast.makeText(
                    FriendsRemoveActivity.this,
                    "Error loading group: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
