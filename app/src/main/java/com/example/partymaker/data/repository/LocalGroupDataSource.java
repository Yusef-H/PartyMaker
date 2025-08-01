package com.example.partymaker.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.partymaker.data.local.AppDatabase;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.system.ThreadUtils;
import java.util.List;
import java.util.Map;

/**
 * Local data source implementation for Group entities using Room database. Handles all local
 * database operations for groups.
 */
public class LocalGroupDataSource implements DataSource<Group, String> {
  private static final String TAG = "LocalGroupDataSource";

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
      callback.onError("Database not available");
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
                () -> callback.onError("Database error: " + e.getMessage()));
          }
        });
  }

  @Override
  public void getAllItems(DataCallback<List<Group>> callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError("Database not available");
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
                () -> callback.onError("Database error: " + e.getMessage()));
          }
        });
  }

  @Override
  public void saveItem(String groupKey, Group group, OperationCallback callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError("Database not available");
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            // Ensure the group has the correct key
            if (group.getGroupKey() == null || group.getGroupKey().isEmpty()) {
              group.setGroupKey(groupKey);
            }

            database.groupDao().insertGroup(group);
            Log.d(TAG, "Group saved to local database: " + groupKey);
            ThreadUtils.runOnMainThread(callback::onComplete);
          } catch (Exception e) {
            Log.e(TAG, "Error saving group to local database: " + groupKey, e);
            ThreadUtils.runOnMainThread(
                () -> callback.onError("Database error: " + e.getMessage()));
          }
        });
  }

  @Override
  public void updateItem(String groupKey, Map<String, Object> updates, OperationCallback callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError("Database not available");
      return;
    }

    ThreadUtils.runInBackground(
        () -> {
          try {
            Group existingGroup = database.groupDao().getGroupByKey(groupKey);
            if (existingGroup == null) {
              ThreadUtils.runOnMainThread(
                  () -> callback.onError("Group not found in local database"));
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
                () -> callback.onError("Database error: " + e.getMessage()));
          }
        });
  }

  @Override
  public void deleteItem(String groupKey, OperationCallback callback) {
    if (database == null) {
      Log.e(TAG, "Database not initialized");
      callback.onError("Database not available");
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
                () -> callback.onError("Database error: " + e.getMessage()));
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
              Log.w(TAG, "Invalid groupType value: " + value);
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
          Log.w(TAG, "Unknown update field: " + key);
          break;
      }
    }
  }
}
