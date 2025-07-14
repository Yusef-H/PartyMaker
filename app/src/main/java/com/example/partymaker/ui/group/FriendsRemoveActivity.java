package com.example.partymaker.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.FirebaseAccessManager;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.UserAdapter;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FriendsRemoveActivity extends AppCompatActivity {
  private static final String TAG = "FriendsRemoveActivity";
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
  private Object usersRef;
  private Object groupsRef;
  private String groupID;
  private ArrayList<User> usersList = new ArrayList<>();
  private ArrayList<String> userKeys = new ArrayList<>();
  private UserAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friends_remove);

    Intent intent = getIntent();
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(intent);
    if (extras == null) {
      Toast.makeText(this, "Error loading group data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    GroupName = extras.getGroupName();
    GroupKey = extras.getGroupKey();
    GroupDay = extras.getGroupDays();
    GroupMonth = extras.getGroupMonths();
    GroupYear = extras.getGroupYears();
    GroupHour = extras.getGroupHours();
    GroupLocation = extras.getGroupLocation();
    AdminKey = extras.getAdminKey();
    CreatedAt = extras.getCreatedAt();
    GroupPrice = extras.getGroupPrice();
    GroupType = extras.getGroupType();
    CanAdd = extras.isCanAdd();
    FriendKeys = extras.getFriendKeys();
    ComingKeys = extras.getComingKeys();
    MessageKeys = extras.getMessageKeys();

    // Initialize Firebase database references
    FirebaseAccessManager accessManager = new FirebaseAccessManager(this);
    usersRef = accessManager.getUsersRef();
    groupsRef = accessManager.getGroupsRef();

    // connection
    btnDeleteFriend = findViewById(R.id.btnDeleteFriend);
    btnHide = findViewById(R.id.btnHide3);
    btnHelp = findViewById(R.id.btnHelp3);
    tvHide = findViewById(R.id.tvHide3);
    tvHelp = findViewById(R.id.tvHelp3);
    tvInstructions1 = findViewById(R.id.tvInstructions3);
    etFriendEmail = findViewById(R.id.etDeleteEmail);
    btnBack = findViewById(R.id.btnBack4);

    groupID = getIntent().getStringExtra("groupID");
    if (groupID == null) {
      Toast.makeText(this, "Error: Group ID not provided", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    ListView listView = findViewById(R.id.friends_remove_list);
    adapter = new UserAdapter(this, R.layout.item_user, R.id.tvUserListUsername, usersList);
    listView.setAdapter(adapter);

    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          User selectedUser = usersList.get(position);
          String selectedKey = userKeys.get(position);
          removeFriend(selectedKey);
        });

    EventHandler();
    loadFriends();
  }

  private void EventHandler() {
    btnHelp.setOnClickListener(
        v -> {
          showViews(tvInstructions1, btnHide, tvHide);
          hideViews(btnHelp, tvHelp);
        });

    btnHide.setOnClickListener(
        v -> {
          showViews(btnHelp, tvHelp);
          hideViews(tvInstructions1, btnHide, tvHide);
        });
    btnDeleteFriend.setOnClickListener(
        v -> {
          // This if - checks if EditText is not Empty
          if (!etFriendEmail.getText().toString().trim().isEmpty()) {
            CurrentFriend = etFriendEmail.getText().toString().replace('.', ' ');

            if (usersRef instanceof DatabaseReference) {
              // Direct Firebase access mode
              DatabaseReference dbRef = (DatabaseReference) usersRef;
              dbRef.addListenerForSingleValueEvent(
                  new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      processUserData(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                      Toast.makeText(
                              FriendsRemoveActivity.this,
                              "Database error: " + databaseError.getMessage(),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            } else if (usersRef instanceof FirebaseServerClient) {
              // Server mode
              FirebaseServerClient serverClient = (FirebaseServerClient) usersRef;
              serverClient.getUsers(
                  new FirebaseServerClient.DataCallback<Map<String, User>>() {
                    @Override
                    public void onSuccess(Map<String, User> data) {
                      processServerUserData(data);
                    }

                    @Override
                    public void onError(String errorMessage) {
                      Toast.makeText(
                              FriendsRemoveActivity.this,
                              "Server error: " + errorMessage,
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            }
          } else {
            Toast.makeText(FriendsRemoveActivity.this, "Input email please", Toast.LENGTH_SHORT)
                .show();
          }
        });
    btnBack.setOnClickListener(
        v -> {
          Intent intent = new Intent(getBaseContext(), AdminOptionsActivity.class);
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

  private void processUserData(DataSnapshot dataSnapshot) {
    boolean flag = false;
    boolean flag1 = false;
    HashMap<String, Object> GroupFriends;
    for (DataSnapshot data : dataSnapshot.getChildren()) {
      String UserEmail =
          Objects.requireNonNull(data.getValue(User.class)).getEmail().replace('.', ' ');
      if (UserEmail.equals(CurrentFriend)) {
        flag = true;
        String friendKey = data.getKey();
        if (FriendKeys != null && FriendKeys.containsValue(friendKey)) {
          flag1 = true;
          if (groupsRef instanceof DatabaseReference) {
            // Direct Firebase access
            DatabaseReference groupRef = ((DatabaseReference) groupsRef).child(GroupKey);

            // Remove from FriendKeys
            if (FriendKeys != null) {
              for (Map.Entry<String, Object> entry : FriendKeys.entrySet()) {
                if (entry.getValue().equals(friendKey)) {
                  groupRef.child("FriendKeys").child(entry.getKey()).removeValue();
                  FriendKeys.remove(entry.getKey());
                  break;
                }
              }
            }

            // Remove from ComingKeys if present
            if (ComingKeys != null) {
              for (Map.Entry<String, Object> entry : ComingKeys.entrySet()) {
                if (entry.getValue().equals(friendKey)) {
                  groupRef.child("ComingKeys").child(entry.getKey()).removeValue();
                  ComingKeys.remove(entry.getKey());
                  break;
                }
              }
            }

            Toast.makeText(FriendsRemoveActivity.this, "Friend deleted", Toast.LENGTH_SHORT).show();
          } else if (groupsRef instanceof FirebaseServerClient) {
            // Server mode
            FirebaseServerClient serverClient = (FirebaseServerClient) groupsRef;
            serverClient.getGroup(
                GroupKey,
                new FirebaseServerClient.DataCallback<com.example.partymaker.data.model.Group>() {
                  @Override
                  public void onSuccess(com.example.partymaker.data.model.Group group) {
                    if (group != null) {
                      // Remove from FriendKeys
                      HashMap<String, Object> updatedFriendKeys = new HashMap<>();
                      if (group.getFriendKeys() != null) {
                        HashMap<String, Object> friendKeys = group.getFriendKeys();
                        for (Map.Entry<String, Object> entry : friendKeys.entrySet()) {
                          if (!entry.getValue().equals(friendKey)) {
                            updatedFriendKeys.put(entry.getKey(), entry.getValue());
                          }
                        }
                      }

                      // Remove from ComingKeys if present
                      HashMap<String, Object> updatedComingKeys = new HashMap<>();
                      if (group.getComingKeys() != null) {
                        HashMap<String, Object> comingKeys = group.getComingKeys();
                        for (Map.Entry<String, Object> entry : comingKeys.entrySet()) {
                          if (!entry.getValue().equals(friendKey)) {
                            updatedComingKeys.put(entry.getKey(), entry.getValue());
                          }
                        }
                      }

                      // Update the group
                      group.setFriendKeys(updatedFriendKeys);
                      group.setComingKeys(updatedComingKeys);

                      serverClient.updateGroup(
                          GroupKey,
                          "FriendKeys",
                          updatedFriendKeys,
                          new FirebaseServerClient.DataCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                              serverClient.updateGroup(
                                  GroupKey,
                                  "ComingKeys",
                                  updatedComingKeys,
                                  new FirebaseServerClient.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                      Toast.makeText(
                                              FriendsRemoveActivity.this,
                                              "Friend deleted",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                      Log.e(TAG, "Error updating ComingKeys: " + errorMessage);
                                      Toast.makeText(
                                              FriendsRemoveActivity.this,
                                              "Failed to remove friend completely",
                                              Toast.LENGTH_SHORT)
                                          .show();
                                    }
                                  });
                            }

                            @Override
                            public void onError(String errorMessage) {
                              Log.e(TAG, "Error updating FriendKeys: " + errorMessage);
                              Toast.makeText(
                                      FriendsRemoveActivity.this,
                                      "Failed to remove friend",
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }
                          });
                    }
                  }

                  @Override
                  public void onError(String errorMessage) {
                    Log.e(TAG, "Error accessing group data: " + errorMessage);
                    Toast.makeText(
                            FriendsRemoveActivity.this,
                            "Failed to remove friend",
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          }
          break;
        }
      }
    }
    if (!flag) {
      Toast.makeText(FriendsRemoveActivity.this, "Friend not found", Toast.LENGTH_SHORT).show();
    } else if (!flag1) {
      Toast.makeText(FriendsRemoveActivity.this, "Not in group", Toast.LENGTH_SHORT).show();
    }
  }

  private void processServerUserData(Map<String, User> userData) {
    boolean flag = false;
    boolean flag1 = false;
    String friendKey = null;

    for (Map.Entry<String, User> entry : userData.entrySet()) {
      String userEmail = entry.getValue().getEmail().replace('.', ' ');
      if (userEmail.equals(CurrentFriend)) {
        flag = true;
        friendKey = entry.getKey();

        if (FriendKeys != null && FriendKeys.containsValue(friendKey)) {
          flag1 = true;

          if (groupsRef instanceof DatabaseReference) {
            // Direct Firebase access
            DatabaseReference groupRef = ((DatabaseReference) groupsRef).child(GroupKey);

            // Remove from FriendKeys
            if (FriendKeys != null) {
              for (Map.Entry<String, Object> fEntry : FriendKeys.entrySet()) {
                if (fEntry.getValue().equals(friendKey)) {
                  groupRef.child("FriendKeys").child(fEntry.getKey()).removeValue();
                  FriendKeys.remove(fEntry.getKey());
                  break;
                }
              }
            }

            // Remove from ComingKeys if present
            if (ComingKeys != null) {
              for (Map.Entry<String, Object> cEntry : ComingKeys.entrySet()) {
                if (cEntry.getValue().equals(friendKey)) {
                  groupRef.child("ComingKeys").child(cEntry.getKey()).removeValue();
                  ComingKeys.remove(cEntry.getKey());
                  break;
                }
              }
            }

            Toast.makeText(FriendsRemoveActivity.this, "Friend deleted", Toast.LENGTH_SHORT).show();
          } else if (groupsRef instanceof FirebaseServerClient) {
            // Server mode
            final String finalFriendKey = friendKey;
            FirebaseServerClient serverClient = (FirebaseServerClient) groupsRef;
            serverClient.getGroup(
                GroupKey,
                new FirebaseServerClient.DataCallback<com.example.partymaker.data.model.Group>() {
                  @Override
                  public void onSuccess(com.example.partymaker.data.model.Group group) {
                    if (group != null) {
                      // Remove from FriendKeys
                      HashMap<String, Object> updatedFriendKeys = new HashMap<>();
                      if (group.getFriendKeys() != null) {
                        HashMap<String, Object> friendKeys = group.getFriendKeys();
                        for (Map.Entry<String, Object> entry : friendKeys.entrySet()) {
                          if (!entry.getValue().equals(finalFriendKey)) {
                            updatedFriendKeys.put(entry.getKey(), entry.getValue());
                          }
                        }
                      }

                      // Remove from ComingKeys if present
                      HashMap<String, Object> updatedComingKeys = new HashMap<>();
                      if (group.getComingKeys() != null) {
                        HashMap<String, Object> comingKeys = group.getComingKeys();
                        for (Map.Entry<String, Object> entry : comingKeys.entrySet()) {
                          if (!entry.getValue().equals(finalFriendKey)) {
                            updatedComingKeys.put(entry.getKey(), entry.getValue());
                          }
                        }
                      }

                      // Update the group
                      Map<String, Object> updates = new HashMap<>();
                      updates.put("FriendKeys", updatedFriendKeys);
                      updates.put("ComingKeys", updatedComingKeys);

                      serverClient.updateGroup(
                          GroupKey,
                          updates,
                          new FirebaseServerClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                              Toast.makeText(
                                      FriendsRemoveActivity.this,
                                      "Friend deleted",
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }

                            @Override
                            public void onError(String errorMessage) {
                              Log.e(TAG, "Error removing friend: " + errorMessage);
                              Toast.makeText(
                                      FriendsRemoveActivity.this,
                                      "Failed to remove friend",
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }
                          });
                    }
                  }

                  @Override
                  public void onError(String errorMessage) {
                    Log.e(TAG, "Error accessing group data: " + errorMessage);
                    Toast.makeText(
                            FriendsRemoveActivity.this,
                            "Failed to remove friend",
                            Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          }
          break;
        }
      }
    }

    if (!flag) {
      Toast.makeText(FriendsRemoveActivity.this, "Friend not found", Toast.LENGTH_SHORT).show();
    } else if (!flag1) {
      Toast.makeText(FriendsRemoveActivity.this, "Not in group", Toast.LENGTH_SHORT).show();
    }
  }

  private void showViews(View... views) {
    for (View v : views) {
      v.setVisibility(View.VISIBLE);
    }
  }

  private void hideViews(View... views) {
    for (View v : views) {
      v.setVisibility(View.INVISIBLE);
    }
  }

  private void loadFriends() {
    if (usersRef instanceof DatabaseReference) {
      // Direct Firebase access
      DatabaseReference groupRef = ((DatabaseReference) groupsRef).child(groupID);
      groupRef
          .child("FriendKeys")
          .addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                  usersList.clear();
                  userKeys.clear();
                  for (DataSnapshot friendKeySnapshot : dataSnapshot.getChildren()) {
                    String friendKey = friendKeySnapshot.getValue(String.class);
                    if (friendKey != null) {
                      ((DatabaseReference) usersRef)
                          .child(friendKey)
                          .addListenerForSingleValueEvent(
                              new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                  processUserData(dataSnapshot, friendKey);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                  Log.e(
                                      TAG, "Error loading user data", databaseError.toException());
                                  Toast.makeText(
                                          FriendsRemoveActivity.this,
                                          "Failed to load user data",
                                          Toast.LENGTH_SHORT)
                                      .show();
                                }
                              });
                    }
                  }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                  Log.e(TAG, "Error loading friends", databaseError.toException());
                  Toast.makeText(
                          FriendsRemoveActivity.this,
                          "Failed to load friends list",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              });
    } else if (usersRef instanceof FirebaseServerClient) {
      // Server mode
      FirebaseServerClient serverClient = (FirebaseServerClient) usersRef;
      serverClient.getGroup(
          groupID,
          new FirebaseServerClient.DataCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
              if (group != null && group.getFriendKeys() != null) {
                usersList.clear();
                userKeys.clear();
                HashMap<String, Object> friendKeys = group.getFriendKeys();
                for (Map.Entry<String, Object> entry : friendKeys.entrySet()) {
                  String friendKey = (String) entry.getValue();
                  serverClient.getUser(
                      friendKey,
                      new FirebaseServerClient.DataCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                          processServerUserData(user, friendKey);
                        }

                        @Override
                        public void onError(String errorMessage) {
                          Log.e(TAG, "Error loading user data: " + errorMessage);
                          Toast.makeText(
                                  FriendsRemoveActivity.this,
                                  "Failed to load user data",
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                      });
                }
              }
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Error loading group: " + errorMessage);
              Toast.makeText(
                      FriendsRemoveActivity.this, "Failed to load friends list", Toast.LENGTH_SHORT)
                  .show();
            }
          });
    }
  }

  private void processUserData(DataSnapshot dataSnapshot, String userKey) {
    User user = dataSnapshot.getValue(User.class);
    if (user != null) {
      usersList.add(user);
      userKeys.add(userKey);
      adapter.notifyDataSetChanged();
    }
  }

  private void processServerUserData(User user, String userKey) {
    if (user != null) {
      usersList.add(user);
      userKeys.add(userKey);
      adapter.notifyDataSetChanged();
    }
  }

  private void removeFriend(String friendKey) {
    if (groupsRef instanceof DatabaseReference) {
      // Direct Firebase access
      DatabaseReference groupRef = ((DatabaseReference) groupsRef).child(groupID);

      groupRef.addListenerForSingleValueEvent(
          new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              Group group = dataSnapshot.getValue(Group.class);
              if (group != null) {
                // Remove from FriendKeys
                HashMap<String, Object> updatedFriendKeys = new HashMap<>();
                if (group.getFriendKeys() != null) {
                  HashMap<String, Object> friendKeys = group.getFriendKeys();
                  for (Map.Entry<String, Object> entry : friendKeys.entrySet()) {
                    if (!entry.getValue().equals(friendKey)) {
                      updatedFriendKeys.put(entry.getKey(), entry.getValue());
                    }
                  }
                  group.setFriendKeys(updatedFriendKeys);
                }

                // Remove from ComingKeys if present
                HashMap<String, Object> updatedComingKeys = new HashMap<>();
                if (group.getComingKeys() != null) {
                  HashMap<String, Object> comingKeys = group.getComingKeys();
                  for (Map.Entry<String, Object> entry : comingKeys.entrySet()) {
                    if (!entry.getValue().equals(friendKey)) {
                      updatedComingKeys.put(entry.getKey(), entry.getValue());
                    }
                  }
                  group.setComingKeys(updatedComingKeys);
                }

                // Update the group
                groupRef
                    .setValue(group)
                    .addOnSuccessListener(
                        aVoid -> {
                          Toast.makeText(
                                  FriendsRemoveActivity.this,
                                  "Friend removed successfully",
                                  Toast.LENGTH_SHORT)
                              .show();
                          loadFriends(); // Reload the list
                        })
                    .addOnFailureListener(
                        e -> {
                          Log.e(TAG, "Error removing friend", e);
                          Toast.makeText(
                                  FriendsRemoveActivity.this,
                                  "Failed to remove friend",
                                  Toast.LENGTH_SHORT)
                              .show();
                        });
              }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
              Log.e(TAG, "Error accessing group data", databaseError.toException());
              Toast.makeText(
                      FriendsRemoveActivity.this, "Failed to remove friend", Toast.LENGTH_SHORT)
                  .show();
            }
          });
    } else if (groupsRef instanceof FirebaseServerClient) {
      // Server mode
      FirebaseServerClient serverClient = (FirebaseServerClient) groupsRef;
      serverClient.getGroup(
          groupID,
          new FirebaseServerClient.DataCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
              if (group != null) {
                // Remove from FriendKeys
                HashMap<String, Object> updatedFriendKeys = new HashMap<>();
                if (group.getFriendKeys() != null) {
                  HashMap<String, Object> friendKeys = group.getFriendKeys();
                  for (Map.Entry<String, Object> entry : friendKeys.entrySet()) {
                    if (!entry.getValue().equals(friendKey)) {
                      updatedFriendKeys.put(entry.getKey(), entry.getValue());
                    }
                  }
                }

                // Remove from ComingKeys if present
                HashMap<String, Object> updatedComingKeys = new HashMap<>();
                if (group.getComingKeys() != null) {
                  HashMap<String, Object> comingKeys = group.getComingKeys();
                  for (Map.Entry<String, Object> entry : comingKeys.entrySet()) {
                    if (!entry.getValue().equals(friendKey)) {
                      updatedComingKeys.put(entry.getKey(), entry.getValue());
                    }
                  }
                }

                // Update the group
                Map<String, Object> updates = new HashMap<>();
                updates.put("FriendKeys", updatedFriendKeys);
                updates.put("ComingKeys", updatedComingKeys);

                serverClient.updateGroup(
                    groupID,
                    updates,
                    new FirebaseServerClient.OperationCallback() {
                      @Override
                      public void onSuccess() {
                        Toast.makeText(
                                FriendsRemoveActivity.this,
                                "Friend removed successfully",
                                Toast.LENGTH_SHORT)
                            .show();
                        loadFriends(); // Reload the list
                      }

                      @Override
                      public void onError(String errorMessage) {
                        Log.e(TAG, "Error removing friend: " + errorMessage);
                        Toast.makeText(
                                FriendsRemoveActivity.this,
                                "Failed to remove friend",
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    });
              }
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Error accessing group data: " + errorMessage);
              Toast.makeText(
                      FriendsRemoveActivity.this, "Failed to remove friend", Toast.LENGTH_SHORT)
                  .show();
            }
          });
    }
  }
}
