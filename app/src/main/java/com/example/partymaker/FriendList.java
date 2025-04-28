package com.example.partymaker;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.User;
import com.example.partymaker.data.UserAdpter;

import java.util.ArrayList;

public class FriendList extends AppCompatActivity {
    private ListView lv;
    public static Context contextOfApplication;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        contextOfApplication = getApplicationContext();

        lv=(ListView)findViewById(R.id.lv);
        ShowData();
        eventHandler();
    }


    private void eventHandler() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    User p = data.getValue(User.class);
                    ArrUsers.add(p);
                }
                UserAdpter adpt= new UserAdpter(FriendList.this,0,0,ArrUsers);
                lv.setAdapter(adpt);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
            });
    }
    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
}
