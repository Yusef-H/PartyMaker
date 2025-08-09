package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for group-related activities providing reactive data management
 * and state handling for group operations.
 * 
 * <p>Features:
 * <ul>
 *   <li>Group creation and joining status tracking
 *   <li>Public groups list management
 *   <li>Group member management
 *   <li>Current group state management
 * </ul>
 */
public class GroupViewModel extends BaseViewModel {

  private static final String TAG = "GroupViewModel";
  
  // LiveData for group state management
  private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();
  private final MutableLiveData<List<Group>> publicGroups = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<List<User>> groupMembers = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Boolean> isGroupCreated = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isGroupJoined = new MutableLiveData<>(false);
  private final MutableLiveData<String> groupKey = new MutableLiveData<>();

  /**
   * Constructor for GroupViewModel.
   *
   * @param application The application context
   */
  public GroupViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<Group> getCurrentGroup() {
    return currentGroup;
  }

  public LiveData<List<Group>> getPublicGroups() {
    return publicGroups;
  }

  public LiveData<List<User>> getGroupMembers() {
    return groupMembers;
  }

  public LiveData<Boolean> getIsGroupCreated() {
    return isGroupCreated;
  }

  public LiveData<Boolean> getIsGroupJoined() {
    return isGroupJoined;
  }

  public LiveData<String> getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(String key) {
    groupKey.setValue(key);
  }

  public void setCurrentGroup(Group group) {
    currentGroup.setValue(group);
  }

  /**
   * Adds a group to the public groups list.
   *
   * @param group The group to add
   */
  public void addPublicGroup(Group group) {
    if (group == null) {
      return;
    }
    
    List<Group> groups = publicGroups.getValue();
    if (groups == null) {
      groups = new ArrayList<>();
    }
    
    // Avoid duplicates
    if (!groups.contains(group)) {
      groups.add(group);
      publicGroups.setValue(groups);
    }
  }

  /**
   * Adds a user to the group members list.
   *
   * @param user The user to add
   */
  public void addGroupMember(User user) {
    if (user == null) {
      return;
    }
    
    List<User> members = groupMembers.getValue();
    if (members == null) {
      members = new ArrayList<>();
    }
    
    // Avoid duplicates based on user key
    boolean userExists = members.stream()
        .anyMatch(existingUser -> existingUser.getUserKey() != null 
            && existingUser.getUserKey().equals(user.getUserKey()));
    
    if (!userExists) {
      members.add(user);
      groupMembers.setValue(members);
    }
  }

  public void setGroupCreated(boolean created) {
    isGroupCreated.setValue(created);
  }

  public void setGroupJoined(boolean joined) {
    isGroupJoined.setValue(joined);
  }

  /**
   * Resets all group operation status flags to their default state.
   */
  public void resetStatus() {
    isGroupCreated.setValue(false);
    isGroupJoined.setValue(false);
  }
  
  /**
   * Sets the public groups list with null safety.
   *
   * @param groups The list of groups to set
   */
  public void setPublicGroups(List<Group> groups) {
    publicGroups.setValue(groups != null ? groups : new ArrayList<>());
  }
  
  /**
   * Sets the group members list with null safety.
   *
   * @param members The list of members to set
   */
  public void setGroupMembers(List<User> members) {
    groupMembers.setValue(members != null ? members : new ArrayList<>());
  }
  
  /**
   * Clears all group data and resets to initial state.
   */
  public void clearAllData() {
    currentGroup.setValue(null);
    publicGroups.setValue(new ArrayList<>());
    groupMembers.setValue(new ArrayList<>());
    groupKey.setValue(null);
    resetStatus();
    clearMessages();
  }
  
  @Override
  protected void onCleared() {
    super.onCleared();
    clearAllData();
  }
}
