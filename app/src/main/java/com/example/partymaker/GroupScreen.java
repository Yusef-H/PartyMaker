package com.example.partymaker;

import static com.example.partymaker.utilities.Constants.ADMIN_KEY;
import static com.example.partymaker.utilities.Constants.CAN_ADD;
import static com.example.partymaker.utilities.Constants.COMING_KEYS;
import static com.example.partymaker.utilities.Constants.CREATED_AT;
import static com.example.partymaker.utilities.Constants.DEFAULT_KEY;
import static com.example.partymaker.utilities.Constants.FRIEND_KEYS;
import static com.example.partymaker.utilities.Constants.GROUP_DAYS;
import static com.example.partymaker.utilities.Constants.GROUP_HOURS;
import static com.example.partymaker.utilities.Constants.GROUP_KEY;
import static com.example.partymaker.utilities.Constants.GROUP_LOCATION;
import static com.example.partymaker.utilities.Constants.GROUP_MONTHS;
import static com.example.partymaker.utilities.Constants.GROUP_NAME;
import static com.example.partymaker.utilities.Constants.GROUP_PRICE;
import static com.example.partymaker.utilities.Constants.GROUP_TYPE;
import static com.example.partymaker.utilities.Constants.GROUP_YEARS;
import static com.example.partymaker.utilities.Constants.MESSAGE_KEYS;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.ChatMessage;
import com.example.partymaker.data.DBref;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GroupScreen extends AppCompatActivity {
    private GridLayout mainGrid;
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

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_screen);

        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Get Values from MainActivity By intent + connection between intent and current activity objects
        GroupName = getIntent().getExtras().getString(GROUP_NAME, DEFAULT_KEY);
        GroupKey = getIntent().getExtras().getString(GROUP_KEY, DEFAULT_KEY);
        GroupDay = getIntent().getExtras().getString(GROUP_DAYS, DEFAULT_KEY);
        GroupMonth = getIntent().getExtras().getString(GROUP_MONTHS, DEFAULT_KEY);
        GroupYear = getIntent().getExtras().getString(GROUP_YEARS, DEFAULT_KEY);
        GroupHour = getIntent().getExtras().getString(GROUP_HOURS, DEFAULT_KEY);
        GroupLocation = getIntent().getExtras().getString(GROUP_LOCATION, DEFAULT_KEY);
        AdminKey = getIntent().getExtras().getString(ADMIN_KEY, DEFAULT_KEY);
        CreatedAt = getIntent().getExtras().getString(CREATED_AT, DEFAULT_KEY);
        GroupType = getIntent().getExtras().getInt(GROUP_TYPE); //if 0 so Public group if 1 so Private group
        GroupPrice = getIntent().getExtras().getString(GROUP_PRICE);
        CanAdd = getIntent().getExtras().getBoolean(CAN_ADD);
        FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra(FRIEND_KEYS);
        ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra(COMING_KEYS);
        MessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra(MESSAGE_KEYS);

        //connection
        mainGrid = (GridLayout) findViewById(R.id.MainGrid);
        back5 = (Button) findViewById(R.id.back5);
        imgCalender = (ImageView) findViewById(R.id.imgCalender);
        imgThumbUp = (ImageView) findViewById(R.id.imgThumbUp);
        imgThumbDown = (ImageView) findViewById(R.id.imgThumbDown);
        imgLocation = (ImageView) findViewById(R.id.imgLocation);
        imgSeeHours = (ImageView) findViewById(R.id.imgSeeHours);
        imgAddFriend = (ImageView) findViewById(R.id.imgAddFriend);
        imgOptions = (ImageView) findViewById(R.id.imgOptions);
        btnEditName = (ImageButton) findViewById(R.id.btnEditName);
        tvDateText = (TextView) findViewById(R.id.tvDateText);
        tvDateDays = (TextView) findViewById(R.id.tvDateDays);
        tvDateMonths = (TextView) findViewById(R.id.tvDateMonths);
        tvDateYears = (TextView) findViewById(R.id.tvDateYears);
        tvDateHours = (TextView) findViewById(R.id.tvDateHours);
        tvComing = (TextView) findViewById(R.id.tvComing);
        tvNotComing = (TextView) findViewById(R.id.tvNotComing);
        tvGroupName = (TextView) findViewById(R.id.tvGroupName);
        tvCreatedBy = (TextView) findViewById(R.id.tvCreatedBy);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvGroupLocation = (TextView) findViewById(R.id.tvGroupLocation);
        tvSeeHours = (TextView) findViewById(R.id.tvSeeHours);
        tvSeeDate = (TextView) findViewById(R.id.tvSeeDate);
        tvAddFriend = (TextView) findViewById(R.id.tvAddFriend);
        tvOptions = (TextView) findViewById(R.id.tvOptions);
        tvAt = (TextView) findViewById(R.id.tvAt);
        tvEntryPrice = (TextView) findViewById(R.id.tvEntryPrice);
        tvYourEntry = (TextView) findViewById(R.id.tvYourEntry);
        card1 = (CardView) findViewById(R.id.Card1);
        card2 = (CardView) findViewById(R.id.Card2);
        card3 = (CardView) findViewById(R.id.Card3);
        card4 = (CardView) findViewById(R.id.Card4);
        card5 = (CardView) findViewById(R.id.Card5);
        card6 = (CardView) findViewById(R.id.Card6);
        card7 = (CardView) findViewById(R.id.Card7);
        card8 = (CardView) findViewById(R.id.Card8);

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

            //if current account is admin
            if (CurrentUser.equals(AdminKey)) {
                AdminEvent(mainGrid);
            }

            //if current account is not admin
            if (!CurrentUser.equals(AdminKey)) {
                //if group is public
                if (GroupType == 0) {
                    isComing();
                    FriendEvent(mainGrid);
                }
                //if group is private
                else {
                    //settings if group is private
                    imgAddFriend.setVisibility(View.INVISIBLE);
                    tvAddFriend.setVisibility(View.INVISIBLE);

                    isComing();
                    FriendEvent(mainGrid);
                }
            }
            EventHandler();
    }

    private void EventHandler() {
        btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edittext = new EditText(GroupScreen.this);
                AlertDialog.Builder alert = new AlertDialog.Builder(GroupScreen.this);
                alert.setMessage("Input new name below");
                alert.setTitle("Change party's name");

                alert.setView(edittext);

                alert.setPositiveButton("Change name", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //if pressed changed name
                        GroupName = edittext.getText().toString();
                        DBref.refGroups.child(GroupKey).child("groupName").setValue(GroupName);
                        tvGroupName.setText(GroupName);
                        Toast.makeText(GroupScreen.this, "Name Changed", Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with Back.
                    }
                });

                alert.show();
            }
        });
        back5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
            }
        });
    }

    private void AdminEvent(GridLayout mainGrid) {
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
                    } else if (finalI == 2) //open 2,1 (3) People Invited
                    {
                        Intent i = new Intent(getBaseContext(), invitedList.class);
                        i.putExtra("FriendKeys", (Serializable) FriendKeys);
                        startActivity(i);
                    } else if (finalI == 3) //open 2,2 (4) People Coming
                    {
                        Intent i = new Intent(getBaseContext(), ComingList.class);
                        i.putExtra("ComingKeys", (Serializable) ComingKeys);
                        startActivity(i);
                    } else if (finalI == 4) //open 3,1 (5) Admin Options
                    {
                        //intent from GroupScreen to AdminOptions
                        Intent intent = new Intent(getBaseContext(), AdminOptions.class);
                        ExtrasMetadata extras = new ExtrasMetadata(GroupName, GroupKey,
                                GroupDay, GroupMonth, GroupYear, GroupHour, GroupLocation,
                                AdminKey, CreatedAt, GroupPrice, GroupType, CanAdd,
                                FriendKeys, ComingKeys, MessageKeys);
                        Common.addExtrasToIntent(intent, extras);
                        startActivity(intent);

                    } else if (finalI == 5) //open 3,2 (6) Chat
                    {

                        Intent i = new Intent(getBaseContext(), Chat.class);
                        i.putExtra("MessageKeys", (Serializable) MessageKeys);
                        i.putExtra("groupKey", GroupKey);
                        startActivity(i);

                    } else if (finalI == 6) //open 4,1 (7) Add Friends
                    {
                        Intent intent = new Intent(getBaseContext(), AddFriends.class);
                        intent.putExtra("GroupName", GroupName);
                        intent.putExtra("groupKey", GroupKey);
                        intent.putExtra("groupDays", GroupDay);
                        intent.putExtra("groupMonths", GroupMonth);
                        intent.putExtra("groupYears", GroupYear);
                        intent.putExtra("groupHours", GroupHour);
                        intent.putExtra("groupLocation", GroupLocation);
                        intent.putExtra("adminKey", AdminKey);
                        intent.putExtra("createdAt", CreatedAt);
                        intent.putExtra("GroupType", GroupType);
                        intent.putExtra("GroupPrice", GroupPrice);
                        intent.putExtra("CanAdd", CanAdd);
                        intent.putExtra("FriendKeys", (Serializable) FriendKeys);
                        intent.putExtra("ComingKeys", (Serializable) ComingKeys);
                        intent.putExtra("MessageKeys", (Serializable) MessageKeys);
                        startActivity(intent);
                    } else if (finalI == 7) //open 4,2 (8) Leave
                    {

                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupScreen.this);
                        builder.setTitle("Leave Party");
                        builder.setMessage("Are you sure you want to leave this party?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //if pressed Yes
                                //if group have no friends so delete friends
                                if (FriendKeys.size() == 1) {
                                    //delete all messages writen by current group
                                    deleteMessages();

                                    //delete group from database
                                    DBref.refGroups.child(GroupKey).removeValue();

                                    //delete group's picture
                                    DBref.refStorage.child("Groups/" + GroupKey).delete();

                                    //if it went successfully so toast write it
                                    Toast.makeText(GroupScreen.this, "successfully left", Toast.LENGTH_SHORT).show();

                                    //intent from GroupScreen to MainMenu
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(intent);

                                }
                                //only admin leave group and set admin key to another user
                                else {
                                    //remove CurrentUser from FriendList and ComingList
                                    FriendKeys.remove(CurrentUser);
                                    ComingKeys.remove(CurrentUser);

                                    //set new admin from users in FriendList
                                    Map.Entry<String, Object> entry = FriendKeys.entrySet().iterator().next();
                                    AdminKey = entry.getKey();
                                    DBref.refGroups.child(GroupKey).child("adminKey").setValue(AdminKey);

                                    //removes friend list from firebase and set new one without Current user (Because cant update Hashmap with value deleting)
                                    DBref.refGroups.child(GroupKey).child("FriendKeys").removeValue();
                                    DBref.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys);

                                    //removes coming list from firebase and set new one without Current user (Because cant update Hashmap with value deleting)
                                    DBref.refGroups.child(GroupKey).child("ComingKeys").removeValue();
                                    DBref.refGroups.child(GroupKey).child("ComingKeys").updateChildren(ComingKeys);

                                    //if it went successfully so toast write it
                                    Toast.makeText(GroupScreen.this, "successfully left", Toast.LENGTH_SHORT).show();

                                    //intent from GroupScreen to MainMenu
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(intent);
                                }

                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.show();

                    }


                }
            });
        }
    }

    private void FriendEvent(GridLayout mainGrid) {
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
                    } else if (finalI == 2) //open 2,1 (3) People Invited
                    {
                        Intent i = new Intent(getBaseContext(), invitedList.class);
                        i.putExtra("FriendKeys", (Serializable) FriendKeys);
                        startActivity(i);
                    } else if (finalI == 3) //open 2,2 (4) People Coming
                    {
                        Intent i = new Intent(getBaseContext(), ComingList.class);
                        i.putExtra("ComingKeys", (Serializable) ComingKeys);
                        startActivity(i);
                    } else if (finalI == 4) //open 3,1 (5) Coming/Not Coming
                    {
                        if (isComing == false) {
                            String CurrentUser = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');
                            ComingKeys.put(CurrentUser, "true");
                            DBref.refGroups.child(GroupKey).child("ComingKeys").updateChildren(ComingKeys);
                            Toast.makeText(GroupScreen.this, "You're Coming", Toast.LENGTH_SHORT).show();
                            tvNotComing.setVisibility(View.INVISIBLE);
                            imgThumbDown.setVisibility(View.INVISIBLE);
                            tvComing.setVisibility(View.VISIBLE);
                            imgThumbUp.setVisibility(View.VISIBLE);
                            card5.setCardBackgroundColor(Color.parseColor("#1986ed"));
                        }
                    } else if (finalI == 5) //open 3,2 (6) Chat
                    {

                        Intent i = new Intent(getBaseContext(), Chat.class);
                        i.putExtra("MessageKeys", (Serializable) MessageKeys);
                        i.putExtra("groupKey", GroupKey);
                        startActivity(i);

                    } else if (finalI == 6) //open 4,1 (7) Add Friends
                    {
                        //if its a public group/got admin set to add
                        if (CanAdd == true) {
                            Intent intent = new Intent(getBaseContext(), AddFriends.class);
                            intent.putExtra("GroupName", GroupName);
                            intent.putExtra("groupKey", GroupKey);
                            intent.putExtra("groupDays", GroupDay);
                            intent.putExtra("groupMonths", GroupMonth);
                            intent.putExtra("groupYears", GroupYear);
                            intent.putExtra("groupHours", GroupHour);
                            intent.putExtra("groupLocation", GroupLocation);
                            intent.putExtra("adminKey", AdminKey);
                            intent.putExtra("createdAt", CreatedAt);
                            intent.putExtra("GroupType", GroupType);
                            intent.putExtra("GroupPrice", GroupPrice);
                            intent.putExtra("CanAdd", CanAdd);
                            intent.putExtra("FriendKeys", (Serializable) FriendKeys);
                            intent.putExtra("ComingKeys", (Serializable) ComingKeys);
                            intent.putExtra("MessageKeys", (Serializable) MessageKeys);
                            startActivity(intent);
                        }
                    } else if (finalI == 7) //open 4,2 (8) Leave
                    {

                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupScreen.this);
                        builder.setTitle("Leave Party");
                        builder.setMessage("Are you sure you want to leave this party?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //if pressed Yes
                                //if group have no friends so delete friends
                                if (FriendKeys.size() == 1) {
                                    //delete all messages writen by current group
                                    deleteMessages();

                                    //delete group screen from database
                                    DBref.refGroups.child(GroupKey).removeValue();

                                    //delete group's picture
                                    DBref.refStorage.child("Groups/" + GroupKey).delete();

                                    //if it went successfully so toast write it
                                    Toast.makeText(GroupScreen.this, "successfully left", Toast.LENGTH_SHORT).show();

                                    //intent from GroupScreen to MainMenu
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                                //only current user leave group
                                else {
                                    //remove CurrentUser from FriendList and ComingList
                                    FriendKeys.remove(CurrentUser);
                                    ComingKeys.remove(CurrentUser);

                                    //removes friend list from firebase and set new one without Current user (Because cant update Hashmap with value deleting)
                                    DBref.refGroups.child(GroupKey).child("FriendKeys").removeValue();
                                    DBref.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys);

                                    //removes coming list from firebase and set new one without Current user (Because cant update Hashmap with value deleting)
                                    DBref.refGroups.child(GroupKey).child("ComingKeys").removeValue();
                                    DBref.refGroups.child(GroupKey).child("ComingKeys").updateChildren(ComingKeys);

                                    //if it went successfully so toast write it
                                    Toast.makeText(GroupScreen.this, "successfully left", Toast.LENGTH_SHORT).show();

                                    //intent from GroupScreen to MainMenu
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.show();

                    }


                }
            });
        }
    }

    private void isComing() {
        String CurrentUser = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');
        boolean flag = false;
        for (String ComingFriend : ComingKeys.keySet()) {
            if (ComingFriend.equals(CurrentUser)) {
                tvNotComing.setVisibility(View.INVISIBLE);
                imgThumbDown.setVisibility(View.INVISIBLE);
                imgOptions.setVisibility(View.INVISIBLE);
                tvOptions.setVisibility(View.INVISIBLE);
                tvComing.setVisibility(View.VISIBLE);
                imgThumbUp.setVisibility(View.VISIBLE);
                card5.setCardBackgroundColor(Color.parseColor("#1986ed"));
                flag = true;
            }
        }
        if (flag == false) {
            tvComing.setVisibility(View.INVISIBLE);
            imgThumbUp.setVisibility(View.INVISIBLE);
            imgOptions.setVisibility(View.INVISIBLE);
            tvOptions.setVisibility(View.INVISIBLE);
            tvNotComing.setVisibility(View.VISIBLE);
            imgThumbDown.setVisibility(View.VISIBLE);
            card5.setCardBackgroundColor(Color.parseColor("#af0707"));
            isComing = false;
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

    private void deleteMessages() {
        DBref.refMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String msgKey = data.getValue(ChatMessage.class).getMessageKey();
                    for (String chatMsgKey : MessageKeys.keySet()) {
                        if (chatMsgKey.equals(msgKey)) {
                            DBref.refMessages.child(chatMsgKey).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
