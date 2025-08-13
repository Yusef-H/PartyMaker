package com.example.partymaker.ui.features.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.ui.base.BaseActivity;

import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.partymaker.R;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.GroupAdapter;
import com.example.partymaker.ui.features.auth.LoginActivity;
import com.example.partymaker.ui.features.auxiliary.chatbot.GptChatActivity;
import com.example.partymaker.ui.features.auxiliary.settings.ServerSettingsActivity;
import com.example.partymaker.ui.features.groups.main.PartyMainActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.ui.animation.ButtonAnimationHelper;
// import com.example.partymaker.utils.ui.animation.CustomRefreshAnimationHelper; // Removed - no refresh animations
import com.example.partymaker.utils.ui.components.LoadingStateManager;
import com.example.partymaker.utils.ui.components.UiStateManager;
import com.example.partymaker.utils.ui.feedback.UserFeedbackManager;
import com.example.partymaker.utils.ui.navigation.NavigationManager;
import com.example.partymaker.utils.ui.ViewOptimizationHelper;
import com.example.partymaker.utils.ui.AnimationOptimizer;
import com.example.partymaker.utils.ui.RecyclerViewScrollOptimizer;
import com.example.partymaker.utils.infrastructure.PerformanceMonitor;
import com.example.partymaker.viewmodel.core.MainActivityViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity {

  private static final String TAG = "MainActivity";

  // UI Style Constants
  private static final String ACTION_BAR_START_COLOR = "#0E81D1";
  private static final String ACTION_BAR_END_COLOR = "#0E81D1";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;

  // Business Constants
  private static final long REFRESH_COOLDOWN_MS = 30000; // 30 seconds cooldown
  private static final String APP_TITLE = "My Parties";

  // UI Animation and Timing Constants
  private static final int HANDLER_DELAY_MS = 100;
  private static final int NAVIGATION_DELAY_MS = 200;
  private static final int FAB_ANIMATION_DELAY_MS = 500;
  private static final int LOGOUT_DELAY_MS = 1000;
  private static final int GROUPS_LOADING_ANIMATION_DELAY_MS = 4000;

  // SharedPreferences keys
  private static final String PREFS_PARTY_MAKER = "PartyMakerPrefs";
  private static final String KEY_USER_EXPLICITLY_LOGGED_IN = "user_explicitly_logged_in";

  // UI Components
  private RecyclerView groupsRecyclerView;
  private FloatingActionButton chatFloatingActionButton;
  private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
  private View rootView;
  private EditText searchEditText;
  private ImageView clearSearchButton;
  private ImageView filterButton;
  private ShimmerFrameLayout shimmerFrameLayout;
  
  // Sort and Filter State
  private String currentSortMode = "date_nearest"; // default sort
  private boolean filterPublicOnly = false;
  private boolean filterPrivateOnly = false;
  
  // Date format patterns for parsing
  private static final java.text.SimpleDateFormat[] DATE_FORMATS = {
      new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()),
      new java.text.SimpleDateFormat("d/MM/yyyy", java.util.Locale.getDefault()),
      new java.text.SimpleDateFormat("dd/M/yyyy", java.util.Locale.getDefault()),
      new java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault()),
      new java.text.SimpleDateFormat("dd/MMMM/yyyy", java.util.Locale.ENGLISH),
      new java.text.SimpleDateFormat("d/MMMM/yyyy", java.util.Locale.ENGLISH),
      new java.text.SimpleDateFormat("dd/MMM/yyyy", java.util.Locale.ENGLISH),
      new java.text.SimpleDateFormat("d/MMM/yyyy", java.util.Locale.ENGLISH)
  };

 
  private boolean filterFreeOnly = false;
  private boolean filterUpcomingOnly = false;
  
  // ViewStub references for performance optimization
  private ViewStub stubShimmerLoading;
  private ViewStub stubEmptyState;
  private ViewStub stubLoadingOverlay;
  private ViewStub stubProgressBar;
  
  // Inflated views from ViewStubs
  private View inflatedShimmerView;
  private View inflatedEmptyView;
  private View inflatedLoadingOverlay;
  private View inflatedProgressBar;

  // Data Components
  private MainActivityViewModel viewModel;
  private String currentUserKey;
  private GroupAdapter groupAdapter;
  private final List<Group> allGroups = new ArrayList<>();
  private final List<Group> filteredGroups = new ArrayList<>();

  // UI State Management
  private LoadingStateManager loadingStateManager;

  // State tracking
  private boolean hasShownLoadingToast = false;
  private long lastRefreshTimeMillis = 0;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    PerformanceMonitor.startTiming("MainActivity.onCreate");
    PerformanceMonitor.trackMemoryUsage("MainActivity.onCreate.start");
    
    super.onCreate(savedInstanceState);
    try {
      initializeActivity();
      PerformanceMonitor.endTiming("MainActivity.onCreate");
      PerformanceMonitor.trackMemoryUsage("MainActivity.onCreate.end");
    } catch (Exception e) {
      Log.e(TAG, "Fatal error in onCreate", e);
      PerformanceMonitor.endTiming("MainActivity.onCreate");
      handleFatalError("An unexpected error occurred. Please restart the app.");
    }
  }

  private void initializeActivity() {
    setContentView(R.layout.activity_main);

    enforceServerConfiguration();

    if (!authenticateUser()) {
      return; // Exit if user authentication failed
    }

    initializeComponents();
    setupUserInterface();
    optimizeViews();
    loadInitialData();
  }

  private void initializeComponents() {
    viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    initializeViews();
  }

  private void setupUserInterface() {
    setupActionBar();
    setupEventHandlers();
    setupSearchBar();
    setupSwipeRefresh();
    setupFloatingChatButton();
    setupBottomNavigation();
    observeViewModel();
  }

  private void loadInitialData() {
    showLoadingState(); // Show loading without toast

    Log.d(TAG, "Loading groups for user: " + currentUserKey);
    viewModel.loadUserGroups(currentUserKey, true);
    updateLastRefreshTime();
  }

  private void showLoadingState() {
    showLoadingStateInternal(true, false);
  }

  private void hideLoadingState() {
    showLoadingStateInternal(false, false);
  }

  private void showLoadingStateInternal(boolean show, boolean showToast) {
    try {
      // Don't show loading state if we already have data (cached)
      boolean hasExistingData = groupAdapter != null && groupAdapter.getItemCount() > 0;
      
      if (show && !hasExistingData) {
        // Inflate shimmer view if needed and show it
        inflateShimmerViewIfNeeded();
        
        if (shimmerFrameLayout != null) {
          shimmerFrameLayout.setVisibility(View.VISIBLE);
          shimmerFrameLayout.startShimmer();
          groupsRecyclerView.setVisibility(View.GONE);
        } else if (inflatedShimmerView != null) {
          inflatedShimmerView.setVisibility(View.VISIBLE);
          groupsRecyclerView.setVisibility(View.GONE);
        }
        
        displayLoadingToastIfRequested(showToast);
      } else {
        // Hide loading states
        if (shimmerFrameLayout != null) {
          shimmerFrameLayout.stopShimmer();
          shimmerFrameLayout.setVisibility(View.GONE);
        }
        if (inflatedShimmerView != null) {
          inflatedShimmerView.setVisibility(View.GONE);
        }
        groupsRecyclerView.setVisibility(View.VISIBLE);
      }
      
      updateRecyclerViewVisibility(show);
    } catch (Exception e) {
      Log.e(TAG, "Error managing loading state", e);
    }
  }

  private void updateRecyclerViewVisibility(boolean isLoading) {
    if (groupsRecyclerView != null) {
      // Don't hide RecyclerView during loading to prevent size issues
      // The loading overlay will handle visual feedback
      groupsRecyclerView.setVisibility(View.VISIBLE);
    }
  }

  private void displayLoadingToastIfRequested(boolean shouldShowToast) {
    if (true && shouldShowToast && !hasShownLoadingToast) {
      displayLoadingToast();
      hasShownLoadingToast = true;
    }
  }

  private void displayLoadingToast() {
    Toast.makeText(this, "Loading your groups... (may take a few seconds)", Toast.LENGTH_LONG)
        .show();
  }

  private void enforceServerConfiguration() {
    try {
      final String productionServerUrl = "https://partymaker.onrender.com";
      androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
          .edit()
          .putString("server_url", productionServerUrl)
          .apply();
      Log.d(TAG, "Server URL configured to: " + productionServerUrl);
    } catch (Exception e) {
      Log.e(TAG, "Failed to configure server URL", e);
    }
  }

  private boolean authenticateUser() {
    try {
      if (!isUserExplicitlyLoggedIn()) {
        Log.d(TAG, "User authentication required - redirecting to login");
        navigateToLogin();
        return false;
      }

      String userEmail = getCurrentUserEmail();
      if (isValidUserEmail(userEmail)) {
        currentUserKey = convertEmailToUserKey(userEmail);
        logSuccessfulAuthentication(userEmail);
        return true;
      } else {
        handleAuthenticationFailure("Authentication error. Please login again.");
        return false;
      }
    } catch (Exception e) {
      Log.e(TAG, "User authentication failed", e);
      handleAuthenticationFailure("User authentication failed");
      return false;
    }
  }

  private boolean isUserExplicitlyLoggedIn() {
    SharedPreferences preferences = getSharedPreferences(PREFS_PARTY_MAKER, Context.MODE_PRIVATE);
    return preferences.getBoolean(KEY_USER_EXPLICITLY_LOGGED_IN, false);
  }

  private String getCurrentUserEmail() {
    return AuthenticationManager.getCurrentUserEmail(this);
  }

  private boolean isValidUserEmail(String email) {
    return email != null && !email.trim().isEmpty();
  }

  private String convertEmailToUserKey(String email) {
    return email.replace('.', ' ');
  }

  private void logSuccessfulAuthentication(String email) {
    Log.d(
        TAG,
        String.format(
            "User authenticated successfully. Email: %s, UserKey: %s", email, currentUserKey));
  }

  private void handleAuthenticationFailure(String message) {
    Log.e(TAG, "Authentication failed: " + message);
    displayErrorMessage(message);
    navigateToLogin();
  }

  private void initializeViews() {
    findViews();
    validateCriticalViews();
    configureRecyclerView();
    setupLoadingStateManager();
    // SwipeRefresh removed - no pull to refresh
  }

  private void findViews() {
    rootView = findViewById(android.R.id.content);
    groupsRecyclerView = findViewById(R.id.lv1);
    chatFloatingActionButton = findViewById(R.id.fabChat);
    swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    searchEditText = findViewById(R.id.etSearchGroups);
    
    // Disable overscroll effect completely
    if (groupsRecyclerView != null) {
      groupsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
    clearSearchButton = findViewById(R.id.clearSearch);
    filterButton = findViewById(R.id.filter_button);
    
    // Find ViewStubs for performance optimization
    stubShimmerLoading = findViewById(R.id.stub_shimmer_loading);
    stubEmptyState = findViewById(R.id.stub_empty_state);
    stubLoadingOverlay = findViewById(R.id.stub_loading_overlay);
    stubProgressBar = findViewById(R.id.stub_progress_bar);
    
    // Keep the original shimmer reference for backward compatibility
    shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
  }

  private void validateCriticalViews() {
    if (groupsRecyclerView == null) {
      Log.e(TAG, "Critical UI component missing: groups RecyclerView");
      handleFatalError("UI initialization failed");
    }
  }

  private void configureRecyclerView() {
    // Use ViewOptimizationHelper for comprehensive RecyclerView optimization
    ViewOptimizationHelper.optimizeRecyclerView(groupsRecyclerView);
    
    // Apply scroll optimizations for smoother scrolling
    RecyclerViewScrollOptimizer.optimizeScrolling(groupsRecyclerView);
    
    // Setup animation optimizations
    AnimationOptimizer.optimizeRecyclerViewAnimations(groupsRecyclerView);
    AnimationOptimizer.applyStaggeredAnimation(groupsRecyclerView);
    
    // Set adapter
    groupAdapter = new GroupAdapter(this, this::navigateToGroupScreen);
    groupsRecyclerView.setAdapter(groupAdapter);
    
    Log.d(TAG, "RecyclerView configured with comprehensive optimizations and scroll enhancements");
  }
  
  // Comprehensive view optimization method
  private void optimizeViews() {
    try {
      // Optimize root view group
      if (rootView instanceof ViewGroup) {
        ViewOptimizationHelper.optimizeViewGroup((ViewGroup) rootView);
      }
      
      // Enable hardware acceleration for smooth animations
      getWindow().setFlags(
          WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
          WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
      
      ViewOptimizationHelper.enableHardwareAcceleration(rootView);
      
      // Animate FAB entrance
      if (chatFloatingActionButton != null) {
        AnimationOptimizer.animateViewEntrance(chatFloatingActionButton, FAB_ANIMATION_DELAY_MS);
      }
      
      Log.d(TAG, "Comprehensive view optimizations applied");
    } catch (Exception e) {
      Log.e(TAG, "Error applying view optimizations", e);
    }
  }
  
  // ViewStub inflation methods for performance optimization
  private void inflateShimmerViewIfNeeded() {
    if (inflatedShimmerView == null && stubShimmerLoading != null) {
      ViewOptimizationHelper.inflateViewStubAsync(stubShimmerLoading, new ViewOptimizationHelper.ViewStubInflationCallback() {
        @Override
        public void onInflationComplete(View inflatedView) {
          inflatedShimmerView = inflatedView;
          shimmerFrameLayout = inflatedView.findViewById(R.id.shimmer_container);
          Log.d(TAG, "Shimmer view inflated successfully");
        }
        
        @Override
        public void onInflationFailed(String error) {
          Log.e(TAG, "Failed to inflate shimmer view: " + error);
        }
      });
    }
  }
  
  private void inflateEmptyStateViewIfNeeded() {
    if (inflatedEmptyView == null && stubEmptyState != null) {
      ViewOptimizationHelper.inflateViewStubAsync(stubEmptyState, new ViewOptimizationHelper.ViewStubInflationCallback() {
        @Override
        public void onInflationComplete(View inflatedView) {
          inflatedEmptyView = inflatedView;
          // Apply entrance animation
          AnimationOptimizer.animateViewEntrance(inflatedView, 0);
          Log.d(TAG, "Empty state view inflated successfully");
        }
        
        @Override
        public void onInflationFailed(String error) {
          Log.e(TAG, "Failed to inflate empty state view: " + error);
        }
      });
    }
  }
  
  private void inflateLoadingOverlayIfNeeded() {
    if (inflatedLoadingOverlay == null && stubLoadingOverlay != null) {
      ViewOptimizationHelper.inflateViewStubAsync(stubLoadingOverlay, new ViewOptimizationHelper.ViewStubInflationCallback() {
        @Override
        public void onInflationComplete(View inflatedView) {
          inflatedLoadingOverlay = inflatedView;
          Log.d(TAG, "Loading overlay inflated successfully");
        }
        
        @Override
        public void onInflationFailed(String error) {
          Log.e(TAG, "Failed to inflate loading overlay: " + error);
        }
      });
    }
  }

  private void handleFatalError(String message) {
    UserFeedbackManager.showErrorDialog(this, message);
    finish();
  }

  private void setupLoadingStateManager() {
    // First try to inflate the ViewStubs if they haven't been inflated yet
    android.widget.ProgressBar progressBar = null;
    android.widget.TextView loadingText = null;
    LottieAnimationView lottieAnimation = null;
    View loadingOverlay = null;
    
    // Try to inflate the progress bar stub
    ViewStub progressStub = findViewById(R.id.stub_progress_bar);
    if (progressStub != null) {
      try {
        progressStub.inflate();
      } catch (Exception e) {
        // Already inflated or error
      }
    }
    progressBar = findViewById(R.id.progress_bar_fallback);
    
    // Try to inflate the loading overlay stub for Lottie
    ViewStub loadingStub = findViewById(R.id.stub_loading_overlay);
    if (loadingStub != null) {
      try {
        loadingStub.inflate();
      } catch (Exception e) {
        // Already inflated or error
      }
    }
    loadingOverlay = findViewById(R.id.loading_overlay);
    loadingText = findViewById(R.id.loading_text);
    lottieAnimation = findViewById(R.id.lottie_loading);
    
    // If progressBar is still null, create one programmatically
    if (progressBar == null) {
      progressBar = findOrCreateProgressBar();
    }

    loadingStateManager =
        createLoadingStateManagerWithLottie(
            progressBar, loadingText, lottieAnimation, loadingOverlay);
  }

  private android.widget.ProgressBar findOrCreateProgressBar() {
    android.widget.ProgressBar progressBar = findViewById(R.id.progressBar);

    if (progressBar == null) {
      progressBar = createProgressBar();
      addProgressBarToLayout(progressBar);
    }

    return progressBar;
  }

  private android.widget.ProgressBar createProgressBar() {
    android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
    progressBar.setId(R.id.progressBar);
    progressBar.setVisibility(View.GONE);
    return progressBar;
  }

  private void addProgressBarToLayout(android.widget.ProgressBar progressBar) {
    if (rootView instanceof android.widget.FrameLayout) {
      ((android.widget.FrameLayout) rootView).addView(progressBar);
    }
  }

  private LoadingStateManager createLoadingStateManager(
      android.widget.ProgressBar progressBar, android.widget.TextView loadingText) {
    return new LoadingStateManager.Builder()
        .contentView(groupsRecyclerView)
        .progressBar(progressBar)
        .loadingText(loadingText)
        .build();
  }

  private LoadingStateManager createLoadingStateManagerWithLottie(
      android.widget.ProgressBar progressBar,
      android.widget.TextView loadingText,
      LottieAnimationView lottieAnimation,
      View errorView) {

    // Set up the Lottie animation
    if (lottieAnimation != null) {
      lottieAnimation.setAnimation("party_loading.json");
      lottieAnimation.setRepeatCount(-1); // Loop indefinitely
      lottieAnimation.setRepeatMode(LottieDrawable.RESTART);
    }

    return new LoadingStateManager.Builder()
        .contentView(groupsRecyclerView)
        .progressBar(progressBar)
        .loadingText(loadingText)
        .errorView(null) // We'll handle error separately
        .lottieAnimation(lottieAnimation)
        .build();
  }

  private void handlePullToRefresh() {
    Log.d(TAG, "Pull-to-refresh initiated");

    if (isRefreshRequestValid()) {
      performForceRefresh();
      updateLastRefreshTime();
    } else {
      // Stop refresh animation if request is not valid
      if (swipeRefreshLayout != null) {
        swipeRefreshLayout.setRefreshing(false);
      }
    }
  }

  private boolean isRefreshRequestValid() {
    return viewModel != null && currentUserKey != null;
  }

  private void performForceRefresh() {
    Log.d(TAG, "Performing force refresh");
    
    // Clear current search and filters for fresh data
    if (searchEditText != null) {
      searchEditText.setText("");
    }
    
    // Force reload from server
    viewModel.loadUserGroups(currentUserKey, true);
  }

  private void updateLastRefreshTime() {
    lastRefreshTimeMillis = System.currentTimeMillis();
  }

  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (!isActionBarAvailable(actionBar)) {
      return;
    }

    try {
      configureActionBarAppearance(actionBar);
      Log.d(TAG, "ActionBar configuration completed");
    } catch (Exception e) {
      Log.e(TAG, "ActionBar configuration failed", e);
    }
  }

  private boolean isActionBarAvailable(ActionBar actionBar) {
    if (actionBar == null) {
      Log.w(TAG, "ActionBar not available");
      return false;
    }
    return true;
  }

  private void configureActionBarAppearance(ActionBar actionBar) {
    setActionBarBackground(actionBar);
    setActionBarTitle(actionBar);
    configureActionBarProperties(actionBar);
  }

  private void setActionBarBackground(ActionBar actionBar) {
    GradientDrawable gradient = createActionBarGradient();
    actionBar.setBackgroundDrawable(gradient);
  }

  private void setActionBarTitle(ActionBar actionBar) {
    String styledTitle = createStyledTitle();
    actionBar.setTitle(Html.fromHtml(styledTitle, Html.FROM_HTML_MODE_LEGACY));
  }

  // Creates a gradient drawable for the action bar background.
  private GradientDrawable createActionBarGradient() {
    GradientDrawable gradient = new GradientDrawable();
    gradient.setShape(GradientDrawable.RECTANGLE);
    gradient.setColors(
        new int[] {
          Color.parseColor(ACTION_BAR_START_COLOR), Color.parseColor(ACTION_BAR_END_COLOR)
        });
    gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
    return gradient;
  }

  private String createStyledTitle() {
    return String.format("<font color='%s'><b>%s</b></font>", ACTION_BAR_TITLE_COLOR, APP_TITLE);
  }

  // Configures action bar properties.
  private void configureActionBarProperties(ActionBar actionBar) {
    actionBar.setElevation(ACTION_BAR_ELEVATION);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(false);
  }

  private void setupSwipeRefresh() {
    if (swipeRefreshLayout != null) {
      // Set refresh colors to match app theme
      swipeRefreshLayout.setColorSchemeResources(
          R.color.colorPrimary,
          R.color.colorAccent,
          R.color.primaryBlue
      );
      
      // Set refresh listener
      swipeRefreshLayout.setOnRefreshListener(this::handlePullToRefresh);
    }
  }
  
  private void setupSearchBar() {
    if (searchEditText == null || clearSearchButton == null) {
      Log.w(TAG, "Search components not found");
      return;
    }
    
    // Setup filter button
    if (filterButton != null) {
      filterButton.setOnClickListener(v -> showSortFilterDialog());
    }

    // Setup text change listener for search
    searchEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        filterGroups(s.toString());
        // Show/hide clear button based on text
        clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
      }

      @Override
      public void afterTextChanged(Editable s) {}
    });

    // Setup clear button click listener
    clearSearchButton.setOnClickListener(v -> {
      searchEditText.setText("");
      clearSearchButton.setVisibility(View.GONE);
    });
  }

  private void filterGroups(String searchText) {
    List<Group> results = new ArrayList<>(allGroups);
    
    // Apply text search filter
    if (!searchText.isEmpty()) {
      results = filterByText(results, searchText);
    }
    
    // Apply other filters
    results = applyFilters(results);
    
    // Apply sorting
    results = sortGroups(results);
    
    // Update filtered groups
    filteredGroups.clear();
    filteredGroups.addAll(results);
    
    // Update the adapter with filtered groups without triggering loading state
    if (groupAdapter != null) {
      // Directly update items without going through ViewModel to avoid loading state
      groupAdapter.updateItems(filteredGroups);
      
      // Ensure the RecyclerView stays visible
      if (groupsRecyclerView != null) {
        groupsRecyclerView.setVisibility(View.VISIBLE);
      }
      
      // Hide any loading overlays that might be showing
      if (loadingStateManager != null) {
        loadingStateManager.showContent();
      }
    }
  }
  
  private List<Group> filterByText(List<Group> groups, String searchText) {
    List<Group> filtered = new ArrayList<>();
    String lowerSearchText = searchText.toLowerCase();
    for (Group group : groups) {
      if (group.getGroupName() != null && 
          group.getGroupName().toLowerCase().contains(lowerSearchText)) {
        filtered.add(group);
      }
    }
    return filtered;
  }
  
  private List<Group> applyFilters(List<Group> groups) {
    List<Group> filtered = new ArrayList<>();
    
    for (Group group : groups) {
      boolean passesFilter = true;
      
      // Public/Private filter
      if (filterPublicOnly && group.getGroupType() != 0) { // 0 is public
        passesFilter = false;
      }
      if (filterPrivateOnly && group.getGroupType() != 1) { // 1 is private
        passesFilter = false;
      }
      
      // Free entry filter
      if (filterFreeOnly) {
        String price = group.getGroupPrice();
        if (price != null && !price.equals("0") && !price.equalsIgnoreCase("free")) {
          passesFilter = false;
        }
      }
      
      // Upcoming only filter
      if (filterUpcomingOnly) {
        try {
          String dateStr = group.getGroupDays() + "/" + group.getGroupMonths() + "/" + group.getGroupYears();
          java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
          java.util.Date groupDate = sdf.parse(dateStr);
          if (groupDate != null && groupDate.before(new java.util.Date())) {
            passesFilter = false;
          }
        } catch (Exception e) {
          // If date parsing fails, exclude the group
          passesFilter = false;
        }
      }
      
      if (passesFilter) {
        filtered.add(group);
      }
    }
    
    return filtered;
  }
  
  private List<Group> sortGroups(List<Group> groups) {
    List<Group> sorted = new ArrayList<>(groups);
    
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
        // Reverse the list to show newest first (assuming they're added at the end)
        java.util.Collections.reverse(sorted);
        break;
    }
    
    return sorted;
  }
  
  private int compareDates(Group g1, Group g2) {
    try {
      // Check for null values first
      if (g1.getGroupDays() == null || g1.getGroupMonths() == null || g1.getGroupYears() == null ||
          g2.getGroupDays() == null || g2.getGroupMonths() == null || g2.getGroupYears() == null) {
        return 0;
      }
      
      String date1 = g1.getGroupDays() + "/" + g1.getGroupMonths() + "/" + g1.getGroupYears();
      String date2 = g2.getGroupDays() + "/" + g2.getGroupMonths() + "/" + g2.getGroupYears();
      
      // Check for "null" strings
      if (date1.contains("null") || date2.contains("null")) {
        return 0;
      }
      
      java.util.Date d1 = parseDate(date1);
      java.util.Date d2 = parseDate(date2);
      
      if (d1 == null && d2 == null) return 0;
      if (d1 == null) return 1;
      if (d2 == null) return -1;
      
      return d1.compareTo(d2);
    } catch (Exception e) {
      Log.w(TAG, "Error comparing dates", e);
    }
    return 0;
  }
  
  /**
   * Parse date string with multiple format support
   */
  private java.util.Date parseDate(String dateStr) {
    if (dateStr == null || dateStr.isEmpty() || dateStr.contains("null")) {
      return null;
    }
    
    // Try each date format
    for (java.text.SimpleDateFormat format : DATE_FORMATS) {
      try {
        format.setLenient(false);
        return format.parse(dateStr);
      } catch (java.text.ParseException e) {
        // Try next format
      }
    }
    
    // Log error only once for unparseable dates
    Log.w(TAG, "Could not parse date: " + dateStr);
    return null;
  }
  
  private void showSortFilterDialog() {
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_sort_filter, null);
    builder.setView(dialogView);
    
    // Get dialog components
    android.widget.RadioGroup radioGroupSort = dialogView.findViewById(R.id.radioGroupSort);
    com.google.android.material.checkbox.MaterialCheckBox checkboxPublic = dialogView.findViewById(R.id.checkboxPublic);
    com.google.android.material.checkbox.MaterialCheckBox checkboxPrivate = dialogView.findViewById(R.id.checkboxPrivate);
    com.google.android.material.checkbox.MaterialCheckBox checkboxFree = dialogView.findViewById(R.id.checkboxFree);
    com.google.android.material.checkbox.MaterialCheckBox checkboxUpcoming = dialogView.findViewById(R.id.checkboxUpcoming);
    
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
    
    checkboxPublic.setChecked(filterPublicOnly);
    checkboxPrivate.setChecked(filterPrivateOnly);
    checkboxFree.setChecked(filterFreeOnly);
    checkboxUpcoming.setChecked(filterUpcomingOnly);
    
    // Ensure public and private are mutually exclusive
    checkboxPublic.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked && checkboxPrivate.isChecked()) {
        checkboxPrivate.setChecked(false);
      }
    });
    
    checkboxPrivate.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked && checkboxPublic.isChecked()) {
        checkboxPublic.setChecked(false);
      }
    });
    
    android.app.AlertDialog dialog = builder.create();
    
    // Setup dialog buttons
    dialogView.findViewById(R.id.btnReset).setOnClickListener(v -> {
      // Reset to defaults
      radioGroupSort.check(R.id.radioDateNearest);
      checkboxPublic.setChecked(false);
      checkboxPrivate.setChecked(false);
      checkboxFree.setChecked(false);
      checkboxUpcoming.setChecked(false);
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
      
      filterPublicOnly = checkboxPublic.isChecked();
      filterPrivateOnly = checkboxPrivate.isChecked();
      filterFreeOnly = checkboxFree.isChecked();
      filterUpcomingOnly = checkboxUpcoming.isChecked();
      
      // Apply the filters
      String currentSearchText = searchEditText != null ? searchEditText.getText().toString() : "";
      filterGroups(currentSearchText);
      
      dialog.dismiss();
    });
    
    dialog.show();
  }

  private void observeViewModel() {
    try {
      observeGroupsData();
      observeLoadingState();
      observeErrorMessages();
    } catch (Exception e) {
      Log.e(TAG, "Failed to setup ViewModel observers", e);
      UserFeedbackManager.showErrorDialog(this, "Error initializing data observers");
    }
  }

  private void observeGroupsData() {
    viewModel.getGroups().observe(this, this::handleGroupsDataUpdate);
  }

  private void observeLoadingState() {
    viewModel.getIsLoading().observe(this, this::handleLoadingStateChange);
  }

  private void observeErrorMessages() {
    viewModel.getErrorMessage().observe(this, this::handleErrorMessage);
  }

  private void handleGroupsDataUpdate(List<Group> groups) {
    try {
      logGroupsDataReceived(groups);

      if (groups != null) {
        // Store all groups for filtering
        allGroups.clear();
        allGroups.addAll(groups);
        
        // Apply current search filter if any
        String currentSearchText = searchEditText != null ? searchEditText.getText().toString() : "";
        if (currentSearchText.isEmpty()) {
          filteredGroups.clear();
          filteredGroups.addAll(allGroups);
        } else {
          filterGroups(currentSearchText);
        }
        
        updateGroupsDisplay(filteredGroups);
        updateUiStateBasedOnGroups(filteredGroups);
      } else {
        handleNullGroupsData();
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to process groups data update", e);
      handleGroupsProcessingError();
    }
  }

  private void logGroupsDataReceived(List<Group> groups) {
    Log.d(TAG, "Groups data received: " + (groups != null ? groups.size() + " groups" : "null"));

    if (groups != null && !groups.isEmpty()) {
      logSampleGroups(groups);
    }
  }

  private void logSampleGroups(List<Group> groups) {
    int sampleSize = Math.min(3, groups.size());
    for (int i = 0; i < sampleSize; i++) {
      Group group = groups.get(i);
      Log.d(
          TAG,
          String.format("Group %d: %s (key: %s)", i, group.getGroupName(), group.getGroupKey()));
    }
  }

  private void updateGroupsDisplay(List<Group> groups) {
    groupAdapter.updateItems(groups);
    Log.d(TAG, "Group adapter updated with " + groups.size() + " groups");

    // Hide Shimmer when data is ready
    if (shimmerFrameLayout != null) {
      shimmerFrameLayout.stopShimmer();
      shimmerFrameLayout.setVisibility(View.GONE);
    }

    // Ensure content is properly visible after update
    if (groupsRecyclerView != null) {
      groupsRecyclerView.setVisibility(View.VISIBLE);
      groupsRecyclerView.setAlpha(1.0f);

      // Slight delay to ensure images have time to load properly
      android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
      handler.postDelayed(
          () -> {
            if (groupsRecyclerView != null) {
              groupsRecyclerView.invalidate();
              groupsRecyclerView.requestLayout();
            }
          },
          HANDLER_DELAY_MS);
    }
  }

  private void updateUiStateBasedOnGroups(List<Group> groups) {
    if (groups.isEmpty()) {
      showEmptyGroupsState();
    } else {
      showGroupsContentState(groups.size());
      checkForFirstGroupCelebration(groups);
    }
  }

  private void checkForFirstGroupCelebration(List<Group> groups) {
    // Check if this is the user's first group by looking at SharedPreferences
    SharedPreferences prefs = getSharedPreferences(PREFS_PARTY_MAKER, Context.MODE_PRIVATE);
    boolean hasShownFirstGroupCelebration =
        prefs.getBoolean("has_shown_first_group_celebration", false);

    if (!hasShownFirstGroupCelebration && groups.size() == 1) {
      // Show celebration for first group using direct Lottie approach
      LottieAnimationView lottieAnimation = findViewById(R.id.lottie_loading);
      View loadingOverlay = findViewById(R.id.loading_overlay);
      TextView loadingText = findViewById(R.id.loading_text);

      if (lottieAnimation != null && loadingOverlay != null) {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingText.setText("🎉 Welcome to PartyMaker! Your first party awaits!");

        lottieAnimation.setAnimation("party_celebration.json");
        lottieAnimation.setRepeatCount(1); // Play twice
        lottieAnimation.playAnimation();

        // Auto-hide after animation
        ThreadUtils.runOnMainThreadDelayed(
            () -> {
              if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
              }
            },
            GROUPS_LOADING_ANIMATION_DELAY_MS);
      }

      // Mark that we've shown the celebration
      prefs.edit().putBoolean("has_shown_first_group_celebration", true).apply();
    }
  }

  private void showEmptyGroupsState() {
    Log.d(TAG, "Displaying empty groups state with animation");
    loadingStateManager.showEmptyWithAnimation("No groups found. Tap + to create a new group");
    
    // Inflate and show empty state view if needed
    inflateEmptyStateViewIfNeeded();
    showEmptyState();
  }

  private void showGroupsContentState(int groupCount) {
    Log.d(TAG, "Displaying " + groupCount + " groups in content state");
    loadingStateManager.showContent();
    hideEmptyState();
  }

  private void handleNullGroupsData() {
    Log.d(TAG, "Groups data is null - keeping current loading state");
  }

  private void handleGroupsProcessingError() {
    UiStateManager.showError(
        rootView, "Error displaying groups", () -> viewModel.loadUserGroups(currentUserKey, true));
    loadingStateManager.showErrorWithAnimation("Error loading groups");
  }

  private void handleLoadingStateChange(Boolean isLoading) {
    try {
      Log.d(TAG, "Loading state changed to: " + isLoading);

      if (Boolean.TRUE.equals(isLoading)) {
        showLoadingIndicator();
      } else {
        hideLoadingIndicator();
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to handle loading state change", e);
    }
  }

  private void showLoadingIndicator() {
    // Only show loading overlay for initial load or force refresh
    if (groupAdapter == null || groupAdapter.getItemCount() == 0) {
      // Show Shimmer for initial load
      if (shimmerFrameLayout != null) {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        groupsRecyclerView.setVisibility(View.GONE);
      }
      loadingStateManager.showNetworkSync("Loading your parties...");
    } else {
      // For refresh with existing data, just show subtle loading feedback
      Log.d(TAG, "Refreshing existing data - minimal loading feedback");
      // Don't show Shimmer when we have cached data
    }
  }

  private void hideLoadingIndicator() {
    // Always ensure loading states are hidden and content is visible
    loadingStateManager.showContent();
    stopSwipeRefreshIfActive();

    // Hide Shimmer and show RecyclerView
    if (shimmerFrameLayout != null) {
      shimmerFrameLayout.stopShimmer();
      shimmerFrameLayout.setVisibility(View.GONE);
    }
    
    // Ensure RecyclerView is fully visible
    if (groupsRecyclerView != null) {
      groupsRecyclerView.setVisibility(View.VISIBLE);
      groupsRecyclerView.setAlpha(1.0f);
    }
  }

  private void stopSwipeRefreshIfActive() {
    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
      swipeRefreshLayout.setRefreshing(false);
      Log.d(TAG, "SwipeRefresh animation stopped");
    }
  }

  private void handleErrorMessage(String errorMessage) {
    try {
      Log.d(TAG, "Error message received: " + errorMessage);

      if (isValidErrorMessage(errorMessage)) {
        displayAndClearError(errorMessage);
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to handle error message", e);
    }
  }

  private boolean isValidErrorMessage(String errorMessage) {
    return errorMessage != null && !errorMessage.trim().isEmpty();
  }

  private void displayAndClearError(String errorMessage) {
    Log.e(TAG, "Displaying error to user with animation: " + errorMessage);

    UiStateManager.showError(
        rootView, errorMessage, () -> viewModel.loadUserGroups(currentUserKey, true));
    loadingStateManager.showErrorWithAnimation(errorMessage);

    viewModel.clearError();
  }

  private void setupEventHandlers() {
    // Event handlers are configured in individual component setup methods
    // GroupAdapter handles item click events through constructor callback
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingChatButton() {
    if (!isChatButtonAvailable()) {
      return;
    }

    configureChatButtonActions();
  }

  private boolean isChatButtonAvailable() {
    if (chatFloatingActionButton == null) {
      Log.w(TAG, "Chat FloatingActionButton not found");
      return false;
    }
    return true;
  }

  private void configureChatButtonActions() {
    // Apply professional FAB animations
    ButtonAnimationHelper.applyPressAnimation(chatFloatingActionButton, true);

    chatFloatingActionButton.setOnClickListener(
        view -> {
          // Add success bounce animation before navigation
          ButtonAnimationHelper.applySuccessBounce(view);

          // Delay navigation to show animation
          android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
          handler.postDelayed(() -> navigateToChat(), NAVIGATION_DELAY_MS);
        });

    chatFloatingActionButton.setOnTouchListener(IntentExtrasManager::dragChatButtonOnTouch);

    // Add subtle entrance animation for the FAB
    ButtonAnimationHelper.applyEntranceAnimation(chatFloatingActionButton, FAB_ANIMATION_DELAY_MS);
  }

  private void setupBottomNavigation() {
    NavigationManager.setupBottomNavigation(this, "myparties");
  }

  private void navigateToChat() {
    try {
      Intent chatIntent = new Intent(this, GptChatActivity.class);
      startActivity(chatIntent);
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to chat", e);
      displayErrorMessage("Failed to open chat");
    }
  }

  private void navigateToGroupScreen(Group group) {
    if (!isValidGroupForNavigation(group)) {
      return;
    }

    try {
      logNavigationAttempt(group);
      Intent groupIntent = createGroupNavigationIntent(group);
      startActivity(groupIntent);
      logNavigationSuccess(group);
    } catch (Exception e) {
      handleNavigationError(e, group);
    }
  }

  private boolean isValidGroupForNavigation(Group group) {
    if (group == null) {
      Log.e(TAG, "Cannot navigate: group is null");
      return false;
    }
    return true;
  }

  private void logNavigationAttempt(Group group) {
    Log.d(
        TAG,
        String.format(
            "Navigating to group: %s (key: %s)", group.getGroupName(), group.getGroupKey()));
  }

  private Intent createGroupNavigationIntent(Group group) {
    Intent intent = new Intent(getBaseContext(), PartyMainActivity.class);

    addGroupDataToIntent(intent, group);
    addUserDataToIntent(intent);
    addBackwardCompatibilityData(intent, group);

    return intent;
  }

  private void addGroupDataToIntent(Intent intent, Group group) {
    intent.putExtra("GroupKey", group.getGroupKey());
    Log.d(TAG, "Added GroupKey to intent: " + group.getGroupKey());
  }

  private void addUserDataToIntent(Intent intent) {
    intent.putExtra("UserKey", currentUserKey);
    Log.d(TAG, "Added UserKey to intent: " + currentUserKey);
  }

  private void addBackwardCompatibilityData(Intent intent, Group group) {
    ExtrasMetadata extras = createExtrasFromGroup(group);
    IntentExtrasManager.addExtrasToIntent(intent, extras);
  }

  private void logNavigationSuccess(Group group) {
    Log.d(TAG, "Successfully navigated to group: " + group.getGroupName());
  }

  private void handleNavigationError(Exception e, Group group) {
    String errorMessage = "Failed to open group: " + e.getMessage();
    Log.e(TAG, "Navigation failed for group: " + group.getGroupName(), e);
    displayErrorMessage(errorMessage);
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

  private void showEmptyState() {
    try {
      ThreadUtils.runOnMainThread(this::displayEmptyStateIfApplicable);
    } catch (Exception e) {
      Log.e(TAG, "Failed to show empty state", e);
    }
  }

  private void displayEmptyStateIfApplicable() {
    if (!shouldShowEmptyState()) {
      return;
    }

    displayEmptyStateMessage();
  }

  private boolean shouldShowEmptyState() {
    return isGroupListEmpty() && !isCurrentlyLoading();
  }

  private boolean isGroupListEmpty() {
    return groupsRecyclerView != null && groupAdapter != null && groupAdapter.getItemCount() == 0;
  }

  private boolean isCurrentlyLoading() {
    Boolean loadingState = viewModel.getIsLoading().getValue();
    return loadingState != null && loadingState;
  }

  private void displayEmptyStateMessage() {
    // With ViewStub optimization, empty state is handled through inflatedEmptyView
    if (inflatedEmptyView != null) {
      inflatedEmptyView.setVisibility(View.VISIBLE);
      // Apply entrance animation
      AnimationOptimizer.animateViewEntrance(inflatedEmptyView, 0);
    } else {
      // Fallback to inflating empty state
      inflateEmptyStateViewIfNeeded();
      showEmptyStateFallback();
    }
  }

  private void showEmptyViewMessage(TextView emptyView) {
    // This method is no longer needed with ViewStub approach
    emptyView.setVisibility(View.VISIBLE);
    emptyView.setText("No groups found. Tap + to create a new group");
  }

  private void showEmptyStateFallback() {
    UiStateManager.showInfo(rootView, "No groups found. Tap + to create a new group");
  }

  private void hideEmptyState() {
    try {
      ThreadUtils.runOnMainThread(this::hideEmptyStateView);
    } catch (Exception e) {
      Log.e(TAG, "Failed to hide empty state", e);
    }
  }

  private void hideEmptyStateView() {
    // With ViewStub optimization, hide the inflated empty state view
    if (inflatedEmptyView != null) {
      inflatedEmptyView.setVisibility(View.GONE);
    }
  }

  private boolean isCurrentUserMemberOfGroup(Group group) {
    if (group == null || currentUserKey == null) {
      return false;
    }

    HashMap<String, Object> groupMembers = group.getFriendKeys();
    return groupMembers != null && groupMembers.containsKey(currentUserKey);
  }

  private boolean isValidAdapterPosition(int position) {
    return groupAdapter != null && position >= 0 && position < groupAdapter.getItemCount();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();

    if (itemId == R.id.settings) {
      navigateToServerSettings();
    } else if (itemId == R.id.logout) {
      handleLogout();
    }

    return true;
  }

  private void navigateToServerSettings() {
    navigateToActivity();
  }

  private void navigateToActivity() {
    if (ServerSettingsActivity.class == null) {
      Log.e(TAG, "Cannot navigate: activity class is null");
      return;
    }

    try {
      Intent intent = new Intent(getApplicationContext(), ServerSettingsActivity.class);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to activity: " + ServerSettingsActivity.class.getSimpleName(), e);
    }
  }

  private void handleLogout() {
    UserFeedbackManager.showConfirmationDialog(
        this, "Logout", "Are you sure you want to logout?", this::performLogoutSequence);
  }

  private void performLogoutSequence() {
    try {
      displayLogoutProgress();
      clearApplicationData();
      clearUserSession();
      showLogoutSuccess();
      scheduleNavigationToLogin();
    } catch (Exception e) {
      handleLogoutError(e);
    }
  }

  private void displayLogoutProgress() {
    loadingStateManager.showUserSwitch("Logging out...");
  }

  private void clearApplicationData() {
    if (viewModel != null) {
      viewModel.clearAllData();
    }
    AuthenticationManager.logout(this);
  }

  private void clearUserSession() {
    SharedPreferences preferences = getSharedPreferences(PREFS_PARTY_MAKER, Context.MODE_PRIVATE);
    preferences.edit().putBoolean(KEY_USER_EXPLICITLY_LOGGED_IN, false).apply();
  }

  private void showLogoutSuccess() {
    UiStateManager.showSuccess(rootView, "Logged out successfully");
    loadingStateManager.showSuccess("Logged out successfully");
  }

  private void scheduleNavigationToLogin() {
    ThreadUtils.runOnMainThreadDelayed(this::completeLogout, LOGOUT_DELAY_MS);
  }

  private void completeLogout() {
    navigateToLogin();
    Log.d(TAG, "User logout completed successfully");
  }

  private void handleLogoutError(Exception e) {
    Log.e(TAG, "Logout process failed", e);
    UiStateManager.showError(rootView, "Logout failed", this::handleLogout);
    loadingStateManager.showErrorWithAnimation("Logout failed");
  }

  private void navigateToLogin() {
    try {
      Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
      startActivity(loginIntent);
      finish();
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to login activity", e);
    }
  }

  private void displayErrorMessage(String message) {
    try {
      UiStateManager.showError(rootView, message);
      Log.e(TAG, "Error displayed to user: " + message);
    } catch (Exception e) {
      Log.e(TAG, "Failed to display error message via UiStateManager", e);
      showFallbackErrorToast(message);
    }
  }

  private void showFallbackErrorToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  protected void onResume() {
    PerformanceMonitor.startTiming("MainActivity.onResume");
    super.onResume();
    refreshGroupsIfNeeded();
    PerformanceMonitor.endTiming("MainActivity.onResume");
    PerformanceMonitor.trackMemoryUsage("MainActivity.onResume");
  }

  @Override
  protected void onPause() {
    PerformanceMonitor.startTiming("MainActivity.onPause");
    super.onPause();
    PerformanceMonitor.endTiming("MainActivity.onPause");
  }

  private void refreshGroupsIfNeeded() {
    if (!isRefreshComponentsAvailable()) {
      return;
    }

    long timeSinceLastRefresh = calculateTimeSinceLastRefresh();

    if (shouldPerformRefresh(timeSinceLastRefresh)) {
      performActivityResumeRefresh(timeSinceLastRefresh);
    } else {
      logRefreshSkipped(timeSinceLastRefresh);
    }
  }

  private boolean isRefreshComponentsAvailable() {
    return viewModel != null && currentUserKey != null;
  }

  private long calculateTimeSinceLastRefresh() {
    return System.currentTimeMillis() - lastRefreshTimeMillis;
  }

  private boolean shouldPerformRefresh(long timeSinceLastRefresh) {
    return timeSinceLastRefresh > REFRESH_COOLDOWN_MS;
  }

  private void performActivityResumeRefresh(long timeSinceLastRefresh) {
    Log.d(
        TAG,
        String.format(
            "onResume: Refreshing groups to catch new invitations (last refresh: %dms ago)",
            timeSinceLastRefresh));
    viewModel.loadUserGroups(currentUserKey, true);
    updateLastRefreshTime();
  }

  private void logRefreshSkipped(long timeSinceLastRefresh) {
    Log.d(
        TAG,
        String.format(
            "onResume: Skipping refresh - within cooldown period (%dms ago, cooldown: %dms)",
            timeSinceLastRefresh, REFRESH_COOLDOWN_MS));
  }

  @Override
  protected void clearActivityReferences() {
    // Clear view references to prevent memory leaks
    if (rootView != null) {
      ViewOptimizationHelper.clearViewReferences(rootView);
    }
    
    // Clear animation cache
    AnimationOptimizer.clearAnimationCache();
    
    // Clear adapter and view model references
    if (groupAdapter != null) {
      groupAdapter = null;
    }
    if (viewModel != null) {
      viewModel = null;
    }
    
    // Clear ViewStub references
    stubShimmerLoading = null;
    stubEmptyState = null;
    stubLoadingOverlay = null;
    stubProgressBar = null;
    
    // Clear inflated view references
    inflatedShimmerView = null;
    inflatedEmptyView = null;
    inflatedLoadingOverlay = null;
    inflatedProgressBar = null;
    
    Log.d(TAG, "Activity references cleared for memory optimization");
  }
}
