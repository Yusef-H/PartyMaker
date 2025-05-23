package com.example.partymaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.data.DBref;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.HashMap;

public class AdminOptions extends AppCompatActivity {
  private GridLayout MyGrid;
  private String AdminKey,
      GroupKey,
      GroupName,
      GroupDay,
      GroupMonth,
      GroupYear,
      GroupHour,
      GroupLocation,
      CreatedAt,
      GroupPrice;
  private TextView tvAdminEmail;
  private int GroupType;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private boolean CanAdd;
  private CardView CardPrice, CardLocation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin_options);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    actionBar.hide();

    // Get Values from GroupScreen By intent + connection between intent and current
    // activity objects
    GroupName = getIntent().getExtras().getString("GroupName", "defaultKey");
    GroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
    GroupDay = getIntent().getExtras().getString("groupDays", "defaultKey");
    GroupMonth = getIntent().getExtras().getString("groupMonths", "defaultKey");
    GroupYear = getIntent().getExtras().getString("groupYears", "defaultKey");
    GroupHour = getIntent().getExtras().getString("groupHours", "defaultKey");
    GroupLocation = getIntent().getExtras().getString("groupLocation", "defaultKey");
    AdminKey = getIntent().getExtras().getString("adminKey", "defaultKey");
    CreatedAt = getIntent().getExtras().getString("createdAt", "defaultKey");
    GroupType =
        getIntent().getExtras().getInt("GroupType"); // if 0 so Public group if 1 so Private group
    GroupPrice = getIntent().getExtras().getString("GroupPrice");
    CanAdd = getIntent().getExtras().getBoolean("CanAdd");
    FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
    ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
    MessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");

    // connection between XML and AdminOptions
    MyGrid = (GridLayout) findViewById(R.id.MyGrid);
    tvAdminEmail = (TextView) findViewById(R.id.tvAdminEmail);
    CardPrice = (CardView) findViewById(R.id.CardPrice);
    CardLocation = (CardView) findViewById(R.id.CardLocation);

    // settings + things to see when activity starts
    tvAdminEmail.setText(AdminKey.replace(' ', '.'));

    // start AdminOptions
    AdminOptions(MyGrid);

    eventHandler();
  }

  private void eventHandler() {
    CardPrice.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            final EditText edittext = new EditText(AdminOptions.this);
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            edittext.setText(GroupPrice);
            AlertDialog.Builder alert = new AlertDialog.Builder(AdminOptions.this);
            alert.setMessage("Input new price below");
            alert.setTitle("Change party's entry price");

            alert.setView(edittext);

            alert.setPositiveButton(
                "Change price",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    // if pressed changed name
                    GroupPrice = edittext.getText().toString();
                    DBref.refGroups.child(GroupKey).child("groupPrice").setValue(GroupPrice);
                    Toast.makeText(AdminOptions.this, "Name Changed", Toast.LENGTH_SHORT).show();
                  }
                });

            alert.setNegativeButton(
                "Back",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with Back.
                  }
                });

            alert.show();
          }
        });

    CardLocation.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            final EditText edittext = new EditText(AdminOptions.this);
            edittext.setText(GroupLocation);
            AlertDialog.Builder alert = new AlertDialog.Builder(AdminOptions.this);
            alert.setMessage("Input new location below");
            alert.setTitle("Change party's location");

            alert.setView(edittext);

            alert.setPositiveButton(
                "Change location",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    // if pressed changed name
                    GroupLocation = edittext.getText().toString();
                    DBref.refGroups.child(GroupKey).child("groupLocation").setValue(GroupLocation);
                    Toast.makeText(AdminOptions.this, "Location Changed", Toast.LENGTH_SHORT)
                        .show();
                  }
                });

            alert.setNegativeButton(
                "Back",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with Back.
                  }
                });

            alert.show();
          }
        });
  }

  private void AdminOptions(GridLayout MyGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < MyGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) MyGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (finalI == 0) // open 1,1 (1) Change Location
              {
              } else if (finalI == 1) // open 1,2 (2) Change Date
              {
                // intent from AdminOptions to ChangeDate
                Intent intent = new Intent(getBaseContext(), ChangeDate.class);
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

              } else if (finalI == 2) // open 2,1 (3) Delete People
              {
                Intent intent = new Intent(getBaseContext(), DeletePeople.class);
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
                ;
                startActivity(intent);
              } else if (finalI == 3) // open 2,2 (4) Change Entry Price
              {
              } else if (finalI == 4) // open 3,1 (5) Group Options
              {
                // intent to GroupOptions Activity with Values
                Intent intent = new Intent(getBaseContext(), GroupOptions.class);
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

              } else if (finalI == 5) // open 3,2 (6) Back
              {
                // intent back to GroupScreen Activity with Values
                Intent intent = new Intent(getBaseContext(), GroupScreen.class);
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
              }
            }
          });
    }
  }
}
