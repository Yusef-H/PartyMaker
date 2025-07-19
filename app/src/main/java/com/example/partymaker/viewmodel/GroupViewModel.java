package com.example.partymaker.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.partymaker.data.api.Result;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for managing group data and state.
 * Provides LiveData objects that UI components can observe.
 */
public class GroupViewModel extends ViewModel {
    private static final String TAG = "GroupViewModel";
    
    // LiveData for group list
    private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>(new ArrayList<>());
    
    // LiveData for currently selected group
    private final MutableLiveData<Group> selectedGroup = new MutableLiveData<>();
    
    // LiveData for loading state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // LiveData for error messages
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Repository instance
    private final GroupRepository repository;
    
    /**
     * Constructor
     */
    public GroupViewModel() {
        repository = GroupRepository.getInstance();
        
        // Observe repository's all groups LiveData
        LiveData<Result<List<Group>>> repoGroups = repository.getAllGroupsLiveData();
        Transformations.map(repoGroups, result -> {
            if (result.isLoading()) {
                isLoading.setValue(true);
            } else if (result.isSuccess()) {
                isLoading.setValue(false);
                List<Group> data = result.getData();
                if (data != null) {
                    sortGroups(data);
                    groupList.setValue(data);
                }
            } else if (result.isError()) {
                isLoading.setValue(false);
                errorMessage.setValue(result.getUserFriendlyError());
            }
            return result;
        });
    }
    
    /**
     * Gets the list of groups as LiveData
     * 
     * @return LiveData containing list of groups
     */
    public LiveData<List<Group>> getGroups() {
        return groupList;
    }
    
    /**
     * Gets the currently selected group as LiveData
     * 
     * @return LiveData containing the selected group
     */
    public LiveData<Group> getSelectedGroup() {
        return selectedGroup;
    }
    
    /**
     * Gets the loading state as LiveData
     * 
     * @return LiveData containing the loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Gets the error message as LiveData
     * 
     * @return LiveData containing the error message
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Loads groups for a specific user
     * 
     * @param userKey The user key (email with dots replaced by spaces)
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void loadUserGroups(String userKey, boolean forceRefresh) {
        if (userKey == null || userKey.isEmpty()) {
            Log.e(TAG, "Cannot load groups: userKey is null or empty");
            errorMessage.setValue("Invalid user key");
            return;
        }
        
        Log.d(TAG, "Loading groups for user: " + userKey);
        isLoading.setValue(true);
        
        repository.getUserGroups(userKey, result -> {
            if (result.isLoading()) {
                isLoading.setValue(true);
            } else if (result.isSuccess()) {
                List<Group> data = result.getData();
                if (data != null) {
                    Log.d(TAG, "Groups loaded successfully: " + data.size() + " groups");
                    sortGroups(data);
                    groupList.setValue(data);
                }
                isLoading.setValue(false);
            } else if (result.isError()) {
                Log.e(TAG, "Error loading groups: " + result.getError());
                errorMessage.setValue(result.getUserFriendlyError());
                isLoading.setValue(false);
            }
        }, forceRefresh);
    }
    
    /**
     * Loads all groups
     * 
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void loadAllGroups(boolean forceRefresh) {
        Log.d(TAG, "Loading all groups");
        isLoading.setValue(true);
        
        repository.getAllGroups(result -> {
            if (result.isLoading()) {
                isLoading.setValue(true);
            } else if (result.isSuccess()) {
                List<Group> data = result.getData();
                if (data != null) {
                    Log.d(TAG, "All groups loaded successfully: " + data.size() + " groups");
                    sortGroups(data);
                    groupList.setValue(data);
                }
                isLoading.setValue(false);
            } else if (result.isError()) {
                Log.e(TAG, "Error loading all groups: " + result.getError());
                errorMessage.setValue(result.getUserFriendlyError());
                isLoading.setValue(false);
            }
        }, forceRefresh);
    }
    
    /**
     * Loads a specific group by its ID
     * 
     * @param groupId The group ID
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
        
        repository.getGroup(groupId, result -> {
            if (result.isLoading()) {
                isLoading.setValue(true);
            } else if (result.isSuccess()) {
                Group data = result.getData();
                if (data != null) {
                    Log.d(TAG, "Group loaded successfully: " + data.getGroupName());
                    selectedGroup.setValue(data);
                }
                isLoading.setValue(false);
            } else if (result.isError()) {
                Log.e(TAG, "Error loading group: " + result.getError());
                errorMessage.setValue(result.getUserFriendlyError());
                isLoading.setValue(false);
            }
        }, forceRefresh);
    }
    
    /**
     * Creates a new group
     * 
     * @param groupId The group ID
     * @param group The group to create
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
        
        repository.saveGroup(groupId, group, result -> {
            if (result.isLoading()) {
                isLoading.setValue(true);
            } else if (result.isSuccess()) {
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
            } else if (result.isError()) {
                Log.e(TAG, "Error creating group: " + result.getError());
                errorMessage.setValue(result.getUserFriendlyError());
                isLoading.setValue(false);
            }
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
        
        repository.updateGroup(groupId, updates, result -> {
            if (result.isLoading()) {
                isLoading.setValue(true);
            } else if (result.isSuccess()) {
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
            } else if (result.isError()) {
                Log.e(TAG, "Error updating group: " + result.getError());
                errorMessage.setValue(result.getUserFriendlyError());
                isLoading.setValue(false);
            }
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
        
        repository.deleteGroup(groupId, result -> {
            if (result.isLoading()) {
                isLoading.setValue(true);
            } else if (result.isSuccess()) {
                Log.d(TAG, "Group deleted successfully");
                
                // Remove the group from the list
                List<Group> currentList = groupList.getValue();
                if (currentList != null) {
                    List<Group> newList = new ArrayList<>(currentList);
                    newList.removeIf(group -> groupId.equals(group.getGroupKey()));
                    groupList.setValue(newList);
                }
                
                // Clear selected group if it was the deleted one
                Group selectedGroupValue = selectedGroup.getValue();
                if (selectedGroupValue != null && groupId.equals(selectedGroupValue.getGroupKey())) {
                    selectedGroup.setValue(null);
                }
                
                isLoading.setValue(false);
            } else if (result.isError()) {
                Log.e(TAG, "Error deleting group: " + result.getError());
                errorMessage.setValue(result.getUserFriendlyError());
                isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Selects a group by its ID
     * 
     * @param groupId The group ID
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void selectGroup(String groupId, boolean forceRefresh) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot select group: groupId is null or empty");
            return;
        }
        
        // First check if the group is in the current list
        List<Group> currentList = groupList.getValue();
        if (currentList != null) {
            for (Group group : currentList) {
                if (groupId.equals(group.getGroupKey())) {
                    selectedGroup.setValue(group);
                    return;
                }
            }
        }
        
        // If not found in the list, load it from the repository
        loadGroup(groupId, forceRefresh);
    }
    
    /**
     * Clears the error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
    
    /**
     * Sorts groups by name
     * 
     * @param groups The list of groups to sort
     */
    private void sortGroups(List<Group> groups) {
        if (groups == null) {
            return;
        }
        
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                if (g1.getGroupName() == null && g2.getGroupName() == null) {
                    return 0;
                } else if (g1.getGroupName() == null) {
                    return 1;
                } else if (g2.getGroupName() == null) {
                    return -1;
                }
                return g1.getGroupName().compareToIgnoreCase(g2.getGroupName());
            }
        });
    }
    
    /**
     * Applies an update to a group object
     * 
     * @param group The group to update
     * @param field The field to update
     * @param value The new value
     */
    @SuppressWarnings("unchecked")
    private void applyUpdateToGroup(Group group, String field, Object value) {
        if (group == null) {
            return;
        }
        
        switch (field) {
            case "groupName":
                if (value instanceof String) {
                    group.setGroupName((String) value);
                }
                break;
            case "groupDescription":
                if (value instanceof String) {
                    group.setGroupDescription((String) value);
                }
                break;
            case "adminKey":
                if (value instanceof String) {
                    group.setAdminKey((String) value);
                }
                break;
            case "friendKeys":
                if (value instanceof Map) {
                    // Convert to HashMap<String, Object>
                    HashMap<String, Object> friendKeys = new HashMap<>();
                    Map<?, ?> valueMap = (Map<?, ?>) value;
                    for (Map.Entry<?, ?> mapEntry : valueMap.entrySet()) {
                        if (mapEntry.getKey() instanceof String) {
                            friendKeys.put((String) mapEntry.getKey(), mapEntry.getValue());
                        }
                    }
                    group.setFriendKeys(friendKeys);
                }
                break;
            // Add more fields as needed
        }
    }
} 