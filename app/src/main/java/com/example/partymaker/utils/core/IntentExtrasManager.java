package com.example.partymaker.utils.core;

import static com.example.partymaker.utils.core.AppConstants.Extras.ADMIN_KEY;
import static com.example.partymaker.utils.core.AppConstants.Extras.CAN_ADD;
import static com.example.partymaker.utils.core.AppConstants.Extras.COMING_KEYS;
import static com.example.partymaker.utils.core.AppConstants.Extras.CREATED_AT;
import static com.example.partymaker.utils.core.AppConstants.Extras.DEFAULT_KEY;
import static com.example.partymaker.utils.core.AppConstants.Extras.FRIEND_KEYS;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_DAYS;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_HOURS;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_KEY;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_LOCATION;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_MONTHS;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_NAME;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_PRICE;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_TYPE;
import static com.example.partymaker.utils.core.AppConstants.Extras.GROUP_YEARS;
import static com.example.partymaker.utils.core.AppConstants.Extras.MESSAGE_KEYS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import java.util.HashMap;

/**
 * Utility class for working with Intents, ExtrasMetadata, and UI helpers in PartyMaker. Provides
 * static methods for intent manipulation, drag-and-drop functionality, and various UI utilities.
 * This class cannot be instantiated.
 */
public final class IntentExtrasManager {

  // Constants for drag and drop functionality
  private static final String PROPERTY_FILE_NAME = "local.properties";
  private static final String EMPTY_STRING = "";

  // Touch handling variables
  private static float downX, downY, dX, dY;
  private static int touchSlop;

  /**
   * Packs all fields from ExtrasMetadata into the given Intent.
   *
   * @param intent the Intent to add extras to (cannot be null)
   * @param extras the ExtrasMetadata object (cannot be null)
   * @throws IllegalArgumentException if intent or extras is null
   */
  public static void addExtrasToIntent(Intent intent, ExtrasMetadata extras) {
    validateNotNull(intent, "Intent cannot be null");
    validateNotNull(extras, "ExtrasMetadata cannot be null");
    intent.putExtra(GROUP_NAME, extras.getGroupName());
    intent.putExtra(GROUP_KEY, extras.getGroupKey());
    intent.putExtra(GROUP_DAYS, extras.getGroupDays());
    intent.putExtra(GROUP_MONTHS, extras.getGroupMonths());
    intent.putExtra(GROUP_YEARS, extras.getGroupYears());
    intent.putExtra(GROUP_HOURS, extras.getGroupHours());
    intent.putExtra(GROUP_LOCATION, extras.getGroupLocation());
    intent.putExtra(ADMIN_KEY, extras.getAdminKey());
    intent.putExtra(CREATED_AT, extras.getCreatedAt());
    intent.putExtra(GROUP_PRICE, extras.getGroupPrice());
    intent.putExtra(GROUP_TYPE, extras.getGroupType());
    intent.putExtra(CAN_ADD, extras.isCanAdd());
    intent.putExtra(FRIEND_KEYS, extras.getFriendKeys());
    intent.putExtra(COMING_KEYS, extras.getComingKeys());
    intent.putExtra(MESSAGE_KEYS, extras.getMessageKeys());
  }

  /**
   * Extracts ExtrasMetadata from an Intent.
   *
   * @param intent the Intent containing extras (cannot be null)
   * @return the extracted ExtrasMetadata, or null if extras bundle is missing
   * @throws IllegalArgumentException if intent is null
   */
  public static ExtrasMetadata getExtrasMetadataFromIntent(Intent intent) {
    validateNotNull(intent, "Intent cannot be null");
    Bundle extras = intent.getExtras();
    if (extras == null) {
      return null;
    }

    return new ExtrasMetadata(
        extras.getString(GROUP_NAME, DEFAULT_KEY),
        extras.getString(GROUP_KEY, DEFAULT_KEY),
        extras.getString(GROUP_DAYS, DEFAULT_KEY),
        extras.getString(GROUP_MONTHS, DEFAULT_KEY),
        extras.getString(GROUP_YEARS, DEFAULT_KEY),
        extras.getString(GROUP_HOURS, DEFAULT_KEY),
        extras.getString(GROUP_LOCATION, DEFAULT_KEY),
        extras.getString(ADMIN_KEY, DEFAULT_KEY),
        extras.getString(CREATED_AT, DEFAULT_KEY),
        extras.getString(GROUP_PRICE, "0"),
        extras.getInt(GROUP_TYPE, 0),
        extras.getBoolean(CAN_ADD, false),
        getHashMapExtra(extras, FRIEND_KEYS),
        getHashMapExtra(extras, COMING_KEYS),
        getHashMapExtra(extras, MESSAGE_KEYS));
  }

  /**
   * Handles drag-and-drop for a floating chat button.
   *
   * @param view the button view (cannot be null)
   * @param event the MotionEvent (cannot be null)
   * @return true if handled, false otherwise
   * @throws IllegalArgumentException if view or event is null
   */
  @SuppressLint("ClickableViewAccessibility")
  public static boolean dragChatButtonOnTouch(View view, MotionEvent event) {
    validateNotNull(view, "View cannot be null");
    validateNotNull(event, "MotionEvent cannot be null");

    initializeTouchSlopIfNeeded(view);

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        downX = event.getRawX();
        downY = event.getRawY();
        dX = view.getX() - downX;
        dY = view.getY() - downY;
        return true;

      case MotionEvent.ACTION_MOVE:
        float newX = event.getRawX() + dX;
        float newY = event.getRawY() + dY;
        View parent = (View) view.getParent();
        // clamp inside parent bounds
        newX = Math.max(0, Math.min(newX, parent.getWidth() - view.getWidth()));
        newY = Math.max(0, Math.min(newY, parent.getHeight() - view.getHeight()));
        view.setX(newX);
        view.setY(newY);
        return true;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        float upX = event.getRawX();
        float upY = event.getRawY();
        // if finger didn’t move more than slop → it’s a tap
        if (Math.hypot(upX - downX, upY - downY) < touchSlop) {
          view.performClick();
        }
        return true;

      default:
        return false;
    }
  }

  /**
   * Reads an API key from local.properties in assets.
   *
   * @param context the context (cannot be null)
   * @param key the property key (cannot be null or empty)
   * @return the API key value, or empty string if not found or error occurs
   * @throws IllegalArgumentException if context is null or key is null/empty
   */
  public static String getApiKey(Context context, String key) {
    validateNotNull(context, "Context cannot be null");
    validateNotNullOrEmpty(key, "Property key cannot be null or empty");

    try (java.io.InputStream inputStream = context.getAssets().open(PROPERTY_FILE_NAME)) {
      java.util.Properties properties = new java.util.Properties();
      properties.load(inputStream);
      String value = properties.getProperty(key);
      return value != null ? value : EMPTY_STRING;
    } catch (java.io.IOException e) {
      android.util.Log.w("IntentExtrasManager", "Error reading API key: " + key, e);
      return EMPTY_STRING;
    }
  }

  /**
   * Extracts a HashMap extra from a Bundle.
   *
   * @param extras the Bundle (cannot be null)
   * @param key the key for the HashMap (cannot be null or empty)
   * @return the HashMap, or null if not found
   * @throws IllegalArgumentException if extras is null or key is null/empty
   */
  @SuppressWarnings("unchecked")
  public static HashMap<String, Object> getHashMapExtra(Bundle extras, String key) {
    validateNotNull(extras, "Bundle cannot be null");
    validateNotNullOrEmpty(key, "Key cannot be null or empty");
    return (HashMap<String, Object>) extras.getSerializable(key);
  }

  // UI utility methods

  /**
   * Sets visibility to VISIBLE for all given views.
   *
   * @param views the views to show (cannot contain null elements)
   * @throws IllegalArgumentException if views array is null or contains null elements
   */
  public static void showViews(View... views) {
    validateNotNull(views, "Views array cannot be null");
    for (View view : views) {
      validateNotNull(view, "View in array cannot be null");
      view.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Sets visibility to INVISIBLE for all given views.
   *
   * @param views the views to hide (cannot contain null elements)
   * @throws IllegalArgumentException if views array is null or contains null elements
   */
  public static void hideViews(View... views) {
    validateNotNull(views, "Views array cannot be null");
    for (View view : views) {
      validateNotNull(view, "View in array cannot be null");
      view.setVisibility(View.INVISIBLE);
    }
  }

  // Private helper methods

  /**
   * Initializes touch slop value if not already set.
   *
   * @param view the view to get context from
   */
  private static void initializeTouchSlopIfNeeded(View view) {
    if (touchSlop == 0) {
      touchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
    }
  }

  /**
   * Validates that an object is not null.
   *
   * @param obj the object to validate
   * @param message the error message if validation fails
   * @throws IllegalArgumentException if obj is null
   */
  private static void validateNotNull(Object obj, String message) {
    if (obj == null) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Validates that a string is not null or empty.
   *
   * @param str the string to validate
   * @param message the error message if validation fails
   * @throws IllegalArgumentException if str is null or empty
   */
  private static void validateNotNullOrEmpty(String str, String message) {
    if (str == null || str.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
  }

  // Private constructor to prevent instantiation
  private IntentExtrasManager() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
