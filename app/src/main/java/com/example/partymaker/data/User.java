package com.example.partymaker.data;

public class User {
  private String Email, UserName;

  /*
   * / private HashMap<String, Object> GroupKey = new HashMap<>();
   *
   * public HashMap<String, Object> getGroupKey() { return GroupKey; }
   *
   * public void setGroupKey(HashMap<String, Object> groupKey) { GroupKey =
   * groupKey; }
   *
   * public void addGroupKey(String GroupKey) { this.GroupKey.put(GroupKey, true);
   * } /
   */

  public String getEmail() {
    return Email;
  }

  public void setEmail(String email) {
    Email = email;
  }

  public String getUserName() {
    return UserName;
  }

  public void setUserName(String userName) {
    UserName = userName;
  }

  public User() {}

  public User(String Email, String UserName) {
    this.Email = Email;
    this.UserName = UserName;
  }
}
