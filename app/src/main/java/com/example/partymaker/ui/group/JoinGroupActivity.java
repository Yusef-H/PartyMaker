package com.example.partymaker.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.utilities.MapUtilities;
import java.util.HashMap;
import java.util.Objects;

public class JoinGroupActivity extends AppCompatActivity {
  private ImageView imgCalender;
  private ImageView imgThumbUp;
  private ImageView imgThumbDown;
  private ImageView imgSeeHours;
  private ImageView imgAddFriend;
  private ImageView imgOptions;
  private ImageButton btnEditName;
  private Button back5;
  private TextView tvDateText;
  private TextView tvDateDays;
  private TextView tvDateMonths;
  private TextView tvDateYears;
  private TextView tvDateHours;
  private TextView tvComing;
  private TextView tvNotComing;
  private TextView tvSeeHours;
  private TextView tvSeeDate;
  private TextView tvAt;
  private TextView tvAddFriend;
  private TextView tvOptions;
  private CardView card1, card2, card3, card4, card5, card6, card7, card8;
  private String GroupKey;
  private String GroupLocation;
  private String GroupDay;
  private String GroupMonth;
  private String GroupYear;
  private String GroupHour;
  private String CurrentUser;
  private int IsClicked = 1;
  private HashMap<String, Object> FriendKeys;
  private final boolean isComing = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_join);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();

    // Get Values from PublicGroups By intent + connection between intent and
    // current activity objects
    String groupName =
        Objects.requireNonNull(getIntent().getExtras()).getString("GroupName", "defaultKey");
    GroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
    GroupDay = getIntent().getExtras().getString("groupDays", "defaultKey");
    GroupMonth = getIntent().getExtras().getString("groupMonths", "defaultKey");
    GroupYear = getIntent().getExtras().getString("groupYears", "defaultKey");
    GroupHour = getIntent().getExtras().getString("groupHours", "defaultKey");
    GroupLocation = getIntent().getExtras().getString("groupLocation", "defaultKey");
    String adminKey = getIntent().getExtras().getString("adminKey", "defaultKey");
    String createdAt = getIntent().getExtras().getString("createdAt", "defaultKey");
    int groupType =
        getIntent().getExtras().getInt("GroupType"); // if 0 so Public group if 1 so Private group
    String groupPrice = getIntent().getExtras().getString("GroupPrice");
    boolean canAdd = getIntent().getExtras().getBoolean("CanAdd");
    FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
    HashMap<String, Object> comingKeys =
        (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
    HashMap<String, Object> messageKeys =
        (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");

    // connection
    GridLayout joinGrid = findViewById(R.id.joinGrid);
    imgCalender = findViewById(R.id.imgGroupDate);
    ImageView imgLocation = findViewById(R.id.imgLocation1);
    imgSeeHours = findViewById(R.id.imgSeeHours1);
    tvDateText = findViewById(R.id.tvGroupDateText);
    tvDateDays = findViewById(R.id.tvGroupDateDays);
    tvDateMonths = findViewById(R.id.tvGroupDateMonths);
    tvDateYears = findViewById(R.id.tvGroupDateYears);
    tvDateHours = findViewById(R.id.tvGroupDateHours);
    TextView tvGroupName = findViewById(R.id.tvGroupName1);
    TextView tvCreatedBy = findViewById(R.id.tvCreatedBy1);
    TextView tvLocation = findViewById(R.id.tvLocation1);
    TextView tvGroupLocation = findViewById(R.id.tvGroupLocation1);
    tvSeeHours = findViewById(R.id.tvSeeHours1);
    tvSeeDate = findViewById(R.id.tvSeeDate1);
    tvAt = findViewById(R.id.tvGroupAt);
    TextView tvEntryPrice = findViewById(R.id.tvEntryPrice1);
    TextView tvYourEntry = findViewById(R.id.tvYourEntry1);

    // get current account's email
    CurrentUser =
        Objects.requireNonNull(Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail())
            .replace('.', ' ');

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
              FriendKeys.put(CurrentUser, "true");
              DBRef.refGroups.child(GroupKey).child("FriendKeys").removeValue();
              DBRef.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys);
              Toast.makeText(JoinGroupActivity.this, "Successfully joined ", Toast.LENGTH_SHORT)
                  .show();
              Intent i1 = new Intent(getBaseContext(), PublicGroupsActivity.class);
              startActivity(i1);
            } else if (finalI == 3) // open 2,2 (4) Back
            {
              Intent i1 = new Intent(getBaseContext(), PublicGroupsActivity.class);
              startActivity(i1);
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
      imgCalender.setVisibility(View.INVISIBLE);
      tvDateText.setVisibility(View.INVISIBLE);
      tvDateDays.setVisibility(View.VISIBLE);
      tvDateMonths.setVisibility(View.VISIBLE);
      tvDateYears.setVisibility(View.VISIBLE);
      tvSeeHours.setVisibility(View.VISIBLE);
      imgSeeHours.setVisibility(View.VISIBLE);
      return 2;
    }
    // if clicked again
    if (isClicked == 2) {
      tvDateDays.setVisibility(View.INVISIBLE);
      tvDateMonths.setVisibility(View.INVISIBLE);
      tvDateYears.setVisibility(View.INVISIBLE);
      tvSeeHours.setVisibility(View.INVISIBLE);
      tvSeeDate.setVisibility(View.VISIBLE);
      tvAt.setVisibility(View.VISIBLE);
      tvDateHours.setVisibility(View.VISIBLE);
      return 3;
    }
    // if click third time
    if (isClicked == 3) {
      tvDateDays.setVisibility(View.VISIBLE);
      tvDateMonths.setVisibility(View.VISIBLE);
      tvDateYears.setVisibility(View.VISIBLE);
      tvSeeHours.setVisibility(View.VISIBLE);
      tvSeeDate.setVisibility(View.INVISIBLE);
      tvAt.setVisibility(View.INVISIBLE);
      tvDateHours.setVisibility(View.INVISIBLE);
    }
    return 2;
  }
}
