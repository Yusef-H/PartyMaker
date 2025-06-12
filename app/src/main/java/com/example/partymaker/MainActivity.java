package com.example.partymaker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.Group;
import com.example.partymaker.data.GroupAdpter;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
  private ListView lv1;
  private DatabaseReference database;
  ArrayList<Group> groupList;
  GroupAdpter allGroupsAdapter;
  String UserKey;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Action bar Design
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      GradientDrawable gradient = new GradientDrawable();
      gradient.setShape(GradientDrawable.RECTANGLE);
      gradient.setColors(new int[] {Color.parseColor("#006099"), Color.parseColor("#DBE3EF")});
      gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
      actionBar.setBackgroundDrawable(gradient);
      actionBar.setTitle(
          Html.fromHtml(
              "<font color='#FFFFFF'><b>My Parties</b></font>", Html.FROM_HTML_MODE_LEGACY));
      actionBar.setElevation(15f);
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(false);
    }

    lv1 = findViewById(R.id.lv1);

    // Get username and handle errors
    if (DBref.Auth.getCurrentUser() != null) {
      UserKey = Objects.requireNonNull(DBref.Auth.getCurrentUser().getEmail()).replace('.', ' ');
    } else {
      Log.e("MainActivity", "User not logged in");
      finish();
      return;
    }

    database = FirebaseDatabase.getInstance().getReference("Groups");
    retrieveData();
    setupEventHandlers();
    setupFloatingChatButton();
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingChatButton() {
    FloatingActionButton fabChat = findViewById(R.id.fabChat);
    fabChat.setOnClickListener(
        view -> {
          Intent intent = new Intent(MainActivity.this, GptChatActivity.class);
          startActivity(intent);
        });

    fabChat.setOnTouchListener(Common::dragChatButtonOnTouch);
  }

  private void setupEventHandlers() {
    lv1.setOnItemClickListener(
        (parent, view, position, id) -> navigateToGroupScreen(groupList.get(position)));
  }

  private void navigateToGroupScreen(Group group) {
    Intent intent = new Intent(getBaseContext(), GroupScreen.class);
    ExtrasMetadata extras =
        new ExtrasMetadata(
            group.getGroupName(),
            group.getGroupKey(),
            group.getGroupDays(),
            group.getGroupMonths(),
            group.getGroupYears(),
            group.getGroupHours(),
            group.getGroupLocation(),
            group.getAdminKey(),
            group.getCreatedAt(),
            group.getGroupPrice(),
            group.getGroupType(),
            group.isCanAdd(),
            group.getFriendKeys(),
            group.getComingKeys(),
            group.getMessageKeys());
    Common.addExtrasToIntent(intent, extras);
    startActivity(intent);
  }

  public void retrieveData() {
    database.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            HashMap<String, Object> UserKeys;
            groupList = new ArrayList<>();

            for (DataSnapshot data : dataSnapshot.getChildren()) {
              Group group = data.getValue(Group.class);
              UserKeys = Objects.requireNonNull(data.getValue(Group.class)).getFriendKeys();

              for (String userKey : UserKeys.keySet()) {

                if (UserKey.equals(userKey)) groupList.add(group);
              }
            }

            groupList.sort(Comparator.comparing(Group::getCreatedAt));
            allGroupsAdapter = new GroupAdpter(MainActivity.this, 0, 0, groupList);
            lv1.setAdapter(allGroupsAdapter);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e("MainActivity", "Database error: " + databaseError.getMessage());
          }
        });
  }

  // menu
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
