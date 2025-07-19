package com.example.partymaker.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utilities.AuthHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for handling user data operations. Acts as a mediator between data sources
 * (server/local) and the ViewModel.
 */
public class UserRepository {
  private static final String TAG = "UserRepository";
  private static UserRepository instance;
  private final FirebaseServerClient serverClient;

  // LiveData objects for caching data
  private final MutableLiveData<Map<String, User>> allUsers =
      new MutableLiveData<>(new HashMap<>());
  private final MutableLiveData<User> currentUser = new MutableLiveData<>();
  private final Map<String, User> userCache = new HashMap<>();

  /** Private constructor to enforce singleton pattern */
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
   * Gets all users
   *
   * @param callback Callback to return the data
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getAllUsers(DataCallback<List<User>> callback, boolean forceRefresh) {
    Map<String, User> cachedUsers = allUsers.getValue();

    if (!forceRefresh && cachedUsers != null && !cachedUsers.isEmpty()) {
      Log.d(TAG, "Returning cached users: " + cachedUsers.size());
      callback.onDataLoaded(new ArrayList<>(cachedUsers.values()));
      return;
    }

    Log.d(TAG, "Fetching all users from server");
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<Map<String, User>>() {
          @Override
          public void onSuccess(Map<String, User> users) {
            if (users == null) {
              Log.w(TAG, "Server returned null users map");
              callback.onDataLoaded(new ArrayList<>()); // Return empty list instead of null
              return;
            }

            Log.d(TAG, "Successfully retrieved " + users.size() + " users");

            // Update cache
            userCache.clear();
            userCache.putAll(users);
            allUsers.postValue(users);

            List<User> userList = new ArrayList<>(users.values());
            callback.onDataLoaded(userList);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error retrieving users: " + errorMessage);

            // If we have cached data, use it as fallback
            if (cachedUsers != null && !cachedUsers.isEmpty()) {
              Log.d(TAG, "Using cached users as fallback");
              callback.onDataLoaded(new ArrayList<>(cachedUsers.values()));
            } else {
              callback.onError(errorMessage);
            }
          }
        });
  }

  /**
   * Gets a specific user by ID
   *
   * @param userId The user ID
   * @param callback Callback to return the data
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getUser(String userId, DataCallback<User> callback, boolean forceRefresh) {
    if (userId == null || userId.isEmpty()) {
      Log.e(TAG, "Invalid userId: null or empty");
      callback.onError("Invalid user ID");
      return;
    }

    // Check cache first
    if (!forceRefresh && userCache.containsKey(userId)) {
      User cachedUser = userCache.get(userId);
      Log.d(TAG, "Returning cached user: " + userId);
      callback.onDataLoaded(cachedUser);
      return;
    }

    Log.d(TAG, "Fetching user from server: " + userId);
    serverClient.getUser(
        userId,
        new FirebaseServerClient.DataCallback<User>() {
          @Override
          public void onSuccess(User user) {
            if (user == null) {
              Log.e(TAG, "Server returned null user for ID: " + userId);
              callback.onError("User not found");
              return;
            }

            Log.d(TAG, "Successfully retrieved user: " + userId);

            // Update cache
            userCache.put(userId, user);

            // If this is the current user, update currentUser LiveData
            if (isCurrentUser(userId)) {
              currentUser.postValue(user);
            }

            callback.onDataLoaded(user);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error retrieving user: " + errorMessage);

            // If we have cached data, use it as fallback
            if (userCache.containsKey(userId)) {
              Log.d(TAG, "Using cached user as fallback");
              callback.onDataLoaded(userCache.get(userId));
            } else {
              callback.onError(errorMessage);
            }
          }
        });
  }

  /**
   * Gets the current user
   *
   * @param context Application context
   * @param callback Callback to return the data
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getCurrentUser(
      android.content.Context context, DataCallback<User> callback, boolean forceRefresh) {
    try {
      String currentUserEmail = AuthHelper.getCurrentUserEmail(context);
      if (currentUserEmail == null || currentUserEmail.isEmpty()) {
        Log.e(TAG, "No current user found");
        callback.onError("No current user found");
        return;
      }

      String userKey = currentUserEmail.replace('.', ' ');

      // Check if we already have the current user in LiveData
      User cachedCurrentUser = currentUser.getValue();
      if (!forceRefresh && cachedCurrentUser != null) {
        Log.d(TAG, "Returning cached current user: " + userKey);
        callback.onDataLoaded(cachedCurrentUser);
        return;
      }

      // Fetch from server
      getUser(
          userKey,
          new DataCallback<User>() {
            @Override
            public void onDataLoaded(User user) {
              currentUser.postValue(user);
              callback.onDataLoaded(user);
            }

            @Override
            public void onError(String error) {
              callback.onError(error);
            }
          },
          forceRefresh);
    } catch (Exception e) {
      Log.e(TAG, "Error getting current user", e);
      callback.onError("Error getting current user: " + e.getMessage());
    }
  }

  /**
   * Saves a user to the server
   *
   * @param userId The user ID
   * @param user The user to save
   * @param callback Callback for operation result
   */
  public void saveUser(String userId, User user, OperationCallback callback) {
    if (userId == null || userId.isEmpty()) {
      Log.e(TAG, "Invalid userId: null or empty");
      callback.onError("Invalid user ID");
      return;
    }

    if (user == null) {
      Log.e(TAG, "Cannot save null user");
      callback.onError("User object is null");
      return;
    }

    Log.d(TAG, "Saving user: " + userId);

    serverClient.saveUser(
        userId,
        user,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "User saved successfully");

            // Update cache
            userCache.put(userId, user);

            // If this is the current user, update currentUser LiveData
            if (isCurrentUser(userId)) {
              currentUser.postValue(user);
            }

            // Update allUsers LiveData if it contains data
            Map<String, User> currentUsers = allUsers.getValue();
            if (currentUsers != null) {
              currentUsers.put(userId, user);
              allUsers.postValue(currentUsers);
            }

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
  public void updateUser(String userId, Map<String, Object> updates, OperationCallback callback) {
    if (userId == null || userId.isEmpty()) {
      Log.e(TAG, "Invalid userId: null or empty");
      callback.onError("Invalid user ID");
      return;
    }

    if (updates == null || updates.isEmpty()) {
      Log.e(TAG, "Updates map is null or empty");
      callback.onError("No updates provided");
      return;
    }

    Log.d(TAG, "Updating user: " + userId);

    serverClient.updateUser(
        userId,
        updates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "User updated successfully");

            // Update cache if we have the user
            if (userCache.containsKey(userId)) {
              User user = userCache.get(userId);
              applyUpdatesToUser(user, updates);
              userCache.put(userId, user);

              // If this is the current user, update currentUser LiveData
              if (isCurrentUser(userId)) {
                currentUser.postValue(user);
              }

              // Update allUsers LiveData if it contains data
              Map<String, User> currentUsers = allUsers.getValue();
              if (currentUsers != null && currentUsers.containsKey(userId)) {
                currentUsers.put(userId, user);
                allUsers.postValue(currentUsers);
              }
            }

            callback.onComplete();
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error updating user: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Checks if a user ID matches the current user
   *
   * @param userId The user ID to check
   * @return true if it matches the current user, false otherwise
   */
  private boolean isCurrentUser(String userId) {
    User current = currentUser.getValue();
    return current != null && userId.equals(current.getUserKey());
  }

  /**
   * Applies updates to a user object
   *
   * @param user The user to update
   * @param updates The updates to apply
   */
  @SuppressWarnings("unchecked")
  private void applyUpdatesToUser(User user, Map<String, Object> updates) {
    if (user == null || updates == null) {
      return;
    }

    for (Map.Entry<String, Object> entry : updates.entrySet()) {
      String field = entry.getKey();
      Object value = entry.getValue();

      switch (field) {
        case "username":
          if (value instanceof String) {
            user.setUsername((String) value);
          }
          break;
        case "email":
          if (value instanceof String) {
            user.setEmail((String) value);
          }
          break;
        case "profileImageUrl":
          if (value instanceof String) {
            user.setProfileImageUrl((String) value);
          }
          break;
        case "friendKeys":
          if (value instanceof Map) {
            user.setFriendKeys((Map<String, Boolean>) value);
          }
          break;
          // Add more fields as needed
      }
    }
  }

  /**
   * Gets all users as LiveData
   *
   * @return LiveData containing map of all users
   */
  public LiveData<Map<String, User>> getAllUsersLiveData() {
    return allUsers;
  }

  /**
   * Gets current user as LiveData
   *
   * @return LiveData containing the current user
   */
  public LiveData<User> getCurrentUserLiveData() {
    return currentUser;
  }

  /** Clears the user cache */
  public void clearCache() {
    userCache.clear();
    allUsers.setValue(new HashMap<>());
    currentUser.setValue(null);
  }

  /** Interface for data callbacks */
  public interface DataCallback<T> {
    void onDataLoaded(T data);

    void onError(String error);
  }

  /** Interface for operation callbacks */
  public interface OperationCallback {
    void onComplete();

    void onError(String error);
  }
}
