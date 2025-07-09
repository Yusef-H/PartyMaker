package com.example.partymaker.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
  @SerializedName("email")
  private String Email;

  @SerializedName("userName")
  private String UserName;

  @SerializedName("lastLogin")
  private String lastLogin;

  public String getEmail() {
    return Email;
  }

  public void setEmail(String email) {
    Email = email;
  }

  public String getUserName() {
    return UserName;
  }

  @SuppressWarnings("unused")
  public void setUserName(String userName) {
    UserName = userName;
  }

  public String getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(String lastLogin) {
    this.lastLogin = lastLogin;
  }

  public User() {}

  public User(String Email, String UserName) {
    this.Email = Email;
    this.UserName = UserName;
  }
}
