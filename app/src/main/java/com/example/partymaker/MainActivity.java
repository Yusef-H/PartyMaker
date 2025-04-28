package com.example.partymaker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.Group;
import com.example.partymaker.data.GroupAdpter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ListView lv1;
    private DatabaseReference database;
    ArrayList<Group> group;
    GroupAdpter allGroupsAdapter;
    String UserKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Change title Name and Color
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'>Your Parties</font>"));
            //set actionbar background
            Drawable d = getResources().getDrawable(R.drawable.fade);
            actionBar.setBackgroundDrawable(d);
        }

        lv1 = (ListView) findViewById(R.id.lv1);
        UserKey = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');
        database = FirebaseDatabase.getInstance().getReference("Groups");
        retriveData();
        EventHandler();
    }

    private void EventHandler() {
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //intent Value
                String GroupName = group.get(position).getGroupName();
                String groupKey = group.get(position).getGroupKey();
                String groupDays = group.get(position).getGroupDays();
                String groupMonths = group.get(position).getGroupMonths();
                String groupYears = group.get(position).getGroupYears();
                String groupHours = group.get(position).getGroupHours();
                String groupLocation = group.get(position).getGroupLocation();
                String adminKey = group.get(position).getAdminKey();
                String createdAt = group.get(position).getCreatedAt();
                String GroupPrice = group.get(position).getGroupPrice();
                int GroupType = group.get(position).getGroupType();
                boolean CanAdd = group.get(position).isCanAdd();
                HashMap<String, Object> FriendKeys = group.get(position).getFriendKeys();
                HashMap<String, Object> ComingKeys = group.get(position).getComingKeys();
                HashMap<String, Object> MessageKeys = group.get(position).getMessageKeys();
                Intent intent = new Intent(getBaseContext(), GroupScreen.class);
                intent.putExtra("GroupName", GroupName);
                intent.putExtra("groupKey", groupKey);
                intent.putExtra("groupDays", groupDays);
                intent.putExtra("groupMonths", groupMonths);
                intent.putExtra("groupYears", groupYears);
                intent.putExtra("groupHours", groupHours);
                intent.putExtra("groupLocation", groupLocation);
                intent.putExtra("adminKey", adminKey);
                intent.putExtra("createdAt", createdAt);
                intent.putExtra("GroupPrice", GroupPrice);
                intent.putExtra("GroupType", GroupType);
                intent.putExtra("CanAdd", CanAdd);
                intent.putExtra("FriendKeys", (Serializable) FriendKeys);
                intent.putExtra("ComingKeys", (Serializable) ComingKeys);
                intent.putExtra("MessageKeys", (Serializable) MessageKeys);
                startActivity(intent);
            }
        });
        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }


    public void retriveData() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> UserKeys = new HashMap<>();
                group = new ArrayList<Group>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Group p = data.getValue(Group.class);
                    UserKeys = data.getValue(Group.class).getFriendKeys();

                    for (String userKey : UserKeys.keySet()) {

                        if (UserKey.equals(userKey))
                            group.add(p);
                    }


                }
                allGroupsAdapter = new GroupAdpter(MainActivity.this, 0, 0, group);
                lv1.setAdapter(allGroupsAdapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //menu
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent goToNextActivity;

        if (item.getItemId() == R.id.idMenu) {
            goToNextActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(goToNextActivity);
        } else if (item.getItemId() == R.id.idAddProfile) {
            goToNextActivity = new Intent(getApplicationContext(), AddGroup.class);
            startActivity(goToNextActivity);
        } else if (item.getItemId() == R.id.idEditProfile) {
            goToNextActivity = new Intent(getApplicationContext(), EditProfile.class);
            startActivity(goToNextActivity);
        } else if (item.getItemId() == R.id.idPublicParties) {
            goToNextActivity = new Intent(getApplicationContext(), PublicGroups.class);
            startActivity(goToNextActivity);
        } else if (item.getItemId() == R.id.idLogout) {
            DBref.Auth.signOut();
            DBref.CurrentUser = null;
            goToNextActivity = new Intent(getApplicationContext(), Login.class);
            startActivity(goToNextActivity);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
