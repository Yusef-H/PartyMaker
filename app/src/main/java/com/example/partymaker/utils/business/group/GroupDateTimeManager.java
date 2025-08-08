package com.example.partymaker.utils.business.group;

/**
 * Immutable data class for managing group date and time information. Provides validation for date
 * components to ensure data integrity.
 */
public class GroupDateTimeManager {
  private final String day;
  private final String month;
  private final String year;
  private final String time;

  /**
   * Creates a new GroupDateTimeManager with validation.
   *
   * @param day The day component (cannot be null or empty)
   * @param month The month component (cannot be null or empty)
   * @param year The year component (cannot be null or empty)
   * @param time The time component (cannot be null or empty)
   * @throws IllegalArgumentException if any parameter is null or empty
   */
  public GroupDateTimeManager(String day, String month, String year, String time) {
    this.day = validateAndTrim(day, "Day");
    this.month = validateAndTrim(month, "Month");
    this.year = validateAndTrim(year, "Year");
    this.time = validateAndTrim(time, "Time");
  }

  /**
   * Gets the day component.
   *
   * @return The day value
   */
  public String getDay() {
    return day;
  }

  /**
   * Gets the month component.
   *
   * @return The month value
   */
  public String getMonth() {
    return month;
  }

  /**
   * Gets the year component.
   *
   * @return The year value
   */
  public String getYear() {
    return year;
  }

  /**
   * Gets the time component.
   *
   * @return The time value
   */
  public String getTime() {
    return time;
  }

  /**
   * Validates and trims a string parameter.
   *
   * @param value The value to validate
   * @param fieldName The name of the field for error messages
   * @return The trimmed value
   * @throws IllegalArgumentException if value is null or empty
   */
  private String validateAndTrim(String value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be empty");
    }
    return trimmed;
  }

  /**
   * Returns a string representation of this GroupDateTimeManager.
   *
   * @return A formatted string with all date/time components
   */
  @Override
  public String toString() {
    return String.format(
        "GroupDateTimeManager{day='%s', month='%s', year='%s', time='%s'}", day, month, year, time);
  }
}
