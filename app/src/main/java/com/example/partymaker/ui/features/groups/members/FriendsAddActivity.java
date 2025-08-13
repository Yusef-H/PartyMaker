package com.example.partymaker.ui.features.groups.members;

import static com.example.partymaker.utils.core.IntentExtrasManager.hideViews;
import static com.example.partymaker.utils.core.IntentExtrasManager.showViews;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import com.example.partymaker.ui.base.BaseActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.features.groups.main.PartyMainActivity;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import java.util.HashMap;
import java.util.Map;

public class FriendsAddActivity extends BaseActivity {
  private static final String TAG = "FriendsAddActivity";
  private static final String INPUT_EMAIL_MESSAGE = "Input email please";
  private static final String EMAIL_NOT_FOUND_MESSAGE = "Email not found";
  private static final String FRIEND_ADDED_MESSAGE = "Friend successfully added";
  private static final String USER_ALREADY_IN_GROUP_MESSAGE = "User already in group";
  private static final String ADDED_TO_COMING_MESSAGE = "Added to Coming List";
  private static final String GROUP_NOT_FOUND_MESSAGE = "Group not found";
  private static final String TRUE_VALUE = "true";
  private Button btnHide, btnHelp, btnAddFriend, btnYes, btnNo, btnFriendsList;
  private TextView tvHide, tvHelp, tvInstructions1, tvAddMore;
  private EditText etFriendEmail;
  private HashMap<String, Object> friendKeys, comingKeys, messageKeys;
  private String groupKey,
      currentFriend,
      groupName,
      groupDay,
      groupMonth,
      groupYear,
      groupHour,
      groupLocation,
      adminKey,
      createdAt,
      groupPrice;
  private int groupType;
  private boolean canAdd;
  private FirebaseServerClient serverClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_friends_add);

    // Initialize server client
    serverClient = FirebaseServerClient.getInstance();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    if (!initializeExtrasData()) {
      return;
    }

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

    setupEventHandlers();
  }

  private void navigateBack() {
    Intent intent = new Intent(this, PartyMainActivity.class);
    ExtrasMetadata extras =
        new ExtrasMetadata(
            groupName,
            groupKey,
            groupDay,
            groupMonth,
            groupYear,
            groupHour,
            groupLocation,
            adminKey,
            createdAt,
            groupPrice,
            groupType,
            canAdd,
            friendKeys,
            comingKeys,
            messageKeys);
    IntentExtrasManager.addExtrasToIntent(intent, extras);
    startActivity(intent);
    finish(); // Close this activity
  }

  private void navigateToMembersInvitedActivity() {
    Intent intent = new Intent(this, MembersInvitedActivity.class);
    ExtrasMetadata extras =
        new ExtrasMetadata(
            groupName,
            groupKey,
            groupDay,
            groupMonth,
            groupYear,
            groupHour,
            groupLocation,
            adminKey,
            createdAt,
            groupPrice,
            groupType,
            canAdd,
            friendKeys,
            comingKeys,
            messageKeys);
    IntentExtrasManager.addExtrasToIntent(intent, extras);
    startActivity(intent);
    finish(); // Close this activity
  }

  private void navigateToMembersComingActivity() {
    Intent intent = new Intent(this, MembersComingActivity.class);
    ExtrasMetadata extras =
        new ExtrasMetadata(
            groupName,
            groupKey,
            groupDay,
            groupMonth,
            groupYear,
            groupHour,
            groupLocation,
            adminKey,
            createdAt,
            groupPrice,
            groupType,
            canAdd,
            friendKeys,
            comingKeys,
            messageKeys);
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

  private boolean initializeExtrasData() {
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return false;
    }

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

    return true;
  }

  private void setupEventHandlers() {
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

    btnAddFriend.setOnClickListener(v -> handleAddFriendClick());
    btnFriendsList.setOnClickListener(v -> navigateToUsersListActivity());
    btnYes.setOnClickListener(v -> handleYesClick());

    btnNo.setOnClickListener(v -> navigateToMembersInvitedActivity());
  }

  private ExtrasMetadata createExtrasMetadata() {
    return new ExtrasMetadata(
        groupName,
        groupKey,
        groupDay,
        groupMonth,
        groupYear,
        groupHour,
        groupLocation,
        adminKey,
        createdAt,
        groupPrice,
        groupType,
        canAdd,
        friendKeys,
        comingKeys,
        messageKeys);
  }

  private void addExtrasToIntent(Intent intent) {
    IntentExtrasManager.addExtrasToIntent(intent, createExtrasMetadata());
  }

  private void navigateToUsersListActivity() {
    Intent intent = new Intent(FriendsAddActivity.this, UsersListActivity.class);
    addExtrasToIntent(intent);
    startActivity(intent);
  }

  private void handleAddFriendClick() {
    if (!etFriendEmail.getText().toString().trim().isEmpty()) {
      currentFriend = etFriendEmail.getText().toString().replace('.', ' ');
      serverClient.getUsers(
          new FirebaseServerClient.DataCallback<>() {
            @Override
            public void onSuccess(Map<String, User> data) {
              processServerUserData(data);
            }

            @Override
            public void onError(String errorMessage) {
              Toast.makeText(
                      FriendsAddActivity.this, "Server error: " + errorMessage, Toast.LENGTH_SHORT)
                  .show();
            }
          });
    } else {
      Toast.makeText(FriendsAddActivity.this, INPUT_EMAIL_MESSAGE, Toast.LENGTH_SHORT).show();
    }
  }

  private void handleYesClick() {
    if (comingKeys == null) {
      comingKeys = new HashMap<>();
    }

    final String currentFriendEmail = currentFriend;
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(com.example.partymaker.data.model.Group group) {
            if (group != null) {
              addFriendToComingList(group, currentFriendEmail);
            } else {
              showGroupNotFoundError();
            }
          }

          @Override
          public void onError(String errorMessage) {
            showGroupLoadError(errorMessage);
          }
        });
  }

  private void addFriendToComingList(
      com.example.partymaker.data.model.Group group, String currentFriendEmail) {
    String friendKey = findFriendKeyInGroup(group, currentFriendEmail);
    final String finalFriendKey = friendKey != null ? friendKey : currentFriendEmail;

    try {
      HashMap<String, Object> updatedComingKeys = group.getComingKeys();
      if (updatedComingKeys == null) {
        updatedComingKeys = new HashMap<>();
      }
      updatedComingKeys.put(finalFriendKey, TRUE_VALUE);

      HashMap<String, Object> groupUpdates = new HashMap<>();
      groupUpdates.put("ComingKeys", updatedComingKeys);

      Log.d(TAG, "Updating group: " + groupKey + " with ComingKeys update");

      serverClient.updateGroup(
          groupKey,
          groupUpdates,
          new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
              Toast.makeText(FriendsAddActivity.this, ADDED_TO_COMING_MESSAGE, Toast.LENGTH_SHORT)
                  .show();
              updateLocalComingKeys(finalFriendKey);
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
      resetViewsAfterError();
    }
  }

  private String findFriendKeyInGroup(
      com.example.partymaker.data.model.Group group, String currentFriendEmail) {
    if (group.getFriendKeys() != null) {
      for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
        if (entry.getKey().equals(currentFriendEmail)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  private void updateLocalComingKeys(String finalFriendKey) {
    if (comingKeys == null) {
      comingKeys = new HashMap<>();
    }
    comingKeys.put(finalFriendKey, TRUE_VALUE);
  }

  private void showGroupNotFoundError() {
    Toast.makeText(FriendsAddActivity.this, GROUP_NOT_FOUND_MESSAGE, Toast.LENGTH_SHORT).show();
    resetViewsAfterError();
  }

  private void showGroupLoadError(String errorMessage) {
    Log.e(TAG, "Error loading group: " + errorMessage);
    Toast.makeText(
            FriendsAddActivity.this, "Error loading group: " + errorMessage, Toast.LENGTH_SHORT)
        .show();
    resetViewsAfterError();
  }

  private void resetViewsAfterError() {
    showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
    hideViews(tvInstructions1, btnHide, tvHide, tvAddMore, btnYes, btnNo);
  }

  private void processServerUserData(Map<String, User> userData) {
    String friendKey = findUserByEmail(userData);

    if (friendKey == null) {
      Toast.makeText(FriendsAddActivity.this, EMAIL_NOT_FOUND_MESSAGE, Toast.LENGTH_SHORT).show();
      return;
    }

    final String finalFriendKey = friendKey;
    final String currentFriendEmail = currentFriend;

    Log.d(TAG, "Adding user to group: " + currentFriendEmail + " with key: " + finalFriendKey);

    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(com.example.partymaker.data.model.Group group) {
            handleGroupForUserAddition(group, finalFriendKey, currentFriendEmail);
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

  private String findUserByEmail(Map<String, User> userData) {
    for (Map.Entry<String, User> entry : userData.entrySet()) {
      User user = entry.getValue();
      if (user != null && user.getEmail() != null) {
        String userEmail = user.getEmail().replace('.', ' ');
        if (userEmail.equals(currentFriend)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  private void handleGroupForUserAddition(
      com.example.partymaker.data.model.Group group,
      String finalFriendKey,
      String currentFriendEmail) {
    if (group == null) {
      Toast.makeText(FriendsAddActivity.this, GROUP_NOT_FOUND_MESSAGE, Toast.LENGTH_SHORT).show();
      return;
    }

    Log.d(TAG, "Group found: " + group.getGroupName());

    if (isUserAlreadyInGroup(group, finalFriendKey, currentFriendEmail)) {
      Toast.makeText(FriendsAddActivity.this, USER_ALREADY_IN_GROUP_MESSAGE, Toast.LENGTH_SHORT)
          .show();
      return;
    }

    addUserToGroup(group, finalFriendKey, currentFriendEmail);
  }

  private boolean isUserAlreadyInGroup(
      com.example.partymaker.data.model.Group group,
      String finalFriendKey,
      String currentFriendEmail) {
    if (group.getFriendKeys() != null) {
      for (Map.Entry<String, Object> entry : group.getFriendKeys().entrySet()) {
        Log.d(TAG, "Checking friend key: " + entry.getKey() + " -> " + entry.getValue());
        if ((entry.getValue() != null && entry.getValue().toString().equals(finalFriendKey))
            || entry.getKey().equals(currentFriendEmail)) {
          return true;
        }
      }
    }
    return false;
  }

  private void addUserToGroup(
      com.example.partymaker.data.model.Group group,
      String finalFriendKey,
      String currentFriendEmail) {
    try {
      HashMap<String, Object> updatedFriendKeys = group.getFriendKeys();
      if (updatedFriendKeys == null) {
        updatedFriendKeys = new HashMap<>();
      }
      updatedFriendKeys.put(currentFriendEmail, finalFriendKey);

      HashMap<String, Object> groupUpdates = new HashMap<>();
      groupUpdates.put("FriendKeys", updatedFriendKeys);

      Log.d(TAG, "Updating group: " + groupKey + " with FriendKeys update");

      serverClient.updateGroup(
          groupKey,
          groupUpdates,
          new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
              handleSuccessfulUserAddition(finalFriendKey, currentFriendEmail);
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
              FriendsAddActivity.this, "Error adding friend: " + e.getMessage(), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void handleSuccessfulUserAddition(String finalFriendKey, String currentFriendEmail) {
    if (friendKeys == null) {
      friendKeys = new HashMap<>();
    }
    friendKeys.put(currentFriendEmail, finalFriendKey);

    Toast.makeText(FriendsAddActivity.this, FRIEND_ADDED_MESSAGE, Toast.LENGTH_SHORT).show();

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
  protected void clearActivityReferences() {
    // Clear UI components
    btnHide = null;
    btnHelp = null;
    btnAddFriend = null;
    btnYes = null;
    btnNo = null;
    btnFriendsList = null;
    tvHide = null;
    tvHelp = null;
    tvInstructions1 = null;
    tvAddMore = null;
    etFriendEmail = null;

    // Clear data objects
    friendKeys = null;
    comingKeys = null;
    messageKeys = null;
    serverClient = null;

    // Clear string fields
    groupKey = null;
    currentFriend = null;
    groupName = null;
    groupDay = null;
    groupMonth = null;
    groupYear = null;
    groupHour = null;
    groupLocation = null;
    adminKey = null;
    createdAt = null;
    groupPrice = null;
  }
}
