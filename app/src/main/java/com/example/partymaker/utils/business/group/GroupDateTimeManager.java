package com.example.partymaker.utils.business.group;

public class GroupDateTimeManager {
  private final String day;
  private final String month;
  private final String year;
  private final String time;

  public GroupDateTimeManager(String day, String month, String year, String time) {
    this.day = day;
    this.month = month;
    this.year = year;
    this.time = time;
  }

  // Getters
  public String getDay() {
    return day;
  }

  public String getMonth() {
    return month;
  }

  public String getYear() {
    return year;
  }

  public String getTime() {
    return time;
  }
}
