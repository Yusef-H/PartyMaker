package com.example.partymaker.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a user in the PartyMaker application.
 */
public class User {
  /** The user's email address. */
  @SerializedName("email")
  private String email;

  /** The user's display name. */
  @SerializedName("userName")
  private String userName;

  /** The user's last login timestamp. */
  @SerializedName("lastLogin")
  private String lastLogin;

  /**
   * Gets the user's email address.
   * @return the email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the user's email address.
   * @param email the email address
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Gets the user's display name.
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user's display name.
   * @param userName the user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Gets the last login timestamp.
   * @return the last login
   */
  public String getLastLogin() {
    return lastLogin;
  }

  /**
   * Sets the last login timestamp.
   * @param lastLogin the last login
   */
  public void setLastLogin(String lastLogin) {
    this.lastLogin = lastLogin;
  }

  /**
   * Default constructor.
   */
  public User() {}

  /**
   * Constructs a user with email and user name.
   * @param email the email address
   * @param userName the user name
   */
  public User(String email, String userName) {
    this.email = email;
    this.userName = userName;
  }
}
