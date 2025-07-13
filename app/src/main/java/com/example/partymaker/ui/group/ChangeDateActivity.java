package com.example.partymaker.ui.group;

import static com.example.partymaker.utilities.Common.hideViews;
import static com.example.partymaker.utilities.Common.showViews;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.example.partymaker.utilities.AuthHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class ChangeDateActivity extends AppCompatActivity {
  private static final String TAG = "ChangeDateActivity";
  private Button btnHide, btnHelp, btnChangeDate, btnSelectDate, btnSelectTime;
  private TextView tvHide, tvHelp, tvInstructions1, tvSelectedDate, tvSelectedTime;
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
      GroupPrice,
      UserKey; // Add UserKey for admin verification
  private int GroupType;
  private boolean CanAdd;
  private Calendar selectedDate;
  private boolean isAdminVerified = false; // Track admin verification status

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_change_date);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.hide();

    // Initialize calendar with current date
    selectedDate = Calendar.getInstance();

    // Get current user key for admin verification
    try {
      UserKey = AuthHelper.getCurrentUserKey(this);
      if (UserKey == null) {
        Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
        finish();
        return;
      }
    } catch (Exception e) {
      Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    // Get Values from MainActivity By intent + connection between intent and
    // current activity objects
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
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

    // Verify admin status before allowing access
    verifyAdminStatus();
  }

  private void verifyAdminStatus() {
    // Show loading message
    Toast.makeText(this, "Verifying admin permissions...", Toast.LENGTH_SHORT).show();

    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<Group>() {
          @Override
          public void onSuccess(Group group) {
            if (group != null
                && group.getAdminKey() != null
                && group.getAdminKey().equals(UserKey)) {
              // User is verified as admin
              isAdminVerified = true;
              // Update local admin key to match server data
              AdminKey = group.getAdminKey();
              // Initialize UI after verification
              initializeUI();
            } else {
              // User is not admin - deny access
              Toast.makeText(
                      ChangeDateActivity.this,
                      "Access denied. Only group admin can change party dates.",
                      Toast.LENGTH_LONG)
                  .show();
              finish();
            }
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    ChangeDateActivity.this,
                    "Failed to verify admin status: " + errorMessage,
                    Toast.LENGTH_LONG)
                .show();
            finish();
          }
        });
  }

  private void initializeUI() {
    // Initialize current date from group data
    initializeDateFromGroupData();

    // connection
    btnChangeDate = findViewById(R.id.btnChangeDate);
    btnSelectDate = findViewById(R.id.btnSelectDate);
    btnSelectTime = findViewById(R.id.btnSelectTime);
    btnHide = findViewById(R.id.btnHide2);
    btnHelp = findViewById(R.id.btnHelp2);
    tvHide = findViewById(R.id.tvHide2);
    tvHelp = findViewById(R.id.tvHelp2);
    tvInstructions1 = findViewById(R.id.tvInstructions2);
    tvSelectedDate = findViewById(R.id.tvSelectedDate);
    tvSelectedTime = findViewById(R.id.tvSelectedTime);

    // Update UI with current values
    updateDateTimeDisplay();

    eventHandler();
  }

  private void initializeDateFromGroupData() {
    try {
      // Parse existing date values
      int day = Integer.parseInt(GroupDay);
      int year = Integer.parseInt(GroupYear);

      // Handle month - could be name or number
      int month;
      if (GroupMonth.matches("\\d+")) {
        month = Integer.parseInt(GroupMonth) - 1; // Calendar months are 0-based
      } else {
        month = getMonthNumber(GroupMonth) - 1;
      }

      selectedDate.set(year, month, day);

    } catch (Exception e) {
      // If parsing fails, use current date
      selectedDate = Calendar.getInstance();
    }
  }

  private int getMonthNumber(String monthName) {
    switch (monthName.toLowerCase()) {
      case "january":
      case "jan":
        return 1;
      case "february":
      case "feb":
        return 2;
      case "march":
      case "mar":
        return 3;
      case "april":
      case "apr":
        return 4;
      case "may":
        return 5;
      case "june":
      case "jun":
        return 6;
      case "july":
      case "jul":
        return 7;
      case "august":
      case "aug":
        return 8;
      case "september":
      case "sep":
        return 9;
      case "october":
      case "oct":
        return 10;
      case "november":
      case "nov":
        return 11;
      case "december":
      case "dec":
        return 12;
      default:
        return 1;
    }
  }

  private void updateDateTimeDisplay() {
    // Update date display
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
    tvSelectedDate.setText(dateFormat.format(selectedDate.getTime()));

    // Update time display
    tvSelectedTime.setText(GroupHour);
  }

  private void eventHandler() {
    // Check admin verification before allowing any operations
    if (!isAdminVerified) {
      Toast.makeText(this, "Admin verification required", Toast.LENGTH_SHORT).show();
      return;
    }

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

    btnSelectDate.setOnClickListener(v -> showDatePicker());

    btnSelectTime.setOnClickListener(v -> showTimePicker());

    btnChangeDate.setOnClickListener(
        v -> {
          // Double-check admin status before making changes
          if (!isAdminVerified || !AdminKey.equals(UserKey)) {
            Toast.makeText(
                    ChangeDateActivity.this,
                    "Access denied. Admin verification failed.",
                    Toast.LENGTH_LONG)
                .show();
            return;
          }

          // Update group data with selected values
          GroupDay = String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH));
          GroupMonth = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(selectedDate.getTime());
          GroupYear = String.valueOf(selectedDate.get(Calendar.YEAR));

          // set new value in firebase
          DBRef.refGroups.child(GroupKey).child("groupDays").setValue(GroupDay);
          DBRef.refGroups.child(GroupKey).child("groupMonths").setValue(GroupMonth);
          DBRef.refGroups.child(GroupKey).child("groupYears").setValue(GroupYear);
          DBRef.refGroups.child(GroupKey).child("groupHours").setValue(GroupHour);

          // Type Successfully Changes
          Toast.makeText(ChangeDateActivity.this, "Successfully Changed", Toast.LENGTH_SHORT)
              .show();

          // intent from ChangeDate to AdminOptions
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

  private void showDatePicker() {
    // Check admin verification before allowing date selection
    if (!isAdminVerified || !AdminKey.equals(UserKey)) {
      Toast.makeText(this, "Access denied. Admin verification failed.", Toast.LENGTH_LONG).show();
      return;
    }

    DatePickerDialog datePickerDialog =
        new DatePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            (view, year, month, dayOfMonth) -> {
              selectedDate.set(year, month, dayOfMonth);
              updateDateTimeDisplay();
              Toast.makeText(
                      this,
                      "Date selected: " + dayOfMonth + "/" + (month + 1) + "/" + year,
                      Toast.LENGTH_SHORT)
                  .show();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH));

    // Set minimum date to today
    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    datePickerDialog.show();
  }

  private void showTimePicker() {
    // Check admin verification before allowing time selection
    if (!isAdminVerified || !AdminKey.equals(UserKey)) {
      Toast.makeText(this, "Access denied. Admin verification failed.", Toast.LENGTH_LONG).show();
      return;
    }

    // Parse current time
    int hour = 12;
    int minute = 0;

    try {
      if (GroupHour != null && GroupHour.contains(":")) {
        String[] timeParts = GroupHour.split(":");
        hour = Integer.parseInt(timeParts[0]);
        minute = Integer.parseInt(timeParts[1]);
      }
    } catch (Exception e) {
      // Use default values if parsing fails
    }

    TimePickerDialog timePickerDialog =
        new TimePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            (view, selectedHour, selectedMinute) -> {
              GroupHour =
                  String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
              updateDateTimeDisplay();
              Toast.makeText(this, "Time selected: " + GroupHour, Toast.LENGTH_SHORT).show();
            },
            hour,
            minute,
            true // 24-hour format
            );

    timePickerDialog.show();
  }
}
