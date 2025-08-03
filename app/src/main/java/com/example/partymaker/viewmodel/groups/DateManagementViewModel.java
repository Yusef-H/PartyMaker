package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ViewModel for group date and time management functionality.
 *
 * <p>Handles date/time editing, validation, scheduling, and date-related operations for
 * groups/events.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Date and time editing and validation
 *   <li>Multiple date format support
 *   <li>Calendar integration
 *   <li>Time zone handling
 *   <li>Date conflict detection
 *   <li>Scheduling recommendations
 * </ul>
 *
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class DateManagementViewModel extends BaseViewModel {

  private static final String TAG = "DateManagementViewModel";

  // Date format patterns
  private static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy";
  private static final String TIME_FORMAT_DISPLAY = "HH:mm";
  private static final String DATETIME_FORMAT_FULL = "dd/MM/yyyy HH:mm";

  // Dependencies
  private final GroupRepository groupRepository;

  // LiveData for date management state
  private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();
  private final MutableLiveData<String> selectedDate = new MutableLiveData<>();
  private final MutableLiveData<String> selectedTime = new MutableLiveData<>();
  private final MutableLiveData<String> originalDate = new MutableLiveData<>();
  private final MutableLiveData<String> originalTime = new MutableLiveData<>();

  // Date/Time components
  private final MutableLiveData<Integer> selectedDay = new MutableLiveData<>();
  private final MutableLiveData<Integer> selectedMonth = new MutableLiveData<>();
  private final MutableLiveData<Integer> selectedYear = new MutableLiveData<>();
  private final MutableLiveData<Integer> selectedHour = new MutableLiveData<>();
  private final MutableLiveData<Integer> selectedMinute = new MutableLiveData<>();

  // Validation state
  private final MutableLiveData<Boolean> isDateValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isTimeValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isDateTimeChanged = new MutableLiveData<>();
  private final MutableLiveData<String> validationError = new MutableLiveData<>();

  // Operation state
  private final MutableLiveData<Boolean> updateInProgress = new MutableLiveData<>();
  private final MutableLiveData<Boolean> dateTimeUpdated = new MutableLiveData<>();

  // Calendar and suggestions
  private final MutableLiveData<Long> minSelectableDate = new MutableLiveData<>();
  private final MutableLiveData<Long> maxSelectableDate = new MutableLiveData<>();
  private final MutableLiveData<String> schedulingSuggestion = new MutableLiveData<>();

  // Current state
  private String currentUserKey;
  private String currentGroupKey;
  private final SimpleDateFormat dateFormat =
      new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
  private final SimpleDateFormat timeFormat =
      new SimpleDateFormat(TIME_FORMAT_DISPLAY, Locale.getDefault());

  /**
   * Constructor for DateManagementViewModel.
   *
   * @param application The application context
   */
  public DateManagementViewModel(@NonNull Application application) {
    super(application);
    this.groupRepository = GroupRepository.getInstance();

    // Initialize state
    isDateValid.setValue(false);
    isTimeValid.setValue(false);
    isDateTimeChanged.setValue(false);
    updateInProgress.setValue(false);
    dateTimeUpdated.setValue(false);

    // Set date constraints (today to 1 year from now)
    Calendar cal = Calendar.getInstance();
    minSelectableDate.setValue(cal.getTimeInMillis());
    cal.add(Calendar.YEAR, 1);
    maxSelectableDate.setValue(cal.getTimeInMillis());

    Log.d(TAG, "DateManagementViewModel initialized");
  }

  // Getters for LiveData

  public LiveData<Group> getCurrentGroup() {
    return currentGroup;
  }

  public LiveData<String> getSelectedDate() {
    return selectedDate;
  }

  public LiveData<String> getSelectedTime() {
    return selectedTime;
  }

  public LiveData<String> getOriginalDate() {
    return originalDate;
  }

  public LiveData<String> getOriginalTime() {
    return originalTime;
  }

  public LiveData<Integer> getSelectedDay() {
    return selectedDay;
  }

  public LiveData<Integer> getSelectedMonth() {
    return selectedMonth;
  }

  public LiveData<Integer> getSelectedYear() {
    return selectedYear;
  }

  public LiveData<Integer> getSelectedHour() {
    return selectedHour;
  }

  public LiveData<Integer> getSelectedMinute() {
    return selectedMinute;
  }

  public LiveData<Boolean> getIsDateValid() {
    return isDateValid;
  }

  public LiveData<Boolean> getIsTimeValid() {
    return isTimeValid;
  }

  public LiveData<Boolean> getIsDateTimeChanged() {
    return isDateTimeChanged;
  }

  public LiveData<String> getValidationError() {
    return validationError;
  }

  public LiveData<Boolean> getUpdateInProgress() {
    return updateInProgress;
  }

  public LiveData<Boolean> getDateTimeUpdated() {
    return dateTimeUpdated;
  }

  public LiveData<Long> getMinSelectableDate() {
    return minSelectableDate;
  }

  public LiveData<Long> getMaxSelectableDate() {
    return maxSelectableDate;
  }

  public LiveData<String> getSchedulingSuggestion() {
    return schedulingSuggestion;
  }

  /**
   * Initializes the ViewModel with user and group information.
   *
   * @param userKey The current user's key
   * @param groupKey The group key to manage dates for
   */
  public void initialize(@NonNull String userKey, @NonNull String groupKey) {
    this.currentUserKey = userKey;
    this.currentGroupKey = groupKey;

    Log.d(TAG, "Initialized with user: " + userKey + ", group: " + groupKey);

    // Load group data
    loadGroupData();
  }

  /** Loads the group data and current date/time settings. */
  public void loadGroupData() {
    if (currentGroupKey == null) {
      setError("Group not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    setLoading(true);
    clearMessages();

    Log.d(TAG, "Loading group data for: " + currentGroupKey);

    ThreadUtils.runOnBackground(
        () -> {
          groupRepository.getGroup(
              currentGroupKey,
              new GroupRepository.Callback<Group>() {
                @Override
                public void onSuccess(Group group) {
                  handleGroupLoaded(group);
                }

                @Override
                public void onError(Exception error) {
                  handleGroupLoadError(error);
                }
              });
        });
  }

  /**
   * Sets the selected date.
   *
   * @param date The date in format "dd/MM/yyyy"
   */
  public void setSelectedDate(String date) {
    selectedDate.setValue(date);
    parseDateComponents(date);
    validateDate(date);
    checkIfChanged();

    if (isDateValid.getValue() != null && isDateValid.getValue()) {
      generateSchedulingSuggestion();
    }
  }

  /**
   * Sets the selected time.
   *
   * @param time The time in format "HH:mm"
   */
  public void setSelectedTime(String time) {
    selectedTime.setValue(time);
    parseTimeComponents(time);
    validateTime(time);
    checkIfChanged();

    if (isTimeValid.getValue() != null && isTimeValid.getValue()) {
      generateSchedulingSuggestion();
    }
  }

  /**
   * Sets the date using individual components.
   *
   * @param day Day of month (1-31)
   * @param month Month (1-12)
   * @param year Year
   */
  public void setDateComponents(int day, int month, int year) {
    selectedDay.setValue(day);
    selectedMonth.setValue(month);
    selectedYear.setValue(year);

    String dateString = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month, year);
    selectedDate.setValue(dateString);
    validateDate(dateString);
    checkIfChanged();

    Log.d(TAG, "Date components set: " + dateString);
  }

  /**
   * Sets the time using individual components.
   *
   * @param hour Hour (0-23)
   * @param minute Minute (0-59)
   */
  public void setTimeComponents(int hour, int minute) {
    selectedHour.setValue(hour);
    selectedMinute.setValue(minute);

    String timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    selectedTime.setValue(timeString);
    validateTime(timeString);
    checkIfChanged();

    Log.d(TAG, "Time components set: " + timeString);
  }

  /** Updates the group's date and time. */
  public void updateDateTime() {
    if (currentGroupKey == null || currentUserKey == null) {
      setError("Group or user not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    if (!canUpdateDateTime()) {
      setError("Invalid date/time or no changes made", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    if (updateInProgress.getValue() != null && updateInProgress.getValue()) {
      Log.w(TAG, "Update already in progress");
      return;
    }

    updateInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Updating group date/time");

    ThreadUtils.runOnBackground(
        () -> {
          try {
            Group updatedGroup = createUpdatedGroup();

            groupRepository.updateGroup(
                updatedGroup,
                new GroupRepository.Callback<Group>() {
                  @Override
                  public void onSuccess(Group result) {
                    handleUpdateSuccess(result);
                  }

                  @Override
                  public void onError(Exception error) {
                    handleUpdateError(error);
                  }
                });

          } catch (Exception e) {
            Log.e(TAG, "Error creating updated group", e);
            handleUpdateError(e);
          }
        });
  }

  /** Resets the date/time to original values. */
  public void resetToOriginal() {
    String origDate = originalDate.getValue();
    String origTime = originalTime.getValue();

    if (origDate != null) {
      setSelectedDate(origDate);
    }

    if (origTime != null) {
      setSelectedTime(origTime);
    }

    setInfo("Date and time reset to original values");

    Log.d(TAG, "Date/time reset to original values");
  }

  /** Sets the date to today. */
  public void setToToday() {
    Calendar cal = Calendar.getInstance();
    int day = cal.get(Calendar.DAY_OF_MONTH);
    int month = cal.get(Calendar.MONTH) + 1; // Calendar months are 0-based
    int year = cal.get(Calendar.YEAR);

    setDateComponents(day, month, year);

    Log.d(TAG, "Date set to today");
  }

  /** Sets the time to current time. */
  public void setToCurrentTime() {
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int minute = cal.get(Calendar.MINUTE);

    setTimeComponents(hour, minute);

    Log.d(TAG, "Time set to current time");
  }

  // Private helper methods

  private void handleGroupLoaded(Group group) {
    Log.d(TAG, "Group loaded successfully: " + group.getGroupName());

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          currentGroup.setValue(group);

          // Set current date/time from group
          initializeDateTimeFromGroup(group);

          setSuccess("Group data loaded successfully");
        });
  }

  private void handleGroupLoadError(Exception error) {
    Log.e(TAG, "Failed to load group", error);

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to load group: " + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  private void initializeDateTimeFromGroup(Group group) {
    // Build date string from group components
    String day = group.getGroupDays();
    String month = group.getGroupMonths();
    String year = group.getGroupYears();
    String hour = group.getGroupHours();
    String minute = group.getGroupMinutes();

    if (day != null && month != null && year != null) {
      String dateString = day + "/" + month + "/" + year;
      originalDate.setValue(dateString);
      setSelectedDate(dateString);
    }

    if (hour != null && minute != null) {
      String timeString = hour + ":" + minute;
      originalTime.setValue(timeString);
      setSelectedTime(timeString);
    }

    // Reset change tracking
    isDateTimeChanged.setValue(false);

    Log.d(TAG, "Date/time initialized from group data");
  }

  private void parseDateComponents(String date) {
    if (date != null && date.matches("\\d{2}/\\d{2}/\\d{4}")) {
      try {
        String[] parts = date.split("/");
        selectedDay.setValue(Integer.parseInt(parts[0]));
        selectedMonth.setValue(Integer.parseInt(parts[1]));
        selectedYear.setValue(Integer.parseInt(parts[2]));
      } catch (NumberFormatException e) {
        Log.w(TAG, "Error parsing date components", e);
      }
    } else {
      selectedDay.setValue(null);
      selectedMonth.setValue(null);
      selectedYear.setValue(null);
    }
  }

  private void parseTimeComponents(String time) {
    if (time != null && time.matches("\\d{2}:\\d{2}")) {
      try {
        String[] parts = time.split(":");
        selectedHour.setValue(Integer.parseInt(parts[0]));
        selectedMinute.setValue(Integer.parseInt(parts[1]));
      } catch (NumberFormatException e) {
        Log.w(TAG, "Error parsing time components", e);
      }
    } else {
      selectedHour.setValue(null);
      selectedMinute.setValue(null);
    }
  }

  private void validateDate(String date) {
    boolean valid = false;
    String error = null;

    if (date != null && date.matches("\\d{2}/\\d{2}/\\d{4}")) {
      try {
        Date parsedDate = dateFormat.parse(date);
        Date currentDate = new Date();

        if (parsedDate != null) {
          // Check if date is not in the past
          Calendar cal = Calendar.getInstance();
          cal.setTime(currentDate);
          cal.set(Calendar.HOUR_OF_DAY, 0);
          cal.set(Calendar.MINUTE, 0);
          cal.set(Calendar.SECOND, 0);
          cal.set(Calendar.MILLISECOND, 0);
          Date todayStart = cal.getTime();

          if (parsedDate.before(todayStart)) {
            error = "Date cannot be in the past";
          } else {
            // Check if date is within reasonable future (1 year)
            cal.add(Calendar.YEAR, 1);
            Date maxDate = cal.getTime();

            if (parsedDate.after(maxDate)) {
              error = "Date cannot be more than 1 year in the future";
            } else {
              valid = true;
            }
          }
        }

      } catch (ParseException e) {
        error = "Invalid date format";
      }
    } else {
      error = "Please enter date in DD/MM/YYYY format";
    }

    isDateValid.setValue(valid);
    if (error != null) {
      validationError.setValue(error);
    } else {
      validationError.setValue(null);
    }
  }

  private void validateTime(String time) {
    boolean valid = false;
    String error = null;

    if (time != null && time.matches("\\d{2}:\\d{2}")) {
      try {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
          valid = true;
        } else {
          error = "Invalid time values";
        }

      } catch (NumberFormatException e) {
        error = "Invalid time format";
      }
    } else {
      error = "Please enter time in HH:MM format";
    }

    isTimeValid.setValue(valid);
    if (error != null && validationError.getValue() == null) {
      validationError.setValue(error);
    } else if (error == null && valid) {
      validationError.setValue(null);
    }
  }

  private void checkIfChanged() {
    String currentDate = selectedDate.getValue();
    String currentTime = selectedTime.getValue();
    String origDate = originalDate.getValue();
    String origTime = originalTime.getValue();

    boolean dateChanged = !objectsEqual(currentDate, origDate);
    boolean timeChanged = !objectsEqual(currentTime, origTime);

    isDateTimeChanged.setValue(dateChanged || timeChanged);
  }

  private boolean objectsEqual(Object a, Object b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  private boolean canUpdateDateTime() {
    Boolean dateValid = isDateValid.getValue();
    Boolean timeValid = isTimeValid.getValue();
    Boolean changed = isDateTimeChanged.getValue();

    return (dateValid != null && dateValid)
        && (timeValid != null && timeValid)
        && (changed != null && changed);
  }

  private Group createUpdatedGroup() {
    Group group = currentGroup.getValue();
    if (group == null) {
      throw new IllegalStateException("No group loaded");
    }

    // Create a copy of the group with updated date/time
    Group updatedGroup = new Group();
    // Copy all existing fields
    updatedGroup.setGroupKey(group.getGroupKey());
    updatedGroup.setGroupName(group.getGroupName());
    updatedGroup.setGroupLocation(group.getGroupLocation());
    updatedGroup.setAdminKey(group.getAdminKey());
    updatedGroup.setCreatedAt(group.getCreatedAt());
    updatedGroup.setGroupType(group.getGroupType());
    updatedGroup.setGroupPrice(group.getGroupPrice());
    updatedGroup.setGroupImageUrl(group.getGroupImageUrl());
    updatedGroup.setFriendKeys(group.getFriendKeys());
    updatedGroup.setComingKeys(group.getComingKeys());
    updatedGroup.setMessageKeys(group.getMessageKeys());
    updatedGroup.setCanAdd(group.isCanAdd());

    // Update date/time components
    Integer day = selectedDay.getValue();
    Integer month = selectedMonth.getValue();
    Integer year = selectedYear.getValue();
    Integer hour = selectedHour.getValue();
    Integer minute = selectedMinute.getValue();

    if (day != null) updatedGroup.setGroupDays(String.format(Locale.US, "%02d", day));
    if (month != null) updatedGroup.setGroupMonths(String.format(Locale.US, "%02d", month));
    if (year != null) updatedGroup.setGroupYears(String.valueOf(year));
    if (hour != null) updatedGroup.setGroupHours(String.format(Locale.US, "%02d", hour));
    if (minute != null) updatedGroup.setGroupMinutes(String.format(Locale.US, "%02d", minute));

    return updatedGroup;
  }

  private void handleUpdateSuccess(Group updatedGroup) {
    Log.d(TAG, "Date/time updated successfully");

    ThreadUtils.runOnMainThread(
        () -> {
          updateInProgress.setValue(false);
          dateTimeUpdated.setValue(true);
          currentGroup.setValue(updatedGroup);

          // Update original values
          String newDate = selectedDate.getValue();
          String newTime = selectedTime.getValue();
          originalDate.setValue(newDate);
          originalTime.setValue(newTime);

          // Reset change tracking
          isDateTimeChanged.setValue(false);

          setSuccess("Event date and time updated successfully");
        });
  }

  private void handleUpdateError(Exception error) {
    Log.e(TAG, "Failed to update date/time", error);

    ThreadUtils.runOnMainThread(
        () -> {
          updateInProgress.setValue(false);
          dateTimeUpdated.setValue(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to update date/time: "
                  + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  private void generateSchedulingSuggestion() {
    try {
      String date = selectedDate.getValue();
      String time = selectedTime.getValue();

      if (date != null && time != null) {
        Calendar selectedCal = Calendar.getInstance();
        Date parsedDate = dateFormat.parse(date);
        selectedCal.setTime(parsedDate);

        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        selectedCal.set(Calendar.HOUR_OF_DAY, hour);
        selectedCal.set(Calendar.MINUTE, minute);

        // Generate suggestion based on day of week and time
        int dayOfWeek = selectedCal.get(Calendar.DAY_OF_WEEK);
        String suggestion = "";

        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
          if (hour >= 10 && hour <= 14) {
            suggestion = "Great choice! Weekend brunch time is perfect for social events.";
          } else if (hour >= 18 && hour <= 22) {
            suggestion = "Weekend evening - ideal for parties and celebrations!";
          } else {
            suggestion =
                "Weekend timing - consider 10 AM-2 PM or 6 PM-10 PM for better attendance.";
          }
        } else {
          if (hour >= 18 && hour <= 21) {
            suggestion = "Good weekday evening timing for after-work gatherings.";
          } else if (hour >= 12 && hour <= 14) {
            suggestion = "Lunch time event - perfect for quick meetups.";
          } else {
            suggestion = "Consider evening hours (6-9 PM) for better weekday attendance.";
          }
        }

        schedulingSuggestion.setValue(suggestion);
      }

    } catch (Exception e) {
      Log.w(TAG, "Error generating scheduling suggestion", e);
      schedulingSuggestion.setValue(null);
    }
  }

  /** Clears all date management data and resets state. */
  public void clearDateManagementData() {
    currentGroup.setValue(null);
    selectedDate.setValue(null);
    selectedTime.setValue(null);
    originalDate.setValue(null);
    originalTime.setValue(null);

    selectedDay.setValue(null);
    selectedMonth.setValue(null);
    selectedYear.setValue(null);
    selectedHour.setValue(null);
    selectedMinute.setValue(null);

    isDateValid.setValue(false);
    isTimeValid.setValue(false);
    isDateTimeChanged.setValue(false);
    validationError.setValue(null);

    updateInProgress.setValue(false);
    dateTimeUpdated.setValue(false);

    schedulingSuggestion.setValue(null);

    currentUserKey = null;
    currentGroupKey = null;

    clearMessages();

    Log.d(TAG, "Date management data cleared");
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearDateManagementData();
    Log.d(TAG, "DateManagementViewModel cleared");
  }
}
