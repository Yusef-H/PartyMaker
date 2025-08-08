package com.example.partymaker.viewmodel.core;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.Result;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for managing Group data operations in MainActivity.
 *
 * <p>This ViewModel provides a clean API for the UI to interact with Group data, including:
 *
 * <ul>
 *   <li>Loading user-specific groups
 *   <li>Creating, updating, and deleting groups
 *   <li>Managing group membership (join/leave operations)
 *   <li>Handling group selection for navigation
 * </ul>
 *
 * <p>Extends BaseViewModel to inherit common UI state management functionality.
 *
 * @author PartyMaker Team
 * @version 2.0
 * @see BaseViewModel
 * @since 1.0
 */
public class MainActivityViewModel extends BaseViewModel {

  /** Tag for logging specific to MainActivityViewModel */
  private static final String TAG = "MainActivityViewModel";

  // Domain-specific LiveData
  private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>();
  private final MutableLiveData<Group> selectedGroup = new MutableLiveData<>();

  /** Repository for Group data operations */
  private final GroupRepository repository;

  /**
   * Constructor for MainActivityViewModel.
   *
   * @param application The application context
   */
  public MainActivityViewModel(@NonNull Application application) {
    super(application);
    repository = GroupRepository.getInstance();

    // Initialize with live data from repository if available
    LiveData<Result<List<Group>>> repoGroups = repository.getAllGroupsLiveData();
    if (repoGroups != null) {
      // Observe the repository's LiveData
      repoGroups.observeForever(
          result -> {
            if (result.isSuccess()) {
              List<Group> data = result.getData();
              if (data != null) {
                sortGroups(data);
                groupList.setValue(data);
              }
            } else if (result.isError()) {
              setError(result.getUserFriendlyError());
            }
            setLoading(result.isLoading());
          });
    }
  }

  /**
   * Gets the list of groups for the current user.
   *
   * @return LiveData containing the user's groups, may be empty but never null
   */
  public LiveData<List<Group>> getGroups() {
    return groupList;
  }

  /**
   * Gets the currently selected group.
   *
   * @return LiveData containing the selected group, null if no group is selected
   */
  public LiveData<Group> getSelectedGroup() {
    return selectedGroup;
  }

  // Note: getIsLoading() and getErrorMessage() are inherited from BaseViewModel

  /**
   * Loads groups for a specific user.
   *
   * @param userKey The user key to load groups for
   * @param forceRefresh Whether to force a refresh from the server
   * @throws IllegalArgumentException if userKey is null or empty
   */
  public void loadUserGroups(@NonNull String userKey, boolean forceRefresh) {
    if (userKey.trim().isEmpty()) {
      throw new IllegalArgumentException("User key cannot be null or empty");
    }

    Log.d(TAG, "Loading groups for user: " + userKey + ", forceRefresh: " + forceRefresh);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.getUserGroups(
              userKey,
              result -> {
                if (result.isLoading()) {
                  setLoading(true);
                } else if (result.isSuccess()) {
                  handleSuccessfulGroupsLoad(result.getData(), "User groups loaded successfully");
                } else if (result.isError()) {
                  Log.e(TAG, "Error loading user groups: " + result.getError());
                  setError(result.getUserFriendlyError());
                }

                if (!result.isLoading()) {
                  setLoading(false);
                }
              },
              forceRefresh);
        });
  }

  /**
   * Loads all groups available in the system.
   *
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void loadAllGroups(boolean forceRefresh) {
    Log.d(TAG, "Loading all groups, forceRefresh: " + forceRefresh);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.getAllGroups(
              groups -> {
                handleSuccessfulGroupsLoad(groups, "All groups loaded successfully");
                setLoading(false);
              },
              forceRefresh);
        });
  }

  /**
   * Loads a specific group by its ID.
   *
   * @param groupId The group ID to load
   * @param forceRefresh Whether to force a refresh from the server
   * @throws IllegalArgumentException if groupId is null or empty
   */
  public void loadGroup(@NonNull String groupId, boolean forceRefresh) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException("Group ID cannot be null or empty");
    }

    Log.d(TAG, "Loading group with ID: " + groupId + ", forceRefresh: " + forceRefresh);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.getGroup(
              groupId,
              group -> {
                if (group != null) {
                  Log.d(TAG, "Group loaded successfully: " + group.getGroupName());
                  selectedGroup.setValue(group);
                } else {
                  Log.e(TAG, "Group not found: " + groupId);
                  setError("Group not found");
                }
                setLoading(false);
              },
              forceRefresh);
        });
  }

  /**
   * Creates a new group.
   *
   * @param groupId The unique group ID
   * @param group The group object to create
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void createGroup(@NonNull String groupId, @NonNull Group group) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException("Group ID cannot be null or empty");
    }

    Log.d(TAG, "Creating new group: " + group.getGroupName());

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.saveGroup(
              groupId,
              group,
              () -> {
                Log.d(TAG, "Group created successfully");
                addGroupToList(group);
                selectedGroup.setValue(group);
                setSuccess("Group created successfully");
                setLoading(false);
              });
        });
  }

  /**
   * Updates an existing group with the provided field updates.
   *
   * @param groupId The group ID to update
   * @param updates Map of field names to new values
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void updateGroup(@NonNull String groupId, @NonNull Map<String, Object> updates) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException("Group ID cannot be null or empty");
    }
    if (updates.isEmpty()) {
      throw new IllegalArgumentException("Updates map cannot be null or empty");
    }

    Log.d(TAG, "Updating group: " + groupId + " with " + updates.size() + " changes");

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.updateGroup(
              groupId,
              updates,
              () -> {
                Log.d(TAG, "Group updated successfully");
                applyUpdatesToLocalGroups(groupId, updates);
                setSuccess("Group updated successfully");
                setLoading(false);
              });
        });
  }

  /**
   * Deletes an existing group.
   *
   * @param groupId The group ID to delete
   * @throws IllegalArgumentException if groupId is invalid
   */
  public void deleteGroup(@NonNull String groupId) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException("Group ID cannot be null or empty");
    }

    Log.d(TAG, "Deleting group: " + groupId);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.deleteGroup(
              groupId,
              () -> {
                Log.d(TAG, "Group deleted successfully");
                removeGroupFromList(groupId);
                clearSelectedGroupIfMatches(groupId);
                setSuccess("Group deleted successfully");
                setLoading(false);
              });
        });
  }

  /**
   * Joins a group for the specified user.
   *
   * @param groupId The group ID to join
   * @param userKey The user key of the user joining
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void joinGroup(@NonNull String groupId, @NonNull String userKey) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException("Group ID cannot be null or empty");
    }
    if (userKey.trim().isEmpty()) {
      throw new IllegalArgumentException("User key cannot be null or empty");
    }

    Log.d(TAG, "Joining group: " + groupId + " for user: " + userKey);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.joinGroup(
              groupId,
              userKey,
              new GroupRepository.OperationCallback() {
                @Override
                public void onComplete() {
                  Log.d(TAG, "Successfully joined group: " + groupId);
                  setSuccess("Successfully joined group");
                  setLoading(false);
                  // Refresh the group data to show the updated membership
                  loadGroup(groupId, true);
                }

                @Override
                public void onError(String error) {
                  Log.e(TAG, "Error joining group: " + error);
                  setError("Failed to join group: " + error);
                  setLoading(false);
                }
              });
        });
  }

  /**
   * Leaves a group for the specified user.
   *
   * @param groupId The group ID to leave
   * @param userKey The user key of the user leaving
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void leaveGroup(@NonNull String groupId, @NonNull String userKey) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException("Group ID cannot be null or empty");
    }
    if (userKey.trim().isEmpty()) {
      throw new IllegalArgumentException("User key cannot be null or empty");
    }

    Log.d(TAG, "Leaving group: " + groupId + " for user: " + userKey);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.leaveGroup(
              groupId,
              userKey,
              new GroupRepository.OperationCallback() {
                @Override
                public void onComplete() {
                  Log.d(TAG, "Successfully left group: " + groupId);
                  removeGroupFromList(groupId);
                  clearSelectedGroupIfMatches(groupId);
                  setSuccess("Successfully left group");
                  setLoading(false);
                }

                @Override
                public void onError(String error) {
                  Log.e(TAG, "Error leaving group: " + error);
                  setError("Failed to leave group: " + error);
                  setLoading(false);
                }
              });
        });
  }

  /**
   * Selects a group by loading it and setting it as the current selection.
   *
   * @param groupId The group ID to select
   * @param forceRefresh Whether to force a refresh from the server
   * @throws IllegalArgumentException if groupId is invalid
   */
  public void selectGroup(@NonNull String groupId, boolean forceRefresh) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException("Group ID cannot be null or empty");
    }

    Log.d(TAG, "Selecting group: " + groupId + ", forceRefresh: " + forceRefresh);
    loadGroup(groupId, forceRefresh);
  }

  /**
   * Adds a group to the current group list and sorts it.
   *
   * @param group The group to add
   */
  private void addGroupToList(@NonNull Group group) {
    List<Group> currentList = groupList.getValue();
    if (currentList == null) {
      currentList = new ArrayList<>();
    } else {
      currentList = new ArrayList<>(currentList); // Create mutable copy
    }

    currentList.add(group);
    sortGroups(currentList);
    groupList.setValue(currentList);
  }

  /**
   * Applies updates to both the group list and selected group if they match the given ID.
   *
   * @param groupId The ID of the group to update
   * @param updates The updates to apply
   */
  private void applyUpdatesToLocalGroups(
      @NonNull String groupId, @NonNull Map<String, Object> updates) {
    // Update the group in the list
    List<Group> currentList = groupList.getValue();
    if (currentList != null) {
      for (Group group : currentList) {
        if (groupId.equals(group.getGroupKey())) {
          for (Map.Entry<String, Object> entry : updates.entrySet()) {
            applyUpdateToGroup(group, entry.getKey(), entry.getValue());
          }
          break;
        }
      }
      groupList.setValue(currentList); // Trigger observers
    }

    // Update selected group if it matches
    Group selectedGroupValue = selectedGroup.getValue();
    if (selectedGroupValue != null && groupId.equals(selectedGroupValue.getGroupKey())) {
      for (Map.Entry<String, Object> entry : updates.entrySet()) {
        applyUpdateToGroup(selectedGroupValue, entry.getKey(), entry.getValue());
      }
      selectedGroup.setValue(selectedGroupValue); // Trigger observers
    }
  }

  /**
   * Removes a group from the current group list.
   *
   * @param groupId The ID of the group to remove
   */
  private void removeGroupFromList(@NonNull String groupId) {
    List<Group> currentList = groupList.getValue();
    if (currentList != null) {
      List<Group> newList = new ArrayList<>(currentList);
      newList.removeIf(group -> groupId.equals(group.getGroupKey()));
      groupList.setValue(newList);
    }
  }

  /**
   * Clears the selected group if it matches the given ID.
   *
   * @param groupId The ID to check against
   */
  private void clearSelectedGroupIfMatches(@NonNull String groupId) {
    Group selectedGroupValue = selectedGroup.getValue();
    if (selectedGroupValue != null && groupId.equals(selectedGroupValue.getGroupKey())) {
      selectedGroup.setValue(null);
    }
  }

  /** Clears all data from the ViewModel. Typically used during logout or when switching users. */
  public void clearAllData() {
    Log.d(TAG, "Clearing all ViewModel data");
    groupList.setValue(new ArrayList<>());
    selectedGroup.setValue(null);
    clearMessages();
    setLoading(false);
  }

  /**
   * Helper method to handle successful groups loading with common logic.
   *
   * @param groups The loaded groups data
   * @param successMessage The success message to log
   */
  private void handleSuccessfulGroupsLoad(
      @Nullable List<Group> groups, @NonNull String successMessage) {
    if (groups != null) {
      Log.d(TAG, successMessage + ": " + groups.size() + " groups");
      sortGroups(groups);
      groupList.setValue(groups);
    } else {
      Log.w(TAG, successMessage + " but data is null - setting empty list");
      groupList.setValue(new ArrayList<>());
    }
  }

  /**
   * Sorts the groups by name in a case-insensitive manner. Groups with null names are placed at the
   * end.
   *
   * @param groups The groups to sort (modified in place)
   */
  private void sortGroups(@Nullable List<Group> groups) {
    if (groups == null || groups.isEmpty()) {
      return;
    }

    groups.sort(
        (g1, g2) -> {
          String name1 = g1 != null ? g1.getGroupName() : null;
          String name2 = g2 != null ? g2.getGroupName() : null;

          if (name1 == null && name2 == null) {
            return 0;
          } else if (name1 == null) {
            return 1; // null names go to the end
          } else if (name2 == null) {
            return -1; // null names go to the end
          }
          return name1.compareToIgnoreCase(name2);
        });
  }

  /**
   * Applies an update to a group
   *
   * @param group The group to update
   * @param field The field to update
   * @param value The new value
   */
  @SuppressWarnings("unchecked")
  private void applyUpdateToGroup(Group group, String field, Object value) {
    if (group == null || field == null) {
      return;
    }

    switch (field) {
      case "groupName":
        if (value instanceof String) {
          group.setGroupName((String) value);
        }
        break;
      case "groupLocation":
        if (value instanceof String) {
          group.setGroupLocation((String) value);
        }
        break;
      case "groupDays":
        if (value instanceof String) {
          group.setGroupDays((String) value);
        }
        break;
      case "groupMonths":
        if (value instanceof String) {
          group.setGroupMonths((String) value);
        }
        break;
      case "groupYears":
        if (value instanceof String) {
          group.setGroupYears((String) value);
        }
        break;
      case "groupHours":
        if (value instanceof String) {
          group.setGroupHours((String) value);
        }
        break;
      case "groupPrice":
        if (value instanceof String) {
          group.setGroupPrice((String) value);
        } else if (value instanceof Double || value instanceof Integer) {
          group.setGroupPrice(String.valueOf(value));
        }
        break;
      case "groupType":
        if (value instanceof Integer) {
          group.setGroupType((Integer) value);
        } else if (value instanceof String) {
          try {
            group.setGroupType(Integer.parseInt((String) value));
          } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid group type format: " + value);
          }
        }
        break;
      case "canAdd":
        if (value instanceof Boolean) {
          group.setCanAdd((Boolean) value);
        }
        break;
      case "groupDescription":
        if (value instanceof String) {
          group.setGroupDescription((String) value);
        }
        break;
      case "friendKeys":
        if (value instanceof HashMap) {
          group.setFriendKeys((HashMap<String, Object>) value);
        }
        break;
      case "comingKeys":
        if (value instanceof HashMap) {
          group.setComingKeys((HashMap<String, Object>) value);
        }
        break;
      case "messageKeys":
        if (value instanceof HashMap) {
          group.setMessageKeys((HashMap<String, Object>) value);
        }
        break;
      default:
        Log.w(TAG, "Unknown field: " + field);
        break;
    }
  }
}
