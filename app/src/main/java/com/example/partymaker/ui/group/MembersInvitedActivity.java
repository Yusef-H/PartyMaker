package com.example.partymaker.ui.group;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.InvitedAdapter;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.data.Common;
import com.example.partymaker.utils.data.ExtrasMetadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembersInvitedActivity extends AppCompatActivity {

  private static final String TAG = "MembersInvitedActivity";
  private ListView lv2;
  private HashMap<String, Object> FriendKeys;
  private String adminKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_party_invited);

    // Set up the toolbar/action bar
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("Invited Members");
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Get extras from intent
    Intent intent = getIntent();
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(intent);
    if (extras != null) {
      FriendKeys = extras.getFriendKeys();
      adminKey = extras.getAdminKey();
      String groupKey = extras.getGroupKey();

      Log.d(
          TAG,
          "Received extras - FriendKeys: " + (FriendKeys != null ? FriendKeys.size() : "null"));
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
      userKey = intent.getStringExtra("UserKey");
      Log.d(TAG, "UserKey from Intent: " + userKey);
    }

    lv2 = findViewById(R.id.lv2);

    // Show data directly without admin verification
    ShowData();
    EventHandler();
  }

  @Override
  public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void EventHandler() {
    lv2.setOnItemClickListener((parent, view, position, id) -> {});
    lv2.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void ShowData() {
    Log.d(TAG, "Starting to load users data");

    // Show loading message
    Toast.makeText(this, "Loading invited members...", Toast.LENGTH_SHORT).show();

    // Use server mode to get users - same as MembersComingActivity
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, User> users) {
            Log.d(TAG, "Received " + users.size() + " users from server");

            // Debug: Print FriendKeys
            if (FriendKeys != null) {
              Log.d(TAG, "FriendKeys contents:");
              for (String key : FriendKeys.keySet()) {
                Log.d(TAG, "  FriendKey: '" + key + "' -> " + FriendKeys.get(key));
              }
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

            ArrayList<User> ArrUsers = new ArrayList<>();
            HashMap<String, Object> GroupFriends;

            for (User p : users.values()) {
              if (p != null && p.getEmail() != null) {
                String UserMail = p.getEmail().replace('.', ' ');
                Log.d(
                    TAG,
                    "Processing user: '" + p.getEmail() + "' -> normalized: '" + UserMail + "'");

                GroupFriends = FriendKeys;
                if (GroupFriends != null) {
                  boolean foundMatch = false;
                  for (String GroupFriend : GroupFriends.keySet()) {
                    Log.d(TAG, "  Comparing '" + UserMail + "' with '" + GroupFriend + "'");
                    if (GroupFriend != null && GroupFriend.equals(UserMail)) {
                      Log.d(TAG, "Found matching user: " + UserMail);
                      ArrUsers.add(p);
                      foundMatch = true;
                      break;
                    }
                  }
                  if (!foundMatch) {
                    Log.d(TAG, "  No match found for user: " + UserMail);
                  }
                }
              } else {
                Log.w(TAG, "Skipping user with null email: " + p);
              }
            }

            Log.d(TAG, "Found " + ArrUsers.size() + " invited users");

            if (ArrUsers.isEmpty()) {
              Toast.makeText(
                      MembersInvitedActivity.this, "No invited members found", Toast.LENGTH_SHORT)
                  .show();
            }

            InvitedAdapter adapt =
                new InvitedAdapter(MembersInvitedActivity.this, 0, 0, ArrUsers, adminKey);
            lv2.setAdapter(adapt);
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
}
