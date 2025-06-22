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
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.InvitedAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MembersComingActivity extends AppCompatActivity {

  private ListView lv3;
  private HashMap<String, Object> ComingKeys;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_coming_list);

    // this 3 lines changes title's name
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).setTitle("Coming to party");
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

    // connection between intent from GroupScreen and InvitedList
    HashMap<String, Object> hashFriendKeys =
        (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
    ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");

    lv3 = findViewById(R.id.lv3);

    ShowData();
    EventHandler();
  }

  private void EventHandler() {
    lv3.setOnItemClickListener((parent, view, position, id) -> {});
    lv3.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void ShowData() {
    DBRef.refUsers.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<User> ArrUsers = new ArrayList<>();
            HashMap<String, Object> ComingFriends;
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              User p = data.getValue(User.class);
              String UserMail = Objects.requireNonNull(p).getEmail().replace('.', ' ');
              ComingFriends = ComingKeys;
              for (String GroupFriend : ComingFriends.keySet()) {
                if (GroupFriend.equals(UserMail)) {
                  ArrUsers.add(p);
                }
              }
            }
            InvitedAdapter adpt = new InvitedAdapter(MembersComingActivity.this, 0, 0, ArrUsers);
            lv3.setAdapter(adpt);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }
}
