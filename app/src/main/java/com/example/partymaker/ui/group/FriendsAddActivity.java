package com.example.partymaker.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class FriendsAddActivity extends AppCompatActivity {
    private Button btnHide, btnHelp, btnAddFriend, btnYes, btnNo, btnFriendsList;
    private TextView tvHide, tvHelp, tvInstructions1, tvAddMore;
    private EditText etFriendEmail;
    private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
    private String GroupKey,
            CurrentFriend,
            GroupName,
            GroupDay,
            GroupMonth,
            GroupYear,
            GroupHour,
            GroupLocation,
            AdminKey,
            CreatedAt,
            GroupPrice;
    private int GroupType;
    private ImageButton btnBack;
    private boolean CanAdd;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        // this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // Get Values from MainActivity By intent + connection between intent and
        // current activity objects
        GroupName =
                Objects.requireNonNull(getIntent().getExtras()).getString("GroupName", "defaultKey");
        GroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
        GroupDay = getIntent().getExtras().getString("groupDays", "defaultKey");
        GroupMonth = getIntent().getExtras().getString("groupMonths", "defaultKey");
        GroupYear = getIntent().getExtras().getString("groupYears", "defaultKey");
        GroupHour = getIntent().getExtras().getString("groupHours", "defaultKey");
        GroupLocation = getIntent().getExtras().getString("groupLocation", "defaultKey");
        AdminKey = getIntent().getExtras().getString("adminKey", "defaultKey");
        CreatedAt = getIntent().getExtras().getString("createdAt", "defaultKey");
        GroupType =
                getIntent().getExtras().getInt("GroupType"); // if 0 so Public group if 1 so Private group
        GroupPrice = getIntent().getExtras().getString("GroupPrice");
        CanAdd = getIntent().getExtras().getBoolean("CanAdd");
        FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
        ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
        MessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");

        // database set
        database = FirebaseDatabase.getInstance().getReference("Users");

        // connection
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnFriendsList = findViewById(R.id.btnFriendsList);
        btnHide = findViewById(R.id.btnHide1);
        btnHelp = findViewById(R.id.btnHelp1);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        tvHide = findViewById(R.id.tvHide1);
        tvHelp = findViewById(R.id.tvHelp1);
        tvAddMore = findViewById(R.id.tvAddMore);
        tvInstructions1 = findViewById(R.id.tvInstructions1);
        etFriendEmail = findViewById(R.id.etFriendEmail);
        btnBack = findViewById(R.id.btnBack3);

        EventHandler();
    }

    private void EventHandler() {
        btnHelp.setOnClickListener(
                v -> {
                    tvInstructions1.setVisibility(View.VISIBLE);
                    btnHelp.setVisibility(View.INVISIBLE);
                    tvHelp.setVisibility(View.INVISIBLE);
                    btnHide.setVisibility(View.VISIBLE);
                    tvHide.setVisibility(View.VISIBLE);
                });

        btnHide.setOnClickListener(
                v -> {
                    tvInstructions1.setVisibility(View.INVISIBLE);
                    btnHide.setVisibility(View.INVISIBLE);
                    tvHide.setVisibility(View.INVISIBLE);
                    btnHelp.setVisibility(View.VISIBLE);
                    tvHelp.setVisibility(View.VISIBLE);
                });
        btnAddFriend.setOnClickListener(
                v -> {
                    // This if - checks if EditText is not Empty
                    if (!etFriendEmail.getText().toString().trim().isEmpty()) {
                        CurrentFriend = etFriendEmail.getText().toString().replace('.', ' ');
                        database.addValueEventListener(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean flag = false;
                                        boolean flag1 = false;
                                        HashMap<String, Object> GroupFriends;
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            String UserEmail =
                                                    Objects.requireNonNull(data.getValue(User.class))
                                                            .getEmail()
                                                            .replace('.', ' ');
                                            String CurrentUserEmail =
                                                    etFriendEmail.getText().toString().replace('.', ' ');
                                            GroupFriends = FriendKeys;
                                            if (CurrentUserEmail.equals(UserEmail)) {
                                                for (String GroupFriend : GroupFriends.keySet()) {
                                                    if (CurrentUserEmail.equals(GroupFriend)) {
                                                        flag1 = true;
                                                        break;
                                                    }
                                                }
                                                if (!flag1) {
                                                    FriendKeys.put(CurrentFriend, "true");
                                                    DBRef.refGroups
                                                            .child(GroupKey)
                                                            .child("FriendKeys")
                                                            .updateChildren(FriendKeys);
                                                    Toast.makeText(
                                                                    FriendsAddActivity.this,
                                                                    "Friend successfully added",
                                                                    Toast.LENGTH_SHORT)
                                                            .show();
                                                } else {
                                                    Toast.makeText(
                                                                    FriendsAddActivity.this,
                                                                    "User already in group",
                                                                    Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                                flag = true;
                                            }
                                        }
                                        if (!flag) {
                                            Toast.makeText(FriendsAddActivity.this, "Email not Exist", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                        if (!flag1 && flag) {
                                            etFriendEmail.setVisibility(View.INVISIBLE);
                                            btnAddFriend.setVisibility(View.INVISIBLE);
                                            btnFriendsList.setVisibility(View.INVISIBLE);
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
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                    } else {
                        Toast.makeText(FriendsAddActivity.this, "Input email please", Toast.LENGTH_SHORT).show();
                    }
                });
        btnFriendsList.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsAddActivity.this, UsersListActivity.class);
            startActivity(intent);
        });
        btnYes.setOnClickListener(
                v -> {

                    // add Friend to coming list
                    ComingKeys.put(CurrentFriend, "true");
                    DBRef.refGroups.child(GroupKey).child("ComingKeys").updateChildren(FriendKeys);
                    Toast.makeText(FriendsAddActivity.this, "Added to Coming List", Toast.LENGTH_SHORT).show();

                    etFriendEmail.setVisibility(View.VISIBLE);
                    btnAddFriend.setVisibility(View.VISIBLE);
                    btnFriendsList.setVisibility(View.VISIBLE);
                    tvInstructions1.setVisibility(View.INVISIBLE);
                    btnHide.setVisibility(View.INVISIBLE);
                    tvHide.setVisibility(View.INVISIBLE);
                    btnHelp.setVisibility(View.VISIBLE);
                    tvHelp.setVisibility(View.VISIBLE);
                    tvAddMore.setVisibility(View.INVISIBLE);
                    btnYes.setVisibility(View.INVISIBLE);
                    btnNo.setVisibility(View.INVISIBLE);
                });
        btnNo.setOnClickListener(
                v -> {
                    etFriendEmail.setVisibility(View.VISIBLE);
                    btnAddFriend.setVisibility(View.VISIBLE);
                    btnFriendsList.setVisibility(View.VISIBLE);
                    tvInstructions1.setVisibility(View.INVISIBLE);
                    btnHide.setVisibility(View.INVISIBLE);
                    tvHide.setVisibility(View.INVISIBLE);
                    btnHelp.setVisibility(View.VISIBLE);
                    tvHelp.setVisibility(View.VISIBLE);
                    tvAddMore.setVisibility(View.INVISIBLE);
                    btnYes.setVisibility(View.INVISIBLE);
                    btnNo.setVisibility(View.INVISIBLE);
                });
        btnBack.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getBaseContext(), GroupDetailsActivity.class);
                    ExtrasMetadata extras =
                            new ExtrasMetadata(
                                    GroupName,
                                    GroupKey,
                                    GroupDay,
                                    GroupMonth,
                                    GroupYear,
                                    GroupHour,
                                    GroupLocation,
                                    AdminKey,
                                    CreatedAt,
                                    GroupPrice,
                                    GroupType,
                                    CanAdd,
                                    FriendKeys,
                                    ComingKeys,
                                    MessageKeys);
                    Common.addExtrasToIntent(intent, extras);
                    startActivity(intent);
                    startActivity(intent);
                });
    }
}
