package com.example.partymaker.utils.business.group;

import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.features.groups.managment.CreateGroupActivity;

// Builder pattern for Group creation
public class GroupDataManager {
  private final Group group = new Group();

  public GroupDataManager setName(String name) {
    group.setGroupName(name);
    return this;
  }

  public GroupDataManager setAdmin(String adminKey) {
    group.setAdminKey(adminKey);
    return this;
  }

  public GroupDataManager setType(int type) {
    group.setGroupType(type);
    group.setCanAdd(type == CreateGroupActivity.GroupType.PUBLIC);
    return this;
  }

  public GroupDataManager setCreationTime(String timestamp) {
    group.setCreatedAt(timestamp);
    return this;
  }

  public GroupDataManager setPrice(String price) {
    group.setGroupPrice(price);
    return this;
  }

  public GroupDataManager setLocation(String location) {
    group.setGroupLocation(location);
    return this;
  }

  public GroupDataManager setDateTime(GroupDateTimeManager dateTime) {
    group.setGroupDays(dateTime.getDay());
    group.setGroupMonths(dateTime.getMonth());
    group.setGroupYears(dateTime.getYear());
    group.setGroupHours(dateTime.getTime());
    return this;
  }

  public Group build() {
    return group;
  }
}
