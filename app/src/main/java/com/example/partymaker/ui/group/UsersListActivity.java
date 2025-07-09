package com.example.partymaker.ui.group;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Map;

public class UsersListActivity extends AppCompatActivity {
  private static final String TAG = "UsersListActivity";
  private ListView lv;
  public static Context contextOfApplication;
  private Object usersRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friend_list);
    contextOfApplication = getApplicationContext();

    // Actionbar settings
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.setTitle("All users");
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

    lv = findViewById(R.id.lv);

    // Get users reference using FirebaseAccessManager
    FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
    usersRef = accessManager.getUsersRef();

    ShowData();
    eventHandler();
  }

  private void eventHandler() {
    lv.setOnItemClickListener((parent, view, position, id) -> {});
    lv.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void ShowData() {
    if (usersRef instanceof DatabaseReference) {
      // Direct Firebase access mode
      DatabaseReference dbRef = (DatabaseReference) usersRef;
      dbRef.addValueEventListener(
          new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              ArrayList<User> ArrUsers = new ArrayList<>();
              for (DataSnapshot data : dataSnapshot.getChildren()) {
                User p = data.getValue(User.class);
                ArrUsers.add(p);
              }
              UserAdapter adapter = new UserAdapter(UsersListActivity.this, 0, 0, ArrUsers);
              lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
              Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
          });
    } else if (usersRef instanceof FirebaseServerClient) {
      // Server mode
      FirebaseServerClient serverClient = (FirebaseServerClient) usersRef;
      serverClient.getUsers(
          new FirebaseServerClient.DataCallback<Map<String, User>>() {
            @Override
            public void onSuccess(Map<String, User> data) {
              ArrayList<User> ArrUsers = new ArrayList<>(data.values());
              UserAdapter adapter = new UserAdapter(UsersListActivity.this, 0, 0, ArrUsers);
              lv.setAdapter(adapter);
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Server error: " + errorMessage);
              Toast.makeText(
                      UsersListActivity.this,
                      "Failed to load users: " + errorMessage,
                      Toast.LENGTH_SHORT)
                  .show();
            }
          });
    }
  }

  public static Context getContextOfApplication() {
    return contextOfApplication;
  }
}
