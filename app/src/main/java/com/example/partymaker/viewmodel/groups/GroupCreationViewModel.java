package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.media.ImageCompressor;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

/**
 * ViewModel for group creation functionality.
 *
 * <p>Handles group creation process including validation, image upload, location selection, and
 * comprehensive group setup.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Group information validation
 *   <li>Image upload and compression
 *   <li>Location selection and validation
 *   <li>Date and time management
 *   <li>Privacy settings configuration
 *   <li>Member capacity management
 * </ul>
 *
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class GroupCreationViewModel extends BaseViewModel {

  private static final String TAG = "GroupCreationViewModel";

  // Dependencies
  private final GroupRepository groupRepository;
  private final ImageCompressor imageCompressor;

  // LiveData for group creation state
  private final MutableLiveData<Boolean> groupCreated = new MutableLiveData<>();
  private final MutableLiveData<Group> createdGroup = new MutableLiveData<>();
  private final MutableLiveData<String> selectedImagePath = new MutableLiveData<>();
  private final MutableLiveData<Uri> selectedImageUri = new MutableLiveData<>();
  private final MutableLiveData<Boolean> imageUploadInProgress = new MutableLiveData<>();

  // Form data LiveData
  private final MutableLiveData<String> groupName = new MutableLiveData<>();
  private final MutableLiveData<String> groupDescription = new MutableLiveData<>();
  private final MutableLiveData<String> groupLocation = new MutableLiveData<>();
  private final MutableLiveData<String> selectedDate = new MutableLiveData<>();
  private final MutableLiveData<String> selectedTime = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isPublicGroup = new MutableLiveData<>();
  private final MutableLiveData<Integer> maxMembers = new MutableLiveData<>();
  private final MutableLiveData<String> groupPrice = new MutableLiveData<>();

  // Form validation state
  private final MutableLiveData<Boolean> isNameValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLocationValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isDateValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isTimeValid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isFormValid = new MutableLiveData<>();

  // Date/Time parsing components
  private final MutableLiveData<String> selectedDay = new MutableLiveData<>();
  private final MutableLiveData<String> selectedMonth = new MutableLiveData<>();
  private final MutableLiveData<String> selectedYear = new MutableLiveData<>();
  private final MutableLiveData<String> selectedHour = new MutableLiveData<>();
  private final MutableLiveData<String> selectedMinute = new MutableLiveData<>();

  /**
   * Constructor for GroupCreationViewModel.
   *
   * @param application The application context
   */
  public GroupCreationViewModel(@NonNull Application application) {
    super(application);
    this.groupRepository = GroupRepository.getInstance();
    this.imageCompressor = new ImageCompressor();

    // Initialize default values
    isPublicGroup.setValue(true);
    maxMembers.setValue(50);
    groupPrice.setValue("0");
    imageUploadInProgress.setValue(false);

    // Initialize validation state
    isNameValid.setValue(false);
    isLocationValid.setValue(false);
    isDateValid.setValue(false);
    isTimeValid.setValue(false);
    isFormValid.setValue(false);

    Log.d(TAG, "GroupCreationViewModel initialized");
  }

  // Getters for LiveData

  public LiveData<Boolean> getGroupCreated() {
    return groupCreated;
  }

  public LiveData<Group> getCreatedGroup() {
    return createdGroup;
  }

  public LiveData<String> getSelectedImagePath() {
    return selectedImagePath;
  }

  public LiveData<Uri> getSelectedImageUri() {
    return selectedImageUri;
  }

  public LiveData<Boolean> getImageUploadInProgress() {
    return imageUploadInProgress;
  }

  public LiveData<String> getGroupName() {
    return groupName;
  }

  public LiveData<String> getGroupDescription() {
    return groupDescription;
  }

  public LiveData<String> getGroupLocation() {
    return groupLocation;
  }

  public LiveData<String> getSelectedDate() {
    return selectedDate;
  }

  public LiveData<String> getSelectedTime() {
    return selectedTime;
  }

  public LiveData<Boolean> getIsPublicGroup() {
    return isPublicGroup;
  }

  public LiveData<Integer> getMaxMembers() {
    return maxMembers;
  }

  public LiveData<String> getGroupPrice() {
    return groupPrice;
  }

  public LiveData<Boolean> getIsFormValid() {
    return isFormValid;
  }

  /**
   * Sets the group name and validates it.
   *
   * @param name The group name
   */
  public void setGroupName(@Nullable String name) {
    groupName.setValue(name);
    validateGroupName(name);
  }

  /**
   * Sets the group description.
   *
   * @param description The group description
   */
  public void setGroupDescription(@Nullable String description) {
    groupDescription.setValue(description);
  }

  /**
   * Sets the group location and validates it.
   *
   * @param location The group location
   */
  public void setGroupLocation(@Nullable String location) {
    groupLocation.setValue(location);
    validateGroupLocation(location);
  }

  /**
   * Sets the event date.
   *
   * @param date The event date in format "DD/MM/YYYY"
   */
  public void setSelectedDate(@Nullable String date) {
    selectedDate.setValue(date);
    parseDateComponents(date);
    validateDate(date);
  }

  /**
   * Sets the event time.
   *
   * @param time The event time in format "HH:MM"
   */
  public void setSelectedTime(@Nullable String time) {
    selectedTime.setValue(time);
    parseTimeComponents(time);
    validateTime(time);
  }

  /**
   * Sets whether the group is public or private.
   *
   * @param isPublic true for public group, false for private
   */
  public void setIsPublicGroup(boolean isPublic) {
    isPublicGroup.setValue(isPublic);
  }

  /**
   * Sets the maximum number of members.
   *
   * @param max Maximum members (1-1000)
   */
  public void setMaxMembers(int max) {
    if (max >= 1 && max <= 1000) {
      maxMembers.setValue(max);
    } else {
      Log.w(TAG, "Invalid max members value: " + max);
    }
  }

  /**
   * Sets the group price.
   *
   * @param price The group price as string
   */
  public void setGroupPrice(@Nullable String price) {
    groupPrice.setValue(price != null ? price : "0");
  }

  /**
   * Selects an image for the group.
   *
   * @param imageUri The URI of the selected image
   */
  public void selectGroupImage(@NonNull Uri imageUri) {
    selectedImageUri.setValue(imageUri);

    // Compress and process the image
    ThreadUtils.runOnBackground(
        () -> {
          try {
            imageUploadInProgress.postValue(true);

            imageCompressor.compressImage(
                getApplication(),
                imageUri,
                new ImageCompressor.CompressCallback() {
                  @Override
                  public void onCompressSuccess(File compressedFile) {
                    selectedImagePath.postValue(compressedFile.getAbsolutePath());

                    ThreadUtils.runOnMainThread(
                        () -> {
                          imageUploadInProgress.setValue(false);
                          setSuccess("Image selected and compressed successfully");
                        });
                  }

                  @Override
                  public void onCompressError(String error) {
                    ThreadUtils.runOnMainThread(
                        () -> {
                          imageUploadInProgress.setValue(false);
                          setError("Failed to process selected image: " + error);
                        });
                  }
                });

          } catch (Exception e) {
            Log.e(TAG, "Error processing selected image", e);
            ThreadUtils.runOnMainThread(
                () -> {
                  imageUploadInProgress.setValue(false);
                  setError("Error processing image: " + e.getMessage());
                });
          }
        });
  }

  /**
   * Creates a new group with the current form data.
   *
   * @param adminKey The admin user key
   */
  public void createGroup(@NonNull String adminKey) {
    if (isCurrentlyLoading()) {
      Log.w(TAG, "Group creation already in progress");
      return;
    }

    executeIfNotLoading(
        () -> {
          // Validate form before creation
          if (!isValidForm()) {
            setError(
                "Please fill in all required fields correctly",
                NetworkUtils.ErrorType.VALIDATION_ERROR);
            return;
          }

          setLoading(true);
          clearMessages();

          Log.d(TAG, "Creating new group with admin: " + adminKey);

          ThreadUtils.runOnBackground(
              () -> {
                try {
                  // Create group object
                  Group newGroup = createGroupFromFormData(adminKey);

                  // Create group via repository
                  groupRepository.createGroup(
                      newGroup,
                      new GroupRepository.Callback<Group>() {
                        @Override
                        public void onSuccess(Group result) {
                          handleGroupCreationSuccess(result);
                        }

                        @Override
                        public void onError(Exception error) {
                          handleGroupCreationError(error);
                        }
                      });

                } catch (Exception e) {
                  Log.e(TAG, "Error creating group", e);
                  handleGroupCreationError(e);
                }
              });
        });
  }

  // Private helper methods

  private void validateGroupName(@Nullable String name) {
    boolean valid = name != null && name.trim().length() >= 3 && name.trim().length() <= 50;
    isNameValid.setValue(valid);
    updateFormValidation();
  }

  private void validateGroupLocation(@Nullable String location) {
    boolean valid = location != null && location.trim().length() >= 3;
    isLocationValid.setValue(valid);
    updateFormValidation();
  }

  private void validateDate(@Nullable String date) {
    boolean valid = false;

    if (date != null && date.matches("\\d{2}/\\d{2}/\\d{4}")) {
      try {
        String[] parts = date.split("/");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);

        // Basic validation
        valid =
            day >= 1
                && day <= 31
                && month >= 1
                && month <= 12
                && year >= Calendar.getInstance().get(Calendar.YEAR);

      } catch (NumberFormatException e) {
        // valid is already false by default
      }
    }

    isDateValid.setValue(valid);
    updateFormValidation();
  }

  private void validateTime(@Nullable String time) {
    boolean valid = false;

    if (time != null && time.matches("\\d{2}:\\d{2}")) {
      try {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        valid = hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;

      } catch (NumberFormatException e) {
        // valid is already false by default
      }
    }

    isTimeValid.setValue(valid);
    updateFormValidation();
  }

  private void parseDateComponents(@Nullable String date) {
    if (date != null && date.matches("\\d{2}/\\d{2}/\\d{4}")) {
      String[] parts = date.split("/");
      selectedDay.setValue(parts[0]);
      selectedMonth.setValue(parts[1]);
      selectedYear.setValue(parts[2]);
    } else {
      selectedDay.setValue(null);
      selectedMonth.setValue(null);
      selectedYear.setValue(null);
    }
  }

  private void parseTimeComponents(@Nullable String time) {
    if (time != null && time.matches("\\d{2}:\\d{2}")) {
      String[] parts = time.split(":");
      selectedHour.setValue(parts[0]);
      selectedMinute.setValue(parts[1]);
    } else {
      selectedHour.setValue(null);
      selectedMinute.setValue(null);
    }
  }

  private void updateFormValidation() {
    ThreadUtils.runOnMainThread(
        () -> {
          Boolean nameValid = isNameValid.getValue();
          Boolean locationValid = isLocationValid.getValue();
          Boolean dateValid = isDateValid.getValue();
          Boolean timeValid = isTimeValid.getValue();

          boolean formValid =
              (nameValid != null && nameValid)
                  && (locationValid != null && locationValid)
                  && (dateValid != null && dateValid)
                  && (timeValid != null && timeValid);

          isFormValid.setValue(formValid);
        });
  }

  private boolean isValidForm() {
    Boolean formValid = isFormValid.getValue();
    return formValid != null && formValid;
  }

  private Group createGroupFromFormData(@NonNull String adminKey) {
    Group group = new Group();

    // Basic information
    group.setGroupName(groupName.getValue() != null ? groupName.getValue().trim() : "");
    group.setGroupLocation(groupLocation.getValue() != null ? groupLocation.getValue().trim() : "");
    group.setAdminKey(adminKey);
    group.setCreatedAt(String.valueOf(System.currentTimeMillis()));

    // Date and time components
    group.setGroupDays(selectedDay.getValue());
    group.setGroupMonths(selectedMonth.getValue());
    group.setGroupYears(selectedYear.getValue());
    group.setGroupHours(selectedHour.getValue());
    group.setGroupMinutes(selectedMinute.getValue());

    // Optional information
    group.setGroupType(isPublicGroup.getValue() != null && isPublicGroup.getValue() ? 0 : 1);
    group.setGroupPrice(groupPrice.getValue() != null ? groupPrice.getValue() : "0");

    // Image path
    String imagePath = selectedImagePath.getValue();
    if (imagePath != null) {
      group.setGroupImageUrl(imagePath);
    }

    // Initialize empty collections
    group.setFriendKeys(new HashMap<>());
    group.setComingKeys(new HashMap<>());
    group.setMessageKeys(new HashMap<>());

    // Add admin as first member
    HashMap<String, Object> members = new HashMap<>();
    members.put(adminKey, true);
    group.setFriendKeys(members);

    // Set capacity
    Integer capacity = maxMembers.getValue();
    if (capacity != null) {
      group.setCanAdd(capacity > 1); // Can add members if capacity > 1
    }

    return group;
  }

  private void handleGroupCreationSuccess(Group result) {
    Log.d(TAG, "Group created successfully: " + result.getGroupName());

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          groupCreated.setValue(true);
          createdGroup.setValue(result);
          setSuccess("Group '" + result.getGroupName() + "' created successfully!");
        });
  }

  private void handleGroupCreationError(Exception error) {
    Log.e(TAG, "Failed to create group", error);

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          groupCreated.setValue(false);

          String errorMessage = error.getMessage();
          NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.UNKNOWN_ERROR;

          if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("network")) {
              errorType = NetworkUtils.ErrorType.NETWORK_ERROR;
            } else if (errorMessage.toLowerCase().contains("permission")) {
              errorType = NetworkUtils.ErrorType.PERMISSION_ERROR;
            } else if (errorMessage.toLowerCase().contains("validation")) {
              errorType = NetworkUtils.ErrorType.VALIDATION_ERROR;
            }
          }

          setError(
              "Failed to create group: " + (errorMessage != null ? errorMessage : "Unknown error"),
              errorType);
        });
  }

  /** Clears all form data and resets the ViewModel state. */
  public void clearFormData() {
    groupName.setValue(null);
    groupDescription.setValue(null);
    groupLocation.setValue(null);
    selectedDate.setValue(null);
    selectedTime.setValue(null);
    selectedImagePath.setValue(null);
    selectedImageUri.setValue(null);

    // Reset to defaults
    isPublicGroup.setValue(true);
    maxMembers.setValue(50);
    groupPrice.setValue("0");

    // Clear validation state
    isNameValid.setValue(false);
    isLocationValid.setValue(false);
    isDateValid.setValue(false);
    isTimeValid.setValue(false);
    isFormValid.setValue(false);

    // Clear date/time components
    selectedDay.setValue(null);
    selectedMonth.setValue(null);
    selectedYear.setValue(null);
    selectedHour.setValue(null);
    selectedMinute.setValue(null);

    // Clear creation state
    groupCreated.setValue(false);
    createdGroup.setValue(null);
    imageUploadInProgress.setValue(false);

    clearMessages();

    Log.d(TAG, "Form data cleared");
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearFormData();
    Log.d(TAG, "GroupCreationViewModel cleared");
  }
}
