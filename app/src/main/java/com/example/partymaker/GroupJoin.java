package com.example.partymaker;

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

import com.example.partymaker.data.DBref;

import java.util.HashMap;

public class GroupJoin extends AppCompatActivity {
    private GridLayout joinGrid;
    private ImageView imgCalender, imgThumbUp, imgThumbDown, imgLocation, imgSeeHours, imgAddFriend, imgOptions;
    private ImageButton btnEditName;
    private Button back5;
    private TextView tvDateText, tvDateDays, tvDateMonths, tvDateYears, tvDateHours, tvComing, tvNotComing, tvGroupName, tvCreatedBy, tvLocation, tvGroupLocation, tvSeeHours, tvSeeDate, tvAt, tvAddFriend, tvOptions, tvEntryPrice, tvYourEntry;
    private CardView card1, card2, card3, card4, card5, card6, card7, card8;
    private String GroupName, GroupKey, GroupLocation, AdminKey, CreatedAt, GroupDay, GroupMonth, GroupYear, GroupHour, CurrentUser, GroupPrice;
    private int GroupType;
    private int IsClicked = 1;
    private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
    private boolean isComing = true;
    private boolean CanAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_join);


        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Get Values from PublicGroups By intent + connection between intent and current activity objects
        GroupName = getIntent().getExtras().getString("GroupName", "defaultKey");
        GroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
        GroupDay = getIntent().getExtras().getString("groupDays", "defaultKey");
        GroupMonth = getIntent().getExtras().getString("groupMonths", "defaultKey");
        GroupYear = getIntent().getExtras().getString("groupYears", "defaultKey");
        GroupHour = getIntent().getExtras().getString("groupHours", "defaultKey");
        GroupLocation = getIntent().getExtras().getString("groupLocation", "defaultKey");
        AdminKey = getIntent().getExtras().getString("adminKey", "defaultKey");
        CreatedAt = getIntent().getExtras().getString("createdAt", "defaultKey");
        GroupType = getIntent().getExtras().getInt("GroupType"); //if 0 so Public group if 1 so Private group
        GroupPrice = getIntent().getExtras().getString("GroupPrice");
        CanAdd = getIntent().getExtras().getBoolean("CanAdd");
        FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
        ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
        MessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");

        //connection
        joinGrid = (GridLayout) findViewById(R.id.joinGrid);
        imgCalender = (ImageView) findViewById(R.id.imgGroupDate);
        imgLocation = (ImageView) findViewById(R.id.imgLocation1);
        imgSeeHours = (ImageView) findViewById(R.id.imgSeeHours1);
        tvDateText = (TextView) findViewById(R.id.tvGroupDateText);
        tvDateDays = (TextView) findViewById(R.id.tvGroupDateDays);
        tvDateMonths = (TextView) findViewById(R.id.tvGroupDateMonths);
        tvDateYears = (TextView) findViewById(R.id.tvGroupDateYears);
        tvDateHours = (TextView) findViewById(R.id.tvGroupDateHours);
        tvGroupName = (TextView) findViewById(R.id.tvGroupName1);
        tvCreatedBy = (TextView) findViewById(R.id.tvCreatedBy1);
        tvLocation = (TextView) findViewById(R.id.tvLocation1);
        tvGroupLocation = (TextView) findViewById(R.id.tvGroupLocation1);
        tvSeeHours = (TextView) findViewById(R.id.tvSeeHours1);
        tvSeeDate = (TextView) findViewById(R.id.tvSeeDate1);
        tvAt = (TextView) findViewById(R.id.tvGroupAt);
        tvEntryPrice = (TextView) findViewById(R.id.tvEntryPrice1);
        tvYourEntry = (TextView) findViewById(R.id.tvYourEntry1);

        //get current account's email
        CurrentUser = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');

        //setting of GroupScreen for all users(not in buttons)
        tvGroupName.setText(GroupName);
        tvCreatedBy.setText(AdminKey.replace(' ', '.') + " ,  " + CreatedAt);

        //if GroupPrice=0 so it write that the party is free to enter if its not so it writes the entry price
        if (GroupPrice.equals("0")) {
            tvEntryPrice.setVisibility(View.INVISIBLE);
            tvYourEntry.setText("Free Party");
        } else {
            tvEntryPrice.setText(String.valueOf(GroupPrice));
        } //convert int to string

        JoinEvent(joinGrid);
    }

    private void JoinEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            //You can see , all child item is CardView , so we just cast object to CardView
            final CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (finalI == 0) //open 1,1 (1) Location
                    {
                        tvGroupLocation.setText(GroupLocation);
                        imgLocation.setVisibility(View.INVISIBLE);
                        tvLocation.setVisibility(View.INVISIBLE);
                        tvGroupLocation.setVisibility(View.VISIBLE);
                    } else if (finalI == 1) //open 1,2 (2) Date
                    {
                        IsClicked = isClicked(IsClicked);
                    } else if (finalI == 2) //open 2,1 (3) Join
                    {
                        FriendKeys.put(CurrentUser, "true");
                        DBref.refGroups.child(GroupKey).child("FriendKeys").removeValue();
                        DBref.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys);
                        Toast.makeText(GroupJoin.this, "Successfully joined ", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getBaseContext(), PublicGroups.class);
                        startActivity(i);
                    } else if (finalI == 3) //open 2,2 (4) Back
                    {
                        Intent i = new Intent(getBaseContext(), PublicGroups.class);
                        startActivity(i);
                    }
                }
            });
        }
    }

    private int isClicked(int isClicked) {
        if (isClicked == 1) {
            //changes Month name to 3 letters
            String Month = GroupMonth.substring(0, 3);
            //set text
            tvDateDays.setText(GroupDay);
            tvDateMonths.setText(Month);
            tvDateYears.setText(GroupYear);
            tvDateHours.setText(GroupHour);
            //Design
            imgCalender.setVisibility(View.INVISIBLE);
            tvDateText.setVisibility(View.INVISIBLE);
            tvDateDays.setVisibility(View.VISIBLE);
            tvDateMonths.setVisibility(View.VISIBLE);
            tvDateYears.setVisibility(View.VISIBLE);
            tvSeeHours.setVisibility(View.VISIBLE);
            imgSeeHours.setVisibility(View.VISIBLE);
            return 2;
        }
        //if clicked again
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
        //if click third time
        if (isClicked == 3) {
            tvDateDays.setVisibility(View.VISIBLE);
            tvDateMonths.setVisibility(View.VISIBLE);
            tvDateYears.setVisibility(View.VISIBLE);
            tvSeeHours.setVisibility(View.VISIBLE);
            tvSeeDate.setVisibility(View.INVISIBLE);
            tvAt.setVisibility(View.INVISIBLE);
            tvDateHours.setVisibility(View.INVISIBLE);
            return 2;
        } else {
            return 2;
        }
    }
}
