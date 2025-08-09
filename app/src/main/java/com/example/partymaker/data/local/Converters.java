package com.example.partymaker.data.local;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Type converters for Room database. These converters allow Room to store complex types such as
 * Date objects and Maps in the SQLite database by converting them to/from primitive types that
 * SQLite can handle.
 */
public class Converters {

  // JSON serialization instance
  private static final Gson gson = new Gson();

  // Prevent instantiation
  private Converters() {
    // Utility class for Room type converters
  }

  /**
   * Converts a timestamp to a Date object.
   *
   * @param value The timestamp
   * @return The Date object
   */
  @TypeConverter
  public static Date fromTimestamp(Long value) {
    return value == null ? null : new Date(value);
  }

  /**
   * Converts a Date object to a timestamp.
   *
   * @param date The Date object
   * @return The timestamp
   */
  @TypeConverter
  public static Long dateToTimestamp(Date date) {
    return date == null ? null : date.getTime();
  }

  /**
   * Converts a JSON string to a HashMap for Room database storage.
   *
   * @param value The JSON string representation
   * @return The HashMap object, or empty map if value is null
   */
  @TypeConverter
  public static HashMap<String, Object> fromJsonString(String value) {
    if (value == null) {
      return new HashMap<>();
    }
    Type mapType = new TypeToken<HashMap<String, Object>>() {}.getType();
    return gson.fromJson(value, mapType);
  }

  /**
   * Converts a HashMap to a JSON string for Room database storage.
   *
   * @param map The Map object to serialize
   * @return The JSON string representation, or null if map is null
   */
  @TypeConverter
  public static String mapToJsonString(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    return gson.toJson(map);
  }

  /**
   * Converts a JSON string to a Boolean HashMap for Room database storage.
   *
   * @param value The JSON string representation
   * @return The Boolean HashMap object, or empty map if value is null
   */
  @TypeConverter
  public static Map<String, Boolean> fromJsonStringToBoolean(String value) {
    if (value == null) {
      return new HashMap<>();
    }
    Type mapType = new TypeToken<HashMap<String, Boolean>>() {}.getType();
    return gson.fromJson(value, mapType);
  }

  /**
   * Converts a Boolean HashMap to a JSON string for Room database storage.
   *
   * @param map The Boolean Map object to serialize
   * @return The JSON string representation, or null if map is null
   */
  @TypeConverter
  public static String booleanMapToJsonString(Map<String, Boolean> map) {
    if (map == null) {
      return null;
    }
    return gson.toJson(map);
  }
}
