package com.example.partymaker.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.partymaker.data.api.Result;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for Group data. This class provides a clean API for the UI to interact with Group data.
 */
public class GroupViewModel extends ViewModel {
    private static final String TAG = "GroupViewModel";

    /**
     * List of all groups.
     */
    private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>(new ArrayList<>());

    /**
     * Currently selected group.
     */
    private final MutableLiveData<Group> selectedGroup = new MutableLiveData<>();

    /**
     * Loading state.
     */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /**
     * Error message.
     */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Repository for Group data.
     */
    private final GroupRepository repository;

    /**
     * Constructor.
     */
    public GroupViewModel() {
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
                            errorMessage.setValue(result.getUserFriendlyError());
                        }
                        isLoading.setValue(result.isLoading());
                    });
        }
    }

    /**
     * Gets the list of groups.
     *
     * @return LiveData for the list of groups
     */
    public LiveData<List<Group>> getGroups() {
        return groupList;
    }

    /**
     * Gets the selected group.
     *
     * @return LiveData for the selected group
     */
    public LiveData<Group> getSelectedGroup() {
        return selectedGroup;
    }

    /**
     * Gets the loading state.
     *
     * @return LiveData for the loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Gets the error message.
     *
     * @return LiveData for the error message
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Loads groups for a specific user
     *
     * @param userKey      The user key
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void loadUserGroups(String userKey, boolean forceRefresh) {
        Log.d(TAG, "Loading groups for user: " + userKey);
        isLoading.setValue(true);

        repository.getUserGroups(
                userKey,
                result -> {
                    if (result.isLoading()) {
                        isLoading.setValue(true);
                    } else if (result.isSuccess()) {
                        List<Group> data = result.getData();
                        if (data != null) {
                            Log.d(TAG, "User groups loaded successfully: " + data.size() + " groups");
                            sortGroups(data);
                            groupList.setValue(data);
                        }
                        isLoading.setValue(false);
                    } else if (result.isError()) {
                        Log.e(TAG, "Error loading user groups: " + result.getError());
                        errorMessage.setValue(result.getUserFriendlyError());
                        isLoading.setValue(false);
                    }
                },
                forceRefresh);
    }

    /**
     * Loads all groups
     *
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void loadAllGroups(boolean forceRefresh) {
        Log.d(TAG, "Loading all groups");
        isLoading.setValue(true);

        repository.getAllGroups(
                groups -> {
                    if (groups != null) {
                        Log.d(TAG, "All groups loaded successfully: " + groups.size() + " groups");
                        sortGroups(groups);
                        groupList.setValue(groups);
                    }
                    isLoading.setValue(false);
                },
                forceRefresh);
    }

    /**
     * Loads a specific group by its ID
     *
     * @param groupId      The group ID
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void loadGroup(String groupId, boolean forceRefresh) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot load group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }

        Log.d(TAG, "Loading group with ID: " + groupId);
        isLoading.setValue(true);

        repository.getGroup(
                groupId,
                group -> {
                    if (group != null) {
                        Log.d(TAG, "Group loaded successfully: " + group.getGroupName());
                        selectedGroup.setValue(group);
                    } else {
                        Log.e(TAG, "Group not found: " + groupId);
                        errorMessage.setValue("Group not found");
                    }
                    isLoading.setValue(false);
                },
                forceRefresh);
    }

    /**
     * Creates a new group
     *
     * @param groupId The group ID
     * @param group   The group to create
     */
    public void createGroup(String groupId, Group group) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot create group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }

        if (group == null) {
            Log.e(TAG, "Cannot create group: group object is null");
            errorMessage.setValue("Invalid group data");
            return;
        }

        Log.d(TAG, "Creating new group: " + group.getGroupName());
        isLoading.setValue(true);

        repository.saveGroup(
                groupId,
                group,
                () -> {
                    Log.d(TAG, "Group created successfully");

                    // Add the new group to the list
                    List<Group> currentList = groupList.getValue();
                    if (currentList != null) {
                        currentList.add(group);
                        sortGroups(currentList);
                        groupList.setValue(currentList);
                    }

                    selectedGroup.setValue(group);
                    isLoading.setValue(false);
                });
    }

    /**
     * Updates an existing group
     *
     * @param groupId The group ID
     * @param updates Map of fields to update
     */
    public void updateGroup(String groupId, Map<String, Object> updates) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot update group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }

        if (updates == null || updates.isEmpty()) {
            Log.e(TAG, "Cannot update group: updates map is null or empty");
            errorMessage.setValue("No updates provided");
            return;
        }

        Log.d(TAG, "Updating group: " + groupId);
        isLoading.setValue(true);

        repository.updateGroup(
                groupId,
                updates,
                () -> {
                    Log.d(TAG, "Group updated successfully");

                    // Update the group in the list and selected group if needed
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
                        groupList.setValue(currentList);
                    }

                    Group selectedGroupValue = selectedGroup.getValue();
                    if (selectedGroupValue != null && groupId.equals(selectedGroupValue.getGroupKey())) {
                        for (Map.Entry<String, Object> entry : updates.entrySet()) {
                            applyUpdateToGroup(selectedGroupValue, entry.getKey(), entry.getValue());
                        }
                        selectedGroup.setValue(selectedGroupValue);
                    }

                    isLoading.setValue(false);
                });
    }

    /**
     * Deletes a group
     *
     * @param groupId The group ID
     */
    public void deleteGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot delete group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }

        Log.d(TAG, "Deleting group: " + groupId);
        isLoading.setValue(true);

        repository.deleteGroup(
                groupId,
                () -> {
                    Log.d(TAG, "Group deleted successfully");

                    // Remove the group from the list
                    List<Group> currentList = groupList.getValue();
                    if (currentList != null) {
                        List<Group> newList = new ArrayList<>(currentList);
                        newList.removeIf(group -> groupId.equals(group.getGroupKey()));
                        groupList.setValue(newList);
                    }

                    // Clear the selected group if it was the deleted group
                    Group selectedGroupValue = selectedGroup.getValue();
                    if (selectedGroupValue != null && groupId.equals(selectedGroupValue.getGroupKey())) {
                        selectedGroup.setValue(null);
                    }

                    isLoading.setValue(false);
                });
    }

    /**
     * Joins a group
     *
     * @param groupId The group ID
     */
    public void joinGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot join group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }

        Log.d(TAG, "Joining group: " + groupId);
        isLoading.setValue(true);

        // This is a placeholder implementation
        // In a real app, you'd call the repository to join the group
        isLoading.setValue(false);
    }

    /**
     * Leaves a group
     *
     * @param groupId The group ID
     */
    public void leaveGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot leave group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }

        Log.d(TAG, "Leaving group: " + groupId);
        isLoading.setValue(true);

        // This is a placeholder implementation
        // In a real app, you'd call the repository to leave the group
        isLoading.setValue(false);
    }

    /**
     * Selects a group
     *
     * @param groupId      The group ID
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void selectGroup(String groupId, boolean forceRefresh) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot select group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }

        Log.d(TAG, "Selecting group: " + groupId);
        loadGroup(groupId, forceRefresh);
    }

    /**
     * Clears the error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Sorts the groups by name
     *
     * @param groups The groups to sort
     */
    private void sortGroups(List<Group> groups) {
        if (groups == null) {
            return;
        }

        groups.sort(
                (g1, g2) -> {
                    if (g1.getGroupName() == null && g2.getGroupName() == null) {
                        return 0;
                    } else if (g1.getGroupName() == null) {
                        return 1;
                    } else if (g2.getGroupName() == null) {
                        return -1;
                    }
                    return g1.getGroupName().compareToIgnoreCase(g2.getGroupName());
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
