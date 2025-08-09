package com.example.partymaker.ui.features.groups.discovery;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
import com.example.partymaker.ui.features.auth.LoginActivity;
import com.example.partymaker.ui.features.auxiliary.settings.ServerSettingsActivity;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.ui.navigation.NavigationManager;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PublicGroupsActivity extends AppCompatActivity {
  private static final String TAG = "PublicGroupsActivity";
  private static final String ACTION_BAR_START_COLOR = "#0E81D1";
  private static final String ACTION_BAR_END_COLOR = "#0E81D1";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;
  private static final String NO_PUBLIC_PARTIES_MESSAGE = "No public parties available";
  private static final String AUTH_ERROR_MESSAGE = "Authentication error. Please login again.";
  private static final int PUBLIC_GROUP_TYPE = 0;
  private static final int TODAY_FILTER = 0;
  private static final int WEEK_FILTER = 7;
  private static final String FREE_PRICE = "0";
  private static final String FREE_PRICE_TEXT = "free";

  private ArrayList<Group> groups;
  private String userKey;
  private Object groupsRef;
  private GroupAdapter allGroupsAdapter;
  private ArrayList<Group> allGroups = new ArrayList<>();
  private ArrayList<Group> filteredGroups = new ArrayList<>();
  private ChipGroup chipGroupFilters;
  private EditText searchEditText;
  private String currentSearchText = "";
  private int currentFilterChipId = R.id.chipAll;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_public_parties);

    setupToolbar();
    if (!initializeUser()) return;
    initializeViews();
    setupEventHandlers();
    setupBottomNavigation();
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

  private void setupToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
    }
    setupActionBar();
  }

  private boolean initializeUser() {
    try {
      userKey = AuthenticationManager.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + userKey);
      return true;
    } catch (Exception e) {
      Log.e(TAG, "Failed to get current user from AuthHelper", e);
      Toast.makeText(this, AUTH_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
      finish();
      return false;
    }
  }

  private void initializeViews() {
    RecyclerView lv1 = findViewById(R.id.lv5);
    if (lv1 != null) {
      lv1.setLayoutManager(new LinearLayoutManager(this));
      allGroupsAdapter = new GroupAdapter(this, this::navigateToJoinGroup);
      lv1.setAdapter(allGroupsAdapter);
    }

    // Initialize chip filter
    chipGroupFilters = findViewById(R.id.chipGroupFilters);
    
    // Initialize search
    searchEditText = findViewById(R.id.etSearch);
  }

  private void setupEventHandlers() {
    // Setup chip filtering
    if (chipGroupFilters != null) {
      chipGroupFilters.setOnCheckedStateChangeListener(
          (group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
              currentFilterChipId = checkedIds.get(0);
              applyFilterAndSearch();
            }
          });
    }
    
    // Setup search
    if (searchEditText != null) {
      searchEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          currentSearchText = s.toString();
          applyFilterAndSearch();
        }

        @Override
        public void afterTextChanged(Editable s) {}
      });
    }
  }

  private void setupBottomNavigation() {
    NavigationManager.setupBottomNavigation(this, "publicparties");
  }

  private void navigateToJoinGroup(Group group) {
    Intent intent = new Intent(getBaseContext(), JoinGroupActivity.class);
    ExtrasMetadata extras = createExtrasFromGroup(group);
    IntentExtrasManager.addExtrasToIntent(intent, extras);
    startActivity(intent);
  }

  private ExtrasMetadata createExtrasFromGroup(Group group) {
    return new ExtrasMetadata(
        group.getGroupName(),
        group.getGroupKey(),
        group.getGroupDays(),
        group.getGroupMonths(),
        group.getGroupYears(),
        group.getGroupHours(),
        group.getGroupLocation(),
        group.getAdminKey(),
        group.getCreatedAt(),
        group.getGroupPrice(),
        group.getGroupType(),
        group.isCanAdd(),
        group.getFriendKeys(),
        group.getComingKeys(),
        group.getMessageKeys());
  }

  public void loadPublicGroups() {
    initializeGroupsRef();
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

  private void initializeGroupsRef() {
    if (groupsRef == null) {
      FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
      groupsRef = accessManager.getGroupsRef();
    }
  }

  private void processServerGroupData(Map<String, Group> groupData) {
    ArrayList<Group> groupList = filterPublicGroups(groupData);
    allGroups = new ArrayList<>(groupList);

    if (!allGroups.isEmpty()) {
      // Apply initial filter and search
      applyFilterAndSearch();
    } else {
      Toast.makeText(this, NO_PUBLIC_PARTIES_MESSAGE, Toast.LENGTH_SHORT).show();
    }
  }

  private ArrayList<Group> filterPublicGroups(Map<String, Group> groupData) {
    ArrayList<Group> groupList = new ArrayList<>();
    for (Group group : groupData.values()) {
      if (isPublicGroupAndUserNotMember(group)) {
        groupList.add(group);
      }
    }
    return groupList;
  }

  private boolean isPublicGroupAndUserNotMember(Group group) {
    if (group.getGroupType() != PUBLIC_GROUP_TYPE) {
      return false;
    }

    HashMap<String, Object> groupUserKeys = group.getFriendKeys();
    if (groupUserKeys != null) {
      for (String key : groupUserKeys.keySet()) {
        if (userKey.equals(key)) {
          return false;
        }
      }
    }
    return true;
  }

  private void applyFilterAndSearch() {
    if (allGroups == null || allGroups.isEmpty()) {
      Log.w(TAG, "No groups to filter");
      if (allGroupsAdapter != null) {
        allGroupsAdapter.updateItems(new ArrayList<>());
      }
      return;
    }

    // First apply chip filter
    ArrayList<Group> chipFiltered = new ArrayList<>();
    
    if (currentFilterChipId == R.id.chipAll) {
      chipFiltered = new ArrayList<>(allGroups);
    } else if (currentFilterChipId == R.id.chipToday) {
      chipFiltered = filterGroupsByDate(allGroups, TODAY_FILTER);
    } else if (currentFilterChipId == R.id.chipThisWeek) {
      chipFiltered = filterGroupsByDate(allGroups, WEEK_FILTER);
    } else if (currentFilterChipId == R.id.chipFree) {
      chipFiltered = filterGroupsByPrice(allGroups);
    }

    // Then apply search filter
    filteredGroups.clear();
    if (currentSearchText.isEmpty()) {
      filteredGroups.addAll(chipFiltered);
    } else {
      String lowerSearchText = currentSearchText.toLowerCase();
      for (Group group : chipFiltered) {
        if (group.getGroupName() != null && 
            group.getGroupName().toLowerCase().contains(lowerSearchText)) {
          filteredGroups.add(group);
        }
      }
    }

    // Update adapter
    if (allGroupsAdapter != null) {
      allGroupsAdapter.updateItems(filteredGroups);
    }

    Log.d(TAG, "Filter and search applied, showing " + filteredGroups.size() + " groups");
  }
  
  private void applyFilter(int chipId) {
    currentFilterChipId = chipId;
    applyFilterAndSearch();
  }

  private ArrayList<Group> filterGroupsByDate(ArrayList<Group> groups, int daysFromNow) {
    ArrayList<Group> filtered = new ArrayList<>();
    Calendar[] dateRange = calculateDateRange(daysFromNow);
    Calendar startDate = dateRange[0];
    Calendar endDate = dateRange[1];

    for (Group group : groups) {
      if (isGroupInDateRange(group, startDate, endDate)) {
        filtered.add(group);
      }
    }

    return filtered;
  }

  private Calendar[] calculateDateRange(int daysFromNow) {
    Calendar startDate = Calendar.getInstance();
    Calendar endDate = Calendar.getInstance();

    if (daysFromNow == TODAY_FILTER) {
      startDate.set(Calendar.HOUR_OF_DAY, 0);
      startDate.set(Calendar.MINUTE, 0);
      startDate.set(Calendar.SECOND, 0);
      endDate.set(Calendar.HOUR_OF_DAY, 23);
      endDate.set(Calendar.MINUTE, 59);
      endDate.set(Calendar.SECOND, 59);
    } else {
      endDate.add(Calendar.DAY_OF_YEAR, daysFromNow);
    }

    return new Calendar[] {startDate, endDate};
  }

  private boolean isGroupInDateRange(Group group, Calendar startDate, Calendar endDate) {
    try {
      String groupDateStr =
          group.getGroupDays() + "/" + group.getGroupMonths() + "/" + group.getGroupYears();
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
      Date groupDate = dateFormat.parse(groupDateStr);

      if (groupDate != null) {
        Calendar groupCalendar = Calendar.getInstance();
        groupCalendar.setTime(groupDate);
        return groupCalendar.compareTo(startDate) >= 0 && groupCalendar.compareTo(endDate) <= 0;
      }
    } catch (Exception e) {
      Log.w(TAG, "Failed to parse date for group: " + group.getGroupName(), e);
    }
    return false;
  }

  private ArrayList<Group> filterGroupsByPrice(ArrayList<Group> groups) {
    ArrayList<Group> filtered = new ArrayList<>();
    for (Group group : groups) {
      if (isFreeGroup(group)) {
        filtered.add(group);
      }
    }
    return filtered;
  }

  private boolean isFreeGroup(Group group) {
    try {
      String priceStr = group.getGroupPrice();
      return priceStr == null
          || priceStr.equals(FREE_PRICE)
          || priceStr.equalsIgnoreCase(FREE_PRICE_TEXT);
    } catch (Exception e) {
      Log.w(TAG, "Failed to parse price for group: " + group.getGroupName(), e);
      return false;
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
