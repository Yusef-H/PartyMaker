package com.example.partymaker.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.api.Result;
import com.example.partymaker.data.local.AppDatabase;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.system.ThreadUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for Group data. This class provides a clean API for accessing Group data from
 * different sources.
 */
public class GroupRepository {
  private static final String TAG = "GroupRepository";

  private static GroupRepository instance;

  private final FirebaseServerClient serverClient;
  private final Executor executor;
  private final Handler mainHandler;
  private AppDatabase database;

  /** Private constructor to prevent direct instantiation. */
  private GroupRepository() {
    this.serverClient = FirebaseServerClient.getInstance();
    this.executor = Executors.newFixedThreadPool(4);
    this.mainHandler = new Handler(Looper.getMainLooper());
  }

  /**
   * Gets the singleton instance of GroupRepository.
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
   * Initializes the repository with a context. This is required for database access.
   *
   * @param context The application context
   */
  public void initialize(Context context) {
    if (context != null) {
      database = AppDatabase.getInstance(context);
      Log.d(TAG, "GroupRepository initialized with database");
    } else {
      Log.e(TAG, "Cannot initialize GroupRepository: context is null");
    }
  }

  /**
   * Gets a group by its key.
   *
   * @param groupKey The group key
   * @param callback Callback to receive the group
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getGroup(String groupKey, final DataCallback<Group> callback, boolean forceRefresh) {
    if (database == null) {
      Log.e(TAG, "Database not initialized. Call initialize() first.");
      getGroupFromServer(groupKey, callback);
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          // Try to get from cache first
          Group cachedGroup = database.groupDao().getGroupByKey(groupKey);

          if (cachedGroup != null && !forceRefresh) {
            Log.d(TAG, "Group found in cache: " + groupKey);
            ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(cachedGroup));
            return;
          }

          // Otherwise, get from server
          getGroupFromServer(
              groupKey,
              new DataCallback<>() {
                @Override
                public void onDataLoaded(Group group) {
                  // Cache the group
                  if (group != null) {
                    ThreadUtils.runInBackground(
                        () -> {
                          database.groupDao().insertGroup(group);
                          Log.d(TAG, "Group cached: " + groupKey);
                        });
                  }

                  // Return the group
                  ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(group));
                }

                @Override
                public void onError(String error) {
                  // If we have a cached group, return it even if there was an error
                  if (cachedGroup != null) {
                    Log.d(TAG, "Returning cached group after server error: " + groupKey);
                    ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(cachedGroup));
                  } else {
                    ThreadUtils.runOnMainThread(() -> callback.onError(error));
                  }
                }
              });
        });
  }

  /**
   * Gets a group from the server.
   *
   * @param groupKey The group key
   * @param callback Callback to receive the group
   */
  private void getGroupFromServer(String groupKey, final DataCallback<Group> callback) {
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            callback.onDataLoaded(group);
          }

          @Override
          public void onError(String errorMessage) {
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Gets all groups.
   *
   * @param callback Callback to receive the groups
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getAllGroups(final DataCallback<List<Group>> callback, boolean forceRefresh) {
    if (database == null) {
      Log.e(TAG, "Database not initialized. Call initialize() first.");
      getAllGroupsFromServer(callback);
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          // Try to get from cache first
          List<Group> cachedGroups = database.groupDao().getAllGroups();

          if (cachedGroups != null && !cachedGroups.isEmpty() && !forceRefresh) {
            Log.d(TAG, "Groups found in cache: " + cachedGroups.size());
            ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(cachedGroups));
            return;
          }

          // Otherwise, get from server
          getAllGroupsFromServer(
              new DataCallback<>() {
                @Override
                public void onDataLoaded(List<Group> groups) {
                  // Cache the groups
                  if (groups != null && !groups.isEmpty()) {
                    ThreadUtils.runInBackground(
                        () -> {
                          database.groupDao().insertGroups(groups);
                          Log.d(TAG, "Groups cached: " + groups.size());
                        });
                  }

                  // Return the groups
                  ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(groups));
                }

                @Override
                public void onError(String error) {
                  // If we have cached groups, return them even if there was an error
                  if (cachedGroups != null && !cachedGroups.isEmpty()) {
                    Log.d(
                        TAG, "Returning cached groups after server error: " + cachedGroups.size());
                    ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(cachedGroups));
                  } else {
                    ThreadUtils.runOnMainThread(() -> callback.onError(error));
                  }
                }
              });
        });
  }

  /**
   * Gets all groups from the server.
   *
   * @param callback Callback to receive the groups
   */
  private void getAllGroupsFromServer(final DataCallback<List<Group>> callback) {
    serverClient.getGroups(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, Group> groupMap) {
            List<Group> groups = new java.util.ArrayList<>(groupMap.values());
            callback.onDataLoaded(groups);
          }

          @Override
          public void onError(String errorMessage) {
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Saves a group.
   *
   * @param groupKey The group key
   * @param group The group to save
   * @param callback Callback for operation result
   */
  public void saveGroup(String groupKey, Group group, final OperationCallback callback) {
    serverClient.saveGroup(
        groupKey,
        group,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            // Cache the group
            if (database != null) {
              ThreadUtils.runInBackground(
                  () -> {
                    database.groupDao().insertGroup(group);
                    Log.d(TAG, "Group saved and cached: " + groupKey);
                  });
            }

            callback.onComplete();
          }

          @Override
          public void onError(String errorMessage) {
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Updates a group.
   *
   * @param groupKey The group key
   * @param updates The updates to apply
   * @param callback Callback for operation result
   */
  public void updateGroup(
      String groupKey, Map<String, Object> updates, final OperationCallback callback) {
    serverClient.updateGroup(
        groupKey,
        updates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            // Update the cached group
            if (database != null) {
              ThreadUtils.runInBackground(
                  () -> {
                    Group cachedGroup = database.groupDao().getGroupByKey(groupKey);
                    if (cachedGroup != null) {
                      // Apply updates to cached group
                      // This is a simplified approach; in a real app, you'd need to handle all
                      // update types
                      if (updates.containsKey("groupName")) {
                        cachedGroup.setGroupName((String) updates.get("groupName"));
                      }
                      if (updates.containsKey("groupLocation")) {
                        cachedGroup.setGroupLocation((String) updates.get("groupLocation"));
                      }
                      // Add more fields as needed

                      database.groupDao().updateGroup(cachedGroup);
                      Log.d(TAG, "Group updated in cache: " + groupKey);
                    }
                  });
            }

            callback.onComplete();
          }

          @Override
          public void onError(String errorMessage) {
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Deletes a group.
   *
   * @param groupKey The group key
   * @param callback Callback for operation result
   */
  public void deleteGroup(String groupKey, final OperationCallback callback) {
    serverClient.deleteGroup(
        groupKey,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            // Delete from cache
            if (database != null) {
              ThreadUtils.runInBackground(
                  () -> {
                    database.groupDao().deleteGroupByKey(groupKey);
                    Log.d(TAG, "Group deleted from cache: " + groupKey);
                  });
            }

            callback.onComplete();
          }

          @Override
          public void onError(String errorMessage) {
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Gets a LiveData object for observing a group.
   *
   * @param groupKey The group key
   * @return LiveData for the group
   */
  public LiveData<Group> observeGroup(String groupKey) {
    if (database == null) {
      Log.e(TAG, "Database not initialized. Call initialize() first.");
      return null;
    }

    return database.groupDao().observeGroupByKey(groupKey);
  }

  /**
   * Gets a LiveData object for observing all groups.
   *
   * @return LiveData for all groups
   */
  public LiveData<List<Group>> observeAllGroups() {
    if (database == null) {
      Log.e(TAG, "Database not initialized. Call initialize() first.");
      return null;
    }

    return database.groupDao().observeAllGroups();
  }

  /**
   * Gets a LiveData object with all groups.
   *
   * @return LiveData with all groups
   */
  public LiveData<Result<List<Group>>> getAllGroupsLiveData() {
    if (database == null) {
      Log.e(TAG, "Database not initialized. Call initialize() first.");
      return null;
    }

    // Transform the Room LiveData to include Result wrapper
    LiveData<List<Group>> roomLiveData = database.groupDao().observeAllGroups();

    // Create a MediatorLiveData to transform the data
    androidx.lifecycle.MediatorLiveData<Result<List<Group>>> mediatorLiveData =
        new androidx.lifecycle.MediatorLiveData<>();

    mediatorLiveData.addSource(
        roomLiveData,
        groups -> {
          if (groups != null) {
            mediatorLiveData.setValue(Result.success(groups));
          } else {
            mediatorLiveData.setValue(Result.success(new ArrayList<>()));
          }
        });

    return mediatorLiveData;
  }

  /**
   * Gets groups for a specific user.
   *
   * @param userKey The user key
   * @param callback Callback to receive the groups
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getUserGroups(
      String userKey, DataCallback<Result<List<Group>>> callback, boolean forceRefresh) {
    Log.d(TAG, "getUserGroups called for user: " + userKey);

    if (database == null) {
      Log.e(TAG, "Database not initialized. Call initialize() first.");
      getUserGroupsFromServer(userKey, callback);
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          // Try to get from cache first
          List<Group> cachedGroups = database.groupDao().getAllGroups();
          List<Group> userGroups = new ArrayList<>();

          // Filter groups for this user
          if (cachedGroups != null && !cachedGroups.isEmpty()) {
            Log.d(TAG, "Filtering " + cachedGroups.size() + " cached groups for user: " + userKey);
            for (Group group : cachedGroups) {
              boolean isAdmin =
                  (group.getAdminKey() != null && group.getAdminKey().equals(userKey));
              boolean isMember =
                  (group.getFriendKeys() != null && group.getFriendKeys().containsKey(userKey));

              if (isAdmin || isMember) {
                userGroups.add(group);
                Log.d(
                    TAG,
                    "Group "
                        + group.getGroupName()
                        + " belongs to user "
                        + userKey
                        + " (admin: "
                        + isAdmin
                        + ", member: "
                        + isMember
                        + ")");
              } else {
                Log.d(
                    TAG,
                    "Group "
                        + group.getGroupName()
                        + " does NOT belong to user "
                        + userKey
                        + " (adminKey: "
                        + group.getAdminKey()
                        + ")");
              }
            }
          } else {
            Log.d(TAG, "No cached groups found or empty cache");
          }

          if (!userGroups.isEmpty() && !forceRefresh) {
            Log.d(TAG, "User groups found in cache: " + userGroups.size());
            final List<Group> finalUserGroups = userGroups;
            ThreadUtils.runOnMainThread(
                () -> callback.onDataLoaded(Result.success(finalUserGroups)));
            return;
          }

          // Otherwise, get from server
          getUserGroupsFromServer(userKey, callback);
        });
  }

  /**
   * Gets groups for a specific user from the server.
   *
   * @param userKey The user key
   * @param callback Callback to receive the groups
   */
  private void getUserGroupsFromServer(String userKey, DataCallback<Result<List<Group>>> callback) {
    Log.d(TAG, "Getting user groups from server for: " + userKey);

    // Notify loading state on main thread
    ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(Result.loading()));

    serverClient.getUserGroups(
        userKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, Group> groupMap) {
            List<Group> groups = new ArrayList<>(groupMap.values());
            Log.d(TAG, "User groups loaded from server: " + groups.size());

            // Cache the groups
            if (database != null && !groups.isEmpty()) {
              ThreadUtils.runInBackground(
                  () -> {
                    database.groupDao().insertGroups(groups);
                    Log.d(TAG, "User groups cached: " + groups.size());
                  });
            }

            // Return result on main thread
            ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(Result.success(groups)));
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error loading user groups: " + errorMessage);

            // Try to get from cache as fallback
            ThreadUtils.runInBackground(
                () -> {
                  List<Group> cachedGroups = database.groupDao().getAllGroups();
                  List<Group> userGroups = new ArrayList<>();

                  // Filter groups for this user
                  if (cachedGroups != null && !cachedGroups.isEmpty()) {
                    for (Group group : cachedGroups) {
                      if ((group.getFriendKeys() != null
                              && group.getFriendKeys().containsKey(userKey))
                          || (group.getAdminKey() != null && group.getAdminKey().equals(userKey))) {
                        userGroups.add(group);
                      }
                    }
                  }

                  if (!userGroups.isEmpty()) {
                    Log.d(
                        TAG, "Using cached user groups due to network error: " + userGroups.size());
                    ThreadUtils.runOnMainThread(
                        () -> callback.onDataLoaded(Result.success(userGroups)));
                  } else {
                    // No cached data available
                    Log.e(TAG, "No cached user groups available");
                    ThreadUtils.runOnMainThread(
                        () -> callback.onDataLoaded(Result.error(errorMessage)));
                  }
                });
          }
        });
  }

  /**
   * Joins a group for the specified user.
   *
   * @param groupKey The group key
   * @param userKey The user key
   * @param callback Callback for operation result
   */
  public void joinGroup(String groupKey, String userKey, final OperationCallback callback) {
    if (groupKey == null || groupKey.isEmpty()) {
      callback.onError("Invalid group key");
      return;
    }

    if (userKey == null || userKey.isEmpty()) {
      callback.onError("Invalid user key");
      return;
    }

    Log.d(TAG, "Joining group: " + groupKey + " for user: " + userKey);

    // Add user to group's friendKeys
    Map<String, Object> updates = new HashMap<>();
    updates.put("friendKeys/" + userKey, true);

    updateGroup(groupKey, updates, callback);
  }

  /**
   * Leaves a group for the specified user.
   *
   * @param groupKey The group key
   * @param userKey The user key
   * @param callback Callback for operation result
   */
  public void leaveGroup(String groupKey, String userKey, final OperationCallback callback) {
    if (groupKey == null || groupKey.isEmpty()) {
      callback.onError("Invalid group key");
      return;
    }

    if (userKey == null || userKey.isEmpty()) {
      callback.onError("Invalid user key");
      return;
    }

    Log.d(TAG, "Leaving group: " + groupKey + " for user: " + userKey);

    // Remove user from group's friendKeys
    Map<String, Object> updates = new HashMap<>();
    updates.put("friendKeys/" + userKey, null); // null removes the field in Firebase

    updateGroup(groupKey, updates, callback);
  }

  /** Clears all cached data and database entries */
  public void clearCache() {
    Log.d(TAG, "Clearing GroupRepository cache and database");

    // Clear database in background thread
    if (database != null) {
      ThreadUtils.runInBackground(
          () -> {
            try {
              database.groupDao().deleteAllGroups();
              Log.d(TAG, "All groups deleted from database");
            } catch (Exception e) {
              Log.e(TAG, "Error clearing groups from database", e);
            }
          });
    }
  }

  /**
   * Callback interface for data operations. This interface is designed to be used with lambda
   * expressions.
   *
   * @param <T> The type of data
   */
  @FunctionalInterface
  public interface DataCallback<T> {
    void onDataLoaded(T data);

    /**
     * Called when an error occurs. Default implementation does nothing.
     *
     * @param error The error message
     */
    default void onError(String error) {
      // Default implementation does nothing
    }
  }

  /**
   * Callback interface for operations. This interface is designed to be used with lambda
   * expressions.
   */
  @FunctionalInterface
  public interface OperationCallback {
    void onComplete();

    /**
     * Called when an error occurs. Default implementation does nothing.
     *
     * @param error The error message
     */
    default void onError(String error) {
      // Default implementation does nothing
    }
  }
}
