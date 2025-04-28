package com.example.partymaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.User;

import java.io.Serializable;
import java.util.HashMap;

public class AddFriends extends AppCompatActivity {
    private Button btnHide, btnHelp, btnAddFriend, btnYes, btnNo;
    private TextView tvHide, tvHelp, tvInstructions1, tvAddMore;
    private EditText etFriendEmail;
    private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
    private String GroupKey, CurrentFriend, GroupName, GroupDay, GroupMonth, GroupYear, GroupHour, GroupLocation, AdminKey, CreatedAt, GroupPrice;
    private int GroupType;
    private ImageButton btnBack;
    private boolean CanAdd;
    private DatabaseReference database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Get Values from MainActivity By intent + connection between intent and current activity objects
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

        //database set
        database = FirebaseDatabase.getInstance().getReference("Users");

        //connection
        btnAddFriend = (Button) findViewById(R.id.btnAddFriend);
        btnHide = (Button) findViewById(R.id.btnHide1);
        btnHelp = (Button) findViewById(R.id.btnHelp1);
        btnYes = (Button) findViewById(R.id.btnYes);
        btnNo = (Button) findViewById(R.id.btnNo);
        tvHide = (TextView) findViewById(R.id.tvHide1);
        tvHelp = (TextView) findViewById(R.id.tvHelp1);
        tvAddMore = (TextView) findViewById(R.id.tvAddMore);
        tvInstructions1 = (TextView) findViewById(R.id.tvInstructions1);
        etFriendEmail = (EditText) findViewById(R.id.etFriendEmail);
        btnBack = (ImageButton) findViewById(R.id.btnBack3);


        EventHandler();
    }

    private void EventHandler() {
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvInstructions1.setVisibility(View.VISIBLE);
                btnHelp.setVisibility(View.INVISIBLE);
                tvHelp.setVisibility(View.INVISIBLE);
                btnHide.setVisibility(View.VISIBLE);
                tvHide.setVisibility(View.VISIBLE);
            }
        });

        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvInstructions1.setVisibility(View.INVISIBLE);
                btnHide.setVisibility(View.INVISIBLE);
                tvHide.setVisibility(View.INVISIBLE);
                btnHelp.setVisibility(View.VISIBLE);
                tvHelp.setVisibility(View.VISIBLE);
            }
        });
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This if - checks if EditText is not Empty
                if (etFriendEmail.getText().toString().trim().length() != 0) {
                    CurrentFriend = etFriendEmail.getText().toString().replace('.', ' ');
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean flag = false;
                            boolean flag1 = false;
                            HashMap<String, Object> GroupFriends = new HashMap<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                String UserEmail = data.getValue(User.class).getEmail().replace('.', ' ');
                                String CurrentUserEmail = etFriendEmail.getText().toString().replace('.', ' ');
                                GroupFriends = FriendKeys;
                                if (CurrentUserEmail.equals(UserEmail)) {
                                    for (String GroupFriend : GroupFriends.keySet()) {
                                        if (CurrentUserEmail.equals(GroupFriend)) {
                                            flag1 = true;
                                        }
                                    }
                                    if (flag1 == false) {
                                        FriendKeys.put(CurrentFriend, "true");
                                        DBref.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys);
                                        Toast.makeText(AddFriends.this, "Friend successfully added", Toast.LENGTH_SHORT).show();
                                        flag = true;
                                    } else if (flag1 == true) {
                                        Toast.makeText(AddFriends.this, "User already in group", Toast.LENGTH_SHORT).show();
                                        flag = true;
                                    }
                                }
                            }
                            if (flag == false) {
                                Toast.makeText(AddFriends.this, "Email not Exist", Toast.LENGTH_SHORT).show();
                            }
                            if (flag1 == false && flag == true) {
                                etFriendEmail.setVisibility(View.INVISIBLE);
                                btnAddFriend.setVisibility(View.INVISIBLE);
                                tvInstructions1.setVisibility(View.INVISIBLE);
                                btnHide.setVisibility(View.INVISIBLE);
                                tvHide.setVisibility(View.INVISIBLE);
                                btnHelp.setVisibility(View.INVISIBLE);
                                tvHelp.setVisibility(View.INVISIBLE);
                                tvAddMore.setVisibility(View.VISIBLE);
                                btnYes.setVisibility(View.VISIBLE);
                                btnNo.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(AddFriends.this, "Input email please", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //add Friend to coming list
                ComingKeys.put(CurrentFriend, "true");
                DBref.refGroups.child(GroupKey).child("ComingKeys").updateChildren(FriendKeys);
                Toast.makeText(AddFriends.this, "Added to Coming List", Toast.LENGTH_SHORT).show();

                etFriendEmail.setVisibility(View.VISIBLE);
                btnAddFriend.setVisibility(View.VISIBLE);
                tvInstructions1.setVisibility(View.INVISIBLE);
                btnHide.setVisibility(View.INVISIBLE);
                tvHide.setVisibility(View.INVISIBLE);
                btnHelp.setVisibility(View.VISIBLE);
                tvHelp.setVisibility(View.VISIBLE);
                tvAddMore.setVisibility(View.INVISIBLE);
                btnYes.setVisibility(View.INVISIBLE);
                btnNo.setVisibility(View.INVISIBLE);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFriendEmail.setVisibility(View.VISIBLE);
                btnAddFriend.setVisibility(View.VISIBLE);
                tvInstructions1.setVisibility(View.INVISIBLE);
                btnHide.setVisibility(View.INVISIBLE);
                tvHide.setVisibility(View.INVISIBLE);
                btnHelp.setVisibility(View.VISIBLE);
                tvHelp.setVisibility(View.VISIBLE);
                tvAddMore.setVisibility(View.INVISIBLE);
                btnYes.setVisibility(View.INVISIBLE);
                btnNo.setVisibility(View.INVISIBLE);

            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GroupScreen.class);
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
        });
    }
    /*/
    private void CheckExist() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String UserEmail = data.getValue(User.class).getEmail().replace('.', ' ');
                    String CurrentUserEmail = etFriendEmail.getText().toString().replace('.', ' ');
                    if (CurrentUserEmail.equals(UserEmail)) {
                        FriendKeys.put(CurrentFriend, "true");
                        DBref.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys);
                        Toast.makeText(AddFriends.this, "Friend successfully added", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    /*/
}