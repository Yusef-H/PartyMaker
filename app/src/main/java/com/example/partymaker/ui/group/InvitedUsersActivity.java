package com.example.partymaker.ui.group;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.ui.adapters.InvitedAdpter;
import com.example.partymaker.data.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class InvitedUsersActivity extends AppCompatActivity {

  private ListView lv2;
  private HashMap<String, Object> FriendKeys;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_invited_list);

    // this 2 lines changes title's name
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).setTitle("Invited to party");
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

    // connection between intent from GroupScreen and InvitedList
    FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");

    lv2 = findViewById(R.id.lv2);

    ShowData();
    EventHandler();
  }

  private void EventHandler() {
    lv2.setOnItemClickListener((parent, view, position, id) -> {});
    lv2.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void ShowData() {
    DBRef.refUsers.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<User> ArrUsers = new ArrayList<>();
            HashMap<String, Object> GroupFriends;
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              User p = data.getValue(User.class);
              String UserMail = Objects.requireNonNull(p).getEmail().replace('.', ' ');
              GroupFriends = FriendKeys;
              for (String GroupFriend : GroupFriends.keySet()) {
                if (GroupFriend.equals(UserMail)) {
                  ArrUsers.add(p);
                }
              }
            }
            InvitedAdpter adpt = new InvitedAdpter(InvitedUsersActivity.this, 0, 0, ArrUsers);
            lv2.setAdapter(adpt);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }
}
