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

/** Simplified ViewModel for group-related activities */
public class GroupViewModel extends BaseViewModel {

  private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();
  private final MutableLiveData<List<Group>> publicGroups =
      new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<List<User>> groupMembers = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Boolean> isGroupCreated = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isGroupJoined = new MutableLiveData<>(false);
  private final MutableLiveData<String> groupKey = new MutableLiveData<>();

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

  public void addPublicGroup(Group group) {
    List<Group> groups = publicGroups.getValue();
    if (groups == null) {
      groups = new ArrayList<>();
    }
    groups.add(group);
    publicGroups.setValue(groups);
  }

  public void addGroupMember(User user) {
    List<User> members = groupMembers.getValue();
    if (members == null) {
      members = new ArrayList<>();
    }
    members.add(user);
    groupMembers.setValue(members);
  }

  public void setGroupCreated(boolean created) {
    isGroupCreated.setValue(created);
  }

  public void setGroupJoined(boolean joined) {
    isGroupJoined.setValue(joined);
  }

  public void resetStatus() {
    isGroupCreated.setValue(false);
    isGroupJoined.setValue(false);
  }
}
