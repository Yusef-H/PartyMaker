package com.example.partymaker.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.api.Result;
import com.example.partymaker.data.model.Group;

import java.util.ArrayList;
import java.util.HashMap;
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
    
    // LiveData for caching data
    private final MutableLiveData<Result<List<Group>>> allGroups = new MutableLiveData<>();
    private final Map<String, Group> groupCache = new HashMap<>();

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
     * @param callback Callback to return the result
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void getUserGroups(String userKey, ResultCallback<List<Group>> callback, boolean forceRefresh) {
        if (userKey == null || userKey.isEmpty()) {
            Log.e(TAG, "Invalid userKey: null or empty");
            callback.onResult(Result.error("Invalid user key"));
            return;
        }
        
        Log.d(TAG, "Getting groups for user: " + userKey);
        
        // Return loading state
        callback.onResult(Result.loading());
        
        serverClient.getUserGroups(userKey, new FirebaseServerClient.DataCallback<Map<String, Group>>() {
            @Override
            public void onSuccess(Map<String, Group> groups) {
                if (groups == null) {
                    Log.w(TAG, "Server returned null groups map");
                    callback.onResult(Result.success(new ArrayList<>())); // Return empty list instead of null
                    return;
                }
                
                Log.d(TAG, "Successfully retrieved " + groups.size() + " user groups");
                List<Group> groupList = new ArrayList<>(groups.values());
                
                // Update cache
                for (Group group : groupList) {
                    if (group.getGroupKey() != null) {
                        groupCache.put(group.getGroupKey(), group);
                    }
                }
                
                callback.onResult(Result.success(groupList));
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving user groups: " + errorMessage);
                
                // Try to get all groups as fallback
                getAllGroups(new ResultCallback<List<Group>>() {
                    @Override
                    public void onResult(Result<List<Group>> result) {
                        if (result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
                            // Filter groups where user is admin or member
                            List<Group> userGroups = new ArrayList<>();
                            for (Group group : result.getData()) {
                                if (group.getAdminKey() != null && group.getAdminKey().equals(userKey)) {
                                    userGroups.add(group);
                                } else if (group.getFriendKeys() != null && group.getFriendKeys().containsKey(userKey)) {
                                    userGroups.add(group);
                                }
                            }
                            
                            if (userGroups.isEmpty()) {
                                Log.w(TAG, "No groups found for user in fallback search");
                                callback.onResult(Result.error("No groups found for user: " + userKey));
                            } else {
                                Log.d(TAG, "Found " + userGroups.size() + " groups for user in fallback search");
                                callback.onResult(Result.success(userGroups));
                            }
                        } else {
                            callback.onResult(Result.error("Failed to retrieve user groups: " + errorMessage, 
                                    NetworkUtils.ErrorType.SERVER_ERROR));
                        }
                    }
                }, true);
            }
        });
    }
    
    /**
     * Gets all groups from the server
     * 
     * @param callback Callback to return the result
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void getAllGroups(ResultCallback<List<Group>> callback, boolean forceRefresh) {
        Log.d(TAG, "Getting all groups, forceRefresh: " + forceRefresh);
        
        // Check if we have cached data and forceRefresh is false
        Result<List<Group>> cachedResult = allGroups.getValue();
        if (!forceRefresh && cachedResult != null && cachedResult.isSuccess() && cachedResult.getData() != null) {
            Log.d(TAG, "Returning cached groups: " + cachedResult.getData().size());
            callback.onResult(cachedResult);
            return;
        }
        
        // Return loading state
        callback.onResult(Result.loading());
        allGroups.setValue(Result.loading());
        
        serverClient.getGroups(new FirebaseServerClient.DataCallback<Map<String, Group>>() {
            @Override
            public void onSuccess(Map<String, Group> groups) {
                if (groups == null) {
                    Log.w(TAG, "Server returned null groups map");
                    Result<List<Group>> emptyResult = Result.success(new ArrayList<>());
                    allGroups.setValue(emptyResult);
                    callback.onResult(emptyResult);
                    return;
                }
                
                Log.d(TAG, "Successfully retrieved " + groups.size() + " groups");
                List<Group> groupList = new ArrayList<>(groups.values());
                
                // Remove any null entries that might have slipped through
                groupList.removeIf(group -> group == null);
                
                // Update cache
                groupCache.clear();
                for (Group group : groupList) {
                    if (group.getGroupKey() != null) {
                        groupCache.put(group.getGroupKey(), group);
                    }
                }
                
                Result<List<Group>> successResult = Result.success(groupList);
                allGroups.setValue(successResult);
                callback.onResult(successResult);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving groups: " + errorMessage);
                Result<List<Group>> errorResult = Result.error(errorMessage, NetworkUtils.ErrorType.SERVER_ERROR);
                allGroups.setValue(errorResult);
                callback.onResult(errorResult);
            }
        });
    }
    
    /**
     * Gets a specific group by its ID
     * 
     * @param groupId The group ID
     * @param callback Callback to return the result
     * @param forceRefresh Whether to force a refresh from the server
     */
    public void getGroup(String groupId, ResultCallback<Group> callback, boolean forceRefresh) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onResult(Result.error("Invalid group ID"));
            return;
        }
        
        Log.d(TAG, "Getting group with ID: " + groupId);
        
        // Check cache first if not forcing refresh
        if (!forceRefresh && groupCache.containsKey(groupId)) {
            Log.d(TAG, "Returning cached group: " + groupId);
            callback.onResult(Result.success(groupCache.get(groupId)));
            return;
        }
        
        // Return loading state
        callback.onResult(Result.loading());
        
        serverClient.getGroup(groupId, new FirebaseServerClient.DataCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group == null) {
                    Log.e(TAG, "Server returned null group for ID: " + groupId);
                    callback.onResult(Result.error("Group not found", NetworkUtils.ErrorType.CLIENT_ERROR));
                    return;
                }
                
                Log.d(TAG, "Successfully retrieved group: " + group.getGroupName());
                
                // Update cache
                if (group.getGroupKey() != null) {
                    groupCache.put(group.getGroupKey(), group);
                }
                
                callback.onResult(Result.success(group));
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving group: " + errorMessage);
                callback.onResult(Result.error(errorMessage, NetworkUtils.ErrorType.SERVER_ERROR));
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
    public void saveGroup(String groupId, Group group, ResultCallback<Void> callback) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onResult(Result.error("Invalid group ID"));
            return;
        }
        
        if (group == null) {
            Log.e(TAG, "Cannot save null group");
            callback.onResult(Result.error("Group object is null"));
            return;
        }
        
        Log.d(TAG, "Saving group: " + group.getGroupName());
        
        // Return loading state
        callback.onResult(Result.loading());
        
        // Ensure the group key is set
        if (group.getGroupKey() == null || group.getGroupKey().isEmpty()) {
            group.setGroupKey(groupId);
        }
        
        serverClient.saveGroup(groupId, group, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group saved successfully");
                
                // Update cache
                groupCache.put(groupId, group);
                
                callback.onResult(Result.success(null));
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error saving group: " + errorMessage);
                callback.onResult(Result.error(errorMessage, NetworkUtils.ErrorType.SERVER_ERROR));
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
    public void updateGroup(String groupId, Map<String, Object> updates, ResultCallback<Void> callback) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onResult(Result.error("Invalid group ID"));
            return;
        }
        
        if (updates == null || updates.isEmpty()) {
            Log.e(TAG, "Updates map is null or empty");
            callback.onResult(Result.error("No updates provided"));
            return;
        }
        
        Log.d(TAG, "Updating group: " + groupId);
        
        // Return loading state
        callback.onResult(Result.loading());
        
        serverClient.updateGroup(groupId, updates, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group updated successfully");
                
                // Update cache if we have the group
                if (groupCache.containsKey(groupId)) {
                    Group group = groupCache.get(groupId);
                    applyUpdatesToGroup(group, updates);
                }
                
                callback.onResult(Result.success(null));
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error updating group: " + errorMessage);
                callback.onResult(Result.error(errorMessage, NetworkUtils.ErrorType.SERVER_ERROR));
            }
        });
    }
    
    /**
     * Deletes a group from the server
     * 
     * @param groupId The group ID
     * @param callback Callback for operation result
     */
    public void deleteGroup(String groupId, ResultCallback<Void> callback) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            callback.onResult(Result.error("Invalid group ID"));
            return;
        }
        
        Log.d(TAG, "Deleting group: " + groupId);
        
        // Return loading state
        callback.onResult(Result.loading());
        
        serverClient.deleteGroup(groupId, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group deleted successfully");
                
                // Remove from cache
                groupCache.remove(groupId);
                
                callback.onResult(Result.success(null));
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error deleting group: " + errorMessage);
                callback.onResult(Result.error(errorMessage, NetworkUtils.ErrorType.SERVER_ERROR));
            }
        });
    }
    
    /**
     * Gets all groups as LiveData
     * 
     * @return LiveData containing result with list of all groups
     */
    public LiveData<Result<List<Group>>> getAllGroupsLiveData() {
        return allGroups;
    }
    
    /**
     * Clears the group cache
     */
    public void clearCache() {
        groupCache.clear();
        allGroups.setValue(Result.success(new ArrayList<>()));
    }
    
    /**
     * Applies updates to a group object
     * 
     * @param group The group to update
     * @param updates The updates to apply
     */
    @SuppressWarnings("unchecked")
    private void applyUpdatesToGroup(Group group, Map<String, Object> updates) {
        if (group == null || updates == null) {
            return;
        }
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            
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
                        group.setFriendKeys((Map<String, Boolean>) value);
                    }
                    break;
                // Add more fields as needed
            }
        }
    }
    
    /**
     * Interface for result callbacks
     */
    public interface ResultCallback<T> {
        void onResult(Result<T> result);
    }
} 