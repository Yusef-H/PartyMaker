package com.example.partymaker.ui.features.groups.management;

import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.ui.features.groups.main.PartyMainActivity;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.media.ImageCompressor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.HashMap;

public class AdminSettingsActivity extends AppCompatActivity {
  
  // UI constants
  private static final String TAG = "AdminSettingsActivity";
  private static final int REQUEST_CODE_IMAGE_PICK = 100;
  private static final String PUBLIC_GROUP_TEXT = "Public Group";
  private static final String PRIVATE_GROUP_TEXT = "Private Group";
  private static final String EVERYONE_ADD_TEXT = "Everyone Add";
  private static final String ONLY_ADMIN_TEXT = "Only Admin";
  private static final String ADMIN_ADD_TEXT = "Admin Add";
  private static final String COMPRESSION_MESSAGE = "Compressing group image...";
  private static final String UPLOAD_MESSAGE = "Image compressed successfully. Uploading...";
  private static final String COMPRESSION_FAILED_MESSAGE = "Compression failed, uploading original image...";
  private static final String UPDATE_SUCCESS_MESSAGE = "Group image updated successfully";
  private static final String NAME_CHANGED_MESSAGE = "Name Changed";
  private static final String DELETE_SUCCESS_MESSAGE = "successfully deleted";
  
  // Group settings
  private boolean canAdd;
  
  // UI Components
  private ImageView addPermissionIcon, typeIcon, groupImageView;
  private ImageButton editNameButton;
  private TextView groupNameText, addPermissionText, typeText;
  
  // Group data
  private String adminKey, groupKey, groupName, groupDay, groupMonth, groupYear;
  private String groupHour, groupLocation, createdAt, groupPrice;
  private int groupType;
  private HashMap<String, Object> friendKeys, comingKeys, messageKeys;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_settings);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // Get Values from GroupScreen By intent
    if (!extractGroupDataFromIntent()) {
      return;
    }

    // Initialize UI components
    initializeViews();
    GridLayout groupGrid = findViewById(R.id.GroupGrid);
    ImageButton btnBack = findViewById(R.id.btnBack);

    // Set up back button click listener
    if (btnBack != null) {
      btnBack.setOnClickListener(v -> navigateBackToPartyMain());
    }

    // Load group image and set initial UI state
    loadGroupImage();
    setupInitialUIState();

    // Setup group type and permission UI
    updateGroupTypeUI();
    updateAddPermissionUI();

    // Setup event handlers and options
    setupGroupOptions(groupGrid);
    setupEventHandlers();
  }

  private void navigateBackToPartyMain() {
    Intent intent = new Intent(this, PartyMainActivity.class);
    ExtrasMetadata extras = createExtrasMetadata();
    IntentExtrasManager.addExtrasToIntent(intent, extras);
    startActivity(intent);
    finish(); // Close this activity to prevent it from staying in the back stack
  }

  @Override
  public void onBackPressed() {
    // Call super first
    super.onBackPressed();
    // Handle back button press to ensure proper navigation
    navigateBackToPartyMain();
  }

  private boolean extractGroupDataFromIntent() {
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return false;
    }
    
    groupName = extras.getGroupName();
    groupKey = extras.getGroupKey();
    groupDay = extras.getGroupDays();
    groupMonth = extras.getGroupMonths();
    groupYear = extras.getGroupYears();
    groupHour = extras.getGroupHours();
    groupLocation = extras.getGroupLocation();
    adminKey = extras.getAdminKey();
    createdAt = extras.getCreatedAt();
    groupPrice = extras.getGroupPrice();
    groupType = extras.getGroupType();
    canAdd = extras.isCanAdd();
    friendKeys = extras.getFriendKeys();
    comingKeys = extras.getComingKeys();
    messageKeys = extras.getMessageKeys();
    
    return true;
  }
  
  private void initializeViews() {
    addPermissionText = findViewById(R.id.tvCanAdd);
    typeText = findViewById(R.id.tvType);
    groupNameText = findViewById(R.id.tvNameGroup);
    addPermissionIcon = findViewById(R.id.imgCanAdd);
    typeIcon = findViewById(R.id.imgType);
    groupImageView = findViewById(R.id.imgEditGroup);
    editNameButton = findViewById(R.id.btnEditName1);
  }
  
  private void loadGroupImage() {
    DBRef.refStorage
        .child("UsersImageProfile/Groups/" + groupKey)
        .getDownloadUrl()
        .addOnSuccessListener(uri -> Picasso.get().load(uri).into(groupImageView))
        .addOnFailureListener(
            exception -> {
              // If image not found in new path, try the old path
              DBRef.refStorage
                  .child("Groups/" + groupKey)
                  .getDownloadUrl()
                  .addOnSuccessListener(uri -> Picasso.get().load(uri).into(groupImageView))
                  .addOnFailureListener(
                      e -> {
                        // Set default image if both paths fail
                        groupImageView.setImageResource(R.drawable.default_group_image);
                      });
            });
  }
  
  private void setupInitialUIState() {
    groupNameText.setText(groupName);
  }
  
  private void updateGroupTypeUI() {
    if (groupType == 0) { // if group is public
      typeIcon.setImageResource(R.drawable.ic_party_everyone);
      typeText.setText(PUBLIC_GROUP_TEXT);
    } else { // if group is private
      typeIcon.setImageResource(R.drawable.ic_party_lock);
      typeText.setText(PRIVATE_GROUP_TEXT);
    }
  }
  
  private void updateAddPermissionUI() {
    if (canAdd) {
      addPermissionIcon.setImageResource(R.drawable.ic_party_everyone);
      addPermissionText.setText(EVERYONE_ADD_TEXT);
    } else {
      addPermissionIcon.setImageResource(R.drawable.ic_party_private);
      addPermissionText.setText(ADMIN_ADD_TEXT);
    }
  }
  
  private ExtrasMetadata createExtrasMetadata() {
    return new ExtrasMetadata(
        groupName, groupKey, groupDay, groupMonth, groupYear, groupHour,
        groupLocation, adminKey, createdAt, groupPrice, groupType, canAdd,
        friendKeys, comingKeys, messageKeys);
  }
  
  private void setupEventHandlers() {
    groupImageView.setOnClickListener(v -> selectImageFromGallery());
    editNameButton.setOnClickListener(v -> showEditNameDialog());
  }
  
  private void selectImageFromGallery() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_IMAGE_PICK);
  }
  
  private void showEditNameDialog() {
    final EditText editText = new EditText(AdminSettingsActivity.this);
    AlertDialog.Builder alert = new AlertDialog.Builder(AdminSettingsActivity.this);
    alert.setMessage("Input new name below");
    alert.setTitle("Change party's name");
    alert.setView(editText);

    alert.setPositiveButton(
        "Change name",
        (dialog, whichButton) -> {
          groupName = editText.getText().toString();
          DBRef.refGroups.child(groupKey).child(GROUP_NAME).setValue(groupName);
          groupNameText.setText(groupName);
          Toast.makeText(AdminSettingsActivity.this, NAME_CHANGED_MESSAGE, Toast.LENGTH_SHORT)
              .show();
        });

    alert.setNegativeButton("Back", (dialog, whichButton) -> {});
    alert.show();
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_CODE_IMAGE_PICK) {
        Uri uri = data.getData();
        if (null != uri) {
          // Show compression progress
          Toast.makeText(this, COMPRESSION_MESSAGE, Toast.LENGTH_SHORT).show();

          // First compress the image to reduce size and improve upload speed
          ImageCompressor.compressImage(
              this,
              uri,
              new ImageCompressor.CompressCallback() {
                @Override
                public void onCompressSuccess(@NonNull File compressedFile) {
                  ThreadUtils.runOnMainThread(
                      () -> {
                        Uri compressedUri = Uri.fromFile(compressedFile);
                        groupImageView.setImageURI(compressedUri);

                        Toast.makeText(AdminSettingsActivity.this, UPLOAD_MESSAGE, Toast.LENGTH_SHORT)
                            .show();

                        // Upload the compressed image
                        uploadGroupImage(compressedUri);
                      });
                }

                @Override
                public void onCompressError(String error) {
                  ThreadUtils.runOnMainThread(
                      () -> {
                        Toast.makeText(AdminSettingsActivity.this, COMPRESSION_FAILED_MESSAGE, Toast.LENGTH_SHORT)
                            .show();
                        groupImageView.setImageURI(uri);

                        // Upload original image if compression fails
                        uploadGroupImage(uri);
                      });
                }
              });
        }
      }
    }
  }

  /**
   * Uploads the group image to Firebase Storage
   *
   * @param uri The URI of the image to upload
   */
  private void uploadGroupImage(Uri uri) {
    DBRef.refStorage
        .child("UsersImageProfile/Groups/" + groupKey)
        .putFile(uri)
        .addOnSuccessListener(
            taskSnapshot ->
                Toast.makeText(AdminSettingsActivity.this, UPDATE_SUCCESS_MESSAGE, Toast.LENGTH_SHORT)
                    .show())
        .addOnFailureListener(
            exception ->
                Toast.makeText(
                        AdminSettingsActivity.this,
                        "Error uploading group image: " + exception.getMessage(),
                        Toast.LENGTH_LONG)
                    .show());
  }

  @SuppressLint("SetTextI18n")
  private void setupGroupOptions(GridLayout groupGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < groupGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) groupGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            if (finalI == 0) // Change GroupType to Public/Private
            {
              toggleGroupType();
            } else if (finalI == 1) // set if anyone can add/only admin can add
            {
              toggleAddPermission();

            } else if (finalI == 2) // Delete Group
            {
              // Show confirmation dialog before deleting
              new AlertDialog.Builder(AdminSettingsActivity.this)
                  .setTitle("Delete Group")
                  .setMessage(
                      "Are you sure you want to delete this group? This action cannot be undone.")
                  .setPositiveButton(
                      "Delete",
                      (dialog, which) -> {
                        deleteGroup();

                        // intent from GroupScreen to MainMenu
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(intent);
                      })
                  .setNegativeButton("Cancel", null)
                  .show();
            } else if (finalI == 3) // Back
            {
              // Navigate back to PartyMainActivity
              navigateBackToPartyMain();
            }
          });
    }
  }

  private void toggleGroupType() {
    if (groupType == 0) {
      typeIcon.setImageResource(R.drawable.ic_party_lock);
      typeText.setText(PRIVATE_GROUP_TEXT);
      groupType = 1;
      DBRef.refGroups.child(groupKey).child("groupType").setValue(1);
    } else {
      typeIcon.setImageResource(R.drawable.ic_party_everyone);
      typeText.setText(PUBLIC_GROUP_TEXT);
      groupType = 0;
      DBRef.refGroups.child(groupKey).child("groupType").setValue(0);
    }
  }
  
  private void toggleAddPermission() {
    if (canAdd) {
      addPermissionIcon.setImageResource(R.drawable.ic_party_private);
      addPermissionText.setText(ONLY_ADMIN_TEXT);
      canAdd = false;
      DBRef.refGroups.child(groupKey).child("canAdd").setValue(false);
    } else {
      addPermissionIcon.setImageResource(R.drawable.ic_party_everyone);
      addPermissionText.setText(EVERYONE_ADD_TEXT);
      canAdd = true;
      DBRef.refGroups.child(groupKey).child("canAdd").setValue(true);
    }
  }
  
  private void deleteGroup() {
    // delete all messages written by current group
    deleteGroupMessages();

    // delete group from database
    DBRef.refGroups.child(groupKey).removeValue();

    // delete group's picture
    DBRef.refStorage.child("Groups/" + groupKey).delete();

    // Show success message
    Toast.makeText(AdminSettingsActivity.this, DELETE_SUCCESS_MESSAGE, Toast.LENGTH_SHORT)
        .show();

    // Navigate to main activity
    Intent intent = new Intent(getBaseContext(), MainActivity.class);
    startActivity(intent);
  }
  
  private void deleteGroupMessages() {
    DBRef.refMessages.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              ChatMessage message = data.getValue(ChatMessage.class);
              if (message != null && message.getGroupId() != null) {
                if (message.getGroupId().equals(groupKey)) {
                  data.getRef().removeValue();
                }
              }
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }
}
