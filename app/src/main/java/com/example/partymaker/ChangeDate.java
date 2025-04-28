package com.example.partymaker;

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

import java.io.Serializable;
import java.util.HashMap;

public class ChangeDate extends AppCompatActivity {
    private Button btnHide, btnHelp, btnChangeDate;
    private TextView tvHide, tvHelp, tvInstructions1;
    private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
    private String GroupKey, GroupName, GroupDay, GroupMonth, GroupYear, GroupHour, GroupLocation, AdminKey, CreatedAt, GroupPrice;
    private int GroupType;
    private boolean CanAdd;
    private Spinner spnDays, spnMonths, spnYears , spnHours;
    private ArrayAdapter<CharSequence> DaysAdapter, MonthsAdapter, YearsAdapter, HoursAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_date);

        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Get Values from MainActivity By intent + connection between intent and current activity objects
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
        btnChangeDate = (Button) findViewById(R.id.btnChangeDate);
        btnHide = (Button) findViewById(R.id.btnHide2);
        btnHelp = (Button) findViewById(R.id.btnHelp2);
        tvHide = (TextView) findViewById(R.id.tvHide2);
        tvHelp = (TextView) findViewById(R.id.tvHelp2);
        tvInstructions1 = (TextView) findViewById(R.id.tvInstructions2);
        spnDays = (Spinner) findViewById(R.id.spnDayDate);
        spnMonths = (Spinner) findViewById(R.id.spnMonthDate);
        spnYears = (Spinner) findViewById(R.id.spnYearDate);
        spnHours = (Spinner) findViewById(R.id.spnHourDate);
        //spinner adapter for days
        DaysAdapter = ArrayAdapter.createFromResource(this, R.array.array_days, android.R.layout.simple_spinner_item);
        DaysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner adapter for months
        MonthsAdapter = ArrayAdapter.createFromResource(this, R.array.array_months, android.R.layout.simple_spinner_item);
        MonthsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner adapter for years
        YearsAdapter = ArrayAdapter.createFromResource(this, R.array.array_years, android.R.layout.simple_spinner_item);
        YearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner adapter for hours
        HoursAdapter = ArrayAdapter.createFromResource(this, R.array.array_hours, android.R.layout.simple_spinner_item);
        HoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set adapter for each spinner
        spnDays.setAdapter(DaysAdapter);
        spnMonths.setAdapter(MonthsAdapter);
        spnYears.setAdapter(YearsAdapter);
        spnHours.setAdapter(HoursAdapter);

        evantHandler();
    }

    private void evantHandler() {
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvInstructions1.setVisibility(View.VISIBLE);
                btnHelp.setVisibility(View.INVISIBLE);
                tvHelp.setVisibility(View.INVISIBLE);
                btnHide.setVisibility(View.VISIBLE);
                tvHide.setVisibility(View.VISIBLE);
            }
        });

        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvInstructions1.setVisibility(View.INVISIBLE);
                btnHide.setVisibility(View.INVISIBLE);
                tvHide.setVisibility(View.INVISIBLE);
                btnHelp.setVisibility(View.VISIBLE);
                tvHelp.setVisibility(View.VISIBLE);
            }
        });
        btnChangeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set new value in firebase
                DBref.refGroups.child(GroupKey).child("groupDays").setValue(GroupDay);
                DBref.refGroups.child(GroupKey).child("groupMonths").setValue(GroupMonth);
                DBref.refGroups.child(GroupKey).child("groupYears").setValue(GroupYear);
                DBref.refGroups.child(GroupKey).child("groupHours").setValue(GroupHour);

                //Type Successfully Changes
                Toast.makeText(ChangeDate.this, "Successfully Changed", Toast.LENGTH_SHORT).show();

                //intent from ChangeDate to AdminOptions
                Intent intent = new Intent(getBaseContext(), AdminOptions.class);
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
        });
        spnDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GroupDay = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GroupMonth = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnYears.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GroupYear = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GroupHour = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
