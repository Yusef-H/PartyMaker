package com.example.partymaker.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import com.google.gson.annotations.SerializedName;
import java.util.HashMap;

/** 
 * Represents a group (party) in the PartyMaker application.
 * This class is annotated for Room database storage.
 */
@Entity(tableName = "groups")
public class Group {
  /** The group's display name. */
  @ColumnInfo(name = "group_name")
  private String groupName;
  
  /** The unique key for the group. */
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "groupKey")
  private String groupKey;
  
  /** The location of the group. */
  @ColumnInfo(name = "group_location")
  private String groupLocation;
  
  /** The admin's user key. */
  @ColumnInfo(name = "admin_key")
  private String adminKey;
  
  /** The creation timestamp. */
  @ColumnInfo(name = "created_at")
  private String createdAt;
  
  /** The day(s) of the event. */
  @ColumnInfo(name = "group_days")
  private String groupDays;
  
  /** The month(s) of the event. */
  @ColumnInfo(name = "group_months")
  private String groupMonths;
  
  /** The year(s) of the event. */
  @ColumnInfo(name = "group_years")
  private String groupYears;
  
  /** The hour(s) of the event. */
  @ColumnInfo(name = "group_hours")
  private String groupHours;
  
  /** The price of the event. */
  @ColumnInfo(name = "group_price")
  private String groupPrice;
  
  /** 0 = Public group, 1 = Private group. */
  @ColumnInfo(name = "group_type")
  private int groupType;
  
  /** Whether users can add new members. */
  @ColumnInfo(name = "can_add")
  private boolean canAdd;
  
  /** The description of the group. */
  @ColumnInfo(name = "group_description")
  private String groupDescription;
  
  /** Map of friend user keys. */
  @SerializedName(
      value = "friendKeys",
      alternate = {"FriendKeys"})
  @ColumnInfo(name = "friend_keys")
  private HashMap<String, Object> friendKeys = new HashMap<>();
  
  /** Map of users who are coming. */
  @SerializedName(
      value = "comingKeys",
      alternate = {"ComingKeys"})
  @ColumnInfo(name = "coming_keys")
  private HashMap<String, Object> comingKeys = new HashMap<>();
  
  /** Map of message keys for the group. */
  @SerializedName(
      value = "messageKeys",
      alternate = {"MessageKeys"})
  @ColumnInfo(name = "message_keys")
  private HashMap<String, Object> messageKeys = new HashMap<>();

  public boolean isCanAdd() {
    return canAdd;
  }

  public void setCanAdd(boolean canAdd) {
    this.canAdd = canAdd;
  }

  public HashMap<String, Object> getMessageKeys() {
    return messageKeys;
  }

  public void setMessageKeys(HashMap<String, Object> messageKeys) {
    this.messageKeys = messageKeys;
  }

  public HashMap<String, Object> getFriendKeys() {
    return friendKeys;
  }

  public void setFriendKeys(HashMap<String, Object> friendKeys) {
    this.friendKeys = friendKeys;
  }

  public HashMap<String, Object> getComingKeys() {
    return comingKeys;
  }

  public void setComingKeys(HashMap<String, Object> comingKeys) {
    this.comingKeys = comingKeys;
  }

  /** Default constructor. */
  public Group() {}

  /**
   * Constructs a group with all fields.
   *
   * @param groupName the group name
   * @param groupKey the group key
   * @param groupLocation the location
   * @param adminKey the admin's key
   * @param createdAt the creation timestamp
   * @param groupDays the days
   * @param groupMonths the months
   * @param groupYears the years
   * @param groupHours the hours
   * @param groupType the group type (0=public, 1=private)
   * @param groupPrice the price
   * @param canAdd whether users can add
   * @param friendKeys map of friend keys
   * @param comingKeys map of coming keys
   * @param messageKeys map of message keys
   */
  @Ignore
  public Group(
      String groupName,
      @NonNull String groupKey,
      String groupLocation,
      String adminKey,
      String createdAt,
      String groupDays,
      String groupMonths,
      String groupYears,
      String groupHours,
      int groupType,
      String groupPrice,
      boolean canAdd,
      HashMap<String, Object> friendKeys,
      HashMap<String, Object> comingKeys,
      HashMap<String, Object> messageKeys) {
    this.groupName = groupName;
    this.groupKey = groupKey;
    this.groupLocation = groupLocation;
    this.adminKey = adminKey;
    this.createdAt = createdAt;
    this.groupDays = groupDays;
    this.groupMonths = groupMonths;
    this.groupYears = groupYears;
    this.groupHours = groupHours;
    this.groupPrice = groupPrice;
    this.groupType = groupType;
    this.canAdd = canAdd;
    this.friendKeys = friendKeys;
    this.comingKeys = comingKeys;
    this.messageKeys = messageKeys;
  }

  public String getGroupPrice() {
    return groupPrice;
  }

  public void setGroupPrice(String groupPrice) {
    this.groupPrice = groupPrice;
  }

  public int getGroupType() {
    return groupType;
  }

  public void setGroupType(int groupType) {
    this.groupType = groupType;
  }

  public String getGroupLocation() {
    return groupLocation;
  }

  public void setGroupLocation(String groupLocation) {
    this.groupLocation = groupLocation;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  public String getAdminKey() {
    return adminKey;
  }

  public void setAdminKey(String adminKey) {
    this.adminKey = adminKey;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getGroupDays() {
    return groupDays;
  }

  public void setGroupDays(String groupDays) {
    this.groupDays = groupDays;
  }

  public String getGroupMonths() {
    return groupMonths;
  }

  public void setGroupMonths(String groupMonths) {
    this.groupMonths = groupMonths;
  }

  public String getGroupYears() {
    return groupYears;
  }

  public void setGroupYears(String groupYears) {
    this.groupYears = groupYears;
  }

  public String getGroupHours() {
    return groupHours;
  }

  public void setGroupHours(String groupHours) {
    this.groupHours = groupHours;
  }

  /**
   * Gets the group description
   *
   * @return the group description
   */
  public String getGroupDescription() {
    return groupDescription;
  }

  /**
   * Sets the group description
   *
   * @param groupDescription the group description
   */
  public void setGroupDescription(String groupDescription) {
    this.groupDescription = groupDescription;
  }
}
