package com.example.partymaker.ui.features.groups.creation;

import static com.example.partymaker.utils.core.IntentExtrasManager.hideViews;
import static com.example.partymaker.utils.core.IntentExtrasManager.showViews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.airbnb.lottie.LottieAnimationView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.features.auth.LoginActivity;
import com.example.partymaker.ui.features.auxiliary.chatbot.GptChatActivity;
import com.example.partymaker.ui.features.auxiliary.settings.ServerSettingsActivity;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.business.group.GroupDataManager;
import com.example.partymaker.utils.business.group.GroupDateTimeManager;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.media.ImageCompressor;
import com.example.partymaker.utils.security.encryption.GroupKeyManager;
import com.example.partymaker.utils.ui.animation.ButtonAnimationHelper;
import com.example.partymaker.utils.ui.components.LoadingStateManager;
import com.example.partymaker.utils.ui.feedback.NotificationManager;
import com.example.partymaker.utils.ui.maps.MapUtilitiesManager;
import com.example.partymaker.utils.ui.navigation.NavigationManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CreateGroupActivity extends AppCompatActivity implements OnMapReadyCallback {
  // Constants
  private static final int IMAGE_PICKER_REQUEST_CODE = 100;
  private static final int INSTRUCTION_DELAY_MS = 3000;
  private static final int ERROR_ANIMATION_HIDE_DELAY_MS = 3000;
  private static final int SUCCESS_ANIMATION_HIDE_DELAY_MS = 2500;
  private static final int LOGIN_ANIMATION_DELAY_MS = 100;
  private static final int NAME_ANIMATION_DELAY_MS = 200;
  private static final int EDIT_TEXT_ANIMATION_DELAY_MS = 300;
  private static final int CHAT_FAB_ANIMATION_DELAY_MS = 800;
  private static final String TAG = "CreateGroupActivity";
  private final int FINE_PERMISSION_CODE = 1;
  private Button btnAddGroup, btnNext1, btnNext2, btnBack1, btnBack2, btnDone;
  private TextView tvPartyName, tvPartyDate, tvGroupPicture, tvHours, tvSelectedDate;
  private EditText etPartyName;
  private ImageView imgLogin, imgGroupPicture;
  private String currentGroupKey, selectedDays, selectedMonths, selectedYears;
  private CheckBox groupTypeCheckBox;
  private Calendar selectedDate;
  private TimePicker timePicker;
  private FloatingActionButton chatFab;
  private GoogleMap map;
  private LatLng chosenLatLng;
  private FusedLocationProviderClient locationClient;
  private LoadingStateManager loadingStateManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_create);

    // Hide action bar to remove black bar at top
    androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    setupUI();
    setupLogic();
    setupBottomNavigation();
  }

  // UI call
  private void setupUI() {
    setupActionBar();
    initializeViews();
    setupMapAndLocation();
  }

  // Logical call
  private void setupLogic() {
    initializePlacesAPI();
    eventHandler();
    setupValidation();
  }

  // Initialize Google Places API if not already initialized
  private void initializePlacesAPI() {
    if (!Places.isInitialized()) {
      String apiKey = IntentExtrasManager.getApiKey(this, "MAPS_KEY");
      if (apiKey.isEmpty()) {
        // Use a default key from resources or show an error message
        Toast.makeText(
                this,
                "Google Maps API key not found. Some features may not work properly.",
                Toast.LENGTH_LONG)
            .show();
        // Try to get key from manifest metadata as fallback
        try {
          apiKey =
              getPackageManager()
                  .getApplicationInfo(
                      getPackageName(), android.content.pm.PackageManager.GET_META_DATA)
                  .metaData
                  .getString("com.google.android.geo.API_KEY");
        } catch (Exception e) {
          Log.e(TAG, "Failed to retrieve API key from metadata", e);
          // API key should be configured in secrets.properties
        }
      }

      try {
        if (apiKey != null) {
          Places.initialize(getApplicationContext(), apiKey);
        } else {
          Log.d(TAG, "Null API Key");
        }
      } catch (IllegalArgumentException e) {
        Toast.makeText(this, "Error initializing Google Maps: " + e.getMessage(), Toast.LENGTH_LONG)
            .show();
        // Continue without maps functionality
      }
    }
  }

  // Configure ActionBar appearance and styling
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) return;

    // Set styled title with color
    int titleColor = ContextCompat.getColor(this, R.color.white);
    SpannableString spannableTitle = new SpannableString(getString(R.string.new_party_title));
    spannableTitle.setSpan(
        new ForegroundColorSpan(titleColor),
        0,
        spannableTitle.length(),
        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    actionBar.setTitle(spannableTitle);

    // Set background drawable
    Drawable backgroundDrawable = ContextCompat.getDrawable(this, R.color.primaryBlue);
    if (backgroundDrawable != null) {
      actionBar.setBackgroundDrawable(backgroundDrawable);
    }
  }

  // Initialize all view references and components
  private void initializeViews() {
    // Image views
    imgLogin = findViewById(R.id.imgLogin);
    imgGroupPicture = findViewById(R.id.imgGroupPicture);

    // Buttons
    btnNext1 = findViewById(R.id.btnNext1);
    btnNext2 = findViewById(R.id.btnNext2);
    btnBack1 = findViewById(R.id.btnBack1);
    btnBack2 = findViewById(R.id.btnBack2);
    btnDone = findViewById(R.id.btnDone);
    btnAddGroup = findViewById(R.id.btnAddGroup);

    // Text views
    tvPartyName = findViewById(R.id.tvPartyName);
    tvPartyDate = findViewById(R.id.tvPartyDate);
    tvGroupPicture = findViewById(R.id.tvGroupPicture);
    tvHours = findViewById(R.id.tvHours);
    tvSelectedDate = findViewById(R.id.tvSelectedDate);

    // Input views
    etPartyName = findViewById(R.id.etPartyName);
    groupTypeCheckBox = findViewById(R.id.cbGroupType);
    timePicker = findViewById(R.id.timePicker);

    // Floating action button
    chatFab = findViewById(R.id.fabChat);

    // Initialize calendar
    selectedDate = Calendar.getInstance();

    // Setup loading state manager for animations
    setupLoadingStateManager();

    // Setup button animations
    setupButtonAnimations();

    // Setup hours spinner
    setupHoursSpinner();
  }

  // Configure hours spinner with adapter
  private void setupHoursSpinner() {
    ArrayAdapter<CharSequence> hoursAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_hours, android.R.layout.simple_spinner_item);
    hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  }

  // Setup loading state manager for success/error animations
  private void setupLoadingStateManager() {
    // Find views for loading state management
    View animationOverlay = findViewById(R.id.animation_overlay);
    LottieAnimationView lottieView = findViewById(R.id.lottie_feedback);
    TextView feedbackText = findViewById(R.id.feedback_text);

    if (animationOverlay != null && lottieView != null) {
      // Create a simple progress bar as fallback
      android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);

      loadingStateManager =
          new LoadingStateManager.Builder()
              .contentView(findViewById(android.R.id.content)) // Use root content view
              .progressBar(progressBar)
              .loadingText(feedbackText)
              .errorView(null)
              .lottieAnimation(lottieView)
              .build();
    }
  }

  // Setup professional animations for all buttons
  private void setupButtonAnimations() {
    // Apply animations to all navigation buttons
    if (btnNext1 != null) ButtonAnimationHelper.enhanceButton(btnNext1);
    if (btnNext2 != null) ButtonAnimationHelper.enhanceButton(btnNext2);
    if (btnBack1 != null) ButtonAnimationHelper.enhanceButton(btnBack1);
    if (btnBack2 != null) ButtonAnimationHelper.enhanceButton(btnBack2);
    if (btnDone != null) ButtonAnimationHelper.enhanceButton(btnDone);
    if (btnAddGroup != null) ButtonAnimationHelper.enhanceButton(btnAddGroup);

    // Apply special animation to FAB chat button
    if (chatFab != null) {
      ButtonAnimationHelper.applyPressAnimation(chatFab, true);
      ButtonAnimationHelper.applyEntranceAnimation(chatFab, CHAT_FAB_ANIMATION_DELAY_MS);
    }

    // Add entrance animations for UI elements with stagger effect
    if (imgLogin != null) ButtonAnimationHelper.applyEntranceAnimation(imgLogin, LOGIN_ANIMATION_DELAY_MS);
    if (tvPartyName != null) ButtonAnimationHelper.applyEntranceAnimation(tvPartyName, NAME_ANIMATION_DELAY_MS);
    if (etPartyName != null) ButtonAnimationHelper.applyEntranceAnimation(etPartyName, EDIT_TEXT_ANIMATION_DELAY_MS);
  }

  // Initialize map fragment and location services
  private void setupMapAndLocation() {
    // Initialize map fragment
    SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    // Initialize autocomplete fragment
    setupAutocompleteFragment();

    // Initialize location client
    locationClient = LocationServices.getFusedLocationProviderClient(this);
  }

  // Configure Google Places autocomplete fragment
  private void setupAutocompleteFragment() {
    AutocompleteSupportFragment autocompleteFragment =
        (AutocompleteSupportFragment)
            getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

    if (autocompleteFragment == null) return;

    // Set required place fields
    autocompleteFragment.setPlaceFields(
        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

    // Set place selection listener
    autocompleteFragment.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {
          @Override
          public void onPlaceSelected(@NonNull Place place) {
            handlePlaceSelection(place);
          }

          @Override
          public void onError(@NonNull Status status) {
            handlePlaceSelectionError(status);
          }
        });
  }

  // Handle successful place selection from autocomplete
  private void handlePlaceSelection(@NonNull Place place) {
    try {
      chosenLatLng = MapUtilitiesManager.centerMapOnChosenPlace(map, place);
      Log.d(TAG, "Place selected: " + place.getName());
    } catch (Exception e) {
      Log.e(TAG, "Error handling place selection", e);
      showErrorMessage(getString(R.string.error_place_selection));
    }
  }

  // Handle place selection errors
  private void handlePlaceSelectionError(@NonNull Status status) {
    String errorMessage = "Search error: " + status.getStatusMessage();
    Log.w(TAG, "Place selection error: " + status.getStatusMessage());
    showErrorMessage(errorMessage);
  }

  // Display error message to user with animation
  private void showErrorMessage(String message) {
    // Show error animation with animation overlay
    View animationOverlay = findViewById(R.id.animation_overlay);
    com.airbnb.lottie.LottieAnimationView lottieView = findViewById(R.id.lottie_feedback);
    TextView feedbackText = findViewById(R.id.feedback_text);

    if (animationOverlay != null && lottieView != null && feedbackText != null) {
      // Setup and show error animation
      animationOverlay.setVisibility(View.VISIBLE);
      feedbackText.setText(message);

      lottieView.setAnimation("error_warning.json");
      lottieView.setRepeatCount(0); // Play once
      lottieView.playAnimation();

      // Auto-hide after animation completes
      ThreadUtils.runOnMainThreadDelayed(
          () -> {
            if (animationOverlay != null) {
              animationOverlay.setVisibility(View.GONE);
            }
          },
          ERROR_ANIMATION_HIDE_DELAY_MS);
    } else {
      // Fallback to toast if overlay not available
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
  }

  // Validate next-button if name is empty
  private void setupValidation() {
    // Check in real-time
    etPartyName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Check if empty
            String text = s.toString().trim();
            btnNext1.setEnabled(!text.isEmpty());

            // Change the color according to the status
            if (text.isEmpty()) {
              btnNext1.setAlpha(0.5f); // Bright button
            } else {
              btnNext1.setAlpha(1.0f); // Normal button
            }
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    // Set start point for button
    btnNext1.setEnabled(false);
    btnNext1.setAlpha(0.5f);
  }

  // Validate the name of the party
  private boolean validatePartyName() {
    String text = etPartyName.getText().toString().trim();
    if (text.isEmpty()) {
      etPartyName.setError("Enter party name");
      etPartyName.requestFocus();
      return false;
    }
    return true;
  }

  // Main event handler
  @SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "DefaultLocale"})
  private void eventHandler() {
    setupNavigationButtons();
    setupFormButtons();
    setupImageSelection();
    setupFloatingActionButton();
  }

  private void setupNavigationButtons() {
    btnNext1.setOnClickListener(this::handleNextButtonClick);
    btnNext2.setOnClickListener(this::handleSecondStepNavigation);
    btnBack1.setOnClickListener(this::handleBackToFirstStep);
    btnBack2.setOnClickListener(this::handleBackToSecondStep);
  }

  private void setupFormButtons() {
    btnAddGroup.setOnClickListener(this::handleCreateGroup);
    btnDone.setOnClickListener(this::handleDoneClick);
    tvSelectedDate.setOnClickListener(v -> showDatePicker());
  }

  private void setupImageSelection() {
    imgGroupPicture.setOnClickListener(this::handleImageSelection);
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingActionButton() {
    chatFab.setOnClickListener(this::handleChatFabClick);
    chatFab.setOnTouchListener(this::handleChatFabTouch);
  }

  // Navigation handlers
  private void handleNextButtonClick(View v) {
    if (!validatePartyName()) {
      return;
    }

    transitionToLocationStep();
  }

  private void handleSecondStepNavigation(View v) {
    transitionToDateTimeStep();
  }

  private void handleBackToFirstStep(View v) {
    transitionToNameStep();
  }

  private void handleBackToSecondStep(View v) {
    transitionBackToLocationStep();
  }

  // Step transition methods
  private void transitionToLocationStep() {
    hideViews(tvPartyName, etPartyName, imgLogin, btnNext1);
    showViews(btnNext2, btnBack1);

    showMapAndLocationSearch();
  }

  private void transitionBackToLocationStep() {
    showViews(groupTypeCheckBox, btnNext2, btnBack1);
    hideViews(tvPartyDate, tvHours, tvSelectedDate, timePicker, btnAddGroup, btnBack2);

    showMapAndLocationSearch();
  }

  private void transitionToDateTimeStep() {
    hideViews(groupTypeCheckBox, btnNext2, btnBack1);
    showViews(tvPartyDate, tvHours, tvSelectedDate, timePicker, btnAddGroup, btnBack2);

    hideMapAndLocationSearch();
    showDatePicker();
  }

  private void transitionToNameStep() {
    hideViews(btnNext2, btnBack1);
    showViews(tvPartyName, etPartyName, imgLogin, btnNext1);

    hideMapAndLocationSearch();
  }

  private void transitionToImageStep() {
    hideViews(
        imgLogin,
        tvPartyDate,
        tvHours,
        tvSelectedDate,
        timePicker,
        btnBack2,
        btnAddGroup,
        groupTypeCheckBox);
    showViews(imgGroupPicture, tvGroupPicture, btnDone);

    updateActionBarForImageStep();
    scheduleInstructionTextUpdate();
  }

  // Form handlers
  private void handleCreateGroup(View v) {
    try {
      Group group = createGroupFromForm();

      // Show loading indicator or disable button
      btnAddGroup.setEnabled(false);

      // Save group asynchronously
      saveGroupToDatabase(group);

      // Success message and UI transition will be handled in the callback
    } catch (Exception e) {
      handleGroupCreationError(e);
    }
  }

  private void handleDoneClick(View v) {
    navigateToMainActivity();
  }

  private void handleImageSelection(View v) {
    openImagePicker();
    tvGroupPicture.setVisibility(View.INVISIBLE);
  }

  private void handleChatFabClick(View view) {
    Intent intent = new Intent(CreateGroupActivity.this, GptChatActivity.class);
    startActivity(intent);
  }

  @SuppressLint("ClickableViewAccessibility")
  private boolean handleChatFabTouch(View view, MotionEvent event) {
    return IntentExtrasManager.dragChatButtonOnTouch(view, event);
  }

  // Group creation logic
  private Group createGroupFromForm() {
    GroupDataManager builder =
        new GroupDataManager()
            .setName(etPartyName.getText().toString())
            .setAdmin(getCurrentUserEmail())
            .setType(determineGroupType())
            .setCreationTime(getCurrentTimestamp())
            .setPrice("0")
            .setLocation(getSelectedLocation())
            .setDateTime(getSelectedDateTime());

    return builder.build();
  }

  private void saveGroupToDatabase(Group group) {
    String groupKey = generateGroupKey();
    currentGroupKey = groupKey;
    group.setGroupKey(groupKey);

    // Use FirebaseServerClient instead of direct Firebase calls
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.saveGroup(
        groupKey,
        group,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group saved successfully via server client");

            // Show success notification
            NotificationManager.showUpdateNotification(
                CreateGroupActivity.this,
                "Party Created",
                "Party '" + group.getGroupName() + "' created successfully!");

            // Add admin to group members
            addAdminToGroup(group);

            // Initialize group chat
            initializeGroupChat(group);

            // Initialize group encryption
            initializeGroupEncryption(groupKey);

            // Show success message and transition to next step on the UI thread
            ThreadUtils.runOnMainThread(
                () -> {
                  showSuccessMessage();
                  transitionToImageStep();
                  btnAddGroup.setEnabled(true);
                });
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error saving group via server client: " + errorMessage);

            // Show error message with animation on the UI thread
            ThreadUtils.runOnMainThread(
                () -> {
                  showErrorMessage("Error creating group: " + errorMessage);
                  btnAddGroup.setEnabled(true);
                });
          }
        });
  }

  // Helper methods
  private void showMapAndLocationSearch() {
    findViewById(R.id.mapFragment).setVisibility(View.VISIBLE);
    findViewById(R.id.autocomplete_fragment).setVisibility(View.VISIBLE);
    MapUtilitiesManager.requestLocationPermission(this, map, locationClient, FINE_PERMISSION_CODE);
  }

  private void hideMapAndLocationSearch() {
    findViewById(R.id.mapFragment).setVisibility(View.INVISIBLE);
    findViewById(R.id.autocomplete_fragment).setVisibility(View.INVISIBLE);
  }

  private void updateActionBarForImageStep() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'>Set picture</font>"));
    }
  }

  @SuppressLint("SetTextI18n")
  private void scheduleInstructionTextUpdate() {
    ThreadUtils.runOnMainThreadDelayed(
        () -> tvGroupPicture.setText("Tap on the picture above to set a profile picture"),
        INSTRUCTION_DELAY_MS);
  }

  private void openImagePicker() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    startActivityForResult(
        Intent.createChooser(intent, "Select Picture"), IMAGE_PICKER_REQUEST_CODE);
  }

  private void navigateToMainActivity() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

  // Group configuration methods
  private int determineGroupType() {
    return groupTypeCheckBox.isChecked() ? GroupType.PRIVATE : GroupType.PUBLIC;
  }

  private String getCurrentUserEmail() {
    // Get current user using AuthHelper
    String currentUserEmail = AuthenticationManager.getCurrentUserEmail(this);

    if (currentUserEmail != null) {
      // Return the email in Firebase key format (replace dots with spaces)
      return currentUserEmail.replace('.', ' ');
    }

    // Fallback to Firebase Auth if AuthHelper fails
    FirebaseUser currentUser = DBRef.Auth.getCurrentUser();
    if (currentUser != null) {
      return Objects.requireNonNull(currentUser.getEmail()).replace('.', ' ');
    }

    // If both methods fail, show error and return empty string
    Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
    finish();
    return "";
  }

  private String getCurrentTimestamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    return sdf.format(new Date());
  }

  private String getSelectedLocation() {
    if (chosenLatLng != null) {
      return MapUtilitiesManager.encodeCoordinatesToStringLocation(chosenLatLng);
    } else {
      showLocationWarning();
      return null;
    }
  }

  private GroupDateTimeManager getSelectedDateTime() {
    int hour = timePicker.getHour();
    int minute = timePicker.getMinute();
    String timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

    return new GroupDateTimeManager(selectedDays, selectedMonths, selectedYears, timeString);
  }

  private String generateGroupKey() {
    return DBRef.refGroups.push().getKey();
  }

  private void addAdminToGroup(Group group) {
    String adminKey = getCurrentUserEmail();
    Map<String, Object> memberData = new HashMap<>();
    memberData.put(adminKey, "true");

    // Use FirebaseServerClient instead of direct Firebase calls
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Add to friend keys
    Map<String, Object> friendKeysUpdate = new HashMap<>();
    friendKeysUpdate.put("FriendKeys", memberData);
    serverClient.updateGroup(
        group.getGroupKey(),
        friendKeysUpdate,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Admin added to FriendKeys successfully");
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error adding admin to FriendKeys: " + errorMessage);
          }
        });

    // Add to coming keys
    Map<String, Object> comingKeysUpdate = new HashMap<>();
    comingKeysUpdate.put("ComingKeys", memberData);
    serverClient.updateGroup(
        group.getGroupKey(),
        comingKeysUpdate,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Admin added to ComingKeys successfully");
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error adding admin to ComingKeys: " + errorMessage);
          }
        });
  }

  private void initializeGroupChat(Group group) {
    Map<String, Object> emptyChat = new HashMap<>();

    // Use FirebaseServerClient instead of direct Firebase calls
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    Map<String, Object> messageKeysUpdate = new HashMap<>();
    messageKeysUpdate.put("MessageKeys", emptyChat);

    serverClient.updateGroup(
        group.getGroupKey(),
        messageKeysUpdate,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group chat initialized successfully");
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error initializing group chat: " + errorMessage);
          }
        });
  }

  /** Initialize group encryption when creating a new group */
  private void initializeGroupEncryption(String groupKey) {
    try {
      String currentUserId = AuthenticationManager.getCurrentUserKey(this);

      GroupKeyManager groupKeyManager = new GroupKeyManager(this, currentUserId);

      // Create encryption for new group
      groupKeyManager
          .createGroupWithEncryption(groupKey)
          .thenAccept(
              success -> {
                if (success) {
                  Log.i(TAG, "Group encryption initialized successfully for: " + groupKey);
                } else {
                  Log.w(TAG, "Failed to initialize group encryption for: " + groupKey);
                }
              });

    } catch (Exception e) {
      Log.e(TAG, "Error initializing group encryption", e);
    }
  }

  private void showSuccessMessage() {
    // Show success animation with animation overlay
    View animationOverlay = findViewById(R.id.animation_overlay);
    com.airbnb.lottie.LottieAnimationView lottieView = findViewById(R.id.lottie_feedback);
    TextView feedbackText = findViewById(R.id.feedback_text);

    if (animationOverlay != null && lottieView != null && feedbackText != null) {
      // Setup and show success animation
      animationOverlay.setVisibility(View.VISIBLE);
      feedbackText.setText("🎉 Party created successfully!");

      lottieView.setAnimation("success_checkmark.json");
      lottieView.setRepeatCount(0); // Play once
      lottieView.playAnimation();

      // Auto-hide after animation completes
      ThreadUtils.runOnMainThreadDelayed(
          () -> {
            if (animationOverlay != null) {
              animationOverlay.setVisibility(View.GONE);
            }
          },
          SUCCESS_ANIMATION_HIDE_DELAY_MS);
    } else {
      // Fallback to toast if overlay not available
      Toast.makeText(this, "🎉 Party created successfully!", Toast.LENGTH_SHORT).show();
    }
  }

  private void showLocationWarning() {
    Toast.makeText(this, "Warning: You have not set an address for your party.", Toast.LENGTH_LONG)
        .show();
  }

  private void handleGroupCreationError(Exception e) {
    Toast.makeText(this, "Error creating group: " + e.getMessage(), Toast.LENGTH_LONG).show();
    Log.e(TAG, "Group creation failed", e);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == IMAGE_PICKER_REQUEST_CODE) {
        Uri uri = data.getData();
        if (null != uri) {

          ((ImageView) findViewById(R.id.imgGroupPicture)).setImageURI(uri);

          // Compress image before uploading for better performance
          Toast.makeText(this, "Compressing image...", Toast.LENGTH_SHORT).show();
          ImageCompressor.compressImage(
              this,
              uri,
              new ImageCompressor.CompressCallback() {
                @Override
                public void onCompressSuccess(@NonNull File compressedFile) {
                  ThreadUtils.runOnMainThread(
                      () -> {
                        Toast.makeText(
                                CreateGroupActivity.this,
                                "Image compressed. Uploading...",
                                Toast.LENGTH_SHORT)
                            .show();
                        uploadGroupImage(Uri.fromFile(compressedFile));
                      });
                }

                @Override
                public void onCompressError(String error) {
                  ThreadUtils.runOnMainThread(
                      () -> {
                        Log.w(TAG, "Image compression failed, uploading original: " + error);
                        uploadGroupImage(uri);
                      });
                }
              });
        }
      }
    }
  }

  private void uploadGroupImage(Uri uri) {
    DBRef.refStorage
        .child("UsersImageProfile/Groups/" + currentGroupKey)
        .putFile(uri)
        .addOnSuccessListener(
            taskSnapshot ->
                Toast.makeText(
                        CreateGroupActivity.this, "Image saved successfully", Toast.LENGTH_SHORT)
                    .show())
        .addOnFailureListener(
            exception ->
                Toast.makeText(
                        CreateGroupActivity.this, "Error while saving image", Toast.LENGTH_SHORT)
                    .show());
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      Intent intent = new Intent(CreateGroupActivity.this, ServerSettingsActivity.class);
      startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.logout) {
      AuthenticationManager.clearAuthData(this);
      Intent intent = new Intent(CreateGroupActivity.this, LoginActivity.class);
      startActivity(intent);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  private void showDatePicker() {
    DatePickerDialog datePickerDialog =
        new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
              Calendar pickedDate = Calendar.getInstance();
              pickedDate.set(year, month, dayOfMonth);

              // Check if picked date is in the past
              Calendar today = Calendar.getInstance();
              today.set(Calendar.HOUR_OF_DAY, 0);
              today.set(Calendar.MINUTE, 0);
              today.set(Calendar.SECOND, 0);
              today.set(Calendar.MILLISECOND, 0);

              if (pickedDate.before(today)) {
                Toast.makeText(
                        this,
                        "Cannot select a date in the past. Please choose today or a future date.",
                        Toast.LENGTH_LONG)
                    .show();
                return;
              }

              selectedDate.set(year, month, dayOfMonth);
              updateSelectedDate();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH));

    // Set minimum date to today to prevent past date selection
    Calendar today = Calendar.getInstance();
    datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());

    datePickerDialog.show();
  }

  @SuppressLint("DefaultLocale")
  private void updateSelectedDate() {
    // Get month name in English
    String monthName = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(selectedDate.getTime());

    // Update the selected date text
    tvSelectedDate.setText(
        String.format(
            "%d %s %d",
            selectedDate.get(Calendar.DAY_OF_MONTH), monthName, selectedDate.get(Calendar.YEAR)));

    // Store the values
    selectedDays = String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH));
    selectedMonths = monthName;
    selectedYears = String.valueOf(selectedDate.get(Calendar.YEAR));
  }

  @Override
  public void onRequestPermissionsResult(
      int code, @NonNull String[] perms, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(code, perms, grantResults);
    MapUtilitiesManager.handlePermissionsResult(
        this, code, grantResults, FINE_PERMISSION_CODE, map, locationClient);
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    this.map = googleMap;
    // Wherever the user clicks gets stored in chosenLatLng
    map.setOnMapClickListener(
        latlng -> {
          chosenLatLng = latlng;
          map.clear();
          map.addMarker(new MarkerOptions().position(latlng).title("Party here"));
        });
  }

  private void setupBottomNavigation() {
    NavigationManager.setupBottomNavigation(this, "creategroup");
  }

  // Inner classes for better organization
  public static class GroupType {
    public static final int PUBLIC = 0;
    public static final int PRIVATE = 1;
  }
}
