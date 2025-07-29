package com.example.partymaker.ui.group;

import static com.example.partymaker.utils.data.Common.hideViews;
import static com.example.partymaker.utils.data.Common.showViews;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utils.data.Common;
import com.example.partymaker.utils.data.ExtrasMetadata;
import java.util.HashMap;
import java.util.Map;

public class FriendsAddActivity extends AppCompatActivity {
  private static final String TAG = "FriendsAddActivity";
  private Button btnHide, btnHelp, btnAddFriend, btnYes, btnNo, btnFriendsList;
  private TextView tvHide, tvHelp, tvInstructions1, tvAddMore;
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
  private FirebaseServerClient serverClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_friends_add);

    // Initialize server client
    serverClient = FirebaseServerClient.getInstance();

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // Get Values from MainActivity By intent + connection between intent and
    // current activity objects
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
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

    // connection
    btnAddFriend = findViewById(R.id.btnAddFriend);
    btnFriendsList = findViewById(R.id.btnFriendsList);
    btnHide = findViewById(R.id.btnHide1);
    btnHelp = findViewById(R.id.btnHelp1);
    btnYes = findViewById(R.id.btnYes);
    btnNo = findViewById(R.id.btnNo);
    tvHide = findViewById(R.id.tvHide1);
    tvHelp = findViewById(R.id.tvHelp1);
    tvAddMore = findViewById(R.id.tvAddMore);
    tvInstructions1 = findViewById(R.id.tvInstructions1);
    etFriendEmail = findViewById(R.id.etFriendEmail);
    ImageButton btnBack = findViewById(R.id.btnBack3);

    // Set up back button
    btnBack.setOnClickListener(v -> navigateBack());

    EventHandler();
  }

  private void navigateBack() {
    Intent intent = new Intent(this, PartyMainActivity.class);
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

  private void navigateToMembersInvitedActivity() {
    Intent intent = new Intent(this, MembersInvitedActivity.class);
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

  private void navigateToMembersComingActivity() {
    Intent intent = new Intent(this, MembersComingActivity.class);
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
    // Call super first
    super.onBackPressed();
    navigateBack();
  }

  private void EventHandler() {
    btnHelp.setOnClickListener(
        v -> {
          showViews(tvInstructions1, btnHide, tvHide);
          hideViews(btnHelp, tvHelp);
        });

    btnHide.setOnClickListener(
        v -> {
          showViews(btnHelp, tvHelp);
          hideViews(tvInstructions1, btnHide, tvHide);
        });

    btnAddFriend.setOnClickListener(
        v -> {
          // This if - checks if EditText is not Empty
          if (!etFriendEmail.getText().toString().trim().isEmpty()) {
            CurrentFriend = etFriendEmail.getText().toString().replace('.', ' ');

            // Always use server client for consistency
            serverClient.getUsers(
                    new FirebaseServerClient.DataCallback<>() {
                        @Override
                        public void onSuccess(Map<String, User> data) {
                            processServerUserData(data);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(
                                            FriendsAddActivity.this,
                                            "Server error: " + errorMessage,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
          } else {
            Toast.makeText(FriendsAddActivity.this, "Input email please", Toast.LENGTH_SHORT)
                .show();
          }
        });
    btnFriendsList.setOnClickListener(
        v -> {
          Intent intent = new Intent(FriendsAddActivity.this, UsersListActivity.class);
          // Pass necessary data to UsersListActivity
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
        });
    btnYes.setOnClickListener(
        v -> {
          // Add friend to coming list
          if (ComingKeys == null) {
            ComingKeys = new HashMap<>();
          }

          // Store CurrentFriend in a final variable to use in inner class
          final String currentFriendEmail = CurrentFriend;

          // Use server client to update ComingKeys
          serverClient.getGroup(
              GroupKey,
                  new FirebaseServerClient.DataCallback<>() {
                      @Override
                      public void onSuccess(com.example.partymaker.data.model.Group group) {
                          if (group != null) {
                              // Find the friend key in FriendKeys
                              String friendKey = null;
                              if (group.getFriendKeys() != null) {
                                  for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
                                      if (entry.getKey().equals(currentFriendEmail)) {
                                          friendKey = entry.getKey();
                                          break;
                                      }
                                  }
                              }

                              // Store friendKey in a final variable for use in inner class
                              final String finalFriendKey =
                                      friendKey != null ? friendKey : currentFriendEmail;

                              try {
                                  // Update the entire ComingKeys object
                                  HashMap<String, Object> updatedComingKeys = group.getComingKeys();
                                  if (updatedComingKeys == null) {
                                      updatedComingKeys = new HashMap<>();
                                  }
                                  updatedComingKeys.put(finalFriendKey, "true");

                                  // Create updates map for the entire group
                                  HashMap<String, Object> groupUpdates = new HashMap<>();
                                  groupUpdates.put("ComingKeys", updatedComingKeys);

                                  Log.d(TAG, "Updating group: " + GroupKey + " with ComingKeys update");

                                  // Use updateGroup instead of updateData
                                  serverClient.updateGroup(
                                          GroupKey,
                                          groupUpdates,
                                          new FirebaseServerClient.OperationCallback() {
                                              @Override
                                              public void onSuccess() {
                                                  Toast.makeText(
                                                                  FriendsAddActivity.this,
                                                                  "Added to Coming List",
                                                                  Toast.LENGTH_SHORT)
                                                          .show();

                                                  // Update local data
                                                  if (ComingKeys == null) {
                                                      ComingKeys = new HashMap<>();
                                                  }
                                                  ComingKeys.put(finalFriendKey, "true");

                                                  // Navigate to MembersComingActivity to show the newly added member in
                                                  // coming list
                                                  navigateToMembersComingActivity();
                                              }

                                              @Override
                                              public void onError(String errorMessage) {
                                                  Log.e(TAG, "Error adding to coming list: " + errorMessage);
                                                  Toast.makeText(
                                                                  FriendsAddActivity.this,
                                                                  "Error adding to coming list: " + errorMessage,
                                                                  Toast.LENGTH_SHORT)
                                                          .show();
                                              }
                                          });
                              } catch (Exception e) {
                                  Log.e(TAG, "Exception updating coming keys", e);
                                  Toast.makeText(
                                                  FriendsAddActivity.this,
                                                  "Error adding to coming list: " + e.getMessage(),
                                                  Toast.LENGTH_SHORT)
                                          .show();

                                  showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
                                  hideViews(tvInstructions1, btnHide, tvHide, tvAddMore, btnYes, btnNo);
                              }
                          } else {
                              Toast.makeText(FriendsAddActivity.this, "Group not found", Toast.LENGTH_SHORT)
                                      .show();
                              showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
                              hideViews(tvInstructions1, btnHide, tvHide, tvAddMore, btnYes, btnNo);
                          }
                      }

                      @Override
                      public void onError(String errorMessage) {
                          Log.e(TAG, "Error loading group: " + errorMessage);
                          Toast.makeText(
                                          FriendsAddActivity.this,
                                          "Error loading group: " + errorMessage,
                                          Toast.LENGTH_SHORT)
                                  .show();

                          showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
                          hideViews(tvInstructions1, btnHide, tvHide, tvAddMore, btnYes, btnNo);
                      }
                  });
        });

    btnNo.setOnClickListener(
        v -> {
          // Navigate to MembersInvitedActivity to show the newly added member in invited list
          navigateToMembersInvitedActivity();
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
      Toast.makeText(FriendsAddActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
      return;
    }

    // Store the friendKey in a final variable for use in inner class
    final String finalFriendKey = friendKey;

    // Store CurrentFriend in a final variable for use in inner class
    final String currentFriendEmail = CurrentFriend;

    Log.d(TAG, "Adding user to group: " + currentFriendEmail + " with key: " + finalFriendKey);

    // Check if user is already in the group
    serverClient.getGroup(
        GroupKey,
            new FirebaseServerClient.DataCallback<>() {
                @Override
                public void onSuccess(com.example.partymaker.data.model.Group group) {
                    if (group == null) {
                        Toast.makeText(FriendsAddActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "Group found: " + group.getGroupName());

                    boolean alreadyInGroup = false;

                    // Check if user is already in FriendKeys
                    if (group.getFriendKeys() != null) {
                        for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
                            Log.d(TAG, "Checking friend key: " + entry.getKey() + " -> " + entry.getValue());
                            if ((entry.getValue() != null && entry.getValue().toString().equals(finalFriendKey))
                                    || entry.getKey().equals(currentFriendEmail)) {
                                alreadyInGroup = true;
                                break;
                            }
                        }
                    }

                    if (alreadyInGroup) {
                        Toast.makeText(FriendsAddActivity.this, "User already in group", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    try {
                        // Update the entire FriendKeys object instead of just the path
                        HashMap<String, Object> updatedFriendKeys = group.getFriendKeys();
                        if (updatedFriendKeys == null) {
                            updatedFriendKeys = new HashMap<>();
                        }
                        updatedFriendKeys.put(currentFriendEmail, finalFriendKey);

                        // Create updates map for the entire group
                        HashMap<String, Object> groupUpdates = new HashMap<>();
                        groupUpdates.put("FriendKeys", updatedFriendKeys);

                        Log.d(TAG, "Updating group: " + GroupKey + " with FriendKeys update");

                        // Use updateGroup instead of updateData
                        serverClient.updateGroup(
                                GroupKey,
                                groupUpdates,
                                new FirebaseServerClient.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Update local data
                                        if (FriendKeys == null) {
                                            FriendKeys = new HashMap<>();
                                        }
                                        FriendKeys.put(currentFriendEmail, finalFriendKey);

                                        Toast.makeText(
                                                        FriendsAddActivity.this,
                                                        "Friend successfully added",
                                                        Toast.LENGTH_SHORT)
                                                .show();

                                        // Ask if user wants to add to coming list
                                        hideViews(
                                                etFriendEmail,
                                                btnAddFriend,
                                                btnFriendsList,
                                                tvInstructions1,
                                                btnHide,
                                                tvHide,
                                                btnHelp,
                                                tvHelp);
                                        showViews(tvAddMore, btnYes, btnNo);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Log.e(TAG, "Error adding friend: " + errorMessage);
                                        Toast.makeText(
                                                        FriendsAddActivity.this,
                                                        "Error adding friend: " + errorMessage,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                    } catch (Exception e) {
                        Log.e(TAG, "Exception updating group", e);
                        Toast.makeText(
                                        FriendsAddActivity.this,
                                        "Error adding friend: " + e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error loading group: " + errorMessage);
                    Toast.makeText(
                                    FriendsAddActivity.this,
                                    "Error loading group: " + errorMessage,
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            });
  }
}
