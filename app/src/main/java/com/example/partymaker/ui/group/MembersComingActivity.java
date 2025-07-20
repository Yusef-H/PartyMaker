package com.example.partymaker.ui.group;

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
import com.example.partymaker.utilities.AuthHelper;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembersComingActivity extends AppCompatActivity {

  private static final String TAG = "MembersComingActivity";
  private ListView lv3;
  private HashMap<String, Object> ComingKeys;
  private String adminKey;
  private String GroupKey;
  private String UserKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_party_coming);

    // this 3 lines changes title's name
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("Coming to party");
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Log.e(TAG, "No extras received from intent");
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // connection between intent from GroupScreen and InvitedList
    ComingKeys = extras.getComingKeys();
    adminKey = extras.getAdminKey();
    GroupKey = extras.getGroupKey();

    Log.d(
        TAG, "Received extras - ComingKeys: " + (ComingKeys != null ? ComingKeys.size() : "null"));
    Log.d(TAG, "AdminKey: " + adminKey);
    Log.d(TAG, "GroupKey: " + GroupKey);

    // Debug: Print detailed ComingKeys information
    if (ComingKeys != null) {
      Log.d(TAG, "ComingKeys is not null, size: " + ComingKeys.size());
      Log.d(TAG, "ComingKeys contents:");
      for (String key : ComingKeys.keySet()) {
        Log.d(TAG, "  ComingKey: '" + key + "' -> " + ComingKeys.get(key));
      }
    } else {
      Log.e(TAG, "ComingKeys is null! This is the problem.");

      // Try to get ComingKeys directly from intent
      @SuppressWarnings("unchecked")
      HashMap<String, Object> directComingKeys =
          (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
      if (directComingKeys != null) {
        Log.d(TAG, "Found ComingKeys directly in intent, size: " + directComingKeys.size());
        ComingKeys = directComingKeys;
      } else {
        Log.e(TAG, "ComingKeys not found in intent either");
      }
    }

    // Get UserKey from AuthHelper instead of Firebase Auth
    try {
      UserKey = AuthHelper.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + UserKey);
    } catch (Exception e) {
      UserKey = getIntent().getStringExtra("UserKey");
      Log.d(TAG, "UserKey from Intent: " + UserKey);
    }

    lv3 = findViewById(R.id.lv3);

    // Show data directly without admin verification
    ShowData();
    EventHandler();
  }

  private void EventHandler() {
    lv3.setOnItemClickListener((parent, view, position, id) -> {});
    lv3.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void ShowData() {
    Log.d(TAG, "Starting to load users data");

    // Use server mode instead of direct Firebase access
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<Map<String, User>>() {
          @Override
          public void onSuccess(Map<String, User> users) {
            Log.d(TAG, "Received " + users.size() + " users from server");

            // Debug: Print ComingKeys
            if (ComingKeys != null) {
              Log.d(TAG, "ComingKeys contents:");
              for (String key : ComingKeys.keySet()) {
                Log.d(TAG, "  ComingKey: '" + key + "' -> " + ComingKeys.get(key));
              }
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

            ArrayList<User> ArrUsers = new ArrayList<>();
            HashMap<String, Object> ComingFriends = ComingKeys;

            for (User p : users.values()) {
              if (p != null && p.getEmail() != null) {
                String UserMail = p.getEmail().replace('.', ' ');
                Log.d(
                    TAG,
                    "Processing user: '" + p.getEmail() + "' -> normalized: '" + UserMail + "'");

                if (ComingFriends != null) {
                  boolean foundMatch = false;
                  for (String GroupFriend : ComingFriends.keySet()) {
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

            Log.d(TAG, "Found " + ArrUsers.size() + " coming users");

            InvitedAdapter adapt =
                new InvitedAdapter(MembersComingActivity.this, 0, 0, ArrUsers, adminKey);
            lv3.setAdapter(adapt);
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

  @Override
  public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
