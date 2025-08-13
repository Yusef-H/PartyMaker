package com.example.partymaker.ui.features.groups.members;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import com.example.partymaker.ui.base.BaseActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.InvitedAdapter;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembersInvitedActivity extends BaseActivity {

  private static final String TAG = "MembersInvitedActivity";

  // UI constants
  private static final String ACTIVITY_TITLE = "Invited Members";
  private static final String ACTION_BAR_COLOR = "#0081d1";
  private static final String LOADING_MESSAGE = "Loading invited members...";
  private static final String NO_MEMBERS_MESSAGE = "No invited members found";

  // UI Components
  private ListView membersList;
  private HashMap<String, Object> friendKeys;
  private String adminKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_party_invited);

    setupActionBar();

    // Get extras from intent
    Intent intent = getIntent();
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(intent);
    if (extras != null) {
      friendKeys = extras.getFriendKeys();
      adminKey = extras.getAdminKey();
      String groupKey = extras.getGroupKey();

      Log.d(
          TAG,
          "Received extras - FriendKeys: " + (friendKeys != null ? friendKeys.size() : "null"));
      Log.d(TAG, "AdminKey: " + adminKey);
      Log.d(TAG, "GroupKey: " + groupKey);
    } else {
      Log.e(TAG, "No extras received from intent");
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // Get UserKey from AuthHelper instead of Firebase Auth
    String userKey;
    try {
      userKey = AuthenticationManager.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + userKey);
    } catch (Exception e) {
      // Safe intent extras handling
      if (intent != null && intent.getExtras() != null) {
        userKey = intent.getStringExtra("UserKey");
        Log.d(TAG, "UserKey from Intent: " + userKey);
      } else {
        userKey = null;
        Log.w(TAG, "No intent or extras available for UserKey");
      }
    }

    membersList = findViewById(R.id.lv2);

    // Show data and setup event handlers
    showInvitedMembersData();
    setupEventHandlers();
  }

  @Override
  public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
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
    membersList.setOnItemClickListener((parent, view, position, id) -> {});
    membersList.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void showInvitedMembersData() {
    Log.d(TAG, "Starting to load users data");

    // Show loading message
    Toast.makeText(this, LOADING_MESSAGE, Toast.LENGTH_SHORT).show();

    // Use server mode to get users - same as MembersComingActivity
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, User> users) {
            Log.d(TAG, "Received " + users.size() + " users from server");

            // Debug: Print FriendKeys
            if (friendKeys != null) {
              logFriendKeysContents();
            } else {
              Log.e(TAG, "FriendKeys is null!");
            }

            // Debug: Print all users from server
            Log.d(TAG, "All users from server:");
            for (Map.Entry<String, User> entry : users.entrySet()) {
              User user = entry.getValue();
              Log.d(
                  TAG,
                  "  User key: '"
                      + entry.getKey()
                      + "' -> email: '"
                      + (user != null ? user.getEmail() : "null user")
                      + "'");
              if (user != null) {
                Log.d(
                    TAG,
                    "    Full user: name='"
                        + user.getUsername()
                        + "', email='"
                        + user.getEmail()
                        + "'");
              }
            }

            ArrayList<User> invitedUsers = new ArrayList<>();
            HashMap<String, Object> groupFriends = friendKeys;

            for (User user : users.values()) {
              if (isValidUser(user)) {
                String normalizedEmail = user.getEmail().replace('.', ' ');
                Log.d(
                    TAG,
                    "Processing user: '"
                        + user.getEmail()
                        + "' -> normalized: '"
                        + normalizedEmail
                        + "'");

                if (groupFriends != null && isUserInvited(normalizedEmail, groupFriends)) {
                  Log.d(TAG, "Found matching user: " + normalizedEmail);
                  invitedUsers.add(user);
                }
              } else {
                Log.w(TAG, "Skipping invalid user: " + user);
              }
            }

            Log.d(TAG, "Found " + invitedUsers.size() + " invited users");

            if (invitedUsers.isEmpty()) {
              Toast.makeText(MembersInvitedActivity.this, NO_MEMBERS_MESSAGE, Toast.LENGTH_SHORT)
                  .show();
            }

            InvitedAdapter adapter =
                new InvitedAdapter(MembersInvitedActivity.this, 0, 0, invitedUsers, adminKey);
            membersList.setAdapter(adapter);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error loading users: " + errorMessage);
            Toast.makeText(
                    MembersInvitedActivity.this,
                    "Error loading users: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void logFriendKeysContents() {
    Log.d(TAG, "FriendKeys contents:");
    for (String key : friendKeys.keySet()) {
      Log.d(TAG, "  FriendKey: '" + key + "' -> " + friendKeys.get(key));
    }
  }

  private boolean isValidUser(User user) {
    return user != null && user.getEmail() != null;
  }

  private boolean isUserInvited(String normalizedEmail, HashMap<String, Object> groupFriends) {
    for (String friendKey : groupFriends.keySet()) {
      Log.d(TAG, "  Comparing '" + normalizedEmail + "' with '" + friendKey + "'");
      if (friendKey != null && friendKey.equals(normalizedEmail)) {
        return true;
      }
    }
    Log.d(TAG, "  No match found for user: " + normalizedEmail);
    return false;
  }

  private void showUserInfo(User user) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("User Information");
    builder.setMessage(
        "Email: "
            + user.getEmail()
            + "\nUsername: "
            + user.getUsername()
            + "\n\nDo you want to remove this user?");
  }

  @Override
  protected void clearActivityReferences() {
    // Clear UI components
    membersList = null;

    // Clear data objects
    friendKeys = null;
    adminKey = null;
  }
}
