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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.example.partymaker.ui.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.partymaker.R;
import com.facebook.shimmer.ShimmerFrameLayout;
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

public class PublicGroupsActivity extends BaseActivity {
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
  private final ArrayList<Group> filteredGroups = new ArrayList<>();
  private ChipGroup chipGroupFilters;
  private EditText searchEditText;
  private String currentSearchText = "";
  private int currentFilterChipId = R.id.chipAll;
  private SwipeRefreshLayout swipeRefreshLayout;
  private ShimmerFrameLayout shimmerFrameLayout;
  private RecyclerView recyclerView;
  private android.widget.ImageButton filterButton;
  
  // Sort and Filter State
  private String currentSortMode = "date_nearest"; // default sort
  private boolean filterOnlyFree = false;
  private boolean filterTodayOnly = false;
  private boolean filterWeekOnly = false;
  private boolean filterUpcomingOnly = false;

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
    recyclerView = findViewById(R.id.lv5);
    if (recyclerView != null) {
      setupPublicGroupsRecyclerView();
    }

    // Initialize Shimmer
    shimmerFrameLayout = findViewById(R.id.shimmer_view_container);

    // Initialize chip filter
    chipGroupFilters = findViewById(R.id.chipGroupFilters);
    
    // Initialize search
    searchEditText = findViewById(R.id.etSearch);
    
    // Initialize filter button
    filterButton = findViewById(R.id.btnFilter);
    
    // Initialize SwipeRefreshLayout
    swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    setupSwipeRefresh();
  }

  private void setupPublicGroupsRecyclerView() {
    // Performance optimizations
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemViewCacheSize(25);
    recyclerView.setDrawingCacheEnabled(true);
    recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    
    // Custom layout manager with performance optimizations
    LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
      @Override
      public boolean supportsPredictiveItemAnimations() {
        return false; // Better scroll performance
      }
    };
    recyclerView.setLayoutManager(layoutManager);
    
    // Shared RecyclerView pool for memory efficiency
    RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
    sharedPool.setMaxRecycledViews(0, 25);
    recyclerView.setRecycledViewPool(sharedPool);
    
    // Set adapter
    allGroupsAdapter = new GroupAdapter(this, this::navigateToJoinGroup);
    recyclerView.setAdapter(allGroupsAdapter);
    
    Log.d(TAG, "Public groups RecyclerView configured with performance optimizations");
  }

  private void setupEventHandlers() {
    // Setup filter button
    if (filterButton != null) {
      filterButton.setOnClickListener(v -> showSortFilterDialog());
    }
    
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
  
  private void setupSwipeRefresh() {
    if (swipeRefreshLayout != null) {
      // Set refresh colors to match app theme
      swipeRefreshLayout.setColorSchemeResources(
          R.color.colorPrimary,
          R.color.colorAccent,
          R.color.blue_primary
      );
      
      // Set refresh listener
      swipeRefreshLayout.setOnRefreshListener(this::refreshPublicGroups);
    }
  }
  
  private void refreshPublicGroups() {
    Log.d(TAG, "Refreshing public groups...");
    // Clear search when refreshing
    if (searchEditText != null) {
      searchEditText.setText("");
    }
    currentSearchText = "";
    
    // Reload groups from server
    loadPublicGroups();
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
    // Show Shimmer only if no data exists
    if (allGroups.isEmpty() && shimmerFrameLayout != null) {
      shimmerFrameLayout.setVisibility(android.view.View.VISIBLE);
      shimmerFrameLayout.startShimmer();
      recyclerView.setVisibility(android.view.View.GONE);
    }
    
    initializeGroupsRef();
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getGroups(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, Group> data) {
            processServerGroupData(data);
            // Hide Shimmer
            hideShimmer();
            // Stop refresh animation
            if (swipeRefreshLayout != null) {
              swipeRefreshLayout.setRefreshing(false);
            }
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    PublicGroupsActivity.this, "Server error: " + errorMessage, Toast.LENGTH_SHORT)
                .show();
            // Hide Shimmer
            hideShimmer();
            // Stop refresh animation even on error
            if (swipeRefreshLayout != null) {
              swipeRefreshLayout.setRefreshing(false);
            }
          }
        });
  }
  
  private void hideShimmer() {
    if (shimmerFrameLayout != null) {
      shimmerFrameLayout.stopShimmer();
      shimmerFrameLayout.setVisibility(android.view.View.GONE);
    }
    if (recyclerView != null) {
      recyclerView.setVisibility(android.view.View.VISIBLE);
    }
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

    // Hide Shimmer when data is processed
    hideShimmer();

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

    // Start with all groups
    ArrayList<Group> results = new ArrayList<>(allGroups);
    
    // Apply chip filter
    if (currentFilterChipId == R.id.chipToday) {
      results = filterGroupsByDate(results, TODAY_FILTER);
    } else if (currentFilterChipId == R.id.chipThisWeek) {
      results = filterGroupsByDate(results, WEEK_FILTER);
    } else if (currentFilterChipId == R.id.chipFree) {
      results = filterGroupsByPrice(results);
    }
    
    // Apply additional filters from dialog
    results = applyAdditionalFilters(results);
    
    // Apply search filter
    if (!currentSearchText.isEmpty()) {
      ArrayList<Group> searchFiltered = new ArrayList<>();
      String lowerSearchText = currentSearchText.toLowerCase();
      for (Group group : results) {
        if (group.getGroupName() != null && 
            group.getGroupName().toLowerCase().contains(lowerSearchText)) {
          searchFiltered.add(group);
        }
      }
      results = searchFiltered;
    }
    
    // Apply sorting
    results = sortGroups(results);
    
    // Update the filtered list
    filteredGroups.clear();
    filteredGroups.addAll(results);

    // Update adapter
    if (allGroupsAdapter != null) {
      allGroupsAdapter.updateItems(filteredGroups);
    }

    Log.d(TAG, "Filter and search applied, showing " + filteredGroups.size() + " groups");
  }
  
  private ArrayList<Group> applyAdditionalFilters(ArrayList<Group> groups) {
    ArrayList<Group> filtered = new ArrayList<>();
    
    for (Group group : groups) {
      boolean passesFilter = true;
      
      // Free only filter from dialog
      if (filterOnlyFree) {
        if (!isFreeGroup(group)) {
          passesFilter = false;
        }
      }
      
      // Today only filter from dialog
      if (filterTodayOnly && !filterWeekOnly) {
        ArrayList<Group> todayGroups = filterGroupsByDate(groups, TODAY_FILTER);
        if (!todayGroups.contains(group)) {
          passesFilter = false;
        }
      }
      
      // Week only filter from dialog
      if (filterWeekOnly) {
        ArrayList<Group> weekGroups = filterGroupsByDate(groups, WEEK_FILTER);
        if (!weekGroups.contains(group)) {
          passesFilter = false;
        }
      }
      
      // Upcoming only filter
      if (filterUpcomingOnly) {
        try {
          String dateStr = group.getGroupDays() + "/" + group.getGroupMonths() + "/" + group.getGroupYears();
          SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
          Date groupDate = sdf.parse(dateStr);
          if (groupDate != null && groupDate.before(new Date())) {
            passesFilter = false;
          }
        } catch (Exception e) {
          passesFilter = false;
        }
      }
      
      if (passesFilter) {
        filtered.add(group);
      }
    }
    
    return filtered;
  }
  
  private ArrayList<Group> sortGroups(ArrayList<Group> groups) {
    ArrayList<Group> sorted = new ArrayList<>(groups);
    
    switch (currentSortMode) {
      case "date_nearest":
        sorted.sort((g1, g2) -> compareDates(g1, g2));
        break;
        
      case "date_farthest":
        sorted.sort((g1, g2) -> compareDates(g2, g1));
        break;
        
      case "name_az":
        sorted.sort((g1, g2) -> {
          String name1 = g1.getGroupName() != null ? g1.getGroupName() : "";
          String name2 = g2.getGroupName() != null ? g2.getGroupName() : "";
          return name1.compareToIgnoreCase(name2);
        });
        break;
        
      case "name_za":
        sorted.sort((g1, g2) -> {
          String name1 = g1.getGroupName() != null ? g1.getGroupName() : "";
          String name2 = g2.getGroupName() != null ? g2.getGroupName() : "";
          return name2.compareToIgnoreCase(name1);
        });
        break;
        
      case "recently_added":
        // Reverse the list to show newest first
        java.util.Collections.reverse(sorted);
        break;
    }
    
    return sorted;
  }
  
  private int compareDates(Group g1, Group g2) {
    try {
      String date1 = g1.getGroupDays() + "/" + g1.getGroupMonths() + "/" + g1.getGroupYears();
      String date2 = g2.getGroupDays() + "/" + g2.getGroupMonths() + "/" + g2.getGroupYears();
      
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
      Date d1 = sdf.parse(date1);
      Date d2 = sdf.parse(date2);
      
      if (d1 != null && d2 != null) {
        return d1.compareTo(d2);
      }
    } catch (Exception e) {
      Log.w(TAG, "Error comparing dates", e);
    }
    return 0;
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
  
  private void showSortFilterDialog() {
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
    android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_sort_filter, null);
    builder.setView(dialogView);
    
    // Get dialog components
    android.widget.RadioGroup radioGroupSort = dialogView.findViewById(R.id.radioGroupSort);
    com.google.android.material.checkbox.MaterialCheckBox checkboxPublic = dialogView.findViewById(R.id.checkboxPublic);
    com.google.android.material.checkbox.MaterialCheckBox checkboxPrivate = dialogView.findViewById(R.id.checkboxPrivate);
    com.google.android.material.checkbox.MaterialCheckBox checkboxFree = dialogView.findViewById(R.id.checkboxFree);
    com.google.android.material.checkbox.MaterialCheckBox checkboxUpcoming = dialogView.findViewById(R.id.checkboxUpcoming);
    
    // Hide public/private checkboxes for PublicGroups screen (all are public here)
    checkboxPublic.setVisibility(android.view.View.GONE);
    checkboxPrivate.setVisibility(android.view.View.GONE);
    
    // Set current values
    switch (currentSortMode) {
      case "date_nearest":
        radioGroupSort.check(R.id.radioDateNearest);
        break;
      case "date_farthest":
        radioGroupSort.check(R.id.radioDateFarthest);
        break;
      case "name_az":
        radioGroupSort.check(R.id.radioNameAZ);
        break;
      case "name_za":
        radioGroupSort.check(R.id.radioNameZA);
        break;
      case "recently_added":
        radioGroupSort.check(R.id.radioRecentlyAdded);
        break;
    }
    
    checkboxFree.setChecked(filterOnlyFree);
    checkboxUpcoming.setChecked(filterUpcomingOnly);
    
    android.app.AlertDialog dialog = builder.create();
    
    // Setup dialog buttons
    dialogView.findViewById(R.id.btnReset).setOnClickListener(v -> {
      // Reset to defaults
      radioGroupSort.check(R.id.radioDateNearest);
      checkboxFree.setChecked(false);
      checkboxUpcoming.setChecked(false);
      // Also reset chips
      if (chipGroupFilters != null) {
        chipGroupFilters.check(R.id.chipAll);
      }
    });
    
    dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
    
    dialogView.findViewById(R.id.btnApply).setOnClickListener(v -> {
      // Apply the selected filters and sort
      int selectedSortId = radioGroupSort.getCheckedRadioButtonId();
      if (selectedSortId == R.id.radioDateNearest) {
        currentSortMode = "date_nearest";
      } else if (selectedSortId == R.id.radioDateFarthest) {
        currentSortMode = "date_farthest";
      } else if (selectedSortId == R.id.radioNameAZ) {
        currentSortMode = "name_az";
      } else if (selectedSortId == R.id.radioNameZA) {
        currentSortMode = "name_za";
      } else if (selectedSortId == R.id.radioRecentlyAdded) {
        currentSortMode = "recently_added";
      }
      
      filterOnlyFree = checkboxFree.isChecked();
      filterUpcomingOnly = checkboxUpcoming.isChecked();
      
      // Apply the filters
      applyFilterAndSearch();
      
      dialog.dismiss();
    });
    
    dialog.show();
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
