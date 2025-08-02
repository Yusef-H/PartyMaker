package com.example.partymaker.ui.group;

import static com.example.partymaker.utils.core.IntentExtrasManager.hideViews;
import static com.example.partymaker.utils.core.IntentExtrasManager.showViews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.sharing.ShareHelper;
import com.example.partymaker.utils.ui.MapUtilities;
import java.util.HashMap;
import java.util.Objects;

public class JoinGroupActivity extends AppCompatActivity {
  private static final String TAG = "JoinGroupActivity";
  private ImageView imgCalender;
  private ImageView imgSeeHours;
  private TextView tvDateText;
  private TextView tvDateDays;
  private TextView tvDateMonths;
  private TextView tvDateYears;
  private TextView tvDateHours;
  private TextView tvSeeHours;
  private TextView tvSeeDate;
  private TextView tvAt;
  private String GroupKey;
  private String GroupLocation;
  private String GroupDay;
  private String GroupMonth;
  private String GroupYear;
  private String GroupHour;
  private String CurrentUser;
  private int IsClicked = 1;
  private HashMap<String, Object> FriendKeys;
  private String groupName;
  private String groupPrice;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_join);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();

    // Get Values from PublicGroups By intent + connection between intent and
    // current activity objects
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    groupName = extras.getGroupName();
    GroupKey = extras.getGroupKey();
    GroupDay = extras.getGroupDays();
    GroupMonth = extras.getGroupMonths();
    GroupYear = extras.getGroupYears();
    GroupHour = extras.getGroupHours();
    GroupLocation = extras.getGroupLocation();
    String adminKey = extras.getAdminKey();
    String createdAt = extras.getCreatedAt();
    groupPrice = extras.getGroupPrice();
    FriendKeys = extras.getFriendKeys();

    // Get UserKey from AuthHelper instead of Firebase Auth
    String userKey;
    try {
      userKey = AuthenticationManager.getCurrentUserKey(this);
      Log.d(TAG, "UserKey from AuthHelper: " + userKey);
    } catch (Exception e) {
      Log.e(TAG, "Failed to get current user from AuthHelper", e);
      Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    // connection
    GridLayout joinGrid = findViewById(R.id.joinGrid);
    imgCalender = findViewById(R.id.imgGroupDate);
    imgSeeHours = findViewById(R.id.imgSeeHours1);
    tvDateText = findViewById(R.id.tvGroupDateText);
    tvDateDays = findViewById(R.id.tvGroupDateDays);
    tvDateMonths = findViewById(R.id.tvGroupDateMonths);
    tvDateYears = findViewById(R.id.tvGroupDateYears);
    tvDateHours = findViewById(R.id.tvGroupDateHours);
    TextView tvGroupName = findViewById(R.id.tvGroupName1);
    TextView tvCreatedBy = findViewById(R.id.tvCreatedBy1);
    tvSeeHours = findViewById(R.id.tvSeeHours1);
    tvSeeDate = findViewById(R.id.tvSeeDate1);
    tvAt = findViewById(R.id.tvGroupAt);
    TextView tvEntryPrice = findViewById(R.id.tvEntryPrice1);
    TextView tvYourEntry = findViewById(R.id.tvYourEntry1);

    // get current account's email - use the UserKey that was already obtained from AuthHelper
    CurrentUser = userKey;

    // setting of GroupScreen for all users(not in buttons)
    tvGroupName.setText(groupName);
    tvCreatedBy.setText(adminKey.replace(' ', '.') + " ,  " + createdAt);

    // if GroupPrice=0 so it write that the party is free to enter if its not so it
    // writes the entry price
    if (Objects.requireNonNull(groupPrice).equals("0")) {
      tvEntryPrice.setVisibility(View.INVISIBLE);
      tvYourEntry.setText("Free Party");
    } else {
      tvEntryPrice.setText(groupPrice);
    } // convert int to string

    JoinEvent(joinGrid);
  }

  private void JoinEvent(GridLayout mainGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < mainGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) mainGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            if (finalI == 0) // open 1,1 (1) Location
            {
              MapUtilities.showGroupLocationOnGoogleMaps(GroupLocation, JoinGroupActivity.this);
            } else if (finalI == 1) // open 1,2 (2) Date
            {
              IsClicked = isClicked(IsClicked);
            } else if (finalI == 2) // open 2,1 (3) Join
            {
              // Add user to both FriendKeys and ComingKeys
              FriendKeys.put(CurrentUser, "true");

              // Use server mode to update the group data
              FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

              // Update FriendKeys first
              serverClient.updateGroup(
                  GroupKey,
                  "FriendKeys",
                  FriendKeys,
                  new FirebaseServerClient.DataCallback<>() {
                    @Override
                    public void onSuccess(Void result) {
                      Log.d(TAG, "Successfully updated FriendKeys");

                      // Now get the current group data to properly update ComingKeys
                      serverClient.getGroup(
                          GroupKey,
                          new FirebaseServerClient.DataCallback<>() {
                            @Override
                            public void onSuccess(Group group) {
                              // Get existing ComingKeys or create new HashMap
                              HashMap<String, Object> comingKeys =
                                  group.getComingKeys() != null
                                      ? new HashMap<>(group.getComingKeys())
                                      : new HashMap<>();

                              // Add the current user to ComingKeys
                              comingKeys.put(CurrentUser, "true");
                              Log.d(TAG, "Adding user to ComingKeys: " + CurrentUser);
                              Log.d(TAG, "ComingKeys now has " + comingKeys.size() + " users");

                              // Update ComingKeys
                              serverClient.updateGroup(
                                  GroupKey,
                                  "ComingKeys",
                                  comingKeys,
                                  new FirebaseServerClient.DataCallback<>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                      Log.d(TAG, "Successfully updated ComingKeys");
                                      Toast.makeText(
                                              JoinGroupActivity.this,
                                              "Successfully joined and marked as coming!",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                      Intent i1 =
                                          new Intent(getBaseContext(), PublicGroupsActivity.class);
                                      startActivity(i1);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                      Log.e(TAG, "Failed to update ComingKeys: " + errorMessage);
                                      Toast.makeText(
                                              JoinGroupActivity.this,
                                              "Joined but failed to mark as coming",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                      Intent i1 =
                                          new Intent(getBaseContext(), PublicGroupsActivity.class);
                                      startActivity(i1);
                                    }
                                  });
                            }

                            @Override
                            public void onError(String errorMessage) {
                              Log.e(
                                  TAG,
                                  "Failed to get group for ComingKeys update: " + errorMessage);
                              // Still try to add user to ComingKeys with a new HashMap
                              HashMap<String, Object> comingKeys = new HashMap<>();
                              comingKeys.put(CurrentUser, "true");

                              serverClient.updateGroup(
                                  GroupKey,
                                  "ComingKeys",
                                  comingKeys,
                                  new FirebaseServerClient.DataCallback<>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                      Log.d(TAG, "Successfully updated ComingKeys (fallback)");
                                      Toast.makeText(
                                              JoinGroupActivity.this,
                                              "Successfully joined and marked as coming!",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                      Intent i1 =
                                          new Intent(getBaseContext(), PublicGroupsActivity.class);
                                      startActivity(i1);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                      Log.e(
                                          TAG,
                                          "Failed to update ComingKeys (fallback): "
                                              + errorMessage);
                                      Toast.makeText(
                                              JoinGroupActivity.this,
                                              "Joined but failed to mark as coming",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                      Intent i1 =
                                          new Intent(getBaseContext(), PublicGroupsActivity.class);
                                      startActivity(i1);
                                    }
                                  });
                            }
                          });
                    }

                    @Override
                    public void onError(String errorMessage) {
                      Log.e(TAG, "Failed to update FriendKeys: " + errorMessage);
                      Toast.makeText(
                              JoinGroupActivity.this, "Failed to join group", Toast.LENGTH_SHORT)
                          .show();
                    }
                  });

            } else if (finalI == 3) // open 2,2 (4) Back
            {
              Intent i1 = new Intent(getBaseContext(), PublicGroupsActivity.class);
              startActivity(i1);
            } else if (finalI == 4) // Share this group
            {
              // Create a Group object from the current data
              Group groupToShare = new Group();
              groupToShare.setGroupKey(GroupKey);
              groupToShare.setGroupName(groupName);
              groupToShare.setGroupLocation(GroupLocation);
              groupToShare.setGroupDays(GroupDay);
              groupToShare.setGroupMonths(GroupMonth);
              groupToShare.setGroupYears(GroupYear);
              groupToShare.setGroupHours(GroupHour);
              groupToShare.setGroupPrice(groupPrice);

              // Show share options dialog
              showShareDialog(groupToShare);
            }
          });
    }
  }

  private int isClicked(int isClicked) {
    if (isClicked == 1) {
      // changes Month name to 3 letters
      String Month = GroupMonth.substring(0, 3);
      // set text
      tvDateDays.setText(GroupDay);
      tvDateMonths.setText(Month);
      tvDateYears.setText(GroupYear);
      tvDateHours.setText(GroupHour);
      // Design
      showViews(tvDateDays, tvDateMonths, tvDateYears, tvSeeHours, imgSeeHours);
      hideViews(imgCalender, tvDateText);
      return 2;
    }
    // if clicked again
    if (isClicked == 2) {
      showViews(tvSeeDate, tvAt, tvDateHours);
      hideViews(tvDateDays, tvDateMonths, tvDateYears, tvSeeHours);
      return 3;
    }
    // if click third time
    if (isClicked == 3) {
      showViews(tvDateDays, tvDateMonths, tvDateYears, tvSeeHours);
      hideViews(tvSeeDate, tvAt, tvDateHours);
    }
    return 2;
  }

  /** Shows a dialog with share options for the group */
  private void showShareDialog(Group group) {
    try {
      String[] shareOptions = {
        "Share via Text",
        "Share to WhatsApp",
        "Share to Facebook",
        "Share via SMS",
        "Share via Email"
      };

      new android.app.AlertDialog.Builder(this)
          .setTitle("Share Party")
          .setItems(
              shareOptions,
              (dialog, which) -> {
                switch (which) {
                  case 0: // Share via Text
                    ShareHelper.sharePartyText(JoinGroupActivity.this, group);
                    break;
                  case 1: // Share to WhatsApp
                    ShareHelper.shareToWhatsApp(JoinGroupActivity.this, group);
                    break;
                  case 2: // Share to Facebook
                    ShareHelper.shareToFacebook(JoinGroupActivity.this, group);
                    break;
                  case 3: // Share via SMS
                    ShareHelper.shareViaSMS(JoinGroupActivity.this, group);
                    break;
                  case 4: // Share via Email
                    ShareHelper.shareViaEmail(JoinGroupActivity.this, group);
                    break;
                }
              })
          .setNegativeButton("Cancel", null)
          .show();
    } catch (Exception e) {
      Log.e(TAG, "Error showing share dialog", e);
      Toast.makeText(this, "Error opening share options", Toast.LENGTH_SHORT).show();
    }
  }
}
