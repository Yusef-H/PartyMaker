package com.example.partymaker;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.InvitedAdpter;
import com.example.partymaker.data.User;

import java.util.ArrayList;
import java.util.HashMap;

public class ComingList extends AppCompatActivity {

    private ListView lv3;
    private HashMap<String, Object> FriendKeys, ComingKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_list);

        //this 2 lines changes title's name
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Coming to party");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

        //connection between intent from GroupScreen and InvitedList
        HashMap<String, Object> hashFriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
        FriendKeys = hashFriendKeys;
        HashMap<String, Object> hashComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
        ComingKeys = hashComingKeys;

        lv3 = (ListView)findViewById(R.id.lv3);

        ShowData();
        EventHandler();
    }

    private void EventHandler() {
        lv3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        lv3.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }
    private void ShowData() {
        DBref.refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> ArrUsers = new ArrayList<User>();
                HashMap<String, Object> ComingFriends = new HashMap<>();
                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    User p = data.getValue(User.class);
                    String UserMail = p.getEmail().replace('.', ' ');
                    ComingFriends=ComingKeys;
                    for (String GroupFriend : ComingFriends.keySet()) {
                        if (GroupFriend.equals(UserMail)) {
                            ArrUsers.add(p);
                        }
                    }
                }
                InvitedAdpter adpt= new InvitedAdpter(ComingList.this,0,0,ArrUsers);
                lv3.setAdapter(adpt);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
