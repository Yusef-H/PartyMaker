package com.example.partymaker;

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
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.User;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Objects;

public class DeletePeople extends AppCompatActivity {
  private Button btnHide, btnHelp, btnDeleteFriend;
  private TextView tvHide, tvHelp, tvInstructions1;
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
    setContentView(R.layout.activity_delete_people);

    // this 3 lines disables the action bar only in this activity
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
    btnDeleteFriend = findViewById(R.id.btnDeleteFriend);
    btnHide = findViewById(R.id.btnHide3);
    btnHelp = findViewById(R.id.btnHelp3);
    tvHide = findViewById(R.id.tvHide3);
    tvHelp = findViewById(R.id.tvHelp3);
    tvInstructions1 = findViewById(R.id.tvInstructions3);
    etFriendEmail = findViewById(R.id.etDeleteEmail);
    btnBack = findViewById(R.id.btnBack4);

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
    btnDeleteFriend.setOnClickListener(
        v -> {
          // This if - checks if EditText is not Empty
          if (etFriendEmail.getText().toString().trim().length() != 0) {
            CurrentFriend = etFriendEmail.getText().toString().replace('.', ' ');
            database.addValueEventListener(
                new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean flag = false;
                    boolean flag1 = false;
                    HashMap<String, Object> GroupFriends = new HashMap<>();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                      String UserEmail = data.getValue(User.class).getEmail().replace('.', ' ');
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
                        if (flag1) {
                          FriendKeys.remove(CurrentFriend);
                          DBref.refGroups.child(GroupKey).child("FriendKeys").removeValue();
                          DBref.refGroups
                              .child(GroupKey)
                              .child("FriendKeys")
                              .updateChildren(FriendKeys);
                          ComingKeys.remove(CurrentFriend);
                          DBref.refGroups.child(GroupKey).child("ComingKeys").removeValue();
                          DBref.refGroups
                              .child(GroupKey)
                              .child("ComingKeys")
                              .updateChildren(ComingKeys);
                          Toast.makeText(
                                  DeletePeople.this,
                                  "Friend successfully deleted",
                                  Toast.LENGTH_SHORT)
                              .show();
                        } else {
                          Toast.makeText(DeletePeople.this, "User not in group", Toast.LENGTH_SHORT)
                              .show();
                        }
                        flag = true;
                      }
                    }
                    if (!flag) {
                      Toast.makeText(DeletePeople.this, "Email not Exist", Toast.LENGTH_SHORT)
                          .show();
                    }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
          } else {
            Toast.makeText(DeletePeople.this, "Input email please", Toast.LENGTH_SHORT).show();
          }
        });
    btnBack.setOnClickListener(
        v -> {
          Intent intent = new Intent(getBaseContext(), AdminOptions.class);
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
        });
  }
  /*
   * / private void CheckExist() { database.addValueEventListener(new
   * ValueEventListener() {
   *
   * @Override public void onDataChange(DataSnapshot dataSnapshot) { for
   * (DataSnapshot data : dataSnapshot.getChildren()) { String UserEmail =
   * data.getValue(User.class).getEmail().replace('.', ' '); String
   * CurrentUserEmail = etFriendEmail.getText().toString().replace('.', ' '); if
   * (CurrentUserEmail.equals(UserEmail)) { FriendKeys.put(CurrentFriend, "true");
   * DBref.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys
   * ); Toast.makeText(AddFriends.this, "Friend successfully added",
   * Toast.LENGTH_SHORT).show(); } } }
   *
   * @Override public void onCancelled(DatabaseError databaseError) {
   *
   * } }); } /
   */
}
