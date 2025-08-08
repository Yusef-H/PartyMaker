package com.example.partymaker.ui.features.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
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
import com.example.partymaker.utils.ui.animation.CustomRefreshAnimationHelper;
import com.example.partymaker.utils.ui.components.LoadingStateManager;
import com.example.partymaker.utils.ui.components.UiStateManager;
import com.example.partymaker.utils.ui.feedback.UserFeedbackManager;
import com.example.partymaker.utils.ui.navigation.NavigationManager;
import com.example.partymaker.viewmodel.core.MainActivityViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

  // Data Components
  private MainActivityViewModel viewModel;
  private String currentUserKey;
  private GroupAdapter groupAdapter;

  // UI State Management
  private LoadingStateManager loadingStateManager;

  // State tracking
  private boolean hasShownLoadingToast = false;
  private long lastRefreshTimeMillis = 0;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      initializeActivity();
    } catch (Exception e) {
      Log.e(TAG, "Fatal error in onCreate", e);
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
    loadInitialData();
  }

  private void initializeComponents() {
    viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    initializeViews();
  }

  private void setupUserInterface() {
    setupActionBar();
    setupEventHandlers();
    setupFloatingChatButton();
    setupBottomNavigation();
    observeViewModel();
  }

  private void loadInitialData() {
    showLoadingState(false); // Show loading without toast

    Log.d(TAG, "Loading groups for user: " + currentUserKey);
    viewModel.loadUserGroups(currentUserKey, true);
    updateLastRefreshTime();
  }

  private void showLoadingState(boolean showToast) {
    showLoadingStateInternal(true, showToast);
  }

  private void hideLoadingState() {
    showLoadingStateInternal(false, false);
  }

  private void showLoadingStateInternal(boolean show, boolean showToast) {
    try {
      updateRecyclerViewVisibility(show);
      displayLoadingToastIfRequested(show, showToast);
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

  private void displayLoadingToastIfRequested(boolean isLoading, boolean shouldShowToast) {
    if (isLoading && shouldShowToast && !hasShownLoadingToast) {
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
    setupSwipeRefresh();
  }

  private void findViews() {
    rootView = findViewById(android.R.id.content);
    groupsRecyclerView = findViewById(R.id.lv1);
    chatFloatingActionButton = findViewById(R.id.fabChat);
    swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
  }

  private void validateCriticalViews() {
    if (groupsRecyclerView == null) {
      Log.e(TAG, "Critical UI component missing: groups RecyclerView");
      handleFatalError("UI initialization failed");
    }
  }

  private void configureRecyclerView() {
    groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    groupAdapter = new GroupAdapter(this, this::navigateToGroupScreen);
    groupsRecyclerView.setAdapter(groupAdapter);
  }

  private void handleFatalError(String message) {
    UserFeedbackManager.showErrorDialog(this, message);
    finish();
  }

  private void setupLoadingStateManager() {
    android.widget.ProgressBar progressBar = findViewById(R.id.progress_bar_fallback);
    android.widget.TextView loadingText = findViewById(R.id.loading_text);
    LottieAnimationView lottieAnimation = findViewById(R.id.lottie_loading);
    View loadingOverlay = findViewById(R.id.loading_overlay);

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
      lottieAnimation.setRepeatMode(android.animation.ValueAnimator.RESTART);
    }

    return new LoadingStateManager.Builder()
        .contentView(groupsRecyclerView)
        .progressBar(progressBar)
        .loadingText(loadingText)
        .errorView(null) // We'll handle error separately
        .lottieAnimation(lottieAnimation)
        .build();
  }

  private void setupSwipeRefresh() {
    if (swipeRefreshLayout == null) {
      return;
    }

    configureRefreshIndicatorColors();
    setRefreshListener();
  }

  private void configureRefreshIndicatorColors() {
    // Apply professional party-themed refresh layout
    CustomRefreshAnimationHelper.setupPartyRefreshLayout(swipeRefreshLayout);
  }

  private void setRefreshListener() {
    swipeRefreshLayout.setOnRefreshListener(this::handlePullToRefresh);
  }

  private void handlePullToRefresh() {
    Log.d(TAG, "Pull-to-refresh triggered");

    if (isRefreshRequestValid()) {
      performForceRefresh();
      updateLastRefreshTime();
    }
  }

  private boolean isRefreshRequestValid() {
    return viewModel != null && currentUserKey != null;
  }

  private void performForceRefresh() {
    // Don't show loading state manager for refresh to avoid shake
    Log.d(TAG, "Performing force refresh without loading overlay");

    // No custom animations during refresh to prevent shake
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
        updateGroupsDisplay(groups);
        updateUiStateBasedOnGroups(groups);
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
    loadingStateManager.showEmptyWithAnimation("לא נמצאו קבוצות. לחץ על + ליצירת קבוצה חדשה");
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
      loadingStateManager.showNetworkSync("Loading your parties...");
    } else {
      // For refresh with existing data, just show subtle loading feedback
      Log.d(TAG, "Refreshing existing data - minimal loading feedback");
    }
  }

  private void hideLoadingIndicator() {
    // Always ensure loading states are hidden and content is visible
    loadingStateManager.showContent();
    stopSwipeRefreshIfActive();

    // Ensure RecyclerView is fully visible
    if (groupsRecyclerView != null) {
      groupsRecyclerView.setVisibility(View.VISIBLE);
      groupsRecyclerView.setAlpha(1.0f);
    }
  }

  private void stopSwipeRefreshIfActive() {
    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
      swipeRefreshLayout.setRefreshing(false);

      // Stop custom refresh animation without zoom feedback
      LottieAnimationView lottieView = findViewById(R.id.lottie_loading);
      if (lottieView != null) {
        CustomRefreshAnimationHelper.createCustomRefreshAnimation(rootView, lottieView, false);
        // Removed the annoying zoom success feedback
      }
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
    TextView emptyView = findViewById(R.id.emptyGroupsView);
    if (emptyView != null) {
      showEmptyViewMessage(emptyView);
    } else {
      showEmptyStateFallback();
    }
  }

  private void showEmptyViewMessage(TextView emptyView) {
    emptyView.setVisibility(View.VISIBLE);
    emptyView.setText("לא נמצאו קבוצות. לחץ על + ליצירת קבוצה חדשה");
  }

  private void showEmptyStateFallback() {
    UiStateManager.showInfo(rootView, "לא נמצאו קבוצות. לחץ על + ליצירת קבוצה חדשה");
  }

  private void hideEmptyState() {
    try {
      ThreadUtils.runOnMainThread(this::hideEmptyStateView);
    } catch (Exception e) {
      Log.e(TAG, "Failed to hide empty state", e);
    }
  }

  private void hideEmptyStateView() {
    TextView emptyView = findViewById(R.id.emptyGroupsView);
    if (emptyView != null) {
      emptyView.setVisibility(View.GONE);
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
    navigateToActivity(ServerSettingsActivity.class);
  }

  private void navigateToActivity(Class<?> activityClass) {
    if (activityClass == null) {
      Log.e(TAG, "Cannot navigate: activity class is null");
      return;
    }

    try {
      Intent intent = new Intent(getApplicationContext(), activityClass);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to activity: " + activityClass.getSimpleName(), e);
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
    super.onResume();
    refreshGroupsIfNeeded();
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
}
