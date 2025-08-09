package com.example.partymaker.utils.core;

/**
 * Constants used throughout the PartyMaker application. Includes keys for SharedPreferences, Intent
 * extras, network configuration, and default values. All constants are organized into logical
 * groups using nested classes for better maintainability.
 */
public final class AppConstants {

  // Private constructor to prevent instantiation
  private AppConstants() {
    throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
  }

  // Network Configuration
  public static final class Network {
    public static final String DEFAULT_SERVER_URL = "https://partymaker.onrender.com";
    public static final String API_BASE_PATH = "/api/firebase/";
    public static final int DEFAULT_TIMEOUT_MS = 10_000; // 10 seconds

    private Network() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }

  // SharedPreferences keys
  public static final class Preferences {
    public static final String PREFS_NAME = "PREFS_NAME";
    public static final String IS_CHECKED = "isChecked";
    public static final String SERVER_URL = "server_url";

    private Preferences() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }

  // Intent Extras
  public static final class Extras {
    public static final String GROUP_NAME = "GroupName";
    public static final String GROUP_KEY = "groupKey";
    public static final String GROUP_DAYS = "groupDays";
    public static final String GROUP_MONTHS = "groupMonths";
    public static final String GROUP_YEARS = "groupYears";
    public static final String GROUP_HOURS = "groupHours";
    public static final String GROUP_LOCATION = "groupLocation";
    public static final String ADMIN_KEY = "adminKey";
    public static final String CREATED_AT = "createdAt";
    public static final String GROUP_PRICE = "GroupPrice";
    public static final String GROUP_TYPE = "GroupType";
    public static final String CAN_ADD = "CanAdd";
    public static final String FRIEND_KEYS = "FriendKeys";
    public static final String COMING_KEYS = "ComingKeys";
    public static final String MESSAGE_KEYS = "MessageKeys";
    public static final String DEFAULT_KEY = "defaultKey";

    private Extras() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }

  // Security Configuration
  public static final class Security {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final long LOCKOUT_DURATION_MS = 300_000L; // 5 minutes

    private Security() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }

  // UI and Animation Constants
  public static final class UI {
    // Delays and timeouts for UI operations
    public static final int NETWORK_STATUS_UPDATE_DELAY_MS = 500;
    public static final int SPLASH_DELAY_MS = 3_000;
    public static final int DOT_ANIMATION_DELAY_MS = 500;
    public static final int DOT_ANIMATION_STAGGER_MS = 200;
    public static final int DOT_ANIMATION_DURATION_MS = 600;
    public static final int DOT_ANIMATION_LOOP_MS = 1_500;
    public static final int RESUME_NETWORK_CHECK_DELAY_MS = 1_000;

    // Animation values
    public static final float DOT_SCALE_FACTOR = 1.3f;
    public static final float DOT_ALPHA_MIN = 0.5f;
    public static final float DOT_ALPHA_MAX = 1.0f;

    // Message display intervals
    public static final int MESSAGE_PADDING_SAME_USER = 2;
    public static final int MESSAGE_PADDING_DIFFERENT_USER = 8;

    private UI() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }

  // Validation Constants
  public static final class Validation {
    public static final int MIN_USERNAME_LENGTH = 2;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int DOWNLOAD_BUFFER_SIZE = 1_024;
    public static final int TIME_SUBSTRING_START = 11;
    public static final int TIME_SUBSTRING_END = 16;

    private Validation() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }

  // Firebase Storage Paths
  public static final class FirebasePaths {
    public static final String PROFILE_IMAGE_PRIMARY = "UsersImageProfile/Users/";
    public static final String PROFILE_IMAGE_SECONDARY = "Users/";
    public static final String PROFILE_IMAGE_TEMPLATE = "profile_%s.jpg";

    private FirebasePaths() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }

  // Database Field Names
  public static final class DatabaseFields {
    public static final String USERNAME = "username";
    public static final String USER_NAME = "userName";
    public static final String ADMIN_KEY = "adminKey";
    public static final String FRIEND_KEYS = "friendKeys";
    public static final String PROFILE_IMAGE_URL = "profileImageUrl";

    private DatabaseFields() {
      throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
  }
}
