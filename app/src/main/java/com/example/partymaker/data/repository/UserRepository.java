package com.example.partymaker.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.local.AppDatabase;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
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
  
  // Error messages
  private static final String ERROR_INVALID_USER_ID = "Invalid user ID";
  private static final String ERROR_USER_NOT_FOUND = "User not found";
  private static final String ERROR_NO_CURRENT_USER = "No current user found";
  private static final String ERROR_USER_IS_NULL = "User object is null";
  private static final String ERROR_NO_UPDATES = "No updates provided";
  private static final String ERROR_USER_CANNOT_BE_NULL = "User cannot be null";
  private static final String ERROR_USER_KEY_NULL_OR_EMPTY = "User key cannot be null or empty";
  private static final String ERROR_GET_CURRENT_USER = "Error getting current user: ";
  
  // Field names for database operations
  private static final String FIELD_USERNAME = "username";
  private static final String FIELD_EMAIL = "email";
  private static final String FIELD_PROFILE_IMAGE_URL = "profileImageUrl";
  private static final String FIELD_FRIEND_KEYS = "friendKeys";
  
  // Character replacement constant
  private static final char DOT_CHAR = '.';
  private static final char SPACE_CHAR = ' ';
  private static UserRepository instance;
  private final FirebaseServerClient serverClient;
  private AppDatabase database;

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
   * Initializes the repository with a context. This is required for database access.
   *
   * @param context The application context
   */
  public void initialize(Context context) {
    if (context != null) {
      database = AppDatabase.getInstance(context);
      Log.d(TAG, "UserRepository initialized with database");
    } else {
      Log.e(TAG, "Cannot initialize UserRepository: context is null");
    }
  }

  /**
   * Gets all users
   *
   * @param callback Callback to return the data
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getAllUsers(DataCallback<List<User>> callback, boolean forceRefresh) {
    if (database == null) {
      Log.e(TAG, "Database not initialized. Call initialize() first.");
      getAllUsersFromServer(callback);
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          // Try to get from cache first
          List<User> cachedUsers = database.userDao().getAllUsers();

          if (cachedUsers != null && !cachedUsers.isEmpty() && !forceRefresh) {
            Log.d(TAG, "Users found in cache: " + cachedUsers.size());
            ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(cachedUsers));
            return;
          }

          // Otherwise, get from server
          getAllUsersFromServer(
              new DataCallback<>() {
                @Override
                public void onDataLoaded(List<User> users) {
                  // Cache the users
                  if (users != null && !users.isEmpty()) {
                    ThreadUtils.runInBackground(
                        () -> {
                          database.userDao().insertUsers(users);
                          Log.d(TAG, "Users cached: " + users.size());
                        });
                  }

                  // Return the users
                  ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(users));
                }

                @Override
                public void onError(String error) {
                  // If we have cached users, return them even if there was an error
                  if (cachedUsers != null && !cachedUsers.isEmpty()) {
                    Log.d(TAG, "Returning cached users after server error: " + cachedUsers.size());
                    ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(cachedUsers));
                  } else {
                    ThreadUtils.runOnMainThread(() -> callback.onError(error));
                  }
                }
              });
        });
  }

  private void getAllUsersFromServer(DataCallback<List<User>> callback) {

    Log.d(TAG, "Fetching all users from server");
    serverClient.getUsers(
        new FirebaseServerClient.DataCallback<>() {
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
            if (!userCache.isEmpty()) {
              Log.d(TAG, "Using cached users as fallback");
              callback.onDataLoaded(new ArrayList<>(userCache.values()));
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
      callback.onError(ERROR_INVALID_USER_ID);
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
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(User user) {
            if (user == null) {
              Log.e(TAG, "Server returned null user for ID: " + userId);
              callback.onError(ERROR_USER_NOT_FOUND);
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
      String currentUserEmail = AuthenticationManager.getCurrentUserEmail(context);
      if (currentUserEmail == null || currentUserEmail.isEmpty()) {
        Log.e(TAG, "No current user found");
        callback.onError(ERROR_NO_CURRENT_USER);
        return;
      }

      String userKey = currentUserEmail.replace(DOT_CHAR, SPACE_CHAR);

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
          new DataCallback<>() {
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
      callback.onError(ERROR_GET_CURRENT_USER + e.getMessage());
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
      callback.onError(ERROR_INVALID_USER_ID);
      return;
    }

    if (user == null) {
      Log.e(TAG, "Cannot save null user");
      callback.onError(ERROR_USER_IS_NULL);
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
      callback.onError(ERROR_INVALID_USER_ID);
      return;
    }

    if (updates == null || updates.isEmpty()) {
      Log.e(TAG, "Updates map is null or empty");
      callback.onError(ERROR_NO_UPDATES);
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
   * Applies a single field update to a user object
   * @param user the user to update
   * @param field the field name to update
   * @param value the new value
   */
  @SuppressWarnings("unchecked")
  private void applyFieldUpdate(User user, String field, Object value) {
    switch (field) {
      case FIELD_USERNAME:
        if (value instanceof String) {
          user.setUsername((String) value);
        }
        break;
      case FIELD_EMAIL:
        if (value instanceof String) {
          user.setEmail((String) value);
        }
        break;
      case FIELD_PROFILE_IMAGE_URL:
        if (value instanceof String) {
          user.setProfileImageUrl((String) value);
        }
        break;
      case FIELD_FRIEND_KEYS:
        if (value instanceof Map) {
          user.setFriendKeys((Map<String, Boolean>) value);
        }
        break;
        // Add more fields as needed
    }
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
  private void applyUpdatesToUser(User user, Map<String, Object> updates) {
    if (user == null || updates == null) {
      return;
    }

    for (Map.Entry<String, Object> entry : updates.entrySet()) {
      String field = entry.getKey();
      Object value = entry.getValue();
      applyFieldUpdate(user, field, value);
    }
  }

  /**
   * Gets a specific user by key
   *
   * @param userKey The user key to find
   * @param callback Callback to return the user
   */
  public void getUser(String userKey, Callback<User> callback) {
    if (userKey == null || userKey.isEmpty()) {
      callback.onError(new IllegalArgumentException(ERROR_USER_KEY_NULL_OR_EMPTY));
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            // Check cache first
            User cachedUser = userCache.get(userKey);
            if (cachedUser != null) {
              ThreadUtils.runOnMainThread(() -> callback.onSuccess(cachedUser));
              return;
            }

            // Get from server
            serverClient.getUser(
                userKey,
                new FirebaseServerClient.DataCallback<>() {
                  @Override
                  public void onSuccess(User user) {
                    if (user != null) {
                      userCache.put(userKey, user);
                    }
                    ThreadUtils.runOnMainThread(() -> callback.onSuccess(user));
                  }

                  @Override
                  public void onError(String errorMessage) {
                    ThreadUtils.runOnMainThread(
                        () -> callback.onError(new Exception(errorMessage)));
                  }
                });

          } catch (Exception e) {
            ThreadUtils.runOnMainThread(() -> callback.onError(e));
          }
        });
  }

  /**
   * Creates a new user
   *
   * @param user The user to create
   * @param callback Callback to return the created user
   */
  public void createUser(User user, Callback<User> callback) {
    if (user == null) {
      callback.onError(new IllegalArgumentException(ERROR_USER_CANNOT_BE_NULL));
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            serverClient.createUser(
                user,
                new FirebaseServerClient.DataCallback<>() {
                  @Override
                  public void onSuccess(User createdUser) {
                    if (createdUser != null) {
                      userCache.put(createdUser.getUserKey(), createdUser);
                    }
                    ThreadUtils.runOnMainThread(() -> callback.onSuccess(createdUser));
                  }

                  @Override
                  public void onError(String errorMessage) {
                    ThreadUtils.runOnMainThread(
                        () -> callback.onError(new Exception(errorMessage)));
                  }
                });

          } catch (Exception e) {
            ThreadUtils.runOnMainThread(() -> callback.onError(e));
          }
        });
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

  /** Interface for generic callbacks */
  public interface Callback<T> {
    void onSuccess(T result);

    void onError(Exception error);
  }

  /**
   * Gets all users (ViewModel wrapper).
   *
   * @param callback Callback to receive the users
   */
  public void getAllUsers(final Callback<List<User>> callback) {
    getAllUsers(
        new DataCallback<>() {
          @Override
          public void onDataLoaded(List<User> users) {
            callback.onSuccess(users);
          }

          @Override
          public void onError(String error) {
            callback.onError(new Exception(error));
          }
        },
        false);
  }
}
