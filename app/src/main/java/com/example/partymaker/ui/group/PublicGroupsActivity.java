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
import androidx.annotation.NonNull;
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
import com.example.partymaker.ui.settings.ServerSettingsActivity;
import com.example.partymaker.utilities.AuthHelper;
import com.example.partymaker.utilities.BottomNavigationHelper;
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

    // Hide action bar to remove black bar at top
    androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // Initialize user
    initializeUser();

    // Initialize views
    initializeViews();

    // Setup event handlers
    setupEventHandlers();

    // Setup bottom navigation
    setupBottomNavigation();

    // Load public groups
    loadPublicGroups();
  }

  private void initializeUser() {
    try {
      UserKey = AuthHelper.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + UserKey);
    } catch (Exception e) {
      Log.e(TAG, "Failed to get current user from AuthHelper", e);
      Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }
  }

  private void initializeViews() {
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
  }

  private void setupEventHandlers() {
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

  private void setupBottomNavigation() {
    BottomNavigationHelper.setupBottomNavigation(this, "publicparties");
  }

  public void loadPublicGroups() {
    // Initialize groupsRef if not already done
    if (groupsRef == null) {
      FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
      groupsRef = accessManager.getGroupsRef();
    }
    
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

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      Intent intent = new Intent(PublicGroupsActivity.this, ServerSettingsActivity.class);
      startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.logout) {
      AuthHelper.clearAuthData(this);
      Intent intent = new Intent(PublicGroupsActivity.this, LoginActivity.class);
      startActivity(intent);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }
}
