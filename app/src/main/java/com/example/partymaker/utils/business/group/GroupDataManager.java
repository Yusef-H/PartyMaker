package com.example.partymaker.utils.business.group;

import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.features.groups.creation.CreateGroupActivity;

/**
 * Builder pattern implementation for Group creation. Provides a fluent interface for constructing
 * Group objects with proper validation and type safety.
 */
public class GroupDataManager {
  private final Group group = new Group();

  /**
   * Sets the group name with validation.
   *
   * @param name The group name (cannot be null or empty)
   * @return This GroupDataManager instance for method chaining
   * @throws IllegalArgumentException if name is null or empty
   */
  public GroupDataManager setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Group name cannot be null or empty");
    }
    group.setGroupName(name.trim());
    return this;
  }

  /**
   * Sets the admin key with validation.
   *
   * @param adminKey The admin key (cannot be null or empty)
   * @return This GroupDataManager instance for method chaining
   * @throws IllegalArgumentException if adminKey is null or empty
   */
  public GroupDataManager setAdmin(String adminKey) {
    if (adminKey == null || adminKey.trim().isEmpty()) {
      throw new IllegalArgumentException("Admin key cannot be null or empty");
    }
    group.setAdminKey(adminKey);
    return this;
  }

  /**
   * Sets the group type and automatically configures canAdd property.
   *
   * @param type The group type (must be a valid GroupType value)
   * @return This GroupDataManager instance for method chaining
   */
  public GroupDataManager setType(int type) {
    group.setGroupType(type);
    group.setCanAdd(type == CreateGroupActivity.GroupType.PUBLIC);
    return this;
  }

  /**
   * Sets the creation timestamp.
   *
   * @param timestamp The creation timestamp (cannot be null or empty)
   * @return This GroupDataManager instance for method chaining
   * @throws IllegalArgumentException if timestamp is null or empty
   */
  public GroupDataManager setCreationTime(String timestamp) {
    if (timestamp == null || timestamp.trim().isEmpty()) {
      throw new IllegalArgumentException("Creation timestamp cannot be null or empty");
    }
    group.setCreatedAt(timestamp);
    return this;
  }

  /**
   * Sets the group price with validation.
   *
   * @param price The group price (cannot be null, defaults to "0" if empty)
   * @return This GroupDataManager instance for method chaining
   */
  public GroupDataManager setPrice(String price) {
    if (price == null) {
      price = "0";
    } else if (price.trim().isEmpty()) {
      price = "0";
    }
    group.setGroupPrice(price.trim());
    return this;
  }

  /**
   * Sets the group location.
   *
   * @param location The group location (can be null or empty)
   * @return This GroupDataManager instance for method chaining
   */
  public GroupDataManager setLocation(String location) {
    group.setGroupLocation(location != null ? location.trim() : null);
    return this;
  }

  /**
   * Sets the group date and time using a GroupDateTimeManager.
   *
   * @param dateTime The date time manager (cannot be null)
   * @return This GroupDataManager instance for method chaining
   * @throws IllegalArgumentException if dateTime is null
   */
  public GroupDataManager setDateTime(GroupDateTimeManager dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime manager cannot be null");
    }
    group.setGroupDays(dateTime.getDay());
    group.setGroupMonths(dateTime.getMonth());
    group.setGroupYears(dateTime.getYear());
    group.setGroupHours(dateTime.getTime());
    return this;
  }

  /**
   * Builds and returns the configured Group object.
   *
   * @return The configured Group instance
   * @throws IllegalStateException if required fields are not set
   */
  public Group build() {
    validateRequiredFields();
    return group;
  }

  /**
   * Validates that all required fields are set before building.
   *
   * @throws IllegalStateException if any required field is missing
   */
  private void validateRequiredFields() {
    if (group.getGroupName() == null || group.getGroupName().trim().isEmpty()) {
      throw new IllegalStateException("Group name is required");
    }
    if (group.getAdminKey() == null || group.getAdminKey().trim().isEmpty()) {
      throw new IllegalStateException("Admin key is required");
    }
    if (group.getCreatedAt() == null || group.getCreatedAt().trim().isEmpty()) {
      throw new IllegalStateException("Creation timestamp is required");
    }
  }
}
