package com.example.partymaker.data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a user in the application. Contains user information and provides
 * methods for accessing and modifying that information.
 */
public class User implements Serializable {
  private String userKey;
  private String username;
  private String email;
  private String profileImageUrl;
  private Map<String, Boolean> friendKeys;
  private Map<String, Boolean> groupKeys;
  private long lastLoginTimestamp;
  private String fcmToken;
  private Map<String, Object> additionalInfo;

  /** Default constructor required for Firebase */
  public User() {
    this.friendKeys = new HashMap<>();
    this.groupKeys = new HashMap<>();
    this.additionalInfo = new HashMap<>();
  }

  /**
   * Constructor with minimal required fields
   *
   * @param username The user's display name
   * @param email The user's email address
   */
  public User(String username, String email) {
    this();
    this.username = username;
    this.email = email;
  }

  /**
   * Full constructor
   *
   * @param userKey The user's key (email with dots replaced by spaces)
   * @param username The user's display name
   * @param email The user's email address
   * @param profileImageUrl The URL of the user's profile image
   */
  public User(String userKey, String username, String email, String profileImageUrl) {
    this(username, email);
    this.userKey = userKey;
    this.profileImageUrl = profileImageUrl;
  }

  /**
   * Gets the user key (email with dots replaced by spaces)
   *
   * @return The user key
   */
  public String getUserKey() {
    return userKey;
  }

  /**
   * Sets the user key
   *
   * @param userKey The user key to set
   */
  public void setUserKey(String userKey) {
    this.userKey = userKey;
  }

  /**
   * Gets the username
   *
   * @return The username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username
   *
   * @param username The username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Gets the email address
   *
   * @return The email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email address
   *
   * @param email The email address to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Gets the profile image URL
   *
   * @return The profile image URL
   */
  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  /**
   * Sets the profile image URL
   *
   * @param profileImageUrl The profile image URL to set
   */
  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  /**
   * Gets the map of friend keys
   *
   * @return Map of friend keys (key = user key, value = true)
   */
  public Map<String, Boolean> getFriendKeys() {
    return friendKeys;
  }

  /**
   * Sets the map of friend keys
   *
   * @param friendKeys Map of friend keys to set
   */
  public void setFriendKeys(Map<String, Boolean> friendKeys) {
    this.friendKeys = friendKeys != null ? friendKeys : new HashMap<>();
  }

  /**
   * Gets the map of group keys
   *
   * @return Map of group keys (key = group key, value = true)
   */
  public Map<String, Boolean> getGroupKeys() {
    return groupKeys;
  }

  /**
   * Sets the map of group keys
   *
   * @param groupKeys Map of group keys to set
   */
  public void setGroupKeys(Map<String, Boolean> groupKeys) {
    this.groupKeys = groupKeys != null ? groupKeys : new HashMap<>();
  }

  /**
   * Gets the last login timestamp
   *
   * @return The last login timestamp
   */
  public long getLastLoginTimestamp() {
    return lastLoginTimestamp;
  }

  /**
   * Sets the last login timestamp
   *
   * @param lastLoginTimestamp The last login timestamp to set
   */
  public void setLastLoginTimestamp(long lastLoginTimestamp) {
    this.lastLoginTimestamp = lastLoginTimestamp;
  }

  /**
   * Gets the FCM token for push notifications
   *
   * @return The FCM token
   */
  public String getFcmToken() {
    return fcmToken;
  }

  /**
   * Sets the FCM token for push notifications
   *
   * @param fcmToken The FCM token to set
   */
  public void setFcmToken(String fcmToken) {
    this.fcmToken = fcmToken;
  }

  /**
   * Gets additional information about the user
   *
   * @return Map of additional information
   */
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  /**
   * Sets additional information about the user
   *
   * @param additionalInfo Map of additional information to set
   */
  public void setAdditionalInfo(Map<String, Object> additionalInfo) {
    this.additionalInfo = additionalInfo != null ? additionalInfo : new HashMap<>();
  }

  /**
   * Adds a friend to the user's friend list
   *
   * @param friendKey The friend's user key
   */
  public void addFriend(String friendKey) {
    if (friendKey != null && !friendKey.isEmpty()) {
      if (friendKeys == null) {
        friendKeys = new HashMap<>();
      }
      friendKeys.put(friendKey, true);
    }
  }

  /**
   * Removes a friend from the user's friend list
   *
   * @param friendKey The friend's user key
   */
  public void removeFriend(String friendKey) {
    if (friendKey != null && !friendKey.isEmpty() && friendKeys != null) {
      friendKeys.remove(friendKey);
    }
  }

  /**
   * Adds a group to the user's group list
   *
   * @param groupKey The group key
   */
  public void addGroup(String groupKey) {
    if (groupKey != null && !groupKey.isEmpty()) {
      if (groupKeys == null) {
        groupKeys = new HashMap<>();
      }
      groupKeys.put(groupKey, true);
    }
  }

  /**
   * Removes a group from the user's group list
   *
   * @param groupKey The group key
   */
  public void removeGroup(String groupKey) {
    if (groupKey != null && !groupKey.isEmpty() && groupKeys != null) {
      groupKeys.remove(groupKey);
    }
  }

  /**
   * Checks if the user has a specific friend
   *
   * @param friendKey The friend's user key
   * @return true if the user has the friend, false otherwise
   */
  public boolean hasFriend(String friendKey) {
    return friendKey != null
        && !friendKey.isEmpty()
        && friendKeys != null
        && friendKeys.containsKey(friendKey);
  }

  /**
   * Checks if the user is in a specific group
   *
   * @param groupKey The group key
   * @return true if the user is in the group, false otherwise
   */
  public boolean isInGroup(String groupKey) {
    return groupKey != null
        && !groupKey.isEmpty()
        && groupKeys != null
        && groupKeys.containsKey(groupKey);
  }

  /** Updates the last login timestamp to the current time */
  public void updateLastLogin() {
    this.lastLoginTimestamp = System.currentTimeMillis();
  }

  /**
   * Converts the user to a map for Firebase storage
   *
   * @return Map representation of the user
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("username", username);
    map.put("email", email);

    if (profileImageUrl != null) {
      map.put("profileImageUrl", profileImageUrl);
    }

    if (friendKeys != null && !friendKeys.isEmpty()) {
      map.put("friendKeys", friendKeys);
    }

    if (groupKeys != null && !groupKeys.isEmpty()) {
      map.put("groupKeys", groupKeys);
    }

    if (lastLoginTimestamp > 0) {
      map.put("lastLoginTimestamp", lastLoginTimestamp);
    }

    if (fcmToken != null) {
      map.put("fcmToken", fcmToken);
    }

    if (additionalInfo != null && !additionalInfo.isEmpty()) {
      map.put("additionalInfo", additionalInfo);
    }

    return map;
  }
}
