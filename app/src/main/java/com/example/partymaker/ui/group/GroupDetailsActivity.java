package com.example.partymaker.ui.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupDetailsActivity extends AppCompatActivity {
  private static final String TAG = "GroupDetailsActivity";

  private String GroupKey;
  private String UserKey;
  private Map<String, String> MessageKeys;
  private ImageButton btnBack;
  private ImageButton btnEditName;
  private Button btnDelete;
  private Button btnEdit;
  private TextView btnChat;
  private TextView btnOptions;
  private TextView tvGroupDetails;
  private TextView tvGroupName;
  private TextView tvAdminValue;
  private TextView tvEntryPrice;
  private TextView tvLocation;
  private TextView tvDate;
  private CardView cardChat;
  private CardView cardAdminOptions;
  private CardView cardLocation;
  private CardView cardDate;
  private CardView cardPeopleInvited;
  private CardView cardPeopleComing;
  private CardView cardAddFriends;
  private CardView cardLeave;
  private Group currentGroup;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_details);

    // Initialize Firebase Server Client
    final FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Get the intent data
    Intent intent = getIntent();

    // Use ExtrasMetadata to get data from intent
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(intent);
    if (extras != null) {
      GroupKey = extras.getGroupKey();

      // Get UserKey from Firebase Auth instead of intent
      try {
        UserKey =
            Objects.requireNonNull(Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail())
                .replace('.', ' ');
        Log.d(TAG, "UserKey initialized: " + UserKey);
      } catch (Exception e) {
        Log.e(TAG, "Failed to get current user", e);
        UserKey = intent.getStringExtra("UserKey"); // Fallback to intent if Firebase auth fails
        Log.d(TAG, "Using fallback UserKey from intent: " + UserKey);
      }

    } else {
      Log.e(TAG, "Missing intent data");
      Toast.makeText(this, "Error: Missing group data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // Initialize views
    initializeViews();

    // Initialize message keys map
    MessageKeys = new HashMap<>();

    // Set click listeners
    setupClickListeners();

    // Check if user is admin to show/hide edit and delete buttons
    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<Group>() {
          @Override
          public void onSuccess(Group group) {
            currentGroup = group;
            if (group != null
                && group.getAdminKey() != null
                && group.getAdminKey().equals(UserKey)) {
              btnDelete.setVisibility(View.VISIBLE);
              btnEdit.setVisibility(View.VISIBLE);
              cardAdminOptions.setVisibility(View.VISIBLE);
              btnEditName.setVisibility(View.VISIBLE);
            } else {
              btnDelete.setVisibility(View.GONE);
              btnEdit.setVisibility(View.GONE);
              cardAdminOptions.setVisibility(View.GONE);
              btnEditName.setVisibility(View.GONE);
            }

            // Update UI with group details
            updateGroupUI(group);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to check if user is admin: " + errorMessage);
            btnDelete.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            cardAdminOptions.setVisibility(View.GONE);
            btnEditName.setVisibility(View.GONE);
          }
        });

    // Load group messages
    serverClient.getMessages(
        GroupKey,
        new FirebaseServerClient.DataCallback<List<ChatMessage>>() {
          @Override
          public void onSuccess(List<ChatMessage> messages) {
            for (ChatMessage message : messages) {
              if (message != null && message.getMessageKey() != null) {
                MessageKeys.put(message.getMessageKey(), message.getMessageText());
              }
            }
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to load messages: " + errorMessage);
          }
        });
  }

  private void initializeViews() {
    // Get references to all views
    btnBack = findViewById(R.id.btnBack);
    btnEditName = findViewById(R.id.btnEditName);
    btnDelete = findViewById(R.id.btnDelete);
    btnEdit = findViewById(R.id.btnEdit);
    btnChat = findViewById(R.id.btnChat);
    btnOptions = findViewById(R.id.btnOptions);
    tvGroupDetails = findViewById(R.id.tvGroupDetails);
    tvGroupName = findViewById(R.id.tvGroupName);
    tvAdminValue = findViewById(R.id.tvAdminValue);
    tvEntryPrice = findViewById(R.id.tvEntryPrice);
    tvLocation = findViewById(R.id.tvLocation);
    tvDate = findViewById(R.id.tvDate);

    // Get references to CardViews
    cardChat = findViewById(R.id.cardChat);
    cardAdminOptions = findViewById(R.id.cardAdminOptions);
    cardLocation = findViewById(R.id.cardLocation);
    cardDate = findViewById(R.id.cardDate);
    cardPeopleInvited = findViewById(R.id.cardPeopleInvited);
    cardPeopleComing = findViewById(R.id.cardPeopleComing);
    cardAddFriends = findViewById(R.id.cardAddFriends);
    cardLeave = findViewById(R.id.cardLeave);
  }

  private void setupClickListeners() {
    btnBack.setOnClickListener(v -> finish());

    btnEditName.setOnClickListener(
        v -> {
          Toast.makeText(
                  GroupDetailsActivity.this,
                  "Edit name functionality not implemented yet",
                  Toast.LENGTH_SHORT)
              .show();
        });

    btnDelete.setOnClickListener(
        v -> {
          deleteMessages();
          deleteGroup();
        });

    btnEdit.setOnClickListener(
        v -> {
          Toast.makeText(
                  GroupDetailsActivity.this,
                  "Edit functionality not implemented yet",
                  Toast.LENGTH_SHORT)
              .show();
        });

    cardChat.setOnClickListener(v -> navigateToChatActivity());

    cardAdminOptions.setOnClickListener(v -> navigateToAdminOptionsActivity());

    cardLocation.setOnClickListener(
        v -> {
          if (currentGroup != null && currentGroup.getGroupLocation() != null) {
            Toast.makeText(
                    GroupDetailsActivity.this,
                    "Location: " + currentGroup.getGroupLocation(),
                    Toast.LENGTH_SHORT)
                .show();
            // Here you could navigate to a map activity showing the location
          }
        });

    cardDate.setOnClickListener(
        v -> {
          if (currentGroup != null) {
            showDateTimeDialog();
          }
        });

    cardPeopleInvited.setOnClickListener(
        v -> {
          if (currentGroup != null && currentGroup.getFriendKeys() != null) {
            int count = currentGroup.getFriendKeys().size();
            Toast.makeText(GroupDetailsActivity.this, count + " people invited", Toast.LENGTH_SHORT)
                .show();
            // Here you could navigate to a list of invited people
          }
        });

    cardPeopleComing.setOnClickListener(
        v -> {
          if (currentGroup != null && currentGroup.getComingKeys() != null) {
            int count = currentGroup.getComingKeys().size();
            Toast.makeText(GroupDetailsActivity.this, count + " people coming", Toast.LENGTH_SHORT)
                .show();
            // Here you could navigate to a list of people coming
          }
        });

    cardAddFriends.setOnClickListener(
        v -> {
          Toast.makeText(
                  GroupDetailsActivity.this,
                  "Add friends functionality not implemented yet",
                  Toast.LENGTH_SHORT)
              .show();
          // Here you could navigate to an activity to add friends
        });

    cardLeave.setOnClickListener(
        v -> {
          Toast.makeText(
                  GroupDetailsActivity.this,
                  "Leave group functionality not implemented yet",
                  Toast.LENGTH_SHORT)
              .show();
          // Here you could implement the logic to leave the group
        });
  }

  private void showDateTimeDialog() {
    if (currentGroup == null) return;

    // Create dialog
    android.app.Dialog dialog = new android.app.Dialog(this);
    dialog.setContentView(R.layout.dialog_date_info);
    dialog
        .getWindow()
        .setBackgroundDrawable(
            new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
    dialog
        .getWindow()
        .setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

    // Get dialog views
    android.widget.TextView tvDialogDate = dialog.findViewById(R.id.tvDialogDate);
    android.widget.TextView tvDialogTime = dialog.findViewById(R.id.tvDialogTime);
    android.widget.ImageButton btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
    android.widget.Button btnOkDialog = dialog.findViewById(R.id.btnOkDialog);

    // Format and display date
    String formattedDate =
        currentGroup.getGroupDays()
            + " "
            + currentGroup.getGroupMonths()
            + " "
            + currentGroup.getGroupYears();
    tvDialogDate.setText(formattedDate);

    // Display time
    String time = currentGroup.getGroupHours();
    tvDialogTime.setText(time != null ? time : "Not set");

    // Set click listeners
    btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
    btnOkDialog.setOnClickListener(v -> dialog.dismiss());

    // Show dialog
    dialog.show();
  }

  @SuppressLint("SetTextI18n")
  private void updateGroupUI(Group group) {
    if (group == null) return;

    // Update group name
    tvGroupName.setText(group.getGroupName());

    // Update admin info
    tvAdminValue.setText(group.getAdminKey());

    // Update entry price
    tvEntryPrice.setText(group.getGroupPrice().equals("0") ? "free" : group.getGroupPrice());

    // Update location and date
    tvLocation.setText("Location");
    tvDate.setText("Date");

    // For backward compatibility, also update the hidden text view
    StringBuilder details = new StringBuilder();
    details.append("Admin: ").append(group.getAdminKey()).append("\n\n");
    details
        .append("Date: ")
        .append(group.getGroupDays())
        .append("/")
        .append(group.getGroupMonths())
        .append("/")
        .append(group.getGroupYears())
        .append("\n\n");
    details.append("Time: ").append(group.getGroupHours()).append("\n\n");
    details.append("Price: ").append(group.getGroupPrice()).append("\n\n");
    details.append("Location: ").append(group.getGroupLocation()).append("\n\n");
    details.append("Group Type: ").append(group.getGroupType());

    tvGroupDetails.setText(details.toString());

    Log.d(
        TAG,
        "Loaded group data from extras: " + group.getGroupName() + ", key: " + group.getGroupKey());
    Log.d(TAG, "Current user: " + UserKey + ", Admin key: " + group.getAdminKey());
  }

  private void navigateToChatActivity() {
    try {
      Intent intent = new Intent(this, ChatActivity.class);

      // Create ExtrasMetadata with all required fields
      ExtrasMetadata extras =
          new ExtrasMetadata(
              currentGroup != null ? currentGroup.getGroupName() : "", // groupName
              GroupKey, // groupKey
              currentGroup != null ? currentGroup.getGroupDays() : "", // groupDays
              currentGroup != null ? currentGroup.getGroupMonths() : "", // groupMonths
              currentGroup != null ? currentGroup.getGroupYears() : "", // groupYears
              currentGroup != null ? currentGroup.getGroupHours() : "", // groupHours
              currentGroup != null ? currentGroup.getGroupLocation() : "", // groupLocation
              currentGroup != null ? currentGroup.getAdminKey() : "", // adminKey
              currentGroup != null ? currentGroup.getCreatedAt() : "", // createdAt
              currentGroup != null ? currentGroup.getGroupPrice() : "", // groupPrice
              currentGroup != null ? currentGroup.getGroupType() : 0, // groupType
              currentGroup != null && currentGroup.isCanAdd(), // canAdd
              currentGroup != null && currentGroup.getFriendKeys() != null
                  ? new HashMap<>(currentGroup.getFriendKeys())
                  : new HashMap<>(), // friendKeys
              currentGroup != null && currentGroup.getComingKeys() != null
                  ? new HashMap<>(currentGroup.getComingKeys())
                  : new HashMap<>(), // comingKeys
              new HashMap<>(MessageKeys) // messageKeys
              );

      Common.addExtrasToIntent(intent, extras);
      intent.putExtra("UserKey", UserKey); // Also add UserKey directly to intent
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Failed to navigate to chat activity", e);
      Toast.makeText(this, "Failed to open chat", Toast.LENGTH_SHORT).show();
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

  private void deleteMessages() {
    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    final FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    serverClient.getMessages(
        GroupKey,
        new FirebaseServerClient.DataCallback<List<ChatMessage>>() {
          @Override
          public void onSuccess(List<ChatMessage> messages) {
            for (final ChatMessage message : messages) {
              if (message != null && message.getMessageKey() != null) {
                serverClient.deleteData(
                    "Messages/" + message.getMessageKey(),
                    new FirebaseServerClient.OperationCallback() {
                      @Override
                      public void onSuccess() {
                        Log.d(TAG, "Message deleted: " + message.getMessageKey());
                      }

                      @Override
                      public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to delete message: " + errorMessage);
                      }
                    });
              }
            }
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to get messages for deletion: " + errorMessage);
          }
        });
  }

  private void deleteGroup() {
    final FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    serverClient.deleteData(
        "Groups/" + GroupKey,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group deleted: " + GroupKey);

            serverClient.deleteData(
                "UserGroups/" + UserKey + "/" + GroupKey,
                new FirebaseServerClient.OperationCallback() {
                  @Override
                  public void onSuccess() {
                    Log.d(TAG, "UserGroup reference deleted");
                    finish();
                  }

                  @Override
                  public void onError(String errorMessage) {
                    Log.e(TAG, "Failed to delete UserGroup reference: " + errorMessage);
                    finish();
                  }
                });
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to delete group: " + errorMessage);
            Toast.makeText(GroupDetailsActivity.this, "Failed to delete group", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
