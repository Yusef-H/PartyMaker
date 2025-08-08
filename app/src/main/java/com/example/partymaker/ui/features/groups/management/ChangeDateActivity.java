package com.example.partymaker.ui.features.groups.management;

import static com.example.partymaker.utils.core.IntentExtrasManager.hideViews;
import static com.example.partymaker.utils.core.IntentExtrasManager.showViews;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ChangeDateActivity extends AppCompatActivity {

  private static final String TAG = "ChangeDateActivity";

  // UI constants
  private static final String ADMIN_VERIFICATION_MESSAGE = "Verifying admin permissions...";
  private static final String ACCESS_DENIED_MESSAGE =
      "Access denied. Only group admin can change party dates.";
  private static final String AUTH_ERROR_MESSAGE = "Authentication error. Please login again.";
  private static final String VERIFICATION_FAILED_MESSAGE = "Failed to verify admin status: ";
  private static final String ACCESS_DENIED_VERIFICATION_MESSAGE =
      "Access denied. Admin verification failed.";
  private static final String DATE_SELECTED_MESSAGE = "Date selected: ";
  private static final String TIME_SELECTED_MESSAGE = "Time selected: ";
  private static final String SUCCESSFULLY_CHANGED_MESSAGE = "Successfully Changed";

  // UI Components
  private Button hideButton, helpButton, changeDateButton, selectDateButton, selectTimeButton;
  private TextView hideText, helpText, instructionsText, selectedDateText, selectedTimeText;

  // Group data
  private HashMap<String, Object> friendKeys, comingKeys, messageKeys;
  private String groupKey, groupName, groupDay, groupMonth, groupYear, groupHour;
  private String groupLocation, adminKey, createdAt, groupPrice, userKey;
  private int groupType;
  private boolean canAdd;

  // Date and verification state
  private Calendar selectedDate;
  private boolean isAdminVerified = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_change_date);

    hideActionBar();
    initializeDate();

    if (!authenticateUser() || !extractGroupDataFromIntent()) {
      return;
    }

    // Verify admin status before allowing access
    verifyAdminStatus();
  }

  private void hideActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
  }

  private void initializeDate() {
    selectedDate = Calendar.getInstance();
  }

  private boolean authenticateUser() {
    try {
      userKey = AuthenticationManager.getCurrentUserKey(this);
      return true;
    } catch (Exception e) {
      Toast.makeText(this, AUTH_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
      finish();
      return false;
    }
  }

  private boolean extractGroupDataFromIntent() {
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return false;
    }

    groupName = extras.getGroupName();
    groupKey = extras.getGroupKey();
    groupDay = extras.getGroupDays();
    groupMonth = extras.getGroupMonths();
    groupYear = extras.getGroupYears();
    groupHour = extras.getGroupHours();
    groupLocation = extras.getGroupLocation();
    adminKey = extras.getAdminKey();
    createdAt = extras.getCreatedAt();
    groupPrice = extras.getGroupPrice();
    groupType = extras.getGroupType();
    canAdd = extras.isCanAdd();
    friendKeys = extras.getFriendKeys();
    comingKeys = extras.getComingKeys();
    messageKeys = extras.getMessageKeys();

    return true;
  }

  private void verifyAdminStatus() {
    // Show loading message
    Toast.makeText(this, ADMIN_VERIFICATION_MESSAGE, Toast.LENGTH_SHORT).show();

    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group != null
                && group.getAdminKey() != null
                && group.getAdminKey().equals(userKey)) {
              // User is verified as admin
              isAdminVerified = true;
              // Update local admin key to match server data
              adminKey = group.getAdminKey();
              // Initialize UI after verification
              initializeUI();
            } else {
              // User is not admin - deny access
              Toast.makeText(ChangeDateActivity.this, ACCESS_DENIED_MESSAGE, Toast.LENGTH_LONG)
                  .show();
              finish();
            }
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    ChangeDateActivity.this,
                    VERIFICATION_FAILED_MESSAGE + errorMessage,
                    Toast.LENGTH_LONG)
                .show();
            finish();
          }
        });
  }

  private void initializeUI() {
    // Initialize current date from group data
    initializeDateFromGroupData();

    // Initialize UI components
    initializeViewComponents();

    // Update UI with current values
    updateDateTimeDisplay();

    setupEventHandlers();
  }

  private void initializeViewComponents() {
    changeDateButton = findViewById(R.id.btnChangeDate);
    selectDateButton = findViewById(R.id.btnSelectDate);
    selectTimeButton = findViewById(R.id.btnSelectTime);
    hideButton = findViewById(R.id.btnHide2);
    helpButton = findViewById(R.id.btnHelp2);
    hideText = findViewById(R.id.tvHide2);
    helpText = findViewById(R.id.tvHelp2);
    instructionsText = findViewById(R.id.tvInstructions2);
    selectedDateText = findViewById(R.id.tvSelectedDate);
    selectedTimeText = findViewById(R.id.tvSelectedTime);
  }

  private void initializeDateFromGroupData() {
    try {
      // Parse existing date values
      int day = Integer.parseInt(groupDay);
      int year = Integer.parseInt(groupYear);

      // Handle month - could be name or number
      int month;
      if (groupMonth.matches("\\d+")) {
        month = Integer.parseInt(groupMonth) - 1; // Calendar months are 0-based
      } else {
        month = getMonthNumber(groupMonth) - 1;
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
    selectedDateText.setText(dateFormat.format(selectedDate.getTime()));

    // Update time display
    selectedTimeText.setText(groupHour);
  }

  private void setupEventHandlers() {
    // Check admin verification before allowing any operations
    if (!isAdminVerified) {
      Toast.makeText(this, "Admin verification required", Toast.LENGTH_SHORT).show();
      return;
    }

    helpButton.setOnClickListener(
        v -> {
          showViews(instructionsText, hideButton, hideText);
          hideViews(helpButton, helpText);
        });

    hideButton.setOnClickListener(
        v -> {
          showViews(helpButton, helpText);
          hideViews(instructionsText, hideButton, hideText);
        });

    selectDateButton.setOnClickListener(v -> showDatePicker());

    selectTimeButton.setOnClickListener(v -> showTimePicker());

    changeDateButton.setOnClickListener(v -> handleDateChange());
  }

  private void handleDateChange() {
    // Double-check admin status before making changes
    if (!isAdminVerified || !adminKey.equals(userKey)) {
      Toast.makeText(this, ACCESS_DENIED_VERIFICATION_MESSAGE, Toast.LENGTH_LONG).show();
      return;
    }

    // Update group data with selected values
    updateGroupDateData();
    saveChangesToFirebase();
    showSuccessAndNavigate();
  }

  private void updateGroupDateData() {
    groupDay = String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH));
    groupMonth = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(selectedDate.getTime());
    groupYear = String.valueOf(selectedDate.get(Calendar.YEAR));
  }

  private void saveChangesToFirebase() {
    DBRef.refGroups.child(groupKey).child("groupDays").setValue(groupDay);
    DBRef.refGroups.child(groupKey).child("groupMonths").setValue(groupMonth);
    DBRef.refGroups.child(groupKey).child("groupYears").setValue(groupYear);
    DBRef.refGroups.child(groupKey).child("groupHours").setValue(groupHour);
  }

  private void showSuccessAndNavigate() {
    Toast.makeText(ChangeDateActivity.this, SUCCESSFULLY_CHANGED_MESSAGE, Toast.LENGTH_SHORT)
        .show();

    // Navigate back to AdminOptions
    Intent intent = new Intent(getBaseContext(), AdminOptionsActivity.class);
    ExtrasMetadata extras = createExtrasMetadata();
    IntentExtrasManager.addExtrasToIntent(intent, extras);
    startActivity(intent);
  }

  private ExtrasMetadata createExtrasMetadata() {
    return new ExtrasMetadata(
        groupName,
        groupKey,
        groupDay,
        groupMonth,
        groupYear,
        groupHour,
        groupLocation,
        adminKey,
        createdAt,
        groupPrice,
        groupType,
        canAdd,
        friendKeys,
        comingKeys,
        messageKeys);
  }

  private void showDatePicker() {
    // Check admin verification before allowing date selection
    if (!isAdminVerified || !adminKey.equals(userKey)) {
      Toast.makeText(this, ACCESS_DENIED_VERIFICATION_MESSAGE, Toast.LENGTH_LONG).show();
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
                      DATE_SELECTED_MESSAGE + dayOfMonth + "/" + (month + 1) + "/" + year,
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
    if (!isAdminVerified || !adminKey.equals(userKey)) {
      Toast.makeText(this, ACCESS_DENIED_VERIFICATION_MESSAGE, Toast.LENGTH_LONG).show();
      return;
    }

    // Parse current time
    int[] timeValues = parseCurrentTime();
    int hour = timeValues[0];
    int minute = timeValues[1];

    TimePickerDialog timePickerDialog =
        new TimePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            (view, selectedHour, selectedMinute) -> {
              groupHour =
                  String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
              updateDateTimeDisplay();
              Toast.makeText(this, TIME_SELECTED_MESSAGE + groupHour, Toast.LENGTH_SHORT).show();
            },
            hour,
            minute,
            true // 24-hour format
            );

    timePickerDialog.show();
  }

  private int[] parseCurrentTime() {
    int hour = 12;
    int minute = 0;

    try {
      if (groupHour != null && groupHour.contains(":")) {
        String[] timeParts = groupHour.split(":");
        hour = Integer.parseInt(timeParts[0]);
        minute = Integer.parseInt(timeParts[1]);
      }
    } catch (Exception e) {
      // Use default values if parsing fails
    }

    return new int[] {hour, minute};
  }
}
