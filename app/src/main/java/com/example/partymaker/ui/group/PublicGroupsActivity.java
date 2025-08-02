package com.example.partymaker.ui.group;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.GroupAdapter;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.ui.settings.ServerSettingsActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.navigation.BottomNavigationHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PublicGroupsActivity extends AppCompatActivity {
  private static final String TAG = "PublicGroupsActivity";
  private static final String ACTION_BAR_START_COLOR = "#0E81D1";
  private static final String ACTION_BAR_END_COLOR = "#0E81D1";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;
  ArrayList<Group> group;
  String UserKey;
  private Object groupsRef;
  private GroupAdapter allGroupsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_public_parties);

    // Set up toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      setupActionBar();
    } else {
      // Fallback to regular action bar if toolbar not found
      setupActionBar();
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

  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      Log.w(TAG, "ActionBar not available");
      return;
    }

    try {
      // Set gradient background
      GradientDrawable gradient = new GradientDrawable();
      gradient.setShape(GradientDrawable.RECTANGLE);
      gradient.setColors(
          new int[] {
            Color.parseColor(ACTION_BAR_START_COLOR), Color.parseColor(ACTION_BAR_END_COLOR)
          });
      gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
      actionBar.setBackgroundDrawable(gradient);

      // Set title with color
      String styledTitle =
          String.format(
              "<font color='%s'><b>%s</b></font>", ACTION_BAR_TITLE_COLOR, "Public Parties");
      actionBar.setTitle(Html.fromHtml(styledTitle, Html.FROM_HTML_MODE_LEGACY));

      // Configure other properties
      actionBar.setElevation(ACTION_BAR_ELEVATION);
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);

      Log.d(TAG, "ActionBar setup completed");
    } catch (Exception e) {
      Log.e(TAG, "Error setting up ActionBar", e);
    }
  }

  private void initializeUser() {
    try {
      UserKey = AuthenticationManager.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + UserKey);
    } catch (Exception e) {
      Log.e(TAG, "Failed to get current user from AuthHelper", e);
      Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void initializeViews() {
    RecyclerView lv1 = findViewById(R.id.lv5);
    if (lv1 != null) {
      lv1.setLayoutManager(new LinearLayoutManager(this));
      allGroupsAdapter =
          new GroupAdapter(
              this,
              group -> {
                // intent Value
                String groupName = group.getGroupName();
                String groupKey = group.getGroupKey();
                String groupDays = group.getGroupDays();
                String groupMonths = group.getGroupMonths();
                String groupYears = group.getGroupYears();
                String groupHours = group.getGroupHours();
                String groupLocation = group.getGroupLocation();
                String adminKey = group.getAdminKey();
                String createdAt = group.getCreatedAt();
                String GroupPrice = group.getGroupPrice();
                int GroupType = group.getGroupType();
                boolean CanAdd = group.isCanAdd();
                HashMap<String, Object> FriendKeys = group.getFriendKeys();
                HashMap<String, Object> ComingKeys = group.getComingKeys();
                HashMap<String, Object> MessageKeys = group.getMessageKeys();
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
                IntentExtrasManager.addExtrasToIntent(intent, extras);
                startActivity(intent);
              });
      lv1.setAdapter(allGroupsAdapter);
    }
  }

  private void setupEventHandlers() {
    // אין צורך ב-setOnItemClickListener, הכל עובר דרך ה-Listener של GroupAdapter החדש
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
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getGroups(
        new FirebaseServerClient.DataCallback<>() {
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

  private void processServerGroupData(Map<String, Group> groupData) {
    ArrayList<Group> groupList = new ArrayList<>();
    for (Group p : groupData.values()) {
      HashMap<String, Object> UserKeys = p.getFriendKeys();
      if (p.getGroupType() == 0) { // if group is public
        boolean flag = false;
        if (UserKeys != null) {
          for (String userKey : UserKeys.keySet()) { // scan all group friends
            if (UserKey.equals(userKey)) {
              flag = true;
              break;
            }
          }
        }
        if (!flag) {
          groupList.add(p);
        }
      }
    }
    if (allGroupsAdapter != null) {
      allGroupsAdapter.updateItems(groupList);
    }
    if (groupList.isEmpty()) {
      Toast.makeText(this, "No public parties available", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      // Handle the back button in the action bar
      onBackPressed();
      return true;
    } else if (itemId == R.id.settings) {
      Intent intent = new Intent(this, ServerSettingsActivity.class);
      startActivity(intent);
      return true;
    } else if (itemId == R.id.logout) {
      AuthenticationManager.logout(this);
      Intent intent = new Intent(this, LoginActivity.class);
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

  @Override
  public void onBackPressed() {
    // Call super first
    super.onBackPressed();
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
  }
}
