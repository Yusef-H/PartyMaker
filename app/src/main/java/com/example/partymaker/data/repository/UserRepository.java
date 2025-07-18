package com.example.partymaker.data.repository;

import android.util.Log;

import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository class for handling user data operations.
 * Acts as a mediator between data sources (server/local) and the ViewModel.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private static UserRepository instance;
    private final FirebaseServerClient serverClient;

    /**
     * Private constructor to enforce singleton pattern
     */
    private UserRepository() {
        serverClient = FirebaseServerClient.getInstance();
    }

    /**
     * Gets the singleton instance of UserRepository
     * 
     * @return The UserRepository instance
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Gets a user by their ID
     * 
     * @param userId The user ID (email with dots replaced by spaces)
     * @param callback Callback to return the data
     */
    public void getUser(String userId, GroupRepository.DataCallback<User> callback) {
        Log.d(TAG, "Getting user with ID: " + userId);
        
        serverClient.getUser(userId, new FirebaseServerClient.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Successfully retrieved user: " + user.getUserName());
                callback.onDataLoaded(user);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving user: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Gets all users from the server
     * 
     * @param callback Callback to return the data
     */
    public void getAllUsers(GroupRepository.DataCallback<List<User>> callback) {
        Log.d(TAG, "Getting all users");
        
        serverClient.getUsers(new FirebaseServerClient.DataCallback<Map<String, User>>() {
            @Override
            public void onSuccess(Map<String, User> users) {
                Log.d(TAG, "Successfully retrieved " + users.size() + " users");
                List<User> userList = new ArrayList<>(users.values());
                callback.onDataLoaded(userList);
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving users: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Saves a user to the server
     * 
     * @param userId The user ID
     * @param user The user to save
     * @param callback Callback for operation result
     */
    public void saveUser(String userId, User user, GroupRepository.OperationCallback callback) {
        Log.d(TAG, "Saving user: " + user.getUserName());
        
        serverClient.saveUser(userId, user, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User saved successfully");
                callback.onComplete();
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error saving user: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    /**
     * Updates a user on the server
     * 
     * @param userId The user ID
     * @param updates Map of fields to update
     * @param callback Callback for operation result
     */
    public void updateUser(String userId, Map<String, Object> updates, GroupRepository.OperationCallback callback) {
        Log.d(TAG, "Updating user: " + userId);
        
        serverClient.updateUser(userId, updates, new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User updated successfully");
                callback.onComplete();
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error updating user: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
} 