package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ViewModel for the main party/group screen functionality.
 *
 * <p>Handles the main group view operations including group details display, member management,
 * admin actions, and user interactions within a group.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Group information display and updates
 *   <li>Member list management and display
 *   <li>Join/leave group operations
 *   <li>Admin privilege checking and actions
 *   <li>Real-time group updates
 *   <li>User status tracking within group
 * </ul>
 *
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class PartyMainViewModel extends BaseViewModel {

  private static final String TAG = "PartyMainViewModel";

  // Dependencies
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  // LiveData for party main state
  private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();
  private final MutableLiveData<List<User>> groupMembers = new MutableLiveData<>();
  private final MutableLiveData<List<User>> comingMembers = new MutableLiveData<>();
  private final MutableLiveData<List<User>> invitedMembers = new MutableLiveData<>();
  private final MutableLiveData<User> currentUser = new MutableLiveData<>();

  // User status within group
  private final MutableLiveData<Boolean> isUserMember = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isUserAdmin = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isUserComing = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isUserInvited = new MutableLiveData<>();

  // Group operations state
  private final MutableLiveData<Boolean> joinLeaveInProgress = new MutableLiveData<>();
  private final MutableLiveData<Boolean> statusUpdateInProgress = new MutableLiveData<>();
  private final MutableLiveData<String> lastActionResult = new MutableLiveData<>();

  // Group statistics
  private final MutableLiveData<Integer> totalMembersCount = new MutableLiveData<>();
  private final MutableLiveData<Integer> comingMembersCount = new MutableLiveData<>();
  private final MutableLiveData<Integer> invitedMembersCount = new MutableLiveData<>();

  // Current state
  private String currentUserKey;
  private String currentGroupKey;

  /**
   * Constructor for PartyMainViewModel.
   *
   * @param application The application context
   */
  public PartyMainViewModel(@NonNull Application application) {
    super(application);
    this.groupRepository = GroupRepository.getInstance();
    this.userRepository = UserRepository.getInstance();

    // Initialize state
    groupMembers.setValue(new ArrayList<>());
    comingMembers.setValue(new ArrayList<>());
    invitedMembers.setValue(new ArrayList<>());

    isUserMember.setValue(false);
    isUserAdmin.setValue(false);
    isUserComing.setValue(false);
    isUserInvited.setValue(false);

    joinLeaveInProgress.setValue(false);
    statusUpdateInProgress.setValue(false);

    totalMembersCount.setValue(0);
    comingMembersCount.setValue(0);
    invitedMembersCount.setValue(0);

    Log.d(TAG, "PartyMainViewModel initialized");
  }

  // Getters for LiveData

  public LiveData<Group> getCurrentGroup() {
    return currentGroup;
  }

  public LiveData<List<User>> getGroupMembers() {
    return groupMembers;
  }

  public LiveData<List<User>> getComingMembers() {
    return comingMembers;
  }

  public LiveData<List<User>> getInvitedMembers() {
    return invitedMembers;
  }

  public LiveData<User> getCurrentUser() {
    return currentUser;
  }

  public LiveData<Boolean> getIsUserMember() {
    return isUserMember;
  }

  public LiveData<Boolean> getIsUserAdmin() {
    return isUserAdmin;
  }

  public LiveData<Boolean> getIsUserComing() {
    return isUserComing;
  }

  public LiveData<Boolean> getIsUserInvited() {
    return isUserInvited;
  }

  public LiveData<Boolean> getJoinLeaveInProgress() {
    return joinLeaveInProgress;
  }

  public LiveData<Boolean> getStatusUpdateInProgress() {
    return statusUpdateInProgress;
  }

  public LiveData<String> getLastActionResult() {
    return lastActionResult;
  }

  public LiveData<Integer> getTotalMembersCount() {
    return totalMembersCount;
  }

  public LiveData<Integer> getComingMembersCount() {
    return comingMembersCount;
  }

  public LiveData<Integer> getInvitedMembersCount() {
    return invitedMembersCount;
  }

  /**
   * Initializes the ViewModel with user and group information.
   *
   * @param userKey The current user's key
   * @param groupKey The group key to display
   */
  public void initialize(@NonNull String userKey, @NonNull String groupKey) {
    this.currentUserKey = userKey;
    this.currentGroupKey = groupKey;

    Log.d(TAG, "Initialized with user: " + userKey + ", group: " + groupKey);

    // Load initial data
    loadGroupData();
    loadCurrentUser();
  }

  /** Loads the current group data and user relationships. */
  public void loadGroupData() {
    if (currentGroupKey == null) {
      setError("Group not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    setLoading(true);
    clearMessages();

    Log.d(TAG, "Loading group data for: " + currentGroupKey);

    ThreadUtils.runInBackground(
        () ->
            groupRepository.getGroup(
                currentGroupKey,
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Group group) {
                    handleGroupLoaded(group);
                  }

                  @Override
                  public void onError(Exception error) {
                    handleGroupLoadError(error);
                  }
                }));
  }

  /** Loads the current user information. */
  public void loadCurrentUser() {
    if (currentUserKey == null) {
      Log.w(TAG, "Cannot load current user - user key not set");
      return;
    }

    ThreadUtils.runInBackground(
        () ->
            userRepository.getUser(
                currentUserKey,
                new UserRepository.Callback<>() {
                  @Override
                  public void onSuccess(User user) {
                    ThreadUtils.runOnMainThread(
                        () -> {
                          currentUser.setValue(user);
                          Log.d(TAG, "Current user loaded: " + user.getUsername());
                        });
                  }

                  @Override
                  public void onError(Exception error) {
                    Log.w(TAG, "Failed to load current user", error);
                  }
                }));
  }

  /** Joins the current group. */
  public void joinGroup() {
    if (currentUserKey == null || currentGroupKey == null) {
      setError("User or group not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    if (joinLeaveInProgress.getValue() != null && joinLeaveInProgress.getValue()) {
      Log.w(TAG, "Join/leave operation already in progress");
      return;
    }

    joinLeaveInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Joining group: " + currentGroupKey);

    ThreadUtils.runInBackground(
        () ->
            groupRepository.joinGroup(
                currentGroupKey,
                currentUserKey,
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Boolean result) {
                    handleJoinSuccess();
                  }

                  @Override
                  public void onError(Exception error) {
                    handleJoinError(error);
                  }
                }));
  }

  /** Leaves the current group. */
  public void leaveGroup() {
    if (currentUserKey == null || currentGroupKey == null) {
      setError("User or group not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    if (joinLeaveInProgress.getValue() != null && joinLeaveInProgress.getValue()) {
      Log.w(TAG, "Join/leave operation already in progress");
      return;
    }

    joinLeaveInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Leaving group: " + currentGroupKey);

    ThreadUtils.runInBackground(
        () ->
            groupRepository.leaveGroup(
                currentGroupKey,
                currentUserKey,
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Boolean result) {
                    handleLeaveSuccess();
                  }

                  @Override
                  public void onError(Exception error) {
                    handleLeaveError(error);
                  }
                }));
  }

  /**
   * Updates the user's attendance status (coming/not coming).
   *
   * @param isComing Whether the user is coming to the event
   */
  public void updateAttendanceStatus(boolean isComing) {
    if (currentUserKey == null || currentGroupKey == null) {
      setError("User or group not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    if (!isCurrentUserMember()) {
      setError(
          "You must be a member to update attendance", NetworkUtils.ErrorType.PERMISSION_ERROR);
      return;
    }

    if (statusUpdateInProgress.getValue() != null && statusUpdateInProgress.getValue()) {
      Log.w(TAG, "Status update already in progress");
      return;
    }

    statusUpdateInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Updating attendance status to: " + isComing);

    ThreadUtils.runInBackground(
        () ->
            groupRepository.updateAttendanceStatus(
                currentGroupKey,
                currentUserKey,
                isComing,
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Boolean result) {
                    handleAttendanceUpdateSuccess(isComing);
                  }

                  @Override
                  public void onError(Exception error) {
                    handleAttendanceUpdateError(error);
                  }
                }));
  }

  /** Refreshes all group data. */
  public void refreshGroupData() {
    loadGroupData();
  }

  /**
   * Checks if the current user can perform admin actions.
   *
   * @return true if user is admin, false otherwise
   */
  public boolean canPerformAdminActions() {
    Boolean adminStatus = isUserAdmin.getValue();
    return adminStatus != null && adminStatus;
  }

  /**
   * Checks if the current user is a member of the group.
   *
   * @return true if user is a member, false otherwise
   */
  public boolean isCurrentUserMember() {
    Boolean memberStatus = isUserMember.getValue();
    return memberStatus != null && memberStatus;
  }

  /**
   * Gets the formatted group date string.
   *
   * @return formatted date string or null if no group loaded
   */
  @Nullable
  public String getFormattedGroupDate() {
    Group group = currentGroup.getValue();
    if (group == null) {
      return null;
    }

    String day = group.getGroupDays();
    String month = group.getGroupMonths();
    String year = group.getGroupYears();

    if (day != null && month != null && year != null) {
      return day + "/" + month + "/" + year;
    }

    return null;
  }

  /**
   * Gets the formatted group time string.
   *
   * @return formatted time string or null if no group loaded
   */
  @Nullable
  public String getFormattedGroupTime() {
    Group group = currentGroup.getValue();
    if (group == null) {
      return null;
    }

    String hour = group.getGroupHours();
    String minute = group.getGroupMinutes();

    if (hour != null && minute != null) {
      return hour + ":" + minute;
    }

    return null;
  }

  // Private helper methods

  private void handleGroupLoaded(Group group) {
    Log.d(TAG, "Group loaded successfully: " + group.getGroupName());

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          currentGroup.setValue(group);

          // Update user status within group
          updateUserStatus(group);

          // Load member details
          loadMemberDetails(group);

          // Update statistics
          updateStatistics(group);

          setSuccess("Group data loaded successfully");
        });
  }

  private void handleGroupLoadError(Exception error) {
    Log.e(TAG, "Failed to load group", error);

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);

          String errorMessage = error.getMessage();
          NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.UNKNOWN_ERROR;

          if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("not found")) {
              errorType = NetworkUtils.ErrorType.NOT_FOUND_ERROR;
            } else if (errorMessage.toLowerCase().contains("permission")) {
              errorType = NetworkUtils.ErrorType.PERMISSION_ERROR;
            }
          }

          setError(
              "Failed to load group: " + (errorMessage != null ? errorMessage : "Unknown error"),
              errorType);
        });
  }

  private void updateUserStatus(Group group) {
    if (currentUserKey == null) {
      return;
    }

    // Check if user is admin
    boolean admin = group.getAdminKey() != null && group.getAdminKey().equals(currentUserKey);
    isUserAdmin.setValue(admin);

    // Check if user is member
    boolean member =
        group.getFriendKeys() != null && group.getFriendKeys().containsKey(currentUserKey);
    isUserMember.setValue(member);

    // Check if user is coming
    boolean coming =
        group.getComingKeys() != null && group.getComingKeys().containsKey(currentUserKey);
    isUserComing.setValue(coming);

    // Check if user is invited (would need separate invitation tracking)
    isUserInvited.setValue(false);

    Log.d(
        TAG,
        "User status updated - Admin: " + admin + ", Member: " + member + ", Coming: " + coming);
  }

  private void loadMemberDetails(Group group) {
    ThreadUtils.runInBackground(
        () -> {
          // Load regular members
          loadUsersFromKeys(
              group.getFriendKeys(),
              users -> ThreadUtils.runOnMainThread(() -> groupMembers.setValue(users)));

          // Load coming members
          loadUsersFromKeys(
              group.getComingKeys(),
              users -> ThreadUtils.runOnMainThread(() -> comingMembers.setValue(users)));

          // Invited members would be loaded separately if we had invitation tracking
          ThreadUtils.runOnMainThread(() -> invitedMembers.setValue(new ArrayList<>()));
        });
  }

  private void loadUsersFromKeys(
      @Nullable HashMap<String, Object> userKeys, UserListCallback callback) {
    if (userKeys == null || userKeys.isEmpty()) {
      callback.onUsersLoaded(new ArrayList<>());
      return;
    }

    List<User> users = new ArrayList<>();
    int totalUsers = userKeys.size();
    int[] loadedCount = {0};

    for (String userKey : userKeys.keySet()) {
      userRepository.getUser(
          userKey,
          new UserRepository.Callback<>() {
            @Override
            public void onSuccess(User user) {
              synchronized (users) {
                users.add(user);
                loadedCount[0]++;

                if (loadedCount[0] == totalUsers) {
                  callback.onUsersLoaded(users);
                }
              }
            }

            @Override
            public void onError(Exception error) {
              Log.w(TAG, "Failed to load user: " + userKey, error);
              synchronized (users) {
                loadedCount[0]++;

                if (loadedCount[0] == totalUsers) {
                  callback.onUsersLoaded(users);
                }
              }
            }
          });
    }
  }

  private void updateStatistics(Group group) {
    int totalMembers = group.getFriendKeys() != null ? group.getFriendKeys().size() : 0;
    int comingCount = group.getComingKeys() != null ? group.getComingKeys().size() : 0;
    int invitedCount = 0; // Would be calculated from invitation tracking

    totalMembersCount.setValue(totalMembers);
    comingMembersCount.setValue(comingCount);
    invitedMembersCount.setValue(invitedCount);

    Log.d(
        TAG,
        "Statistics updated - Total: "
            + totalMembers
            + ", Coming: "
            + comingCount
            + ", Invited: "
            + invitedCount);
  }

  private void handleJoinSuccess() {
    Log.d(TAG, "Successfully joined group");

    ThreadUtils.runOnMainThread(
        () -> {
          joinLeaveInProgress.setValue(false);
          setSuccess("Successfully joined the group!");
          lastActionResult.setValue("joined");

          // Refresh group data to update membership status
          loadGroupData();
        });
  }

  private void handleJoinError(Exception error) {
    Log.e(TAG, "Failed to join group", error);

    ThreadUtils.runOnMainThread(
        () -> {
          joinLeaveInProgress.setValue(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to join group: " + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  private void handleLeaveSuccess() {
    Log.d(TAG, "Successfully left group");

    ThreadUtils.runOnMainThread(
        () -> {
          joinLeaveInProgress.setValue(false);
          setSuccess("Successfully left the group");
          lastActionResult.setValue("left");

          // Refresh group data to update membership status
          loadGroupData();
        });
  }

  private void handleLeaveError(Exception error) {
    Log.e(TAG, "Failed to leave group", error);

    ThreadUtils.runOnMainThread(
        () -> {
          joinLeaveInProgress.setValue(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to leave group: " + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  private void handleAttendanceUpdateSuccess(boolean isComing) {
    Log.d(TAG, "Attendance status updated successfully to: " + isComing);

    ThreadUtils.runOnMainThread(
        () -> {
          statusUpdateInProgress.setValue(false);
          isUserComing.setValue(isComing);

          String message =
              isComing
                  ? "You're now attending this event!"
                  : "You're no longer attending this event";
          setSuccess(message);
          lastActionResult.setValue("attendance_" + isComing);

          // Refresh group data to update counts
          loadGroupData();
        });
  }

  private void handleAttendanceUpdateError(Exception error) {
    Log.e(TAG, "Failed to update attendance status", error);

    ThreadUtils.runOnMainThread(
        () -> {
          statusUpdateInProgress.setValue(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to update attendance: "
                  + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  // Interface for user list loading callback
  private interface UserListCallback {
    void onUsersLoaded(List<User> users);
  }

  /** Clears all party main data and resets state. */
  public void clearPartyMainData() {
    currentGroup.setValue(null);
    groupMembers.setValue(new ArrayList<>());
    comingMembers.setValue(new ArrayList<>());
    invitedMembers.setValue(new ArrayList<>());
    currentUser.setValue(null);

    isUserMember.setValue(false);
    isUserAdmin.setValue(false);
    isUserComing.setValue(false);
    isUserInvited.setValue(false);

    joinLeaveInProgress.setValue(false);
    statusUpdateInProgress.setValue(false);
    lastActionResult.setValue(null);

    totalMembersCount.setValue(0);
    comingMembersCount.setValue(0);
    invitedMembersCount.setValue(0);

    currentUserKey = null;
    currentGroupKey = null;

    clearMessages();

    Log.d(TAG, "Party main data cleared");
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearPartyMainData();
    Log.d(TAG, "PartyMainViewModel cleared");
  }
}
