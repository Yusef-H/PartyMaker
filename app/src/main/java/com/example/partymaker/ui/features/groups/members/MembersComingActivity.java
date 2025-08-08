package com.example.partymaker.ui.features.groups.members;

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
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembersComingActivity extends AppCompatActivity {

  private static final String TAG = "MembersComingActivity";
  
  // UI constants
  private static final String ACTIVITY_TITLE = "Coming to party";
  private static final String ACTION_BAR_COLOR = "#0081d1";
  private static final String LOADING_MESSAGE = "Loading coming members...";
  private static final String NO_MEMBERS_MESSAGE = "No members coming";
  
  // UI Components
  private ListView membersList;
  private HashMap<String, Object> comingKeys;
  private String adminKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_party_coming);

    setupActionBar();

    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Log.e(TAG, "No extras received from intent");
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // Extract data from intent extras
    comingKeys = extras.getComingKeys();
    adminKey = extras.getAdminKey();
    String groupKey = extras.getGroupKey();

    Log.d(
        TAG, "Received extras - ComingKeys: " + (comingKeys != null ? comingKeys.size() : "null"));
    Log.d(TAG, "AdminKey: " + adminKey);
    Log.d(TAG, "GroupKey: " + groupKey);

    // Debug: Print detailed ComingKeys information
    if (comingKeys != null) {
      Log.d(TAG, "ComingKeys is not null, size: " + comingKeys.size());
      logComingKeysDetails();
      for (String key : comingKeys.keySet()) {
        Log.d(TAG, "  ComingKey: '" + key + "' -> " + comingKeys.get(key));
      }
    } else {
      Log.e(TAG, "ComingKeys is null! This is the problem.");

      // Try to get ComingKeys directly from intent as fallback
      comingKeys = extractComingKeysFromIntent();
      if (comingKeys != null) {
        Log.d(TAG, "Found ComingKeys directly in intent, size: " + comingKeys.size());
      } else {
        Log.e(TAG, "ComingKeys not found in intent either");
      }
    }

    // Get UserKey from AuthHelper instead of Firebase Auth
    String userKey;
    try {
      userKey = AuthenticationManager.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + userKey);
    } catch (Exception e) {
      userKey = getIntent().getStringExtra("UserKey");
      Log.d(TAG, "UserKey from Intent: " + userKey);
    }

    membersList = findViewById(R.id.lv3);

    // Show data and setup event handlers
    showMembersData();
    setupEventHandlers();
  }

  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(ACTIVITY_TITLE);
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(ACTION_BAR_COLOR)));
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }
  
  private void logComingKeysDetails() {
    Log.d(TAG, "ComingKeys contents:");
  }
  
  @SuppressWarnings("unchecked")
  private HashMap<String, Object> extractComingKeysFromIntent() {
    return (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
  }
  
  private void setupEventHandlers() {
    membersList.setOnItemClickListener((parent, view, position, id) -> {});
    membersList.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void showMembersData() {
    Log.d(TAG, "Starting to load users data");

    // Use server mode instead of direct Firebase access
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, User> users) {
            Log.d(TAG, "Received " + users.size() + " users from server");

            // Debug: Print ComingKeys
            if (comingKeys != null) {
              logComingKeysContents();
            } else {
              Log.e(TAG, "ComingKeys is null!");
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

            ArrayList<User> comingUsers = new ArrayList<>();
            HashMap<String, Object> comingFriends = comingKeys;

            for (User user : users.values()) {
              if (isValidUser(user)) {
                String normalizedEmail = user.getEmail().replace('.', ' ');
                Log.d(TAG, "Processing user: '" + user.getEmail() + "' -> normalized: '" + normalizedEmail + "'");

                if (comingFriends != null && isUserComing(normalizedEmail, comingFriends)) {
                  Log.d(TAG, "Found matching user: " + normalizedEmail);
                  comingUsers.add(user);
                }
              } else {
                Log.w(TAG, "Skipping invalid user: " + user);
              }
            }

            Log.d(TAG, "Found " + comingUsers.size() + " coming users");

            InvitedAdapter adapter =
                new InvitedAdapter(MembersComingActivity.this, 0, 0, comingUsers, adminKey);
            membersList.setAdapter(adapter);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error loading users: " + errorMessage);
            Toast.makeText(
                    MembersComingActivity.this,
                    "Error loading users: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
  
  private void logComingKeysContents() {
    Log.d(TAG, "ComingKeys contents:");
    for (String key : comingKeys.keySet()) {
      Log.d(TAG, "  ComingKey: '" + key + "' -> " + comingKeys.get(key));
    }
  }
  
  private boolean isValidUser(User user) {
    return user != null && user.getEmail() != null;
  }
  
  private boolean isUserComing(String normalizedEmail, HashMap<String, Object> comingFriends) {
    for (String friendKey : comingFriends.keySet()) {
      Log.d(TAG, "  Comparing '" + normalizedEmail + "' with '" + friendKey + "'");
      if (friendKey != null && friendKey.equals(normalizedEmail)) {
        return true;
      }
    }
    Log.d(TAG, "  No match found for user: " + normalizedEmail);
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
