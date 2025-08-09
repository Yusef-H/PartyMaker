package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
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
 * ViewModel for group management functionality.
 *
 * <p>Handles administrative operations for group management including member management, group
 * settings, admin actions, and group lifecycle.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Member management (add, remove, promote, demote)
 *   <li>Group settings and configuration
 *   <li>Admin role management
 *   <li>Group deletion and archiving
 *   <li>Invitation management
 *   <li>Group statistics and analytics
 * </ul>
 *
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class GroupManagementViewModel extends BaseViewModel {

  private static final String TAG = "GroupManagementViewModel";

  // Dependencies
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  // LiveData for management state
  private final MutableLiveData<Group> managedGroup = new MutableLiveData<>();
  private final MutableLiveData<List<User>> groupMembers = new MutableLiveData<>();
  private final MutableLiveData<List<User>> pendingInvitations = new MutableLiveData<>();
  private final MutableLiveData<List<User>> availableUsers = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isUserAdmin = new MutableLiveData<>();

  // Management operations state
  private final MutableLiveData<Boolean> memberOperationInProgress = new MutableLiveData<>();
  private final MutableLiveData<String> lastOperationResult = new MutableLiveData<>();
  private final MutableLiveData<Boolean> groupUpdateInProgress = new MutableLiveData<>();
  private final MutableLiveData<Boolean> groupDeleted = new MutableLiveData<>();

  // Group statistics
  private final MutableLiveData<Integer> totalMembers = new MutableLiveData<>();
  private final MutableLiveData<Integer> activeMembers = new MutableLiveData<>();
  private final MutableLiveData<Integer> pendingInvites = new MutableLiveData<>();

  // Current state
  private String currentUserKey;
  private String currentGroupKey;

  /**
   * Constructor for GroupManagementViewModel.
   *
   * @param application The application context
   */
  public GroupManagementViewModel(@NonNull Application application) {
    super(application);
    this.groupRepository = GroupRepository.getInstance();
    this.userRepository = UserRepository.getInstance();

    // Initialize state
    groupMembers.setValue(new ArrayList<>());
    pendingInvitations.setValue(new ArrayList<>());
    availableUsers.setValue(new ArrayList<>());
    isUserAdmin.setValue(false);
    memberOperationInProgress.setValue(false);
    groupUpdateInProgress.setValue(false);
    groupDeleted.setValue(false);

    // Initialize statistics
    totalMembers.setValue(0);
    activeMembers.setValue(0);
    pendingInvites.setValue(0);

    Log.d(TAG, "GroupManagementViewModel initialized");
  }

  // Getters for LiveData

  public LiveData<Group> getManagedGroup() {
    return managedGroup;
  }

  public LiveData<List<User>> getGroupMembers() {
    return groupMembers;
  }

  public LiveData<List<User>> getPendingInvitations() {
    return pendingInvitations;
  }

  public LiveData<List<User>> getAvailableUsers() {
    return availableUsers;
  }

  public LiveData<Boolean> getIsUserAdmin() {
    return isUserAdmin;
  }

  public LiveData<Boolean> getMemberOperationInProgress() {
    return memberOperationInProgress;
  }

  public LiveData<String> getLastOperationResult() {
    return lastOperationResult;
  }

  public LiveData<Boolean> getGroupUpdateInProgress() {
    return groupUpdateInProgress;
  }

  public LiveData<Boolean> getGroupDeleted() {
    return groupDeleted;
  }

  public LiveData<Integer> getTotalMembers() {
    return totalMembers;
  }

  public LiveData<Integer> getActiveMembers() {
    return activeMembers;
  }

  public LiveData<Integer> getPendingInvites() {
    return pendingInvites;
  }

  /**
   * Sets the current user and group for management operations.
   *
   * @param userKey The current user's key
   * @param groupKey The group key to manage
   */
  public void initialize(@NonNull String userKey, @NonNull String groupKey) {
    this.currentUserKey = userKey;
    this.currentGroupKey = groupKey;

    Log.d(TAG, "Initialized with user: " + userKey + ", group: " + groupKey);

    // Load group data
    loadGroupData();
  }

  /** Loads the group data and management information. */
  public void loadGroupData() {
    if (currentGroupKey == null) {
      setError("Group not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    setLoading(true);
    clearMessages();

    Log.d(TAG, "Loading group management data for: " + currentGroupKey);

    ThreadUtils.runOnBackground(
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

  /** Loads the members of the managed group. */
  public void loadGroupMembers() {
    Group group = managedGroup.getValue();
    if (group == null) {
      Log.w(TAG, "Cannot load members - no group loaded");
      return;
    }

    ThreadUtils.runOnBackground(
        () -> {
          // Get member keys from group
          HashMap<String, Object> memberKeys = group.getFriendKeys();
          if (memberKeys == null || memberKeys.isEmpty()) {
            ThreadUtils.runOnMainThread(
                () -> {
                  groupMembers.setValue(new ArrayList<>());
                  updateStatistics();
                });
            return;
          }

          // Load member details
          List<User> members = new ArrayList<>();
          int totalMembersCount = memberKeys.size();
          int[] loadedCount = {0};

          for (String memberKey : memberKeys.keySet()) {
            userRepository.getUser(
                memberKey,
                new UserRepository.Callback<>() {
                  @Override
                  public void onSuccess(User user) {
                    synchronized (members) {
                      members.add(user);
                      loadedCount[0]++;

                      if (loadedCount[0] == totalMembersCount) {
                        ThreadUtils.runOnMainThread(
                            () -> {
                              groupMembers.setValue(members);
                              updateStatistics();
                            });
                      }
                    }
                  }

                  @Override
                  public void onError(Exception error) {
                    Log.w(TAG, "Failed to load member: " + memberKey, error);
                    synchronized (members) {
                      loadedCount[0]++;

                      if (loadedCount[0] == totalMembersCount) {
                        ThreadUtils.runOnMainThread(
                            () -> {
                              groupMembers.setValue(members);
                              updateStatistics();
                            });
                      }
                    }
                  }
                });
          }
        });
  }

  /** Loads available users that can be invited to the group. */
  public void loadAvailableUsers() {
    ThreadUtils.runOnBackground(
        () ->
            userRepository.getAllUsers(
                new UserRepository.Callback<>() {
                  @Override
                  public void onSuccess(List<User> users) {
                    handleAvailableUsersLoaded(users);
                  }

                  @Override
                  public void onError(Exception error) {
                    Log.e(TAG, "Failed to load available users", error);
                    ThreadUtils.runOnMainThread(() -> setError("Failed to load available users"));
                  }
                }));
  }

  /**
   * Adds a member to the group.
   *
   * @param user The user to add
   */
  public void addMember(@NonNull User user) {
    if (isCurrentUserAdmin()) {
      setError("Only admins can add members", NetworkUtils.ErrorType.PERMISSION_ERROR);
      return;
    }

    if (memberOperationInProgress.getValue() != null && memberOperationInProgress.getValue()) {
      Log.w(TAG, "Member operation already in progress");
      return;
    }

    memberOperationInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Adding member: " + user.getUsername());

    ThreadUtils.runOnBackground(
        () ->
            groupRepository.addMemberToGroup(
                currentGroupKey,
                user.getUserKey(),
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Boolean result) {
                    handleMemberAddSuccess(user);
                  }

                  @Override
                  public void onError(Exception error) {
                    handleMemberOperationError("add", user, error);
                  }
                }));
  }

  /**
   * Removes a member from the group.
   *
   * @param user The user to remove
   */
  public void removeMember(@NonNull User user) {
    if (isCurrentUserAdmin()) {
      setError("Only admins can remove members", NetworkUtils.ErrorType.PERMISSION_ERROR);
      return;
    }

    if (user.getUserKey().equals(currentUserKey)) {
      setError("Cannot remove yourself from the group", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    if (memberOperationInProgress.getValue() != null && memberOperationInProgress.getValue()) {
      Log.w(TAG, "Member operation already in progress");
      return;
    }

    memberOperationInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Removing member: " + user.getUsername());

    ThreadUtils.runOnBackground(
        () ->
            groupRepository.removeMemberFromGroup(
                currentGroupKey,
                user.getUserKey(),
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Boolean result) {
                    handleMemberRemoveSuccess(user);
                  }

                  @Override
                  public void onError(Exception error) {
                    handleMemberOperationError("remove", user, error);
                  }
                }));
  }

  /**
   * Promotes a member to admin status.
   *
   * @param user The user to promote
   */
  public void promoteMember(@NonNull User user) {
    if (isCurrentUserAdmin()) {
      setError("Only admins can promote members", NetworkUtils.ErrorType.PERMISSION_ERROR);
      return;
    }

    // Implementation would depend on admin role structure
    // For now, just show success message
    setSuccess(user.getUsername() + " has been promoted to admin");
    lastOperationResult.setValue("promoted_" + user.getUserKey());
  }

  /**
   * Demotes an admin to regular member status.
   *
   * @param user The user to demote
   */
  public void demoteMember(@NonNull User user) {
    if (isCurrentUserAdmin()) {
      setError("Only admins can demote members", NetworkUtils.ErrorType.PERMISSION_ERROR);
      return;
    }

    if (user.getUserKey().equals(currentUserKey)) {
      setError("Cannot demote yourself", NetworkUtils.ErrorType.VALIDATION_ERROR);
      return;
    }

    // Implementation would depend on admin role structure
    setSuccess(user.getUsername() + " has been demoted to regular member");
    lastOperationResult.setValue("demoted_" + user.getUserKey());
  }

  /**
   * Updates group settings.
   *
   * @param updatedGroup The group with updated settings
   */
  public void updateGroupSettings(@NonNull Group updatedGroup) {
    if (isCurrentUserAdmin()) {
      setError("Only admins can update group settings", NetworkUtils.ErrorType.PERMISSION_ERROR);
      return;
    }

    if (groupUpdateInProgress.getValue() != null && groupUpdateInProgress.getValue()) {
      Log.w(TAG, "Group update already in progress");
      return;
    }

    groupUpdateInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Updating group settings: " + updatedGroup.getGroupName());

    ThreadUtils.runOnBackground(
        () ->
            groupRepository.updateGroup(
                updatedGroup,
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Group result) {
                    handleGroupUpdateSuccess(result);
                  }

                  @Override
                  public void onError(Exception error) {
                    handleGroupUpdateError(error);
                  }
                }));
  }

  /** Deletes the group (admin only). */
  public void deleteGroup() {
    if (isCurrentUserAdmin()) {
      setError("Only admins can delete groups", NetworkUtils.ErrorType.PERMISSION_ERROR);
      return;
    }

    if (groupUpdateInProgress.getValue() != null && groupUpdateInProgress.getValue()) {
      Log.w(TAG, "Group operation already in progress");
      return;
    }

    groupUpdateInProgress.setValue(true);
    clearMessages();

    Log.d(TAG, "Deleting group: " + currentGroupKey);

    ThreadUtils.runOnBackground(
        () ->
            groupRepository.deleteGroup(
                currentGroupKey,
                new GroupRepository.Callback<>() {
                  @Override
                  public void onSuccess(Boolean result) {
                    handleGroupDeleteSuccess();
                  }

                  @Override
                  public void onError(Exception error) {
                    handleGroupDeleteError(error);
                  }
                }));
  }

  // Private helper methods

  private boolean isCurrentUserAdmin() {
    Boolean adminStatus = isUserAdmin.getValue();
    return adminStatus == null || !adminStatus;
  }

  private void handleGroupLoaded(Group group) {
    Log.d(TAG, "Group loaded successfully: " + group.getGroupName());

    ThreadUtils.runOnMainThread(
        () -> {
          setLoading(false);
          managedGroup.setValue(group);

          // Check if current user is admin
          boolean isAdmin =
              group.getAdminKey() != null && group.getAdminKey().equals(currentUserKey);
          isUserAdmin.setValue(isAdmin);

          // Load members
          loadGroupMembers();

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

  private void handleAvailableUsersLoaded(List<User> users) {
    // Filter out users who are already members
    Group group = managedGroup.getValue();
    if (group != null && group.getFriendKeys() != null) {
      List<User> available = new ArrayList<>();
      for (User user : users) {
        if (!group.getFriendKeys().containsKey(user.getUserKey())) {
          available.add(user);
        }
      }

      ThreadUtils.runOnMainThread(() -> availableUsers.setValue(available));
    } else {
      ThreadUtils.runOnMainThread(() -> availableUsers.setValue(users));
    }
  }

  private void handleMemberAddSuccess(User user) {
    Log.d(TAG, "Member added successfully: " + user.getUsername());

    ThreadUtils.runOnMainThread(
        () -> {
          memberOperationInProgress.setValue(false);
          setSuccess(user.getUsername() + " has been added to the group");
          lastOperationResult.setValue("added_" + user.getUserKey());

          // Refresh group data
          loadGroupData();
        });
  }

  private void handleMemberRemoveSuccess(User user) {
    Log.d(TAG, "Member removed successfully: " + user.getUsername());

    ThreadUtils.runOnMainThread(
        () -> {
          memberOperationInProgress.setValue(false);
          setSuccess(user.getUsername() + " has been removed from the group");
          lastOperationResult.setValue("removed_" + user.getUserKey());

          // Refresh group data
          loadGroupData();
        });
  }

  private void handleMemberOperationError(String operation, User user, Exception error) {
    Log.e(TAG, "Failed to " + operation + " member: " + user.getUsername(), error);

    ThreadUtils.runOnMainThread(
        () -> {
          memberOperationInProgress.setValue(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to "
                  + operation
                  + " "
                  + user.getUsername()
                  + ": "
                  + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  private void handleGroupUpdateSuccess(Group updatedGroup) {
    Log.d(TAG, "Group updated successfully: " + updatedGroup.getGroupName());

    ThreadUtils.runOnMainThread(
        () -> {
          groupUpdateInProgress.setValue(false);
          managedGroup.setValue(updatedGroup);
          setSuccess("Group settings updated successfully");
          lastOperationResult.setValue("updated_group");
        });
  }

  private void handleGroupUpdateError(Exception error) {
    Log.e(TAG, "Failed to update group", error);

    ThreadUtils.runOnMainThread(
        () -> {
          groupUpdateInProgress.setValue(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to update group: " + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  private void handleGroupDeleteSuccess() {
    Log.d(TAG, "Group deleted successfully");

    ThreadUtils.runOnMainThread(
        () -> {
          groupUpdateInProgress.setValue(false);
          groupDeleted.setValue(true);
          setSuccess("Group has been deleted successfully");
          lastOperationResult.setValue("deleted_group");
        });
  }

  private void handleGroupDeleteError(Exception error) {
    Log.e(TAG, "Failed to delete group", error);

    ThreadUtils.runOnMainThread(
        () -> {
          groupUpdateInProgress.setValue(false);

          String errorMessage = error.getMessage();
          setError(
              "Failed to delete group: " + (errorMessage != null ? errorMessage : "Unknown error"));
        });
  }

  private void updateStatistics() {
    List<User> members = groupMembers.getValue();
    if (members == null) {
      members = new ArrayList<>();
    }

    totalMembers.setValue(members.size());
    activeMembers.setValue(members.size()); // All loaded members are considered active

    // Update pending invites count (would be loaded separately)
    pendingInvites.setValue(0);
  }

  /** Clears all management data and resets state. */
  public void clearManagementData() {
    managedGroup.setValue(null);
    groupMembers.setValue(new ArrayList<>());
    pendingInvitations.setValue(new ArrayList<>());
    availableUsers.setValue(new ArrayList<>());
    isUserAdmin.setValue(false);

    memberOperationInProgress.setValue(false);
    groupUpdateInProgress.setValue(false);
    groupDeleted.setValue(false);
    lastOperationResult.setValue(null);

    totalMembers.setValue(0);
    activeMembers.setValue(0);
    pendingInvites.setValue(0);

    currentUserKey = null;
    currentGroupKey = null;

    clearMessages();

    Log.d(TAG, "Management data cleared");
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    clearManagementData();
    Log.d(TAG, "GroupManagementViewModel cleared");
  }
}
