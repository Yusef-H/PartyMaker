package com.example.partymaker.data.repository;

import android.util.Log;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.model.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Remote data source implementation for Group entities using FirebaseServerClient. Handles all
 * network operations for groups via the PartyMaker backend server.
 */
public class RemoteGroupDataSource implements DataSource<Group, String> {
  private static final String TAG = "RemoteGroupDataSource";

  private final FirebaseServerClient serverClient;

  /** Constructor for RemoteGroupDataSource. */
  public RemoteGroupDataSource() {
    this.serverClient = FirebaseServerClient.getInstance();
  }

  @Override
  public void getItem(String groupKey, DataCallback<Group> callback) {
    if (groupKey == null || groupKey.isEmpty()) {
      Log.e(TAG, "Invalid group key provided");
      callback.onError("Invalid group key");
      return;
    }

    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            Log.d(TAG, "Group retrieved from server: " + groupKey);
            callback.onDataLoaded(group);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error retrieving group from server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  @Override
  public void getAllItems(DataCallback<List<Group>> callback) {
    serverClient.getGroups(
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, Group> groupMap) {
            List<Group> groups = new ArrayList<>(groupMap.values());
            Log.d(TAG, "All groups retrieved from server: " + groups.size());
            callback.onDataLoaded(groups);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error retrieving all groups from server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  @Override
  public void saveItem(String groupKey, Group group, OperationCallback callback) {
    if (groupKey == null || groupKey.isEmpty()) {
      Log.e(TAG, "Invalid group key provided");
      callback.onError("Invalid group key");
      return;
    }

    if (group == null) {
      Log.e(TAG, "Group object is null");
      callback.onError("Invalid group data");
      return;
    }

    // Ensure the group has the correct key
    if (group.getGroupKey().isEmpty()) {
      group.setGroupKey(groupKey);
    }

    serverClient.saveGroup(
        groupKey,
        group,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group saved to server: " + groupKey);
            callback.onComplete();
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error saving group to server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  @Override
  public void updateItem(String groupKey, Map<String, Object> updates, OperationCallback callback) {
    if (groupKey == null || groupKey.isEmpty()) {
      Log.e(TAG, "Invalid group key provided");
      callback.onError("Invalid group key");
      return;
    }

    if (updates == null || updates.isEmpty()) {
      Log.e(TAG, "No updates provided");
      callback.onError("No updates to apply");
      return;
    }

    serverClient.updateGroup(
        groupKey,
        updates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group updated on server: " + groupKey);
            callback.onComplete();
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error updating group on server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  @Override
  public void deleteItem(String groupKey, OperationCallback callback) {
    if (groupKey == null || groupKey.isEmpty()) {
      Log.e(TAG, "Invalid group key provided");
      callback.onError("Invalid group key");
      return;
    }

    serverClient.deleteGroup(
        groupKey,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group deleted from server: " + groupKey);
            callback.onComplete();
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error deleting group from server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  /**
   * Gets groups for a specific user from the server.
   *
   * @param userKey The user key
   * @param callback Callback to receive the groups
   */
  public void getUserGroups(String userKey, DataCallback<List<Group>> callback) {
    if (userKey == null || userKey.isEmpty()) {
      Log.e(TAG, "Invalid user key provided");
      callback.onError("Invalid user key");
      return;
    }

    serverClient.getUserGroups(
        userKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Map<String, Group> groupMap) {
            List<Group> groups = new ArrayList<>(groupMap.values());
            Log.d(TAG, "User groups retrieved from server: " + groups.size());
            callback.onDataLoaded(groups);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error retrieving user groups from server: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }
}
