package com.example.partymaker.ui.group;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.UserAdapter;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsRemoveActivity extends AppCompatActivity {
  private static final String TAG = "FriendsRemoveActivity";
  private Button btnDeleteFriend;
  private TextView tvInstructions1;
  private EditText etFriendEmail;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private String GroupKey,
      CurrentFriend,
      GroupName,
      GroupDay,
      GroupMonth,
      GroupYear,
      GroupHour,
      GroupLocation,
      AdminKey,
      CreatedAt,
      GroupPrice;
  private int GroupType;
  private boolean CanAdd;
  private ArrayList<User> usersList = new ArrayList<>();
  private ArrayList<String> userKeys = new ArrayList<>();
  private UserAdapter adapter;
  private FirebaseServerClient serverClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friends_remove);

    // Set up the toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setupActionBar();

    // Initialize the server client
    serverClient = FirebaseServerClient.getInstance();

    Intent intent = getIntent();
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(intent);
    if (extras == null) {
      Toast.makeText(this, "Error loading group data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    GroupName = extras.getGroupName();
    GroupKey = extras.getGroupKey();
    GroupDay = extras.getGroupDays();
    GroupMonth = extras.getGroupMonths();
    GroupYear = extras.getGroupYears();
    GroupHour = extras.getGroupHours();
    GroupLocation = extras.getGroupLocation();
    AdminKey = extras.getAdminKey();
    CreatedAt = extras.getCreatedAt();
    GroupPrice = extras.getGroupPrice();
    GroupType = extras.getGroupType();
    CanAdd = extras.isCanAdd();
    FriendKeys = extras.getFriendKeys();
    ComingKeys = extras.getComingKeys();
    MessageKeys = extras.getMessageKeys();

    // Initialize UI elements
    btnDeleteFriend = findViewById(R.id.btnDeleteFriend);
    tvInstructions1 = findViewById(R.id.tvInstructions3);
    etFriendEmail = findViewById(R.id.etDeleteEmail);

    ListView listView = findViewById(R.id.friends_remove_list);
    adapter = new UserAdapter(this, R.layout.item_user, R.id.tvUserListUsername, usersList);
    listView.setAdapter(adapter);

    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          User selectedUser = usersList.get(position);
          String selectedKey = userKeys.get(position);
          
          // Confirm before removing
          android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
          builder.setTitle("Remove Friend");
          builder.setMessage("Are you sure you want to remove " + selectedUser.getEmail() + "?");
          builder.setPositiveButton("Yes", (dialog, which) -> {
              removeFriend(selectedKey);
          });
          builder.setNegativeButton("No", null);
          builder.show();
        });

    setupEventHandlers();
    loadFriends();
  }

  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("Remove Friends");
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setElevation(4);
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
    ExtrasMetadata extras =
        new ExtrasMetadata(
            GroupName,
            GroupKey,
            GroupDay,
            GroupMonth,
            GroupYear,
            GroupHour,
            GroupLocation,
            AdminKey,
            CreatedAt,
            GroupPrice,
            GroupType,
            CanAdd,
            FriendKeys,
            ComingKeys,
            MessageKeys);
    Common.addExtrasToIntent(intent, extras);
    startActivity(intent);
    finish(); // Close this activity
  }

  @Override
  public void onBackPressed() {
    navigateBack();
  }

  private void setupEventHandlers() {
    btnDeleteFriend.setOnClickListener(
        v -> {
          // This if - checks if EditText is not Empty
          if (!etFriendEmail.getText().toString().trim().isEmpty()) {
            CurrentFriend = etFriendEmail.getText().toString().replace('.', ' ');

            // Always use server client for consistency
            serverClient.getUsers(
                new FirebaseServerClient.DataCallback<Map<String, User>>() {
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
          } else {
            Toast.makeText(FriendsRemoveActivity.this, "Please enter an email address", Toast.LENGTH_SHORT)
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
        if (userEmail.equals(CurrentFriend)) {
          userFound = true;
          friendKey = entry.getKey();
          break;
        }
      }
    }
    
    if (!userFound) {
      Toast.makeText(FriendsRemoveActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
      return;
    }
    
    // Check if user is in the group
    final String finalFriendKey = friendKey;
    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<Group>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null) {
              Toast.makeText(FriendsRemoveActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
              return;
            }
            
            boolean isInGroup = false;
            String friendKeyInGroup = null;
            
            // Check if user is in FriendKeys
            if (group.getFriendKeys() != null) {
              for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
                if (entry.getValue().equals(finalFriendKey)) {
                  isInGroup = true;
                  friendKeyInGroup = entry.getKey();
                  break;
                }
              }
            }
            
            if (!isInGroup) {
              Toast.makeText(FriendsRemoveActivity.this, "User is not in this group", Toast.LENGTH_SHORT).show();
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
    Toast.makeText(this, "Loading friends...", Toast.LENGTH_SHORT).show();
    
    // Get the group data
    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<Group>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null || group.getFriendKeys() == null || group.getFriendKeys().isEmpty()) {
              Toast.makeText(FriendsRemoveActivity.this, "No friends in this group", Toast.LENGTH_SHORT).show();
              adapter.notifyDataSetChanged();
              return;
            }
            
            // Update local data
            FriendKeys = new HashMap<>(group.getFriendKeys());
            
            // Load each friend's data
            for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
              String friendKey = entry.getValue().toString();
              serverClient.getUser(
                  friendKey,
                  new FirebaseServerClient.DataCallback<User>() {
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
        GroupKey,
        new FirebaseServerClient.DataCallback<Group>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null) {
              Toast.makeText(FriendsRemoveActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
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
              Toast.makeText(FriendsRemoveActivity.this, "Friend not found in group", Toast.LENGTH_SHORT).show();
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
                GroupKey,
                groupUpdates,
                new FirebaseServerClient.OperationCallback() {
                  @Override
                  public void onSuccess() {
                    Log.d(TAG, "Friend removed successfully from both FriendKeys and ComingKeys");
                    
                    // Update local data
                    if (FriendKeys != null) {
                      FriendKeys = updatedFriendKeys;
                    }
                    
                    if (ComingKeys != null) {
                      ComingKeys = updatedComingKeys;
                    }
                    
                    Toast.makeText(FriendsRemoveActivity.this, "Friend removed successfully", Toast.LENGTH_SHORT).show();
                    
                    // Clear the email field
                    etFriendEmail.setText("");
                    
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
