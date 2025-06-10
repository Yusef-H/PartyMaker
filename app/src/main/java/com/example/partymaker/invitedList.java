package com.example.partymaker;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.InvitedAdpter;
import com.example.partymaker.data.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class invitedList extends AppCompatActivity {

  private ListView lv2;
  private HashMap<String, Object> FriendKeys;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_invited_list);

    // this 2 lines changes title's name
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle("Invited to party");
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

    // connection between intent from GroupScreen and InvitedList
    FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");

    lv2 = findViewById(R.id.lv2);

    ShowData();
    EventHandler();
  }

  private void EventHandler() {
    lv2.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}
        });
    lv2.setOnItemLongClickListener(
        new AdapterView.OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return false;
          }
        });
  }

  private void ShowData() {
    DBref.refUsers.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<User> ArrUsers = new ArrayList<User>();
            HashMap<String, Object> GroupFriends = new HashMap<>();
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              User p = data.getValue(User.class);
              String UserMail = p.getEmail().replace('.', ' ');
              GroupFriends = FriendKeys;
              for (String GroupFriend : GroupFriends.keySet()) {
                if (GroupFriend.equals(UserMail)) {
                  ArrUsers.add(p);
                }
              }
            }
            InvitedAdpter adpt = new InvitedAdpter(invitedList.this, 0, 0, ArrUsers);
            lv2.setAdapter(adpt);
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {}
        });
  }
}
