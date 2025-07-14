package com.example.partymaker.ui.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.GroupAdapter;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.ui.profile.EditProfileActivity;
import com.example.partymaker.utilities.AuthHelper;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PublicGroupsActivity extends AppCompatActivity {
  private ListView lv1;
  private Object groupsRef;
  ArrayList<Group> group;
  GroupAdapter allGroupsAdapter;
  String UserKey;
  private static final String TAG = "PublicGroupsActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_public_parties);

    // Change title Name and Color
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar)
        .setTitle(Html.fromHtml("<font color='#E4E9EF'>Public Parties</font>"));

    // set actionbar background
    @SuppressLint("UseCompatLoadingForDrawables")
    Drawable d = getResources().getDrawable(R.color.primaryBlue);
    actionBar.setBackgroundDrawable(d);

    // connection
    lv1 = findViewById(R.id.lv5);

    // Get UserKey from AuthHelper instead of Firebase Auth
    try {
      UserKey = AuthHelper.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + UserKey);
    } catch (Exception e) {
      Log.e(TAG, "Failed to get current user from AuthHelper", e);
      Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    // Initialize Firebase database reference
    FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
    groupsRef = accessManager.getGroupsRef();

    retrieveData();
    EventHandler();
  }

  private void EventHandler() {
    lv1.setOnItemClickListener(
        (parent, view, position, id) -> {
          // intent Value
          String groupName = group.get(position).getGroupName();
          String groupKey = group.get(position).getGroupKey();
          String groupDays = group.get(position).getGroupDays();
          String groupMonths = group.get(position).getGroupMonths();
          String groupYears = group.get(position).getGroupYears();
          String groupHours = group.get(position).getGroupHours();
          String groupLocation = group.get(position).getGroupLocation();
          String adminKey = group.get(position).getAdminKey();
          String createdAt = group.get(position).getCreatedAt();
          String GroupPrice = group.get(position).getGroupPrice();
          int GroupType = group.get(position).getGroupType();
          boolean CanAdd = group.get(position).isCanAdd();
          HashMap<String, Object> FriendKeys = group.get(position).getFriendKeys();
          HashMap<String, Object> ComingKeys = group.get(position).getComingKeys();
          HashMap<String, Object> MessageKeys = group.get(position).getMessageKeys();
          Intent intent = new Intent(getBaseContext(), JoinGroupActivity.class);
          ExtrasMetadata extras =
              new ExtrasMetadata(
                  groupName,
                  groupKey,
                  groupDays,
                  groupMonths,
                  groupYears,
                  groupHours,
                  groupLocation,
                  adminKey,
                  createdAt,
                  GroupPrice,
                  GroupType,
                  CanAdd,
                  FriendKeys,
                  ComingKeys,
                  MessageKeys);
          Common.addExtrasToIntent(intent, extras);
          startActivity(intent);
        });
    lv1.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  public void retrieveData() {
    // Always use server mode
    FirebaseServerClient serverClient = (FirebaseServerClient) groupsRef;
    serverClient.getGroups(
        new FirebaseServerClient.DataCallback<Map<String, Group>>() {
          @Override
          public void onSuccess(Map<String, Group> data) {
            processServerGroupData(data);
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    PublicGroupsActivity.this, "Server error: " + errorMessage, Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void processGroupData(DataSnapshot dataSnapshot) {
    HashMap<String, Object> UserKeys;
    group = new ArrayList<>();

    for (DataSnapshot data : dataSnapshot.getChildren()) { // scan all group in data
      Group p = data.getValue(Group.class);
      UserKeys = Objects.requireNonNull(data.getValue(Group.class)).getFriendKeys();

      if (Objects.requireNonNull(p).getGroupType() == 0) { // if group is public
        boolean flag = false;
        for (String userKey : UserKeys.keySet()) { // scan all group friends
          if (UserKey.equals(userKey)) // if current user not friend in current group so it show
          // current group
          {
            flag = true;
            break;
          }
        }
        if (!flag) {
          group.add(p);
        }
      }
    }
    allGroupsAdapter = new GroupAdapter(PublicGroupsActivity.this, 0, 0, group);
    lv1.setAdapter(allGroupsAdapter);
  }

  private void processServerGroupData(Map<String, Group> groupData) {
    group = new ArrayList<>();

    for (Group p : groupData.values()) {
      HashMap<String, Object> UserKeys = p.getFriendKeys();

      if (p.getGroupType() == 0) { // if group is public
        boolean flag = false;
        for (String userKey : UserKeys.keySet()) { // scan all group friends
          if (UserKey.equals(userKey)) {
            flag = true;
            break;
          }
        }
        if (!flag) {
          group.add(p);
        }
      }
    }

    allGroupsAdapter = new GroupAdapter(PublicGroupsActivity.this, 0, 0, group);
    lv1.setAdapter(allGroupsAdapter);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    Intent goToNextActivity;

    if (item.getItemId() == R.id.idMenu) {
      goToNextActivity = new Intent(getApplicationContext(), MainActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idAddProfile) {
      goToNextActivity = new Intent(getApplicationContext(), CreateGroupActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idEditProfile) {
      goToNextActivity = new Intent(getApplicationContext(), EditProfileActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idPublicParties) {
      goToNextActivity = new Intent(getApplicationContext(), PublicGroupsActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idLogout) {
      DBRef.Auth.signOut();
      DBRef.CurrentUser = null;
      goToNextActivity = new Intent(getApplicationContext(), LoginActivity.class);
      startActivity(goToNextActivity);
    }

    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }
}
