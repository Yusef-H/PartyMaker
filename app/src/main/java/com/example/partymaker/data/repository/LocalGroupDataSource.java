package com.example.partymaker.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.partymaker.data.local.AppDatabase;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import java.util.List;
import java.util.Map;

/**
 * Local data source implementation for Group entities using Room database. Handles all local
 * database operations for groups with proper error handling and thread management.
 */
public class LocalGroupDataSource implements DataSource<Group, String> {
  private static final String TAG = "LocalGroupDataSource";
  private static final String ERROR_DATABASE_NOT_INITIALIZED = "Database not available";
  private static final String ERROR_DATABASE_OPERATION_FAILED = "Database error: ";
  private static final String ERROR_GROUP_NOT_FOUND = "Group not found in local database";
  private static final String ERROR_INVALID_INPUT = "Invalid group key or group data provided";
  private static final String ERROR_INVALID_UPDATES = "Invalid group key or updates provided";
  private static final String WARNING_UNKNOWN_UPDATE_FIELD = "Unknown update field: ";
  private static final String WARNING_INVALID_GROUP_TYPE_VALUE = "Invalid groupType value: ";

  private final AppDatabase database;

  /**
   * Constructor for LocalGroupDataSource.
   *
   * @param context The application context
   */
  public LocalGroupDataSource(Context context) {
    this.database = AppDatabase.getInstance(context);
  }

  @Override
  public void getItem(String groupKey, DataCallback<Group> callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError(ERROR_DATABASE_NOT_INITIALIZED);
      return;
    }

    if (isInvalidGroupKey(groupKey)) {
      callback.onError("Invalid group key provided");
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            Group group = database.groupDao().getGroupByKey(groupKey);
            ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(group));
          } catch (Exception e) {
            Log.e(TAG, "Error getting group from local database: " + groupKey, e);
            ThreadUtils.runOnMainThread(
                () -> callback.onError(ERROR_DATABASE_OPERATION_FAILED + e.getMessage()));
          }
        });
  }

  @Override
  public void getAllItems(DataCallback<List<Group>> callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError(ERROR_DATABASE_NOT_INITIALIZED);
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            List<Group> groups = database.groupDao().getAllGroups();
            ThreadUtils.runOnMainThread(() -> callback.onDataLoaded(groups));
          } catch (Exception e) {
            Log.e(TAG, "Error getting all groups from local database", e);
            ThreadUtils.runOnMainThread(
                () -> callback.onError(ERROR_DATABASE_OPERATION_FAILED + e.getMessage()));
          }
        });
  }

  @Override
  public void saveItem(String groupKey, Group group, OperationCallback callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError(ERROR_DATABASE_NOT_INITIALIZED);
      return;
    }

    if (isInvalidGroupKey(groupKey) || group == null) {
      callback.onError(ERROR_INVALID_INPUT);
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            ensureGroupKeyIsSet(group, groupKey);

            database.groupDao().insertGroup(group);
            Log.d(TAG, "Group saved to local database: " + groupKey);
            ThreadUtils.runOnMainThread(callback::onComplete);
          } catch (Exception e) {
            Log.e(TAG, "Error saving group to local database: " + groupKey, e);
            ThreadUtils.runOnMainThread(
                () -> callback.onError(ERROR_DATABASE_OPERATION_FAILED + e.getMessage()));
          }
        });
  }

  @Override
  public void updateItem(String groupKey, Map<String, Object> updates, OperationCallback callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError(ERROR_DATABASE_NOT_INITIALIZED);
      return;
    }

    if (isInvalidGroupKey(groupKey) || updates == null || updates.isEmpty()) {
      callback.onError(ERROR_INVALID_UPDATES);
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            Group existingGroup = database.groupDao().getGroupByKey(groupKey);
            if (existingGroup == null) {
              ThreadUtils.runOnMainThread(() -> callback.onError(ERROR_GROUP_NOT_FOUND));
              return;
            }

            // Apply updates to the existing group
            applyUpdatesToGroup(existingGroup, updates);

            database.groupDao().updateGroup(existingGroup);
            Log.d(TAG, "Group updated in local database: " + groupKey);
            ThreadUtils.runOnMainThread(callback::onComplete);
          } catch (Exception e) {
            Log.e(TAG, "Error updating group in local database: " + groupKey, e);
            ThreadUtils.runOnMainThread(
                () -> callback.onError(ERROR_DATABASE_OPERATION_FAILED + e.getMessage()));
          }
        });
  }

  @Override
  public void deleteItem(String groupKey, OperationCallback callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError(ERROR_DATABASE_NOT_INITIALIZED);
      return;
    }

    if (isInvalidGroupKey(groupKey)) {
      callback.onError("Invalid group key provided");
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            database.groupDao().deleteGroupByKey(groupKey);
            Log.d(TAG, "Group deleted from local database: " + groupKey);
            ThreadUtils.runOnMainThread(callback::onComplete);
          } catch (Exception e) {
            Log.e(TAG, "Error deleting group from local database: " + groupKey, e);
            ThreadUtils.runOnMainThread(
                () -> callback.onError(ERROR_DATABASE_OPERATION_FAILED + e.getMessage()));
          }
        });
  }

  @Override
  public LiveData<Group> observeItem(String groupKey) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      return null;
    }
    return database.groupDao().observeGroupByKey(groupKey);
  }

  @Override
  public LiveData<List<Group>> observeAllItems() {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      return null;
    }
    return database.groupDao().observeAllGroups();
  }

  @Override
  public void clearCache() {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            database.groupDao().deleteAllGroups();
            Log.d(TAG, "All groups cleared from local database");
          } catch (Exception e) {
            Log.e(TAG, "Error clearing groups from local database", e);
          }
        });
  }

  /**
   * Applies updates from a map to a Group object. This is a simplified approach - in a production
   * app, you might want to use reflection or create a more robust mapping system.
   *
   * @param group The group to update
   * @param updates The updates to apply
   */
  private void applyUpdatesToGroup(Group group, Map<String, Object> updates) {
    for (Map.Entry<String, Object> entry : updates.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      switch (key) {
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
        case "groupPrice":
          if (value instanceof String) {
            group.setGroupPrice((String) value);
          }
          break;
        case "groupType":
          if (value instanceof Integer) {
            group.setGroupType((Integer) value);
          } else if (value instanceof String) {
            try {
              group.setGroupType(Integer.parseInt((String) value));
            } catch (NumberFormatException e) {
              Log.w(TAG, WARNING_INVALID_GROUP_TYPE_VALUE + value);
            }
          }
          break;
        case "adminKey":
          if (value instanceof String) {
            group.setAdminKey((String) value);
          }
          break;
          // Add more fields as needed
        default:
          Log.w(TAG, WARNING_UNKNOWN_UPDATE_FIELD + key);
          break;
      }
    }
  }

  /**
   * Validates if a group key is invalid (null or empty).
   *
   * @param groupKey The group key to validate
   * @return true if the key is invalid, false otherwise
   */
  private boolean isInvalidGroupKey(String groupKey) {
    return groupKey == null || groupKey.trim().isEmpty();
  }

  /**
   * Ensures the group has the correct key set.
   *
   * @param group The group to update
   * @param groupKey The key to set
   */
  private void ensureGroupKeyIsSet(Group group, String groupKey) {
    if (group.getGroupKey() == null || group.getGroupKey().isEmpty()) {
      group.setGroupKey(groupKey);
    }
  }
}
