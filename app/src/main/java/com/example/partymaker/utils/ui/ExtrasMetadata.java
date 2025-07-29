package com.example.partymaker.utils.ui;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Holds all the data that needs to be passed between activities in PartyMaker. Immutable,
 * Serializable data holder for group/event metadata.
 */
public class ExtrasMetadata implements Serializable {
  private final String groupName;
  private final String groupKey;
  private final String groupDays;
  private final String groupMonths;
  private final String groupYears;
  private final String groupHours;
  private final String groupLocation;
  private final String adminKey;
  private final String createdAt;
  private final String groupPrice;
  private final int groupType;
  private final boolean canAdd;
  private final HashMap<String, Object> friendKeys;
  private final HashMap<String, Object> comingKeys;
  private final HashMap<String, Object> messageKeys;

  public ExtrasMetadata(
      String groupName,
      String groupKey,
      String groupDays,
      String groupMonths,
      String groupYears,
      String groupHours,
      String groupLocation,
      String adminKey,
      String createdAt,
      String groupPrice,
      int groupType,
      boolean canAdd,
      HashMap<String, Object> friendKeys,
      HashMap<String, Object> comingKeys,
      HashMap<String, Object> messageKeys) {
    this.groupName = groupName;
    this.groupKey = groupKey;
    this.groupDays = groupDays;
    this.groupMonths = groupMonths;
    this.groupYears = groupYears;
    this.groupHours = groupHours;
    this.groupLocation = groupLocation;
    this.adminKey = adminKey;
    this.createdAt = createdAt;
    this.groupPrice = groupPrice;
    this.groupType = groupType;
    this.canAdd = canAdd;
    this.friendKeys = friendKeys;
    this.comingKeys = comingKeys;
    this.messageKeys = messageKeys;
  }

  /**
   * Returns the name of the group.
   *
   * @return The name of the group.
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Returns the unique key of the group.
   *
   * @return The group key.
   */
  public String getGroupKey() {
    return groupKey;
  }

  /**
   * Returns the days of the group.
   *
   * @return The days of the group.
   */
  public String getGroupDays() {
    return groupDays;
  }

  /**
   * Returns the months of the group.
   *
   * @return The months of the group.
   */
  public String getGroupMonths() {
    return groupMonths;
  }

  /**
   * Returns the years of the group.
   *
   * @return The years of the group.
   */
  public String getGroupYears() {
    return groupYears;
  }

  /**
   * Returns the hours of the group.
   *
   * @return The hours of the group.
   */
  public String getGroupHours() {
    return groupHours;
  }

  /**
   * Returns the location of the group.
   *
   * @return The location of the group.
   */
  public String getGroupLocation() {
    return groupLocation;
  }

  /**
   * Returns the admin key of the group.
   *
   * @return The admin key.
   */
  public String getAdminKey() {
    return adminKey;
  }

  /**
   * Returns the creation timestamp of the group.
   *
   * @return The creation timestamp.
   */
  public String getCreatedAt() {
    return createdAt;
  }

  /**
   * Returns the price of the group.
   *
   * @return The price of the group.
   */
  public String getGroupPrice() {
    return groupPrice;
  }

  /**
   * Returns the type of the group.
   *
   * @return The group type.
   */
  public int getGroupType() {
    return groupType;
  }

  /**
   * Checks if the user can add new members to the group.
   *
   * @return True if the user can add, false otherwise.
   */
  public boolean isCanAdd() {
    return canAdd;
  }

  /**
   * Returns the keys of friends in the group.
   *
   * @return A map of friend keys.
   */
  public HashMap<String, Object> getFriendKeys() {
    return friendKeys;
  }

  /**
   * Returns the keys of people who are coming to the group.
   *
   * @return A map of coming keys.
   */
  public HashMap<String, Object> getComingKeys() {
    return comingKeys;
  }

  /**
   * Returns the keys of messages in the group.
   *
   * @return A map of message keys.
   */
  public HashMap<String, Object> getMessageKeys() {
    return messageKeys;
  }
}
