package com.example.partymaker.ui.group;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utilities.AuthHelper;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyMainActivity extends AppCompatActivity {
  private static final String TAG = "PartyMainActivity";

  private String GroupKey;
  private String UserKey;
  private Map<String, String> MessageKeys;
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

  // Add UI elements for coming/not coming toggle
  private ImageView imgThumbUp, imgThumbDown, imgOptions;
  private TextView tvComing, tvNotComing, tvOptions;
  private boolean isUserAdmin = false;
  private boolean isUserComing = false;

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
        ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(intent);
        if (extras != null) {
          GroupKey = extras.getGroupKey();
          Log.d(TAG, "GroupKey from ExtrasMetadata: " + GroupKey);
        } else {
          Log.d(TAG, "ExtrasMetadata is null");
        }
      }

      // Try to get UserKey from AuthHelper first
      try {
        UserKey = AuthHelper.getCurrentUserKey(this);
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
  
  /**
   * Loads group data from the server with proper error handling
   */
  private void loadGroupData() {
    try {
      // Show loading indicator
      showLoading(true);
      
      FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
      serverClient.getGroup(
          GroupKey,
          new FirebaseServerClient.DataCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
              try {
                if (group == null) {
                  Log.e(TAG, "Group data is null");
                  showErrorAndFinish("Failed to load group data");
                  return;
                }

                currentGroup = group;
                Log.d(
                    TAG,
                    "Loaded group data from server: " + group.getGroupName() + ", key: " + GroupKey);
                Log.d(TAG, "Current user: " + UserKey + ", Admin key: " + group.getAdminKey());

                // Debug: Log ComingKeys data from server
                if (group.getComingKeys() != null) {
                  Log.d(TAG, "ComingKeys loaded from server - size: " + group.getComingKeys().size());
                  Log.d(TAG, "ComingKeys details from server:");
                  for (String key : group.getComingKeys().keySet()) {
                    Log.d(
                        TAG,
                        "  ComingKey from server: '" + key + "' -> " + group.getComingKeys().get(key));
                  }
                } else {
                  Log.e(TAG, "ComingKeys is null in group data from server!");
                }

                // Update UI with group data
                runOnUiThread(() -> {
                  try {
                    updateGroupUI(group);
                    setupClickListeners();
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
          });

      // Load messages with error handling
      loadMessages();
      
    } catch (Exception e) {
      Log.e(TAG, "Error loading group data", e);
      showError("Error loading group data");
    }
  }
  
  /**
   * Loads messages for the current group with proper error handling
   */
  private void loadMessages() {
    try {
      FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
      serverClient.getMessages(
          GroupKey,
          new FirebaseServerClient.DataCallback<List<ChatMessage>>() {
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
    runOnUiThread(() -> {
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
    runOnUiThread(() -> {
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

    // Initialize MessageKeys
    MessageKeys = new HashMap<>();

    Log.d(TAG, "Views initialized successfully");
  }

  private void setupClickListeners() {
    Log.d(TAG, "Setting up click listeners");

    // Back button
    back5.setOnClickListener(v -> finish());

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
                    "Party Date: " + date + " at " + time + "\n\nOnly admin can change the date.",
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
          Log.d(TAG, "Card5 clicked - isUserAdmin: " + isUserAdmin + ", isUserComing: " + isUserComing);
          
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
    tvEntryPrice.setText(group.getGroupPrice().equals("0") ? "free" : group.getGroupPrice());

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

    // Update Card5 UI based on admin status
    updateCard5UI();
  }

  private void updateCard5UI() {
    // Log the admin status for debugging
    Log.d(TAG, "updateCard5UI - isUserAdmin: " + isUserAdmin + ", UserKey: " + UserKey + 
          ", Admin key: " + (currentGroup != null ? currentGroup.getAdminKey() : "null"));
    
    // Double check admin status to ensure it's correct
    if (currentGroup != null && currentGroup.getAdminKey() != null) {
      isUserAdmin = currentGroup.getAdminKey().equals(UserKey);
      Log.d(TAG, "Re-checked admin status: " + isUserAdmin);
    }
    
    if (isUserAdmin) {
      // Show admin options
      imgOptions.setVisibility(View.VISIBLE);
      tvOptions.setVisibility(View.VISIBLE);
      imgThumbUp.setVisibility(View.INVISIBLE);
      imgThumbDown.setVisibility(View.INVISIBLE);
      tvComing.setVisibility(View.INVISIBLE);
      tvNotComing.setVisibility(View.INVISIBLE);
      Log.d(TAG, "Showing admin options UI");
    } else {
      // Show coming/not coming toggle
      imgOptions.setVisibility(View.INVISIBLE);
      tvOptions.setVisibility(View.INVISIBLE);

      if (isUserComing) {
        // User is coming - show thumb up
        imgThumbUp.setVisibility(View.VISIBLE);
        tvComing.setVisibility(View.VISIBLE);
        imgThumbDown.setVisibility(View.INVISIBLE);
        tvNotComing.setVisibility(View.INVISIBLE);
        Log.d(TAG, "Showing 'coming' UI");
      } else {
        // User is not coming - show thumb down
        imgThumbUp.setVisibility(View.INVISIBLE);
        tvComing.setVisibility(View.INVISIBLE);
        imgThumbDown.setVisibility(View.VISIBLE);
        tvNotComing.setVisibility(View.VISIBLE);
        Log.d(TAG, "Showing 'not coming' UI");
      }
    }
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
      
      Toast.makeText(this, "מסומן כמגיע", Toast.LENGTH_SHORT).show();
    } else {
      // Remove user from ComingKeys
      updatedComingKeys.remove(UserKey);
      currentGroup.setComingKeys(updatedComingKeys);
      
      Toast.makeText(this, "מסומן כלא מגיע", Toast.LENGTH_SHORT).show();
    }
    
    // Update server with new ComingKeys
    Map<String, Object> updates = new HashMap<>();
    updates.put("ComingKeys", updatedComingKeys);
    
    serverClient.updateGroup(GroupKey, updates, new FirebaseServerClient.OperationCallback() {
      @Override
      public void onSuccess() {
        Log.d(TAG, "Coming status updated successfully on server");
      }
      
      @Override
      public void onError(String errorMessage) {
        Log.e(TAG, "Failed to update coming status on server: " + errorMessage);
        // Revert the UI change on error
        isUserComing = !isUserComing;
        updateCard5UI();
        Toast.makeText(PartyMainActivity.this, "שגיאה בעדכון הסטטוס", Toast.LENGTH_SHORT).show();
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
      ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
      if (extras != null) {
        Common.addExtrasToIntent(intent, extras);
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
                (HashMap<String, Object>) currentGroup.getFriendKeys(),
                (HashMap<String, Object>) currentGroup.getComingKeys(),
                (HashMap<String, Object>) currentGroup.getMessageKeys());
        Common.addExtrasToIntent(intent, extras);
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

        Common.addExtrasToIntent(intent, extras);
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

        Common.addExtrasToIntent(intent, extras);
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
        new FirebaseServerClient.DataCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            // Update coming keys
            serverClient.updateGroup(
                GroupKey,
                "ComingKeys",
                updatedComingKeys,
                new FirebaseServerClient.DataCallback<Void>() {
                  @Override
                  public void onSuccess(Void result) {
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
      dialog.setContentView(R.layout.dialog_map_view);

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
}
