package com.example.partymaker.data.repository;

import android.util.Log;

import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository class for handling group data operations.
 * Acts as a mediator between data sources (server/local) and the ViewModel.
 */
public class GroupRepository {
    private static final String TAG = "GroupRepository";
    private static GroupRepository instance;
    private final FirebaseServerClient serverClient;

    /**
     * Private constructor to enforce singleton pattern
     */
    private GroupRepository() {
        serverClient = FirebaseServerClient.getInstance();
    }

    /**
     * Gets the singleton instance of GroupRepository
     * 
     * @return The GroupRepository instance
     */
    public static synchronized GroupRepository getInstance() {
        if (instance == null) {
            instance = new GroupRepository();
        }
        return instance;
    }

    /**
     * Gets all groups for a specific user
     * 
     * @param userKey The user key (email with dots replaced by spaces)
     * @param callback Callback to return the data
     */
    public void getUserGroups(String userKey, DataCallback<List<Group>> callback) {
        if (userKey == null || userKey.isEmpty()) {
            Log.e(TAG, "Invalid userKey: null or empty");
            callback.onError("Invalid user key");
            return;
        }
        
        Log.d(TAG, "Getting groups for user: " + userKey);
        
        serverClient.getUserGroups(userKey, new FirebaseServerClient.DataCallback<Map<String, Group>>() {
            @Override
            public void onSuccess(Map<String, Group> groups) {
                if (groups == null) {
                    Log.w(TAG, "Server returned null groups map");
                    callback.onDataLoaded(new ArrayList<>()); // Return empty list instead of null
                    return;
                }
                
                Log.d(TAG, "Successfully retrieved " + groups.size() + " user groups");
                List<Group> groupList = new ArrayList<>(groups.values());
                callback.onDataLoaded(groupList);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving user groups: " + errorMessage);
                // Try to get all groups as fallback
                getAllGroups(new DataCallback<List<Group>>() {
                    @Override
                    public void onDataLoaded(List<Group> allGroups) {
                        if (allGroups == null || allGroups.isEmpty()) {
                            callback.onError("No groups found for user: " + userKey);
                            return;
                        }
                        
                        // Filter groups where user is admin or member
                        List<Group> userGroups = new ArrayList<>();
                        for (Group group : allGroups) {
                            if (group.getAdminKey() != null && group.getAdminKey().equals(userKey)) {
                                userGroups.add(group);
                            } else if (group.getFriendKeys() != null && group.getFriendKeys().containsKey(userKey)) {
                                userGroups.add(group);
                            }
                        }
                        
                        if (userGroups.isEmpty()) {
                            Log.w(TAG, "No groups found for user in fallback search");
                            callback.onError("No groups found for user: " + userKey);
                        } else {
                            Log.d(TAG, "Found " + userGroups.size() + " groups for user in fallback search");
                            callback.onDataLoaded(userGroups);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error in fallback group retrieval: " + error);
                        callback.onError("Failed to retrieve user groups: " + errorMessage);
                    }
                });
            }
        });
    }
    
    /**
     * Gets all groups from the server
     * 
     * @param callback Callback to return the data
     */
    public void getAllGroups(DataCallback<List<Group>> callback) {
        Log.d(TAG, "Getting all groups");
        
        serverClient.getGroups(new FirebaseServerClient.DataCallback<Map<String, Group>>() {
            @Override
            public void onSuccess(Map<String, Group> groups) {
                if (groups == null) {
                    Log.w(TAG, "Server returned null groups map");
                    callback.onDataLoaded(new ArrayList<>()); // Return empty list instead of null
                    return;
                }
                
                Log.d(TAG, "Successfully retrieved " + groups.size() + " groups");
                List<Group> groupList = new ArrayList<>(groups.values());
                
                // Remove any null entries that might have slipped through
                groupList.removeIf(group -> group == null);
                
                callback.onDataLoaded(groupList);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving groups: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Gets a specific group by its ID
     * 
     * @param groupId The group ID
     * @param callback Callback to return the data
     */
    public void getGroup(String groupId, DataCallback<Group> callback) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onError("Invalid group ID");
            return;
        }
        
        Log.d(TAG, "Getting group with ID: " + groupId);
        
        serverClient.getGroup(groupId, new FirebaseServerClient.DataCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group == null) {
                    Log.e(TAG, "Server returned null group for ID: " + groupId);
                    callback.onError("Group not found");
                    return;
                }
                
                Log.d(TAG, "Successfully retrieved group: " + group.getGroupName());
                callback.onDataLoaded(group);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving group: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Saves a group to the server
     * 
     * @param groupId The group ID
     * @param group The group to save
     * @param callback Callback for operation result
     */
    public void saveGroup(String groupId, Group group, OperationCallback callback) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onError("Invalid group ID");
            return;
        }
        
        if (group == null) {
            Log.e(TAG, "Cannot save null group");
            callback.onError("Group object is null");
            return;
        }
        
        Log.d(TAG, "Saving group: " + group.getGroupName());
        
        // Ensure the group key is set
        if (group.getGroupKey() == null || group.getGroupKey().isEmpty()) {
            group.setGroupKey(groupId);
        }
        
        serverClient.saveGroup(groupId, group, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group saved successfully");
                callback.onComplete();
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error saving group: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Updates a group on the server
     * 
     * @param groupId The group ID
     * @param updates Map of fields to update
     * @param callback Callback for operation result
     */
    public void updateGroup(String groupId, Map<String, Object> updates, OperationCallback callback) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onError("Invalid group ID");
            return;
        }
        
        if (updates == null || updates.isEmpty()) {
            Log.e(TAG, "Updates map is null or empty");
            callback.onError("No updates provided");
            return;
        }
        
        Log.d(TAG, "Updating group: " + groupId);
        
        serverClient.updateGroup(groupId, updates, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group updated successfully");
                callback.onComplete();
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error updating group: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Deletes a group from the server
     * 
     * @param groupId The group ID to delete
     * @param callback Callback for operation result
     */
    public void deleteGroup(String groupId, OperationCallback callback) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onError("Invalid group ID");
            return;
        }
        
        Log.d(TAG, "Deleting group: " + groupId);
        
        serverClient.deleteGroup(groupId, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group deleted successfully");
                callback.onComplete();
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error deleting group: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Interface for data callbacks
     */
    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String error);
    }
    
    /**
     * Interface for operation callbacks
     */
    public interface OperationCallback {
        void onComplete();
        void onError(String error);
    }
} 