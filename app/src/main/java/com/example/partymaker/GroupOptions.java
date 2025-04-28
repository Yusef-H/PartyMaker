package com.example.partymaker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.example.partymaker.data.ChatMessage;
import com.example.partymaker.data.DBref;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.HashMap;

public class GroupOptions extends AppCompatActivity {
    private GridLayout GroupGrid;
    private ImageView imgCanAdd, imgType, imgEditGroup;
    private ImageButton btnEditName;
    private String AdminKey, GroupKey, GroupName, GroupDay, GroupMonth, GroupYear, GroupHour, GroupLocation, CreatedAt,GroupPrice;
    private TextView tvNameGroup, tvCanAdd, tvType;
    private int GroupType;
    private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
    boolean CanAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_options);

        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Get Values from GroupScreen By intent + connection between intent and current activity objects
        GroupName = getIntent().getExtras().getString("GroupName", "defaultKey");
        GroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
        GroupDay = getIntent().getExtras().getString("groupDays", "defaultKey");
        GroupMonth = getIntent().getExtras().getString("groupMonths", "defaultKey");
        GroupYear = getIntent().getExtras().getString("groupYears", "defaultKey");
        GroupHour = getIntent().getExtras().getString("groupHours", "defaultKey");
        GroupLocation = getIntent().getExtras().getString("groupLocation", "defaultKey");
        AdminKey = getIntent().getExtras().getString("adminKey", "defaultKey");
        CreatedAt = getIntent().getExtras().getString("createdAt", "defaultKey");
        GroupType = getIntent().getExtras().getInt("GroupType"); //if 0 so Public group if 1 so Private group
        GroupPrice = getIntent().getExtras().getString("GroupPrice");
        CanAdd = getIntent().getExtras().getBoolean("CanAdd");
        FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
        ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
        MessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");

        //connection
        GroupGrid = (GridLayout) findViewById(R.id.GroupGrid);
        tvCanAdd = (TextView) findViewById(R.id.tvCanAdd);
        tvType = (TextView) findViewById(R.id.tvType);
        tvNameGroup = (TextView) findViewById(R.id.tvNameGroup);
        imgCanAdd = (ImageView) findViewById(R.id.imgCanAdd);
        imgType = (ImageView) findViewById(R.id.imgType);
        imgEditGroup = (ImageView) findViewById(R.id.imgEditGroup);
        btnEditName = (ImageButton) findViewById(R.id.btnEditName1);

        //group's Picture options
        DBref.refStorage.child("Groups/" + GroupKey).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imgEditGroup);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

        //set Group Name
        tvNameGroup.setText(GroupName);

        //set 1st button if group is private or if group is public
        if (GroupType == 0) {                   //if group is public
            imgType.setImageResource(R.drawable.everyone);
            tvType.setText("Public Group");
        } else {                             //if group is private
            imgType.setImageResource(R.drawable.lock);
            tvType.setText("Private Group");
        }

        //set 3rd button if all can add it shows that all can if if only admin can add it shows that only admin can add
        if (CanAdd == true) {
            imgCanAdd.setImageResource(R.drawable.everyone);
            tvCanAdd.setText("Everyone Add");
        } else {
            imgCanAdd.setImageResource(R.drawable.one);
            tvCanAdd.setText("Admin Add");
        }

        //call GroupOptions
        Options(GroupGrid);

        eventHandler();
    }

    private void eventHandler() {
        //Profile picture on click to change it
        imgEditGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), 100);

            }
        });
        btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edittext = new EditText(GroupOptions.this);
                AlertDialog.Builder alert = new AlertDialog.Builder(GroupOptions.this);
                alert.setMessage("Input new name below");
                alert.setTitle("Change party's name");

                alert.setView(edittext);

                alert.setPositiveButton("Change name", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //if pressed changed name
                        GroupName = edittext.getText().toString();
                        DBref.refGroups.child(GroupKey).child("groupName").setValue(GroupName);
                        tvNameGroup.setText(GroupName);
                        Toast.makeText(GroupOptions.this, "Name Changed", Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with Back.
                    }
                });

                alert.show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                Uri uri = data.getData();
                if (null != uri) {
                    ((ImageView) findViewById(R.id.imgEditGroup)).setImageURI(uri);

                    DBref.refStorage.child("Groups/" + GroupKey).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(GroupOptions.this, "saved", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(GroupOptions.this, "error while saving ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void Options(GridLayout GroupGrid) {
        //Loop all child item of Main Grid
        for (int i = 0; i < GroupGrid.getChildCount(); i++) {
            //You can see , all child item is CardView , so we just cast object to CardView
            final CardView cardView = (CardView) GroupGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (finalI == 0) //open 1,1 (1) Change GroupType to Public/Private
                    {
                        if (GroupType == 0) {
                            imgType.setImageResource(R.drawable.lock);
                            tvType.setText("Private Group");
                            GroupType = 1;
                            DBref.refGroups.child(GroupKey).child("groupType").setValue(1);
                        } else {
                            imgType.setImageResource(R.drawable.everyone);
                            tvType.setText("Public Group");
                            GroupType = 0;
                            DBref.refGroups.child(GroupKey).child("groupType").setValue(0);
                        }
                    } else if (finalI == 1) //open 1,2 (2) set if anyone can add/only admin can add
                    {

                        if (CanAdd == true) {
                            imgCanAdd.setImageResource(R.drawable.one);
                            tvCanAdd.setText("Only Admin");
                            CanAdd = false;
                            DBref.refGroups.child(GroupKey).child("canAdd").setValue(false);
                        } else {
                            imgCanAdd.setImageResource(R.drawable.everyone);
                            tvCanAdd.setText("Everyone Add");
                            CanAdd = true;
                            DBref.refGroups.child(GroupKey).child("canAdd").setValue(true);
                        }

                    } else if (finalI == 2) //open 2,1 (3) Delete Group
                    {
                        //delete all messages writen by current group
                        deleteMessages();

                        //delete group from database
                        DBref.refGroups.child(GroupKey).removeValue();

                        //delete group's picture
                        DBref.refStorage.child("Groups/" + GroupKey).delete();

                        //if it went successfully so toast write it
                        Toast.makeText(GroupOptions.this, "successfully deleted", Toast.LENGTH_SHORT).show();

                        //intent from GroupScreen to MainMenu
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(intent);
                    } else if (finalI == 3) //open 2,2 (4) Back
                    {
                        //intent to AdminOptions Activity with Values
                        Intent intent = new Intent(getBaseContext(),AdminOptions.class);
                        intent.putExtra("GroupName", GroupName);
                        intent.putExtra("groupKey", GroupKey);
                        intent.putExtra("groupDays", GroupDay);
                        intent.putExtra("groupMonths", GroupMonth);
                        intent.putExtra("groupYears", GroupYear);
                        intent.putExtra("groupHours", GroupHour);
                        intent.putExtra("groupLocation", GroupLocation);
                        intent.putExtra("adminKey", AdminKey);
                        intent.putExtra("createdAt", CreatedAt);
                        intent.putExtra("GroupType", GroupType);
                        intent.putExtra("GroupPrice", GroupPrice);
                        intent.putExtra("CanAdd", CanAdd);
                        intent.putExtra("FriendKeys", (Serializable) FriendKeys);
                        intent.putExtra("ComingKeys", (Serializable) ComingKeys);
                        intent.putExtra("MessageKeys", (Serializable) MessageKeys);
                        startActivity(intent);
                    }

                }
            });
        }
    }

    private void deleteMessages() {
        DBref.refMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String msgKey = data.getValue(ChatMessage.class).getMessageKey();
                    for (String chatMsgKey : MessageKeys.keySet()) {
                        if (chatMsgKey.equals(msgKey)) {
                            DBref.refMessages.child(chatMsgKey).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
