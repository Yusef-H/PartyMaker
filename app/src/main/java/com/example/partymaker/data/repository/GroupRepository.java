package com.example.partymaker.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.partymaker.data.api.Result;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.security.GroupKeyManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for Group data following the Repository pattern with proper separation of concerns.
 * This class provides a clean API for accessing Group data from both local and remote sources,
 * implementing caching strategies and offline support.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Single source of truth for Group data
 *   <li>Automatic caching with cache-first strategy
 *   <li>Offline support with graceful fallback
 *   <li>LiveData support for reactive UI updates
 *   <li>Clean separation between local and remote data sources
 * </ul>
 */
public class GroupRepository {
  private static final String TAG = "GroupRepository";

  private static GroupRepository instance;

  private LocalGroupDataSource localDataSource;
  private final RemoteGroupDataSource remoteDataSource;
  private Context context;
  private boolean isInitialized = false;

  /** Private constructor to prevent direct instantiation. */
  private GroupRepository() {
    this.remoteDataSource = new RemoteGroupDataSource();
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
    if (context != null && !isInitialized) {
      this.context = context.getApplicationContext();
      this.localDataSource = new LocalGroupDataSource(context);
      this.isInitialized = true;
      Log.d(TAG, "GroupRepository initialized with local and remote data sources");
    } else if (context == null) {
      Log.e(TAG, "Cannot initialize GroupRepository: context is null");
    } else {
      Log.d(TAG, "GroupRepository already initialized");
    }
  }

  /**
   * Gets a group by its key using cache-first strategy.
   *
   * @param groupKey The group key
   * @param callback Callback to receive the group
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getGroup(String groupKey, final DataCallback<Group> callback, boolean forceRefresh) {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      callback.onError("Repository not initialized");
      return;
    }

    if (groupKey == null || groupKey.isEmpty()) {
      Log.e(TAG, "Invalid group key provided");
      callback.onError("Invalid group key");
      return;
    }

    if (forceRefresh) {
      // Force refresh: get from server and update cache
      fetchFromRemoteAndCache(groupKey, callback);
      return;
    }

    // Cache-first strategy: try local first, then remote
    localDataSource.getItem(
        groupKey,
        new DataSource.DataCallback<Group>() {
          @Override
          public void onDataLoaded(Group cachedGroup) {
            if (cachedGroup != null) {
              Log.d(TAG, "Group found in cache: " + groupKey);
              callback.onDataLoaded(cachedGroup);
            } else {
              Log.d(TAG, "Group not in cache, fetching from server: " + groupKey);
              fetchFromRemoteAndCache(groupKey, callback);
            }
          }

          @Override
          public void onError(String error) {
            Log.w(TAG, "Cache error, trying server: " + error);
            fetchFromRemoteAndCache(groupKey, callback);
          }
        });
  }

  /**
   * Fetches a group from remote source and caches it locally.
   *
   * @param groupKey The group key
   * @param callback Callback to receive the group
   */
  private void fetchFromRemoteAndCache(String groupKey, final DataCallback<Group> callback) {
    remoteDataSource.getItem(
        groupKey,
        new DataSource.DataCallback<Group>() {
          @Override
          public void onDataLoaded(Group group) {
            if (group != null && isInitialized) {
              // Cache the group locally
              localDataSource.saveItem(
                  groupKey,
                  group,
                  new DataSource.OperationCallback() {
                    @Override
                    public void onComplete() {
                      Log.d(TAG, "Group cached successfully: " + groupKey);
                    }

                    @Override
                    public void onError(String error) {
                      Log.w(TAG, "Failed to cache group: " + error);
                    }
                  });
            }
            callback.onDataLoaded(group);
          }

          @Override
          public void onError(String error) {
            // Fallback to cache if remote fails
            if (isInitialized) {
              localDataSource.getItem(
                  groupKey,
                  new DataSource.DataCallback<Group>() {
                    @Override
                    public void onDataLoaded(Group cachedGroup) {
                      if (cachedGroup != null) {
                        Log.d(TAG, "Using cached group as fallback: " + groupKey);
                        callback.onDataLoaded(cachedGroup);
                      } else {
                        callback.onError(error);
                      }
                    }

                    @Override
                    public void onError(String cacheError) {
                      callback.onError(error); // Return original remote error
                    }
                  });
            } else {
              callback.onError(error);
            }
          }
        });
  }

  /**
   * Gets all groups using cache-first strategy.
   *
   * @param callback Callback to receive the groups
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void getAllGroups(final DataCallback<List<Group>> callback, boolean forceRefresh) {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      callback.onError("Repository not initialized");
      return;
    }

    if (forceRefresh) {
      // Force refresh: get from server and update cache
      fetchAllFromRemoteAndCache(callback);
      return;
    }

    // Cache-first strategy: try local first, then remote
    localDataSource.getAllItems(
        new DataSource.DataCallback<List<Group>>() {
          @Override
          public void onDataLoaded(List<Group> cachedGroups) {
            if (cachedGroups != null && !cachedGroups.isEmpty()) {
              Log.d(TAG, "Groups found in cache: " + cachedGroups.size());
              callback.onDataLoaded(cachedGroups);
            } else {
              Log.d(TAG, "No groups in cache, fetching from server");
              fetchAllFromRemoteAndCache(callback);
            }
          }

          @Override
          public void onError(String error) {
            Log.w(TAG, "Cache error, trying server: " + error);
            fetchAllFromRemoteAndCache(callback);
          }
        });
  }

  /**
   * Fetches all groups from remote source and caches them locally.
   *
   * @param callback Callback to receive the groups
   */
  private void fetchAllFromRemoteAndCache(final DataCallback<List<Group>> callback) {
    remoteDataSource.getAllItems(
        new DataSource.DataCallback<List<Group>>() {
          @Override
          public void onDataLoaded(List<Group> groups) {
            if (groups != null && !groups.isEmpty() && isInitialized) {
              // Cache the groups locally (save each group individually)
              for (Group group : groups) {
                if (group.getGroupKey() != null) {
                  localDataSource.saveItem(
                      group.getGroupKey(),
                      group,
                      new DataSource.OperationCallback() {
                        @Override
                        public void onComplete() {
                          // Group cached successfully
                        }

                        @Override
                        public void onError(String error) {
                          Log.w(TAG, "Failed to cache group: " + error);
                        }
                      });
                }
              }
              Log.d(TAG, "Groups cached successfully: " + groups.size());
            }
            callback.onDataLoaded(groups);
          }

          @Override
          public void onError(String error) {
            // Fallback to cache if remote fails
            if (isInitialized) {
              localDataSource.getAllItems(
                  new DataSource.DataCallback<List<Group>>() {
                    @Override
                    public void onDataLoaded(List<Group> cachedGroups) {
                      if (cachedGroups != null && !cachedGroups.isEmpty()) {
                        Log.d(TAG, "Using cached groups as fallback: " + cachedGroups.size());
                        callback.onDataLoaded(cachedGroups);
                      } else {
                        callback.onError(error);
                      }
                    }

                    @Override
                    public void onError(String cacheError) {
                      callback.onError(error); // Return original remote error
                    }
                  });
            } else {
              callback.onError(error);
            }
          }
        });
  }

  /**
   * Saves a group to both remote and local sources.
   *
   * @param groupKey The group key
   * @param group The group to save
   * @param callback Callback for operation result
   */
  public void saveGroup(String groupKey, Group group, final OperationCallback callback) {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      callback.onError("Repository not initialized");
      return;
    }

    if (groupKey == null || groupKey.isEmpty() || group == null) {
      Log.e(TAG, "Invalid parameters for saveGroup");
      callback.onError("Invalid group data");
      return;
    }

    // Save to remote first (source of truth)
    remoteDataSource.saveItem(
        groupKey,
        group,
        new DataSource.OperationCallback() {
          @Override
          public void onComplete() {
            // Then cache locally
            localDataSource.saveItem(
                groupKey,
                group,
                new DataSource.OperationCallback() {
                  @Override
                  public void onComplete() {
                    Log.d(TAG, "Group saved and cached successfully: " + groupKey);
                    callback.onComplete();
                  }

                  @Override
                  public void onError(String error) {
                    Log.w(TAG, "Failed to cache group after saving: " + error);
                    // Still report success since remote save succeeded
                    callback.onComplete();
                  }
                });
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to save group to server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Updates a group in both remote and local sources.
   *
   * @param groupKey The group key
   * @param updates The updates to apply
   * @param callback Callback for operation result
   */
  public void updateGroup(
      String groupKey, Map<String, Object> updates, final OperationCallback callback) {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      callback.onError("Repository not initialized");
      return;
    }

    if (groupKey == null || groupKey.isEmpty() || updates == null || updates.isEmpty()) {
      Log.e(TAG, "Invalid parameters for updateGroup");
      callback.onError("Invalid update data");
      return;
    }

    // Update remote first (source of truth)
    remoteDataSource.updateItem(
        groupKey,
        updates,
        new DataSource.OperationCallback() {
          @Override
          public void onComplete() {
            // Then update local cache
            localDataSource.updateItem(
                groupKey,
                updates,
                new DataSource.OperationCallback() {
                  @Override
                  public void onComplete() {
                    Log.d(TAG, "Group updated successfully: " + groupKey);
                    callback.onComplete();
                  }

                  @Override
                  public void onError(String error) {
                    Log.w(TAG, "Failed to update cached group: " + error);
                    // Still report success since remote update succeeded
                    callback.onComplete();
                  }
                });
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to update group on server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Deletes a group from both remote and local sources.
   *
   * @param groupKey The group key
   * @param callback Callback for operation result
   */
  public void deleteGroup(String groupKey, final OperationCallback callback) {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      callback.onError("Repository not initialized");
      return;
    }

    if (groupKey == null || groupKey.isEmpty()) {
      Log.e(TAG, "Invalid group key for deleteGroup");
      callback.onError("Invalid group key");
      return;
    }

    // Delete from remote first (source of truth)
    remoteDataSource.deleteItem(
        groupKey,
        new DataSource.OperationCallback() {
          @Override
          public void onComplete() {
            // Then delete from local cache
            localDataSource.deleteItem(
                groupKey,
                new DataSource.OperationCallback() {
                  @Override
                  public void onComplete() {
                    Log.d(TAG, "Group deleted successfully: " + groupKey);
                    callback.onComplete();
                  }

                  @Override
                  public void onError(String error) {
                    Log.w(TAG, "Failed to delete cached group: " + error);
                    // Still report success since remote delete succeeded
                    callback.onComplete();
                  }
                });
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to delete group from server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Gets a LiveData object for observing a group.
   *
   * @param groupKey The group key
   * @return LiveData for the group, or null if not initialized
   */
  public LiveData<Group> observeGroup(String groupKey) {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      return null;
    }

    return localDataSource.observeItem(groupKey);
  }

  /**
   * Gets a LiveData object for observing all groups.
   *
   * @return LiveData for all groups, or null if not initialized
   */
  public LiveData<List<Group>> observeAllGroups() {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      return null;
    }

    return localDataSource.observeAllItems();
  }

  /**
   * Gets a LiveData object with all groups wrapped in Result.
   *
   * @return LiveData with all groups wrapped in Result
   */
  public LiveData<Result<List<Group>>> getAllGroupsLiveData() {
    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      return null;
    }

    // Transform the Room LiveData to include Result wrapper
    LiveData<List<Group>> roomLiveData = localDataSource.observeAllItems();
    if (roomLiveData == null) {
      return null;
    }

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
    Log.d(TAG, "getUserGroups called for user: " + userKey + ", forceRefresh: " + forceRefresh);

    if (!isInitialized) {
      Log.e(TAG, "Repository not initialized. Call initialize() first.");
      callback.onDataLoaded(Result.error("Repository not initialized"));
      return;
    }

    if (userKey == null || userKey.isEmpty()) {
      Log.e(TAG, "Invalid user key provided");
      callback.onDataLoaded(Result.error("Invalid user key"));
      return;
    }

    // If forceRefresh is true, skip cache entirely and go directly to server
    if (forceRefresh) {
      Log.d(TAG, "Force refresh requested - skipping cache, going directly to server");
      getUserGroupsFromServer(userKey, callback);
      return;
    }

    // Get all groups from cache and filter for user
    localDataSource.getAllItems(
        new DataSource.DataCallback<List<Group>>() {
          @Override
          public void onDataLoaded(List<Group> cachedGroups) {
            List<Group> userGroups = new ArrayList<>();

            // Filter groups for this user
            if (cachedGroups != null && !cachedGroups.isEmpty()) {
              Log.d(
                  TAG, "Filtering " + cachedGroups.size() + " cached groups for user: " + userKey);
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
                }
              }
            }

            if (!userGroups.isEmpty()) {
              Log.d(TAG, "User groups found in cache: " + userGroups.size());
              callback.onDataLoaded(Result.success(userGroups));
            } else {
              Log.d(TAG, "No cached user groups found, fetching from server");
              getUserGroupsFromServer(userKey, callback);
            }
          }

          @Override
          public void onError(String error) {
            Log.w(TAG, "Cache error, trying server: " + error);
            getUserGroupsFromServer(userKey, callback);
          }
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

    // Notify loading state
    callback.onDataLoaded(Result.loading());

    // Use RemoteGroupDataSource to get user groups
    if (remoteDataSource instanceof RemoteGroupDataSource) {
      ((RemoteGroupDataSource) remoteDataSource)
          .getUserGroups(
              userKey,
              new DataSource.DataCallback<List<Group>>() {
                @Override
                public void onDataLoaded(List<Group> groups) {
                  Log.d(
                      TAG,
                      "User groups loaded from server: " + (groups != null ? groups.size() : 0));

                  // Cache the groups
                  if (groups != null && !groups.isEmpty() && isInitialized) {
                    for (Group group : groups) {
                      if (group.getGroupKey() != null) {
                        localDataSource.saveItem(
                            group.getGroupKey(),
                            group,
                            new DataSource.OperationCallback() {
                              @Override
                              public void onComplete() {
                                // Group cached successfully
                              }

                              @Override
                              public void onError(String error) {
                                Log.w(TAG, "Failed to cache user group: " + error);
                              }
                            });
                      }
                    }
                    Log.d(TAG, "User groups cached: " + groups.size());
                  }

                  // Return result
                  callback.onDataLoaded(
                      Result.success(groups != null ? groups : new ArrayList<>()));
                }

                @Override
                public void onError(String errorMessage) {
                  Log.e(TAG, "Error loading user groups: " + errorMessage);

                  // Try to get from cache as fallback
                  localDataSource.getAllItems(
                      new DataSource.DataCallback<List<Group>>() {
                        @Override
                        public void onDataLoaded(List<Group> cachedGroups) {
                          List<Group> userGroups = new ArrayList<>();

                          // Filter groups for this user
                          if (cachedGroups != null && !cachedGroups.isEmpty()) {
                            for (Group group : cachedGroups) {
                              if ((group.getFriendKeys() != null
                                      && group.getFriendKeys().containsKey(userKey))
                                  || (group.getAdminKey() != null
                                      && group.getAdminKey().equals(userKey))) {
                                userGroups.add(group);
                              }
                            }
                          }

                          if (!userGroups.isEmpty()) {
                            Log.d(
                                TAG,
                                "Using cached user groups due to network error: "
                                    + userGroups.size());
                            callback.onDataLoaded(Result.success(userGroups));
                          } else {
                            Log.e(TAG, "No cached user groups available");
                            callback.onDataLoaded(Result.error(errorMessage));
                          }
                        }

                        @Override
                        public void onError(String cacheError) {
                          Log.e(TAG, "Cache also failed: " + cacheError);
                          callback.onDataLoaded(Result.error(errorMessage));
                        }
                      });
                }
              });
    } else {
      Log.e(TAG, "RemoteGroupDataSource not available");
      callback.onDataLoaded(Result.error("Remote data source not available"));
    }
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

    updateGroup(groupKey, updates, new OperationCallback() {
      @Override
      public void onComplete() {
        Log.d(TAG, "User added to group, now adding to encryption");
        
        // Add user to group encryption
        if (context != null) {
          try {
            GroupKeyManager groupKeyManager = new GroupKeyManager(context, userKey);
            groupKeyManager.addUserToGroupEncryption(groupKey, userKey).thenAccept(success -> {
              if (success) {
                Log.i(TAG, "User added to group encryption successfully");
                callback.onComplete();
              } else {
                Log.w(TAG, "Failed to add user to group encryption, but group join succeeded");
                callback.onComplete(); // Still complete the join, encryption can be retried later
              }
            });
          } catch (Exception e) {
            Log.e(TAG, "Error adding user to group encryption", e);
            callback.onComplete(); // Still complete the join
          }
        } else {
          Log.w(TAG, "Context is null, cannot add to group encryption");
          callback.onComplete();
        }
      }
      
      @Override
      public void onError(String error) {
        callback.onError(error);
      }
    });
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

    updateGroup(groupKey, updates, new OperationCallback() {
      @Override
      public void onComplete() {
        Log.d(TAG, "User removed from group, now removing from encryption and rotating key");
        
        // Remove user from group encryption and rotate key for security
        if (context != null) {
          try {
            GroupKeyManager groupKeyManager = new GroupKeyManager(context, userKey);
            groupKeyManager.removeUserAndRotateKey(groupKey, userKey).thenAccept(success -> {
              if (success) {
                Log.i(TAG, "User removed from group encryption and key rotated successfully");
                callback.onComplete();
              } else {
                Log.w(TAG, "Failed to remove user from group encryption, but group leave succeeded");
                callback.onComplete(); // Still complete the leave
              }
            });
          } catch (Exception e) {
            Log.e(TAG, "Error removing user from group encryption", e);
            callback.onComplete(); // Still complete the leave
          }
        } else {
          Log.w(TAG, "Context is null, cannot remove from group encryption");
          callback.onComplete();
        }
      }
      
      @Override
      public void onError(String error) {
        callback.onError(error);
      }
    });
  }

  /** Clears all cached data and database entries */
  public void clearCache() {
    Log.d(TAG, "Clearing GroupRepository cache");

    if (isInitialized) {
      localDataSource.clearCache();
    } else {
      Log.w(TAG, "Repository not initialized, cannot clear cache");
    }
  }

  // Re-use DataSource interfaces for consistency
  public interface DataCallback<T> extends DataSource.DataCallback<T> {}

  public interface OperationCallback extends DataSource.OperationCallback {}
}
