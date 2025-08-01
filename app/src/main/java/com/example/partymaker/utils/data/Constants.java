package com.example.partymaker.utils.data;

/**
 * Constants used throughout the PartyMaker application. Includes keys for SharedPreferences, Intent
 * extras, network configuration, and default values.
 */
public final class Constants {

  // Private constructor to prevent instantiation
  private Constants() {
    throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
  }

  // Network Configuration
  public static final class Network {
    public static final String DEFAULT_SERVER_URL = "https://partymaker.onrender.com";
    public static final String LOCAL_SERVER_URL = "http://10.0.2.2:8080";
    public static final String API_BASE_PATH = "/api/firebase/";

    public static final int DEFAULT_TIMEOUT_MS = 10000;
    public static final int LONG_TIMEOUT_MS = 15000;
    public static final int SHORT_TIMEOUT_MS = 5000;

    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_DELAY_MS = 1000L;

    private Network() {}
  }

  // SharedPreferences keys
  public static final class Preferences {
    public static final String PREFS_NAME = "PREFS_NAME";
    public static final String IS_CHECKED = "isChecked";
    public static final String SERVER_URL = "server_url";

    private Preferences() {}
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

    private Extras() {}
  }

  // Database Configuration
  public static final class Database {
    public static final String DATABASE_NAME = "partymaker_database";
    public static final int DATABASE_VERSION = 1;
    public static final long CACHE_EXPIRY_TIME_MS = 300000L; // 5 minutes

    private Database() {}
  }

  // Security Configuration
  public static final class Security {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final long LOCKOUT_DURATION_MS = 300000L; // 5 minutes

    private Security() {}
  }

  // UI Configuration
  public static final class UI {
    public static final int DEFAULT_ANIMATION_DURATION_MS = 300;
    public static final int SPLASH_SCREEN_DURATION_MS = 2000;
    public static final int DEBOUNCE_DELAY_MS = 500;

    private UI() {}
  }
}
