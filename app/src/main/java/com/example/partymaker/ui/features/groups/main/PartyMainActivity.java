package com.example.partymaker.ui.features.groups.main;

import static com.example.partymaker.utils.core.IntentExtrasManager.hideViews;
import static com.example.partymaker.utils.core.IntentExtrasManager.showViews;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.ui.features.groups.management.AdminOptionsActivity;
import com.example.partymaker.ui.features.groups.chat.ChatActivity;
import com.example.partymaker.ui.features.groups.members.FriendsAddActivity;
import com.example.partymaker.ui.features.groups.members.MembersComingActivity;
import com.example.partymaker.ui.features.groups.members.MembersInvitedActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.security.encryption.GroupKeyManager;
import com.example.partymaker.utils.business.sharing.ContentSharingManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.ui.feedback.NotificationManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;
import java.util.Map;

public class PartyMainActivity extends AppCompatActivity {
  private static final String TAG = "PartyMainActivity";

  private String GroupKey;
  private String UserKey;
  private Map<String, String> MessageKeys;
  private GroupRepository groupRepository;
  private Button back5;
  private TextView tvGroupName;
  private TextView tvCreatedBy;
  private TextView tvEntryPrice;
  private TextView tvGroupLocation;
  private TextView tvDateDays;
  private TextView tvDateMonths;
  private TextView tvDateYears;
  private TextView tvDateHours;
  private CardView Card1, Card2, Card3, Card4, Card5, Card6, Card7, Card8;
  private Group currentGroup;
  private LinearLayout paymentPullUpContainer;

  // Variables for pull-up animation
  private float dY;
  private float initialY;
  private boolean isExpanded = false;

  // Add UI elements for coming/not coming toggle
  private ImageView imgThumbUp, imgThumbDown, imgOptions;
  private TextView tvComing, tvNotComing, tvOptions;
  private boolean isUserAdmin = false;
  private boolean isUserComing = false;
  private boolean hasUserPaid = false;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_main);

    try {
      // Hide action bar to remove black bar at top
      androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.hide();
      }

      Log.d(TAG, "PartyMainActivity onCreate started");

      // Get data from intent
      Intent intent = getIntent();
      Log.d(TAG, "Got intent: " + intent);
      Log.d(
          TAG,
          "Intent extras: " + (intent.getExtras() != null ? intent.getExtras().keySet() : "null"));

      // Try to get GroupKey directly from intent first
      GroupKey = intent.getStringExtra("GroupKey");
      Log.d(TAG, "GroupKey from intent: " + GroupKey);

      // If not found, try to get it from ExtrasMetadata
      if (GroupKey == null || GroupKey.isEmpty()) {
        Log.d(TAG, "GroupKey not found directly in intent, checking ExtrasMetadata");
        ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(intent);
        if (extras != null) {
          GroupKey = extras.getGroupKey();
          Log.d(TAG, "GroupKey from ExtrasMetadata: " + GroupKey);
        } else {
          Log.d(TAG, "ExtrasMetadata is null");
        }
      }

      // Try to get UserKey from AuthHelper first
      try {
        UserKey = AuthenticationManager.getCurrentUserKey(this);
        Log.d(TAG, "UserKey from AuthHelper: " + UserKey);
      } catch (Exception e) {
        Log.e(TAG, "Failed to get current user email", e);
        // Fallback to intent if auth fails
        UserKey = intent.getStringExtra("UserKey");
        Log.d(TAG, "UserKey from intent: " + UserKey);
      }

      Log.d(TAG, "UserKey initialized: " + UserKey);
      Log.d(TAG, "GroupKey initialized: " + GroupKey);

      // Initialize views
      initializeViews();

      // Check if Card6 is null
      if (Card6 == null) {
        Log.e(TAG, "Card6 is null");
      } else {
        Log.d(TAG, "Card6 is not null");
      }

      // Check if we have the required data
      if (GroupKey == null || GroupKey.isEmpty()) {
        Log.e(TAG, "Missing GroupKey in intent");
        showErrorAndFinish("Missing group data. Please try again.");
        return;
      }

      if (UserKey == null || UserKey.isEmpty()) {
        Log.e(TAG, "Missing UserKey");
        showErrorAndFinish("Missing user data. Please log in again.");
        return;
      }

      // Initialize repository
      groupRepository = GroupRepository.getInstance();

      // Load group data
      loadGroupData();

    } catch (Exception e) {
      Log.e(TAG, "Unexpected error in onCreate", e);
      showErrorAndFinish("An unexpected error occurred. Please try again.");
    }
  }

  /**
   * Shows an error message and finishes the activity
   *
   * @param message The error message to display
   */
  private void showErrorAndFinish(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    finish();
  }

  /** Loads group data from the repository with caching support */
  private void loadGroupData() {
    loadGroupData(false); // Use cache by default
  }

  /**
   * Loads group data from the repository with caching support
   *
   * @param forceRefresh Whether to force refresh from server
   */
  private void loadGroupData(boolean forceRefresh) {
    try {
      // Show loading indicator
      showLoading(true);

      groupRepository.getGroup(
          GroupKey,
          new GroupRepository.DataCallback<Group>() {
            @Override
            public void onDataLoaded(Group group) {
              try {
                if (group == null) {
                  Log.e(TAG, "Group data is null");
                  showErrorAndFinish("Failed to load group data");
                  return;
                }

                currentGroup = group;
                Log.d(
                    TAG,
                    "Loaded group data from server: "
                        + group.getGroupName()
                        + ", key: "
                        + GroupKey);
                Log.d(TAG, "Current user: " + UserKey + ", Admin key: " + group.getAdminKey());

                // Debug: Log ComingKeys data from server
                if (group.getComingKeys() != null) {
                  Log.d(
                      TAG, "ComingKeys loaded from server - size: " + group.getComingKeys().size());
                  Log.d(TAG, "ComingKeys details from server:");
                  for (String key : group.getComingKeys().keySet()) {
                    Log.d(
                        TAG,
                        "  ComingKey from server: '"
                            + key
                            + "' -> "
                            + group.getComingKeys().get(key));
                  }
                } else {
                  Log.e(TAG, "ComingKeys is null in group data from server!");
                }

                // Update UI with group data
                ThreadUtils.runOnMainThread(
                    () -> {
                      try {
                        updateGroupUI(group);
                        setupClickListeners();
                        
                        // Initialize group encryption proactively
                        initializeGroupEncryption();
                        
                        showLoading(false);
                      } catch (Exception e) {
                        Log.e(TAG, "Error updating UI with group data", e);
                        showError("Error displaying group data");
                      }
                    });
              } catch (Exception e) {
                Log.e(TAG, "Error processing group data", e);
                showError("Error processing group data");
              }
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Failed to get group details: " + errorMessage);
              showError("Failed to load group details: " + errorMessage);
            }
          },
          forceRefresh);

      // Load messages with error handling
      loadMessages();

    } catch (Exception e) {
      Log.e(TAG, "Error loading group data", e);
      showError("Error loading group data");
    }
  }

  /** Loads messages for the current group with proper error handling */
  private void loadMessages() {
    try {
      FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
      serverClient.getMessages(
          GroupKey,
          new FirebaseServerClient.DataCallback<>() {
            @Override
            public void onSuccess(
                java.util.List<com.example.partymaker.data.model.ChatMessage> messages) {
              try {
                if (messages == null || messages.isEmpty()) {
                  Log.d(TAG, "No messages found for group");
                  return;
                }

                // Update message keys
                MessageKeys = new HashMap<>();
                for (ChatMessage message : messages) {
                  MessageKeys.put(message.getMessageKey(), "true");
                }

                Log.d(TAG, "Loaded " + messages.size() + " messages for group");
              } catch (Exception e) {
                Log.e(TAG, "Error processing messages", e);
                showError("Error processing messages");
              }
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Failed to load messages: " + errorMessage);
              // Don't show error to user for messages - non-critical
            }
          });
    } catch (Exception e) {
      Log.e(TAG, "Error loading messages", e);
      // Don't show error to user for messages - non-critical
    }
  }

  /**
   * Shows or hides a loading indicator
   *
   * @param show True to show loading, false to hide
   */
  private void showLoading(boolean show) {
    ThreadUtils.runOnMainThread(
        () -> {
          // You can implement a proper loading indicator here
          // For now, just disable/enable UI elements
          if (Card1 != null) Card1.setEnabled(!show);
          if (Card2 != null) Card2.setEnabled(!show);
          if (Card3 != null) Card3.setEnabled(!show);
          if (Card4 != null) Card4.setEnabled(!show);
          if (Card5 != null) Card5.setEnabled(!show);
          if (Card6 != null) Card6.setEnabled(!show);
          if (Card7 != null) Card7.setEnabled(!show);
          if (Card8 != null) Card8.setEnabled(!show);
        });
  }

  /**
   * Shows an error message to the user
   *
   * @param message The error message to display
   */
  private void showError(String message) {
    ThreadUtils.runOnMainThread(
        () -> {
          try {
            Toast.makeText(PartyMainActivity.this, message, Toast.LENGTH_LONG).show();
            showLoading(false);
          } catch (Exception e) {
            Log.e(TAG, "Error showing error message", e);
          }
        });
  }

  private void initializeViews() {
    Log.d(TAG, "Initializing views");

    // Initialize existing views
    back5 = findViewById(R.id.back5);
    tvGroupName = findViewById(R.id.tvGroupName);
    tvCreatedBy = findViewById(R.id.tvCreatedBy);
    tvEntryPrice = findViewById(R.id.tvEntryPrice);
    tvGroupLocation = findViewById(R.id.tvGroupLocation);
    tvDateDays = findViewById(R.id.tvDateDays);
    tvDateMonths = findViewById(R.id.tvDateMonths);
    tvDateYears = findViewById(R.id.tvDateYears);
    tvDateHours = findViewById(R.id.tvDateHours);

    // Initialize card views
    Card1 = findViewById(R.id.Card1);
    Card2 = findViewById(R.id.Card2);
    Card3 = findViewById(R.id.Card3);
    Card4 = findViewById(R.id.Card4);
    Card5 = findViewById(R.id.Card5);
    Card6 = findViewById(R.id.Card6);
    Card7 = findViewById(R.id.Card7);
    Card8 = findViewById(R.id.Card8);

    // Initialize Card5 coming/not coming elements
    imgThumbUp = findViewById(R.id.imgThumbUp);
    imgThumbDown = findViewById(R.id.imgThumbDown);
    imgOptions = findViewById(R.id.imgOptions);
    tvComing = findViewById(R.id.tvComing);
    tvNotComing = findViewById(R.id.tvNotComing);
    tvOptions = findViewById(R.id.tvOptions);

    // Initialize payment elements
    paymentPullUpContainer = findViewById(R.id.paymentPullUpContainer);

    if (paymentPullUpContainer != null) {
      // Set click listener for the payment button
      paymentPullUpContainer.setOnClickListener(v -> handlePayment());
    } else {
      Log.e(TAG, "Payment container not found in layout");
    }

    // Initialize share button
    FloatingActionButton fabShare = findViewById(R.id.fabShare);
    if (fabShare != null) {
      fabShare.setOnClickListener(v -> showShareOptions());
    } else {
      Log.e(TAG, "Share button not found in layout");
    }

    // Initialize pencil icon for group name edit
    ImageView imgEditGroupName = findViewById(R.id.imgEditGroupName);
    if (imgEditGroupName != null) {
      imgEditGroupName.setOnClickListener(v -> showEditGroupNameDialog());
    }

    // Initialize MessageKeys
    MessageKeys = new HashMap<>();

    Log.d(TAG, "Views initialized successfully");
  }

  /** Shows a dialog for editing the group name */
  private void showEditGroupNameDialog() {
    try {
      // Create an EditText for user input
      final android.widget.EditText input = new android.widget.EditText(this);
      input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
      input.setText(currentGroup != null ? currentGroup.getGroupName() : "");
      input.setSelectAllOnFocus(true);

      // Add padding to the EditText
      int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
      input.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

      // Create the AlertDialog
      new android.app.AlertDialog.Builder(this)
          .setTitle("Edit Group Name")
          .setView(input)
          .setPositiveButton(
              "Save",
              (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                  updateGroupName(newName);
                } else {
                  Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                }
              })
          .setNegativeButton("Cancel", null)
          .show();
    } catch (Exception e) {
      Log.e(TAG, "Error showing edit group name dialog", e);
      Toast.makeText(this, "Error opening edit dialog", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Updates the group name in Firebase through the server
   *
   * @param newName The new group name
   */
  private void updateGroupName(String newName) {
    if (currentGroup == null || GroupKey == null || GroupKey.isEmpty()) {
      Toast.makeText(this, "Group data not available", Toast.LENGTH_SHORT).show();
      return;
    }

    // Show loading indicator
    showLoading(true);

    // Create updates map
    Map<String, Object> updates = new HashMap<>();
    updates.put("groupName", newName);

    // Update the group name through FirebaseServerClient
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.updateGroup(
        GroupKey,
        updates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            // Update UI
            tvGroupName.setText(newName);

            // Update local group object
            currentGroup.setGroupName(newName);

            // Hide loading indicator and show success message
            showLoading(false);
            Toast.makeText(
                    PartyMainActivity.this, "Group name updated successfully", Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onError(String errorMessage) {
            // Hide loading indicator and show error message
            showLoading(false);
            Toast.makeText(
                    PartyMainActivity.this,
                    "Failed to update group name: " + errorMessage,
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  /** Sets up the pull-up gesture for the payment panel */
  @SuppressLint("ClickableViewAccessibility")
  private void setupPullUpGesture() {
    paymentPullUpContainer.setOnTouchListener(
        (view, event) -> {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              // Record initial touch position
              dY = view.getY() - event.getRawY();
              initialY = view.getY();
              return true;

            case MotionEvent.ACTION_MOVE:
              // Calculate new position with constraints
              float newY = event.getRawY() + dY;

              // For pull down, constraints are different
              if (newY < -70) newY = -70;
              if (newY > 0) newY = 0;

              // Update position
              view.animate().y(newY).setDuration(0).start();
              return true;

            case MotionEvent.ACTION_UP:
              // Determine if we should expand or collapse based on how far user pulled
              float pulledDistance = view.getY() - initialY;
              int containerHeight = view.getHeight();

              if (pulledDistance > (float) containerHeight / 3) {
                // Expand if pulled more than 1/3 of the height
                expandPaymentPanel();
              } else {
                // Collapse
                collapsePaymentPanel();
              }
              return true;

            default:
              return false;
          }
        });
  }

  /** Toggles the payment panel between expanded and collapsed states */
  private void togglePaymentPanel() {
    if (isExpanded) {
      collapsePaymentPanel();
    } else {
      expandPaymentPanel();
    }
  }

  /** Expands the payment panel with animation */
  private void expandPaymentPanel() {
    ObjectAnimator animation = ObjectAnimator.ofFloat(paymentPullUpContainer, "translationY", 0f);
    animation.setDuration(300);
    animation.setInterpolator(new DecelerateInterpolator());
    animation.start();
    isExpanded = true;
  }

  /** Collapses the payment panel with animation */
  private void collapsePaymentPanel() {
    // Collapse to show only the handle (-70dp is set in XML)
    ObjectAnimator animation = ObjectAnimator.ofFloat(paymentPullUpContainer, "translationY", -70f);
    animation.setDuration(300);
    animation.setInterpolator(new DecelerateInterpolator());
    animation.start();
    isExpanded = false;
  }

  private void setupClickListeners() {
    Log.d(TAG, "Setting up click listeners");

    // Back button - navigate to MainActivity instead of just finishing
    back5.setOnClickListener(
        v -> {
          Log.d(TAG, "Back button clicked, navigating to MainActivity");
          Intent intent =
              new Intent(
                  PartyMainActivity.this, MainActivity.class);
          startActivity(intent);
          finish();
        });

    // Check if Card6 is null
    if (Card6 == null) {
      Log.e(TAG, "Card6 is null! Cannot set up click listener");
    } else {
      Log.d(TAG, "Card6 is not null");

      Card6.setOnClickListener(
          v -> {
            Log.d(TAG, "Card6 (Chat button) clicked");
            navigateToChatActivity();
          });
    }

    // Remove test button code

    Card1.setOnClickListener(
        v -> {
          if (currentGroup != null && currentGroup.getGroupLocation() != null) {
            String location = currentGroup.getGroupLocation();
            if (location.contains(",")) {
              // It's coordinates, show map
              showMapDialog(location);
            } else {
              // It's a text location, show in toast
              Toast.makeText(PartyMainActivity.this, "Location: " + location, Toast.LENGTH_LONG)
                  .show();
            }
          } else {
            Toast.makeText(PartyMainActivity.this, "No location available", Toast.LENGTH_SHORT)
                .show();
          }
        });

    Card2.setOnClickListener(
        v -> {
          if (currentGroup != null) {
            // Show date information only - no editing allowed
            String date =
                currentGroup.getGroupDays()
                    + "/"
                    + currentGroup.getGroupMonths()
                    + "/"
                    + currentGroup.getGroupYears();
            String time = currentGroup.getGroupHours();
            Toast.makeText(
                    PartyMainActivity.this,
                    "Party Date: " + date + " at " + time,
                    Toast.LENGTH_LONG)
                .show();
          }
        });

    Card3.setOnClickListener(
        v -> {
          if (currentGroup != null && currentGroup.getFriendKeys() != null) {
            navigateToMembersInvitedActivity();
          } else {
            Toast.makeText(PartyMainActivity.this, "No invited people", Toast.LENGTH_SHORT).show();
          }
        });

    Card4.setOnClickListener(
        v -> {
          if (currentGroup != null && currentGroup.getComingKeys() != null) {
            navigateToMembersComingActivity();
          } else {
            Toast.makeText(PartyMainActivity.this, "No people coming", Toast.LENGTH_SHORT).show();
          }
        });

    // Card5 - Admin options OR Coming/Not coming toggle
    Card5.setOnClickListener(
        v -> {
          Log.d(
              TAG,
              "Card5 clicked - isUserAdmin: " + isUserAdmin + ", isUserComing: " + isUserComing);

          // Double check admin status
          boolean isAdmin = false;
          if (currentGroup != null && currentGroup.getAdminKey() != null && UserKey != null) {
            isAdmin = currentGroup.getAdminKey().equals(UserKey);
            Log.d(TAG, "Card5 click - Rechecked admin status: " + isAdmin);
          }

          if (isAdmin) {
            Log.d(TAG, "Navigating to admin options as user is admin");
            navigateToAdminOptionsActivity();
          } else {
            Log.d(TAG, "Toggling coming status as user is not admin");
            toggleComingStatus();
          }
        });

    Card7.setOnClickListener(v -> navigateToAddFriendsActivity());

    Card8.setOnClickListener(
        v -> {
          // Show confirmation dialog for leaving group
          new android.app.AlertDialog.Builder(this)
              .setTitle("Leave Group")
              .setMessage("Are you sure you want to leave this group?")
              .setPositiveButton("Yes", (dialog, which) -> leaveGroup())
              .setNegativeButton("No", null)
              .show();
        });
  }

  @SuppressLint("SetTextI18n")
  private void updateGroupUI(Group group) {
    if (group == null) return;

    // Update group name
    tvGroupName.setText(group.getGroupName());

    // Update admin info
    tvCreatedBy.setText(group.getAdminKey());

    // Update entry price
    String price = group.getGroupPrice();
    tvEntryPrice.setText(price.equals("0") ? "free" : price);

    // Update payment button visibility based on price
    updatePaymentButtonVisibility(price);

    // Load group image
    ImageView groupImage = findViewById(R.id.imgGroupPicture);
    if (groupImage != null) {
      // Try to load from new path first
      com.example.partymaker.data.firebase.DBRef.refStorage
          .child("UsersImageProfile/Groups/" + GroupKey)
          .getDownloadUrl()
          .addOnSuccessListener(
              uri -> {
                com.squareup.picasso.Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.default_group_image)
                    .error(R.drawable.default_group_image)
                    .into(groupImage);
                Log.d(TAG, "Loaded group image from new path");
              })
          .addOnFailureListener(
              e -> {
                // Try old path as fallback
                com.example.partymaker.data.firebase.DBRef.refStorage
                    .child("Groups/" + GroupKey)
                    .getDownloadUrl()
                    .addOnSuccessListener(
                        uri -> {
                          com.squareup.picasso.Picasso.get()
                              .load(uri)
                              .placeholder(R.drawable.default_group_image)
                              .error(R.drawable.default_group_image)
                              .into(groupImage);
                          Log.d(TAG, "Loaded group image from old path");
                        })
                    .addOnFailureListener(
                        e2 -> {
                          // Set default image if both paths fail
                          groupImage.setImageResource(R.drawable.default_group_image);
                          Log.d(TAG, "Failed to load group image from either path");
                        });
              });
    }

    // Update location
    String location = group.getGroupLocation();
    if (location != null && !location.isEmpty()) {
      // Check if it's coordinates (contains comma and numbers)
      if (location.contains(",") && location.matches(".*\\d+.*")) {
        // It's coordinates, show a clean location text
        tvGroupLocation.setText("📍 Location");
        tvGroupLocation.setVisibility(View.VISIBLE);
      } else {
        // It's a text location, show as is
        tvGroupLocation.setText(location);
        tvGroupLocation.setVisibility(View.VISIBLE);
      }
    } else {
      tvGroupLocation.setText("No Location");
      tvGroupLocation.setVisibility(View.VISIBLE);
    }

    // Update date
    tvDateDays.setText(group.getGroupDays());
    tvDateMonths.setText(group.getGroupMonths());
    tvDateYears.setText(group.getGroupYears());
    tvDateHours.setText(group.getGroupHours());

    // Check if user is admin - compare the actual strings
    String adminKey = group.getAdminKey();
    Log.d(TAG, "Checking admin status - AdminKey: " + adminKey + ", UserKey: " + UserKey);

    if (adminKey != null && UserKey != null) {
      isUserAdmin = adminKey.equals(UserKey);
      Log.d(TAG, "Admin status determined: " + isUserAdmin);
    } else {
      isUserAdmin = false;
      Log.d(TAG, "Admin status set to false due to null keys");
    }

    // Check if user is coming
    boolean userComingStatus = false;
    if (group.getComingKeys() != null) {
      userComingStatus = group.getComingKeys().containsKey(UserKey);
      Log.d(TAG, "User coming status from server: " + userComingStatus);
    }
    isUserComing = userComingStatus;

    // Check if user has paid (this would be stored in the database)
    checkPaymentStatus();

    // Update Card5 UI based on admin status
    updateCard5UI();
  }

  /**
   * Updates the payment button visibility based on price and payment status
   *
   * @param price The price of the event
   */
  private void updatePaymentButtonVisibility(String price) {
    // If payment container is null, exit
    if (paymentPullUpContainer == null) return;

    // If price is free or user is admin, hide payment container
    if (price.equals("0") || isUserAdmin) {
      paymentPullUpContainer.setVisibility(View.GONE);
      return;
    }

    // Check if user is coming to the party
    if (!isUserComing) {
      paymentPullUpContainer.setVisibility(View.GONE);
      return;
    }

    // FOR TESTING: Always show payment container
    paymentPullUpContainer.setVisibility(View.VISIBLE);

    // Get the payment text view
    TextView tvPayNow = findViewById(R.id.tvPayNow);

    // Check if user has already paid
    if (hasUserPaid && tvPayNow != null) {
      tvPayNow.setText(R.string.payment_success);
      tvPayNow.setEnabled(false);
      tvPayNow.setTextColor(getResources().getColor(android.R.color.darker_gray));
    } else if (tvPayNow != null) {
      // Show payment button with price
      tvPayNow.setText(getString(R.string.pay_now));
      tvPayNow.setEnabled(true);
      tvPayNow.setTextColor(getResources().getColor(R.color.green));
    }
  }

  /** Checks if the user has already paid for the event */
  private void checkPaymentStatus() {
    if (currentGroup == null || UserKey == null) {
      return;
    }

    // For demo purposes, we'll just set this to false
    hasUserPaid = false;

    // Update the payment button based on this status
    updatePaymentButtonVisibility(currentGroup.getGroupPrice());
  }

  /** Handles the payment process when the user clicks the payment button */
  private void handlePayment() {
    if (currentGroup == null) {
      Toast.makeText(this, "Error: Group data not available", Toast.LENGTH_SHORT).show();
      return;
    }

    String price = currentGroup.getGroupPrice();
    if (price.equals("0")) {
      Toast.makeText(this, "This event is free!", Toast.LENGTH_SHORT).show();
      return;
    }

    // Show payment processing dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.payment_for_party, currentGroup.getGroupName()));
    builder.setMessage(getString(R.string.payment_processing));
    builder.setCancelable(false);
    AlertDialog processingDialog = builder.create();
    processingDialog.show();

    // Simulate payment processing
    ThreadUtils.runOnMainThreadDelayed(
        () -> {
          processingDialog.dismiss();

          // Update payment status in database
          updatePaymentInDatabase();

          // Show success message
          Toast.makeText(this, R.string.payment_success, Toast.LENGTH_LONG).show();

          // Update UI
          hasUserPaid = true;
          TextView tvPayNow = findViewById(R.id.tvPayNow);
          if (tvPayNow != null) {
            tvPayNow.setText(R.string.payment_success);
            tvPayNow.setEnabled(false);
            tvPayNow.setTextColor(getResources().getColor(android.R.color.darker_gray));
          }
        },
        2000); // Simulate 2 second processing time
  }

  /** Updates the payment status in the database */
  private void updatePaymentInDatabase() {
    if (currentGroup == null || UserKey == null) {
      return;
    }

    // In a real app, this would update a payments collection in the database
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Example of how this might be implemented:
    // Path would be something like "Payments/{groupKey}/{userKey}"
    String paymentPath = "Payments/" + GroupKey;
    Map<String, Object> paymentData = new HashMap<>();
    paymentData.put(UserKey, true);

    // Update the database
    serverClient.updateData(
        paymentPath,
        paymentData,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Payment recorded successfully");
            hasUserPaid = true;
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error recording payment: " + errorMessage);
            Toast.makeText(
                    PartyMainActivity.this,
                    "Payment recorded but failed to update database. Please contact support.",
                    Toast.LENGTH_LONG)
                .show();
          }
        });
  }

  private void updateCard5UI() {
    // Log the admin status for debugging
    Log.d(
        TAG,
        "updateCard5UI - isUserAdmin: "
            + isUserAdmin
            + ", UserKey: "
            + UserKey
            + ", Admin key: "
            + (currentGroup != null ? currentGroup.getAdminKey() : "null"));

    // Update admin status based on current group data
    isUserAdmin = checkIfUserIsAdmin();
    Log.d(TAG, "Re-checked admin status: " + isUserAdmin);

    if (isUserAdmin) {
      showAdminUI();
    } else if (isUserComing) {
      showComingUI();
    } else {
      showNotComingUI();
    }
  }

  private boolean checkIfUserIsAdmin() {
    return currentGroup != null
        && currentGroup.getAdminKey() != null
        && currentGroup.getAdminKey().equals(UserKey);
  }

  private void showAdminUI() {
    showViews(imgOptions, tvOptions);
    hideViews(imgThumbUp, imgThumbDown, tvComing, tvNotComing);
    Log.d(TAG, "Showing admin options UI");
  }

  private void showComingUI() {
    showViews(imgThumbUp, tvComing);
    hideViews(imgOptions, tvOptions, imgThumbDown, tvNotComing);
    Log.d(TAG, "Showing 'coming' UI");
  }

  private void showNotComingUI() {
    showViews(imgThumbDown, tvNotComing);
    hideViews(imgOptions, tvOptions, imgThumbUp, tvComing);
    Log.d(TAG, "Showing 'not coming' UI");
  }

  private void toggleComingStatus() {
    if (currentGroup == null || UserKey == null) {
      Toast.makeText(this, "Error: Missing group or user data", Toast.LENGTH_SHORT).show();
      return;
    }

    // Toggle the coming status
    isUserComing = !isUserComing;

    // Update UI immediately for better UX
    updateCard5UI();

    // Update payment button visibility
    if (currentGroup.getGroupPrice() != null) {
      updatePaymentButtonVisibility(currentGroup.getGroupPrice());
    }

    // Update server
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Prepare ComingKeys update
    HashMap<String, Object> updatedComingKeys = new HashMap<>();
    if (currentGroup.getComingKeys() != null) {
      updatedComingKeys.putAll(currentGroup.getComingKeys());
    }

    if (isUserComing) {
      // Add user to ComingKeys
      updatedComingKeys.put(UserKey, true);
      currentGroup.setComingKeys(updatedComingKeys);

      Toast.makeText(this, "Marked as coming", Toast.LENGTH_SHORT).show();
    } else {
      // Remove user from ComingKeys
      updatedComingKeys.remove(UserKey);
      currentGroup.setComingKeys(updatedComingKeys);

      Toast.makeText(this, "Marked as not coming", Toast.LENGTH_SHORT).show();
    }

    // Update server with new ComingKeys
    Map<String, Object> updates = new HashMap<>();
    updates.put("ComingKeys", updatedComingKeys);

    serverClient.updateGroup(
        GroupKey,
        updates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Coming status updated successfully to: " + isUserComing);

            // Update UI to reflect the change
            updateCard5UI();

            // Update payment button visibility
            if (currentGroup.getGroupPrice() != null) {
              updatePaymentButtonVisibility(currentGroup.getGroupPrice());
            }

            // Show toast message
            String message =
                isUserComing
                    ? "You are now coming to this party!"
                    : "You are not coming to this party.";
            Toast.makeText(PartyMainActivity.this, message, Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to update coming status on server: " + errorMessage);
            // Revert the UI change on error
            isUserComing = !isUserComing;
            updateCard5UI();
            Toast.makeText(PartyMainActivity.this, "Error updating status", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void navigateToChatActivity() {
    Log.d(TAG, "Attempting to navigate to ChatActivity");

    // Check if we have the required data
    if (GroupKey == null || GroupKey.isEmpty()) {
      Log.e(TAG, "Cannot navigate to ChatActivity: GroupKey is null or empty");
      Toast.makeText(this, "Missing group data for chat", Toast.LENGTH_SHORT).show();
      return;
    }

    if (UserKey == null || UserKey.isEmpty()) {
      Log.e(TAG, "Cannot navigate to ChatActivity: UserKey is null or empty");
      Toast.makeText(this, "Missing user data for chat", Toast.LENGTH_SHORT).show();
      return;
    }

    try {
      Intent intent = new Intent(PartyMainActivity.this, ChatActivity.class);
      intent.putExtra("GroupKey", GroupKey);
      intent.putExtra("UserKey", UserKey);
      Log.d(
          TAG,
          "Adding extras to ChatActivity intent: GroupKey=" + GroupKey + ", UserKey=" + UserKey);

      // Add additional debug info
      if (currentGroup != null) {
        Log.d(
            TAG,
            "Current group info: name="
                + currentGroup.getGroupName()
                + ", adminKey="
                + currentGroup.getAdminKey()
                + ", messageKeys size="
                + (currentGroup.getMessageKeys() != null
                    ? currentGroup.getMessageKeys().size()
                    : "null"));
      } else {
        Log.w(TAG, "Current group is null when navigating to ChatActivity");
      }

      Log.d(TAG, "Starting ChatActivity...");
      startActivity(intent);
      Log.d(TAG, "ChatActivity started successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error navigating to ChatActivity", e);
      Toast.makeText(this, "Failed to open chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void navigateToAdminOptionsActivity() {
    try {
      Intent intent = new Intent(this, AdminOptionsActivity.class);

      // Pass the same extras that we received
      ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
      if (extras != null) {
        IntentExtrasManager.addExtrasToIntent(intent, extras);
        intent.putExtra("UserKey", UserKey);
        startActivity(intent);
      } else {
        Toast.makeText(this, "Missing group data", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to admin options activity", e);
      Toast.makeText(this, "Failed to open options", Toast.LENGTH_SHORT).show();
    }
  }

  private void navigateToAddFriendsActivity() {
    try {
      Intent intent = new Intent(this, FriendsAddActivity.class);

      // Create ExtrasMetadata with current group data
      if (currentGroup != null) {
        ExtrasMetadata extras =
            new ExtrasMetadata(
                currentGroup.getGroupName(),
                GroupKey,
                currentGroup.getGroupDays(),
                currentGroup.getGroupMonths(),
                currentGroup.getGroupYears(),
                currentGroup.getGroupHours(),
                currentGroup.getGroupLocation(),
                currentGroup.getAdminKey(),
                currentGroup.getCreatedAt(),
                currentGroup.getGroupPrice(),
                currentGroup.getGroupType(),
                true, // CanAdd
                currentGroup.getFriendKeys(),
                currentGroup.getComingKeys(),
                currentGroup.getMessageKeys());
        IntentExtrasManager.addExtrasToIntent(intent, extras);
        startActivity(intent);
      } else {
        Toast.makeText(this, "Group data not available", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to add friends activity", e);
      Toast.makeText(this, "Failed to open add friends", Toast.LENGTH_SHORT).show();
    }
  }

  private void navigateToMembersInvitedActivity() {
    try {
      Intent intent = new Intent(this, MembersInvitedActivity.class);

      // Create ExtrasMetadata with current group data
      if (currentGroup != null) {
        Log.d(
            TAG, "Navigating to MembersInvitedActivity with group: " + currentGroup.getGroupName());
        Log.d(
            TAG,
            "FriendKeys size: "
                + (currentGroup.getFriendKeys() != null
                    ? currentGroup.getFriendKeys().size()
                    : "null"));
        Log.d(
            TAG,
            "ComingKeys size: "
                + (currentGroup.getComingKeys() != null
                    ? currentGroup.getComingKeys().size()
                    : "null"));
        Log.d(TAG, "AdminKey: " + currentGroup.getAdminKey());

        ExtrasMetadata extras =
            new ExtrasMetadata(
                currentGroup.getGroupName(),
                GroupKey,
                currentGroup.getGroupDays(),
                currentGroup.getGroupMonths(),
                currentGroup.getGroupYears(),
                currentGroup.getGroupHours(),
                currentGroup.getGroupLocation(),
                currentGroup.getAdminKey(),
                currentGroup.getCreatedAt(),
                currentGroup.getGroupPrice(),
                currentGroup.getGroupType(),
                currentGroup.isCanAdd(),
                currentGroup.getFriendKeys() != null
                    ? new HashMap<>(currentGroup.getFriendKeys())
                    : new HashMap<>(),
                currentGroup.getComingKeys() != null
                    ? new HashMap<>(currentGroup.getComingKeys())
                    : new HashMap<>(),
                new HashMap<>(MessageKeys));

        IntentExtrasManager.addExtrasToIntent(intent, extras);
        intent.putExtra("UserKey", UserKey);
        startActivity(intent);
      } else {
        Log.e(TAG, "currentGroup is null when trying to navigate to MembersInvitedActivity");
        Toast.makeText(this, "Group data not available", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to members invited activity", e);
      Toast.makeText(this, "Failed to open members invited", Toast.LENGTH_SHORT).show();
    }
  }

  private void navigateToMembersComingActivity() {
    try {
      Intent intent = new Intent(this, MembersComingActivity.class);

      // Create ExtrasMetadata with current group data
      if (currentGroup != null) {
        Log.d(
            TAG, "Navigating to MembersComingActivity with group: " + currentGroup.getGroupName());
        Log.d(
            TAG,
            "FriendKeys size: "
                + (currentGroup.getFriendKeys() != null
                    ? currentGroup.getFriendKeys().size()
                    : "null"));
        Log.d(
            TAG,
            "ComingKeys size: "
                + (currentGroup.getComingKeys() != null
                    ? currentGroup.getComingKeys().size()
                    : "null"));
        Log.d(TAG, "AdminKey: " + currentGroup.getAdminKey());

        // Debug: Print detailed ComingKeys information
        if (currentGroup.getComingKeys() != null) {
          Log.d(TAG, "ComingKeys details:");
          for (String key : currentGroup.getComingKeys().keySet()) {
            Log.d(TAG, "  ComingKey: '" + key + "' -> " + currentGroup.getComingKeys().get(key));
          }
        } else {
          Log.e(TAG, "currentGroup.getComingKeys() is null!");
        }

        HashMap<String, Object> comingKeysToPass =
            currentGroup.getComingKeys() != null
                ? new HashMap<>(currentGroup.getComingKeys())
                : new HashMap<>();
        Log.d(TAG, "ComingKeys to pass size: " + comingKeysToPass.size());

        ExtrasMetadata extras =
            new ExtrasMetadata(
                currentGroup.getGroupName(),
                GroupKey,
                currentGroup.getGroupDays(),
                currentGroup.getGroupMonths(),
                currentGroup.getGroupYears(),
                currentGroup.getGroupHours(),
                currentGroup.getGroupLocation(),
                currentGroup.getAdminKey(),
                currentGroup.getCreatedAt(),
                currentGroup.getGroupPrice(),
                currentGroup.getGroupType(),
                currentGroup.isCanAdd(),
                currentGroup.getFriendKeys() != null
                    ? new HashMap<>(currentGroup.getFriendKeys())
                    : new HashMap<>(),
                comingKeysToPass,
                new HashMap<>(MessageKeys));

        IntentExtrasManager.addExtrasToIntent(intent, extras);
        intent.putExtra("UserKey", UserKey);

        // Debug: Also add ComingKeys directly to intent as backup
        intent.putExtra("ComingKeys", comingKeysToPass);
        Log.d(TAG, "Added ComingKeys directly to intent as backup");

        startActivity(intent);
      } else {
        Log.e(TAG, "currentGroup is null when trying to navigate to MembersComingActivity");
        Toast.makeText(this, "Group data not available", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to members coming activity", e);
      Toast.makeText(this, "Failed to open members coming", Toast.LENGTH_SHORT).show();
    }
  }

  private void leaveGroup() {
    if (currentGroup == null || UserKey == null) {
      Toast.makeText(this, "Cannot leave group: missing data", Toast.LENGTH_SHORT).show();
      return;
    }

    // Check if current user is admin
    boolean isCurrentUserAdmin =
        currentGroup.getAdminKey() != null && currentGroup.getAdminKey().equals(UserKey);

    if (isCurrentUserAdmin) {
      handleAdminLeavingGroup();
    } else {
      handleRegularUserLeavingGroup();
    }
  }

  private void handleAdminLeavingGroup() {
    // Get the current friend keys
    HashMap<String, Object> friendKeys = currentGroup.getFriendKeys();

    if (friendKeys == null || friendKeys.size() <= 1) {
      // Admin is the only member - delete the group completely
      deleteGroupCompletely();
    } else {
      // Transfer admin to another user randomly
      transferAdminAndLeave();
    }
  }

  private void handleRegularUserLeavingGroup() {
    // Remove user from friend keys and coming keys
    HashMap<String, Object> updatedFriendKeys = new HashMap<>(currentGroup.getFriendKeys());
    HashMap<String, Object> updatedComingKeys = new HashMap<>(currentGroup.getComingKeys());

    updatedFriendKeys.remove(UserKey);
    updatedComingKeys.remove(UserKey);

    // Update the group in the server
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Update friend keys
    serverClient.updateGroup(
        GroupKey,
        "FriendKeys",
        updatedFriendKeys,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Void result) {
            // Update coming keys
            serverClient.updateGroup(
                GroupKey,
                "ComingKeys",
                updatedComingKeys,
                new FirebaseServerClient.DataCallback<>() {
                  @Override
                  public void onSuccess(Void result) {
                    // Remove group from local cache since user is no longer part of it
                    try {
                      java.util.concurrent.Executor executor =
                          java.util.concurrent.Executors.newSingleThreadExecutor();
                      executor.execute(
                          () -> {
                            try {
                              com.example.partymaker.data.local.AppDatabase database =
                                  com.example.partymaker.data.local.AppDatabase.getInstance(
                                      getApplicationContext());
                              if (database != null) {
                                database.groupDao().deleteGroupByKey(GroupKey);
                                Log.d(TAG, "Group removed from local cache after user left");
                              }
                            } catch (Exception e) {
                              Log.e(TAG, "Error removing group from local cache", e);
                            }
                          });
                    } catch (Exception e) {
                      Log.e(TAG, "Error accessing local database", e);
                    }

                    Toast.makeText(
                            PartyMainActivity.this, "Left group successfully", Toast.LENGTH_SHORT)
                        .show();
                    finish(); // Close this activity
                  }

                  @Override
                  public void onError(String errorMessage) {
                    Log.e(TAG, "Failed to update coming keys: " + errorMessage);
                    Toast.makeText(
                            PartyMainActivity.this, "Failed to leave group", Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to update friend keys: " + errorMessage);
            Toast.makeText(PartyMainActivity.this, "Failed to leave group", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void deleteGroupCompletely() {
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    serverClient.deleteGroup(
        GroupKey,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            // Delete from local cache directly using database
            try {
              com.example.partymaker.data.repository.GroupRepository repository =
                  com.example.partymaker.data.repository.GroupRepository.getInstance();

              // Use executor to delete from local database directly
              java.util.concurrent.Executor executor =
                  java.util.concurrent.Executors.newSingleThreadExecutor();
              executor.execute(
                  () -> {
                    try {
                      com.example.partymaker.data.local.AppDatabase database =
                          com.example.partymaker.data.local.AppDatabase.getInstance(
                              getApplicationContext());
                      if (database != null) {
                        database.groupDao().deleteGroupByKey(GroupKey);
                        Log.d(TAG, "Group deleted from local cache directly");
                      }
                    } catch (Exception e) {
                      Log.e(TAG, "Error deleting group from local cache", e);
                    }
                  });
            } catch (Exception e) {
              Log.e(TAG, "Error accessing local database", e);
            }

            Toast.makeText(PartyMainActivity.this, "Group deleted successfully", Toast.LENGTH_SHORT)
                .show();
            finish(); // Close this activity
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to delete group: " + errorMessage);
            Toast.makeText(PartyMainActivity.this, "Failed to delete group", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void transferAdminAndLeave() {
    HashMap<String, Object> friendKeys = currentGroup.getFriendKeys();

    // Find a random user to transfer admin to (excluding current admin)
    String newAdminKey = null;
    for (Map.Entry<String, Object> entry : friendKeys.entrySet()) {
      String userKey = entry.getValue().toString();
      if (!userKey.equals(UserKey)) {
        newAdminKey = userKey;
        break; // Take the first non-admin user (pseudo-random since HashMap iteration order is not
        // guaranteed)
      }
    }

    if (newAdminKey == null) {
      Toast.makeText(this, "Failed to find new admin", Toast.LENGTH_SHORT).show();
      return;
    }

    final String finalNewAdminKey = newAdminKey;

    // Remove current admin from friend keys and coming keys
    HashMap<String, Object> updatedFriendKeys = new HashMap<>(friendKeys);
    HashMap<String, Object> updatedComingKeys = new HashMap<>(currentGroup.getComingKeys());

    updatedFriendKeys.remove(UserKey);
    updatedComingKeys.remove(UserKey);

    // Create the update map with all changes
    Map<String, Object> groupUpdates = new HashMap<>();
    groupUpdates.put("adminKey", finalNewAdminKey);
    groupUpdates.put("FriendKeys", updatedFriendKeys);
    groupUpdates.put("ComingKeys", updatedComingKeys);

    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    serverClient.updateGroup(
        GroupKey,
        groupUpdates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            // Delete from local cache directly since user is no longer part of the group
            try {
              java.util.concurrent.Executor executor =
                  java.util.concurrent.Executors.newSingleThreadExecutor();
              executor.execute(
                  () -> {
                    try {
                      com.example.partymaker.data.local.AppDatabase database =
                          com.example.partymaker.data.local.AppDatabase.getInstance(
                              getApplicationContext());
                      if (database != null) {
                        database.groupDao().deleteGroupByKey(GroupKey);
                        Log.d(TAG, "Group removed from local cache after admin transfer");
                      }
                    } catch (Exception e) {
                      Log.e(TAG, "Error removing group from local cache", e);
                    }
                  });
            } catch (Exception e) {
              Log.e(TAG, "Error accessing local database", e);
            }

            Toast.makeText(
                    PartyMainActivity.this,
                    "Admin transferred and left group successfully",
                    Toast.LENGTH_SHORT)
                .show();
            finish(); // Close this activity
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to transfer admin and leave: " + errorMessage);
            Toast.makeText(
                    PartyMainActivity.this,
                    "Failed to transfer admin and leave group",
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void showMapDialog(String locationStr) {
    try {
      // Parse the location string to get latitude and longitude
      String[] latLng = locationStr.split(",");
      if (latLng.length != 2) {
        Log.e(TAG, "Invalid location format: " + locationStr);
        Toast.makeText(this, "Invalid location format", Toast.LENGTH_SHORT).show();
        return;
      }

      final double latitude = Double.parseDouble(latLng[0].trim());
      final double longitude = Double.parseDouble(latLng[1].trim());
      final String locationName = currentGroup.getGroupName();

      // Create a dialog to show the map
      Dialog dialog =
          new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      dialog.setContentView(R.layout.helper_map_view);

      // Set up the close button
      Button btnCloseMap = dialog.findViewById(R.id.btnCloseMap);
      btnCloseMap.setOnClickListener(v -> dialog.dismiss());

      // Set up the open in Google Maps button
      Button btnOpenGoogleMaps = dialog.findViewById(R.id.btnOpenGoogleMaps);
      btnOpenGoogleMaps.setOnClickListener(
          v -> {
            try {
              // Create a URI for Google Maps with the coordinates
              Uri gmmIntentUri =
                  Uri.parse(
                      "geo:"
                          + latitude
                          + ","
                          + longitude
                          + "?q="
                          + latitude
                          + ","
                          + longitude
                          + "("
                          + Uri.encode(locationName)
                          + ")&z=16");
              Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
              mapIntent.setPackage("com.google.android.apps.maps");

              // Check if Google Maps is installed
              if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
                Log.d(
                    TAG,
                    "Successfully opened Google Maps with coordinates: "
                        + latitude
                        + ","
                        + longitude);
              } else {
                // If Google Maps is not installed, open in browser
                Uri browserUri =
                    Uri.parse(
                        "https://www.google.com/maps/search/?api=1&query="
                            + latitude
                            + ","
                            + longitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
                Log.d(TAG, "Opened maps in browser");
              }

              // Close the dialog
              dialog.dismiss();
            } catch (Exception e) {
              Log.e(TAG, "Error opening Google Maps", e);
              Toast.makeText(
                      PartyMainActivity.this,
                      "Could not open Google Maps: " + e.getMessage(),
                      Toast.LENGTH_SHORT)
                  .show();
            }
          });

      // Show the dialog
      dialog.show();

    } catch (NumberFormatException e) {
      Log.e(TAG, "Error parsing location coordinates", e);
      Toast.makeText(this, "Invalid location format", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      Log.e(TAG, "Error showing map dialog", e);
      Toast.makeText(this, "Could not show map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private int getMonthNumber(String monthName) {
    switch (monthName.toLowerCase()) {
      case "jan":
      case "january":
        return 1;
      case "feb":
      case "february":
        return 2;
      case "mar":
      case "march":
        return 3;
      case "apr":
      case "april":
        return 4;
      case "may":
        return 5;
      case "jun":
      case "june":
        return 6;
      case "jul":
      case "july":
        return 7;
      case "aug":
      case "august":
        return 8;
      case "sep":
      case "september":
        return 9;
      case "oct":
      case "october":
        return 10;
      case "nov":
      case "november":
        return 11;
      case "dec":
      case "december":
        return 12;
      default:
        return -1; // Indicate an error
    }
  }

  /** Shows a dialog with share options for the party */
  private void showShareOptions() {
    if (currentGroup == null) {
      Toast.makeText(this, "Party details not available", Toast.LENGTH_SHORT).show();
      return;
    }

    final String[] options = {
      "Share via Text", "Share to WhatsApp", "Share to Facebook", "Share via SMS", "Share via Email"
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Share Party");
    builder.setItems(
        options,
        (dialog, which) -> {
          switch (which) {
            case 0: // Share via Text
              ContentSharingManager.sharePartyText(PartyMainActivity.this, currentGroup);
              break;
            case 1: // Share to WhatsApp
              ContentSharingManager.shareToWhatsApp(PartyMainActivity.this, currentGroup);
              break;
            case 2: // Share to Facebook
              ContentSharingManager.shareToFacebook(PartyMainActivity.this, currentGroup);
              break;
            case 3: // Share via SMS
              ContentSharingManager.shareViaSMS(PartyMainActivity.this, currentGroup);
              break;
            case 4: // Share via Email
              ContentSharingManager.shareViaEmail(PartyMainActivity.this, currentGroup);
              break;
          }
        });

    builder.show();

    // Subscribe to group notifications when sharing
    NotificationManager.subscribeToGroup(currentGroup.getGroupKey());
  }

  /** Initialize group encryption proactively */
  private void initializeGroupEncryption() {
    if (UserKey == null || GroupKey == null) {
      Log.w(TAG, "Cannot initialize encryption: missing UserKey or GroupKey");
      return;
    }
    
    try {
      // Initialize encryption managers proactively
      GroupKeyManager groupKeyManager = new GroupKeyManager(this, UserKey);
      
      // Check if user is already a member of this group's encryption
      groupKeyManager.isGroupMember(GroupKey).thenAccept(isMember -> {
        if (!isMember) {
          Log.i(TAG, "User not in group encryption, adding automatically");
          // Auto-add current user to group encryption
          groupKeyManager.addUserToGroupEncryption(GroupKey, UserKey).thenAccept(success -> {
            if (success) {
              Log.i(TAG, "Successfully added user to group encryption in main screen");
              // Now initialize for existing group
              groupKeyManager.initializeForExistingGroup(GroupKey);
            } else {
              Log.e(TAG, "Failed to add user to group encryption in main screen");
            }
          });
        } else {
          Log.i(TAG, "User already in group encryption, initializing in main screen");
          // Initialize encryption for existing group
          groupKeyManager.initializeForExistingGroup(GroupKey);
        }
      });
      
      Log.i(TAG, "Group encryption initialization started proactively for: " + GroupKey);
      
    } catch (Exception e) {
      Log.e(TAG, "Failed to initialize group encryption proactively", e);
    }
  }

  @Override
  public void onBackPressed() {
    // Call super first
    super.onBackPressed();
    Log.d(TAG, "Back button pressed, navigating to MainActivity");
    Intent intent =
        new Intent(PartyMainActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }
}
