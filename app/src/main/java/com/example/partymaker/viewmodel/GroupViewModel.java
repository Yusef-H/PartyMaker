package com.example.partymaker.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
     */
    public void loadUserGroups(String userKey) {
        if (userKey == null || userKey.isEmpty()) {
            Log.e(TAG, "Cannot load groups: userKey is null or empty");
            errorMessage.setValue("Invalid user key");
            return;
        }
        
        Log.d(TAG, "Loading groups for user: " + userKey);
        isLoading.setValue(true);
        
        repository.getUserGroups(userKey, new GroupRepository.DataCallback<List<Group>>() {
            @Override
            public void onDataLoaded(List<Group> data) {
                Log.d(TAG, "Groups loaded successfully: " + data.size() + " groups");
                sortGroups(data);
                groupList.setValue(data);
                isLoading.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading groups: " + error);
                errorMessage.setValue("Failed to load groups: " + error);
                isLoading.setValue(false);
                
                // Don't try to get all groups as fallback - this is now handled in the repository
            }
        });
    }
    
    /**
     * Loads all groups
     */
    public void loadAllGroups() {
        Log.d(TAG, "Loading all groups");
        isLoading.setValue(true);
        
        repository.getAllGroups(new GroupRepository.DataCallback<List<Group>>() {
            @Override
            public void onDataLoaded(List<Group> data) {
                Log.d(TAG, "All groups loaded successfully: " + data.size() + " groups");
                sortGroups(data);
                groupList.setValue(data);
                isLoading.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading all groups: " + error);
                errorMessage.setValue("Failed to load groups: " + error);
                isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Loads a specific group by its ID
     * 
     * @param groupId The group ID
     */
    public void loadGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot load group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }
        
        Log.d(TAG, "Loading group with ID: " + groupId);
        isLoading.setValue(true);
        
        repository.getGroup(groupId, new GroupRepository.DataCallback<Group>() {
            @Override
            public void onDataLoaded(Group data) {
                Log.d(TAG, "Group loaded successfully: " + data.getGroupName());
                selectedGroup.setValue(data);
                isLoading.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading group: " + error);
                errorMessage.setValue("Failed to load group: " + error);
                isLoading.setValue(false);
            }
        });
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
        
        repository.saveGroup(groupId, group, new GroupRepository.OperationCallback() {
            @Override
            public void onComplete() {
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
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error creating group: " + error);
                errorMessage.setValue("Failed to create group: " + error);
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
        
        repository.updateGroup(groupId, updates, new GroupRepository.OperationCallback() {
            @Override
            public void onComplete() {
                Log.d(TAG, "Group updated successfully");
                
                // Update the group in the list and selected group
                Group currentGroup = selectedGroup.getValue();
                if (currentGroup != null && currentGroup.getGroupKey().equals(groupId)) {
                    // Apply updates to the current group
                    for (Map.Entry<String, Object> entry : updates.entrySet()) {
                        applyUpdateToGroup(currentGroup, entry.getKey(), entry.getValue());
                    }
                    selectedGroup.setValue(currentGroup);
                }
                
                // Also update in the list
                List<Group> currentList = groupList.getValue();
                if (currentList != null) {
                    for (int i = 0; i < currentList.size(); i++) {
                        Group group = currentList.get(i);
                        if (group != null && group.getGroupKey() != null && 
                            group.getGroupKey().equals(groupId)) {
                            // Apply updates to the group in the list
                            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                                applyUpdateToGroup(group, entry.getKey(), entry.getValue());
                            }
                            break;
                        }
                    }
                    groupList.setValue(currentList);
                }
                
                isLoading.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error updating group: " + error);
                errorMessage.setValue("Failed to update group: " + error);
                isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Deletes a group
     * 
     * @param groupId The group ID to delete
     */
    public void deleteGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot delete group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }
        
        Log.d(TAG, "Deleting group: " + groupId);
        isLoading.setValue(true);
        
        repository.deleteGroup(groupId, new GroupRepository.OperationCallback() {
            @Override
            public void onComplete() {
                Log.d(TAG, "Group deleted successfully");
                
                // Remove the group from the list
                List<Group> currentList = groupList.getValue();
                if (currentList != null) {
                    currentList.removeIf(group -> 
                        group != null && 
                        group.getGroupKey() != null && 
                        group.getGroupKey().equals(groupId)
                    );
                    groupList.setValue(currentList);
                }
                
                // Clear selected group if it was the deleted one
                Group currentGroup = selectedGroup.getValue();
                if (currentGroup != null && 
                    currentGroup.getGroupKey() != null && 
                    currentGroup.getGroupKey().equals(groupId)) {
                    selectedGroup.setValue(null);
                }
                
                isLoading.setValue(false);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error deleting group: " + error);
                errorMessage.setValue("Failed to delete group: " + error);
                isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Selects a group from the list
     * 
     * @param groupId The group ID to select
     */
    public void selectGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot select group: groupId is null or empty");
            errorMessage.setValue("Invalid group ID");
            return;
        }
        
        List<Group> groups = groupList.getValue();
        if (groups != null) {
            for (Group group : groups) {
                if (group != null && 
                    group.getGroupKey() != null && 
                    group.getGroupKey().equals(groupId)) {
                    selectedGroup.setValue(group);
                    return;
                }
            }
        }
        
        // If not found in the list, load it from the server
        loadGroup(groupId);
    }
    
    /**
     * Clears the error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
    
    /**
     * Sorts the groups by creation date (newest first)
     * 
     * @param groups The list of groups to sort
     */
    private void sortGroups(List<Group> groups) {
        if (groups != null && !groups.isEmpty()) {
            Collections.sort(groups, (g1, g2) -> {
                // Handle null dates properly
                if (g1 == null || g1.getCreatedAt() == null) return 1;
                if (g2 == null || g2.getCreatedAt() == null) return -1;
                return g2.getCreatedAt().compareTo(g1.getCreatedAt());
            });
        }
    }
    
    /**
     * Applies an update to a group field
     * 
     * @param group The group to update
     * @param field The field name
     * @param value The new value
     */
    @SuppressWarnings("unchecked")
    private void applyUpdateToGroup(Group group, String field, Object value) {
        if (group == null || field == null || value == null) {
            Log.w(TAG, "Cannot apply update: group, field, or value is null");
            return;
        }
        
        switch (field) {
            case "groupName":
                group.setGroupName((String) value);
                break;
            case "groupType":
                if (value instanceof String) {
                    try {
                        group.setGroupType(Integer.parseInt((String) value));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing groupType: " + e.getMessage());
                    }
                } else if (value instanceof Integer) {
                    group.setGroupType((Integer) value);
                } else if (value instanceof Double) {
                    group.setGroupType(((Double) value).intValue());
                }
                break;
            case "groupLocation":
                group.setGroupLocation((String) value);
                break;
            case "groupPrice":
                group.setGroupPrice((String) value);
                break;
            case "groupDays":
                group.setGroupDays((String) value);
                break;
            case "groupMonths":
                group.setGroupMonths((String) value);
                break;
            case "groupYears":
                group.setGroupYears((String) value);
                break;
            case "groupHours":
                group.setGroupHours((String) value);
                break;
            case "adminKey":
                group.setAdminKey((String) value);
                break;
            case "canAdd":
                group.setCanAdd((Boolean) value);
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
                Log.w(TAG, "Unknown field in group update: " + field);
                break;
        }
    }
} 