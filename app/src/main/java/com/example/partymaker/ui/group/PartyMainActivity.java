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

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_main);

    Log.d(TAG, "PartyMainActivity onCreate started");

    // Remove automatic navigation - let user click to navigate
    // Auto-navigate to ChatActivity after 5 seconds
    // new Handler().postDelayed(() -> {
    //     Log.d(TAG, "Auto-navigating to ChatActivity after delay");
    //     navigateToChatActivity();
    // }, 5000);

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

    // Remove automatic navigation - let user click to navigate
    // Add a test button for ChatActivity - add to the root FrameLayout
    // Button testChatButton = new Button(this);
    // testChatButton.setText("TEST CHAT");
    // testChatButton.setBackgroundDrawable(Color.RED);
    // testChatButton.setTextColor(Color.WHITE);
    // testChatButton.setPadding(40, 40, 40, 40);
    // testChatButton.setTextSize(18);

    // Add the button to the root FrameLayout
    // FrameLayout rootLayout = (FrameLayout) findViewById(android.R.id.content);
    // FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
    //         FrameLayout.LayoutParams.WRAP_CONTENT,
    //         FrameLayout.LayoutParams.WRAP_CONTENT
    // );
    // params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
    // params.setMargins(0, 0, 0, 100);
    // rootLayout.addView(testChatButton, params);

    // testChatButton.setOnClickListener(v -> {
    //     Log.d(TAG, "Test chat button clicked");
    //     navigateToChatActivity();
    // });

    // Remove automatic navigation - let user click to navigate
    // Automatically navigate to ChatActivity after 3 seconds
    // new Handler().postDelayed(() -> {
    //     Log.d(TAG, "Auto-navigating to ChatActivity after delay");
    //     navigateToChatActivity();
    // }, 3000);

    // Check if we have the required data
    if (GroupKey == null || GroupKey.isEmpty()) {
      Log.e(TAG, "Missing GroupKey in intent");
      Toast.makeText(this, "Missing group data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // Load group data
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<Group>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null) {
              Log.e(TAG, "Group data is null");
              Toast.makeText(
                      PartyMainActivity.this, "Failed to load group data", Toast.LENGTH_SHORT)
                  .show();
              finish();
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
            updateGroupUI(group);

            // Setup click listeners
            setupClickListeners();
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to get group details: " + errorMessage);
            Toast.makeText(
                    PartyMainActivity.this, "Failed to load group details", Toast.LENGTH_SHORT)
                .show();
            finish();
          }
        });

    // Load messages
    serverClient.getMessages(
        GroupKey,
        new FirebaseServerClient.DataCallback<List<ChatMessage>>() {
          @Override
          public void onSuccess(
              java.util.List<com.example.partymaker.data.model.ChatMessage> messages) {
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
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to load messages: " + errorMessage);
          }
        });
  }

  private void initializeViews() {
    // Get references to all views
    back5 = findViewById(R.id.back5);
    tvGroupName = findViewById(R.id.tvGroupName);
    tvCreatedBy = findViewById(R.id.tvCreatedBy);
    tvEntryPrice = findViewById(R.id.tvEntryPrice);
    tvGroupLocation = findViewById(R.id.tvGroupLocation);
    tvDateDays = findViewById(R.id.tvDateDays);
    tvDateMonths = findViewById(R.id.tvDateMonths);
    tvDateYears = findViewById(R.id.tvDateYears);
    tvDateHours = findViewById(R.id.tvDateHours);

    // Get references to CardViews
    Card1 = findViewById(R.id.Card1);
    Card2 = findViewById(R.id.Card2);
    Card3 = findViewById(R.id.Card3);
    Card4 = findViewById(R.id.Card4);
    Card5 = findViewById(R.id.Card5);
    Card6 = findViewById(R.id.Card6);
    Card7 = findViewById(R.id.Card7);
    Card8 = findViewById(R.id.Card8);
  }

  private void setupClickListeners() {
    Log.d(TAG, "Setting up click listeners");

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

    // Add a test button for direct navigation to ChatActivity
    Button testButton = new Button(this);
    testButton.setText("TEST CHAT");
    testButton.setBackgroundColor(Color.RED);
    testButton.setTextColor(Color.WHITE);

    // Add the button to the layout
    ViewGroup rootView = findViewById(android.R.id.content);
    ViewGroup.LayoutParams params =
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    rootView.addView(testButton, params);

    testButton.setOnClickListener(
        v -> {
          Log.d(TAG, "Test button clicked - navigating to ChatActivity");
          navigateToChatActivity();
        });

    // Remove automatic navigation - let user click to navigate
    // Auto-navigate to ChatActivity after 5 seconds
    // new Handler().postDelayed(() -> {
    //     Log.d(TAG, "Auto-navigating to ChatActivity after delay");
    //     navigateToChatActivity();
    // }, 5000);

    back5.setOnClickListener(v -> finish());

    Card1.setOnClickListener(
        v -> {
          if (currentGroup != null && currentGroup.getGroupLocation() != null) {
            String location = currentGroup.getGroupLocation();
            Log.d(TAG, "Opening location in map dialog: " + location);

            try {
              // Check if the location is in format like "31.7768,35.2224"
              if (location.contains(",")) {
                showMapDialog(location);
              } else {
                // If location is not coordinates, show a toast and open Google Maps
                Toast.makeText(
                        PartyMainActivity.this,
                        "Opening location in Google Maps",
                        Toast.LENGTH_SHORT)
                    .show();

                // Open in Google Maps
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Check if Google Maps is installed
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                  startActivity(mapIntent);
                } else {
                  // If Google Maps is not installed, open in browser
                  Uri browserUri =
                      Uri.parse(
                          "https://www.google.com/maps/search/?api=1&query="
                              + Uri.encode(location));
                  Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                  startActivity(browserIntent);
                }
              }
            } catch (Exception e) {
              Log.e(TAG, "Error opening maps", e);
              Toast.makeText(
                      PartyMainActivity.this,
                      "Could not open maps: " + e.getMessage(),
                      Toast.LENGTH_SHORT)
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

    Card5.setOnClickListener(v -> navigateToAdminOptionsActivity());

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
      tvGroupLocation.setText(location);
      tvGroupLocation.setVisibility(View.VISIBLE);

      // Add a map icon to indicate it's clickable
      if (location.contains(",")) {
        // It's likely coordinates
        tvGroupLocation.setCompoundDrawablesWithIntrinsicBounds(
            0, 0, R.drawable.ic_party_location, 0);
      }
    } else {
      tvGroupLocation.setText("No location set");
      tvGroupLocation.setVisibility(View.VISIBLE);
    }

    // Update date
    tvDateDays.setText(group.getGroupDays());
    tvDateMonths.setText(group.getGroupMonths());
    tvDateYears.setText(group.getGroupYears());
    tvDateHours.setText(group.getGroupHours());
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
