package com.example.partymaker.data.local;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Type converters for Room database. These converters allow Room to store complex types in the
 * database.
 */
public class Converters {

  private static final Gson gson = new Gson();

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
   * Converts a JSON string to a Map.
   *
   * @param value The JSON string
   * @return The Map object
   */
  @TypeConverter
  public static HashMap<String, Object> fromString(String value) {
    if (value == null) {
      return new HashMap<>();
    }
    Type mapType = new TypeToken<HashMap<String, Object>>() {}.getType();
    return gson.fromJson(value, mapType);
  }

  /**
   * Converts a Map to a JSON string.
   *
   * @param map The Map object
   * @return The JSON string
   */
  @TypeConverter
  public static String mapToString(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    return gson.toJson(map);
  }

  /**
   * Converts a JSON string to a Map<String, Boolean>.
   *
   * @param value The JSON string
   * @return The Map object
   */
  @TypeConverter
  public static Map<String, Boolean> fromStringToBoolean(String value) {
    if (value == null) {
      return new HashMap<>();
    }
    Type mapType = new TypeToken<HashMap<String, Boolean>>() {}.getType();
    return gson.fromJson(value, mapType);
  }

  /**
   * Converts a Map<String, Boolean> to a JSON string.
   *
   * @param map The Map object
   * @return The JSON string
   */
  @TypeConverter
  public static String booleanMapToString(Map<String, Boolean> map) {
    if (map == null) {
      return null;
    }
    return gson.toJson(map);
  }
}
