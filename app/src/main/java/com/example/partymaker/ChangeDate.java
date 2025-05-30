package com.example.partymaker;

import static com.example.partymaker.utilities.Constants.ADMIN_KEY;
import static com.example.partymaker.utilities.Constants.CAN_ADD;
import static com.example.partymaker.utilities.Constants.COMING_KEYS;
import static com.example.partymaker.utilities.Constants.CREATED_AT;
import static com.example.partymaker.utilities.Constants.DEFAULT_KEY;
import static com.example.partymaker.utilities.Constants.FRIEND_KEYS;
import static com.example.partymaker.utilities.Constants.GROUP_DAYS;
import static com.example.partymaker.utilities.Constants.GROUP_HOURS;
import static com.example.partymaker.utilities.Constants.GROUP_KEY;
import static com.example.partymaker.utilities.Constants.GROUP_LOCATION;
import static com.example.partymaker.utilities.Constants.GROUP_MONTHS;
import static com.example.partymaker.utilities.Constants.GROUP_NAME;
import static com.example.partymaker.utilities.Constants.GROUP_PRICE;
import static com.example.partymaker.utilities.Constants.GROUP_TYPE;
import static com.example.partymaker.utilities.Constants.GROUP_YEARS;
import static com.example.partymaker.utilities.Constants.MESSAGE_KEYS;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.data.DBref;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.HashMap;
import java.util.Objects;

public class ChangeDate extends AppCompatActivity {
  private Button btnHide, btnHelp, btnChangeDate;
  private TextView tvHide, tvHelp, tvInstructions1;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private String GroupKey,
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
  private boolean CanAdd;
  private Spinner spnDays, spnMonths, spnYears, spnHours;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_change_date);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.hide();

    // Get Values from MainActivity By intent + connection between intent and
    // current activity objects
    GroupName = Objects.requireNonNull(getIntent().getExtras()).getString(GROUP_NAME, DEFAULT_KEY);
    GroupKey = getIntent().getExtras().getString(GROUP_KEY, DEFAULT_KEY);
    GroupDay = getIntent().getExtras().getString(GROUP_DAYS, DEFAULT_KEY);
    GroupMonth = getIntent().getExtras().getString(GROUP_MONTHS, DEFAULT_KEY);
    GroupYear = getIntent().getExtras().getString(GROUP_YEARS, DEFAULT_KEY);
    GroupHour = getIntent().getExtras().getString(GROUP_HOURS, DEFAULT_KEY);
    GroupLocation = getIntent().getExtras().getString(GROUP_LOCATION, DEFAULT_KEY);
    AdminKey = getIntent().getExtras().getString(ADMIN_KEY, DEFAULT_KEY);
    CreatedAt = getIntent().getExtras().getString(CREATED_AT, DEFAULT_KEY);
    GroupType =
        getIntent().getExtras().getInt(GROUP_TYPE); // if 0 so Public group if 1 so Private group
    GroupPrice = getIntent().getExtras().getString(GROUP_PRICE);
    CanAdd = getIntent().getExtras().getBoolean(CAN_ADD);
    FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra(FRIEND_KEYS);
    ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra(COMING_KEYS);
    MessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra(MESSAGE_KEYS);

    // connection
    btnChangeDate = findViewById(R.id.btnChangeDate);
    btnHide = findViewById(R.id.btnHide2);
    btnHelp = findViewById(R.id.btnHelp2);
    tvHide = findViewById(R.id.tvHide2);
    tvHelp = findViewById(R.id.tvHelp2);
    tvInstructions1 = findViewById(R.id.tvInstructions2);
    spnDays = findViewById(R.id.spnDayDate);
    spnMonths = findViewById(R.id.spnMonthDate);
    spnYears = findViewById(R.id.spnYearDate);
    spnHours = findViewById(R.id.spnHourDate);
    // spinner adapter for days
    ArrayAdapter<CharSequence> daysAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_days, android.R.layout.simple_spinner_item);
    daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // spinner adapter for months
    ArrayAdapter<CharSequence> monthsAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_months, android.R.layout.simple_spinner_item);
    monthsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // spinner adapter for years
    ArrayAdapter<CharSequence> yearsAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_years, android.R.layout.simple_spinner_item);
    yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // spinner adapter for hours
    ArrayAdapter<CharSequence> hoursAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_hours, android.R.layout.simple_spinner_item);
    hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // set adapter for each spinner
    spnDays.setAdapter(daysAdapter);
    spnMonths.setAdapter(monthsAdapter);
    spnYears.setAdapter(yearsAdapter);
    spnHours.setAdapter(hoursAdapter);

    evantHandler();
  }

  private void evantHandler() {
    btnHelp.setOnClickListener(
        v -> {
          tvInstructions1.setVisibility(View.VISIBLE);
          btnHelp.setVisibility(View.INVISIBLE);
          tvHelp.setVisibility(View.INVISIBLE);
          btnHide.setVisibility(View.VISIBLE);
          tvHide.setVisibility(View.VISIBLE);
        });

    btnHide.setOnClickListener(
        v -> {
          tvInstructions1.setVisibility(View.INVISIBLE);
          btnHide.setVisibility(View.INVISIBLE);
          tvHide.setVisibility(View.INVISIBLE);
          btnHelp.setVisibility(View.VISIBLE);
          tvHelp.setVisibility(View.VISIBLE);
        });
    btnChangeDate.setOnClickListener(
        v -> {
          // set new value in firebase
          DBref.refGroups.child(GroupKey).child("groupDays").setValue(GroupDay);
          DBref.refGroups.child(GroupKey).child("groupMonths").setValue(GroupMonth);
          DBref.refGroups.child(GroupKey).child("groupYears").setValue(GroupYear);
          DBref.refGroups.child(GroupKey).child("groupHours").setValue(GroupHour);

          // Type Successfully Changes
          Toast.makeText(ChangeDate.this, "Successfully Changed", Toast.LENGTH_SHORT).show();

          // intent from ChangeDate to AdminOptions
          Intent intent = new Intent(getBaseContext(), AdminOptions.class);
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
    spnDays.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupDay = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    spnMonths.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupMonth = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    spnYears.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupYear = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    spnHours.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupHour = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
  }
}
