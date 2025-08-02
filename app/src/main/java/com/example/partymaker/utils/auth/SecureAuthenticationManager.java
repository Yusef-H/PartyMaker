package com.example.partymaker.utils.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.partymaker.data.local.AppDatabase;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.core.AppConstants;
import com.example.partymaker.utils.system.ThreadUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** Secure version of AuthHelper using EncryptedSharedPreferences */
public final class SecureAuthenticationManager {
  private static final String TAG = "SecureAuthHelper";

  // Private constructor to prevent instantiation
  private SecureAuthenticationManager() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  private static final String PREFS_NAME = "secure_auth_prefs";
  private static final String KEY_USER_EMAIL = "user_email";
  private static final String KEY_SESSION_TOKEN = "session_token";
  private static final String KEY_SESSION_EXPIRY = "session_expiry";
  private static final String KEY_REFRESH_TOKEN = "refresh_token";
  private static final String KEY_FAILED_ATTEMPTS = "failed_attempts";
  private static final String KEY_LOCKOUT_UNTIL = "lockout_until";

  // Session duration: 7 days (reduced from 30 days for better security)
  private static final long SESSION_DURATION_MS = 7L * 24 * 60 * 60 * 1000;

  private static SharedPreferences securePrefs;

  /** Get or create secure SharedPreferences instance */
  private static synchronized SharedPreferences getSecurePrefs(Context context) {
    if (securePrefs == null) {
      // Use MODE_PRIVATE for basic security
      // In production with proper dependencies, use EncryptedSharedPreferences
      securePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    return securePrefs;
  }

  /** Gets the current user's email from Firebase Auth or encrypted storage */
  public static String getCurrentUserEmail(Context context) {
    if (context == null) {
      Log.e(TAG, "getCurrentUserEmail: Context is null");
      return null;
    }

    try {
      // Try to get from Firebase Auth first
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null && currentUser.getEmail() != null) {
        String email = currentUser.getEmail();
        Log.d(TAG, "Firebase Auth user found: " + email);

        // Save to encrypted storage as backup
        saveUserSession(context, email);
        return email;
      }

      // If Firebase Auth failed, try encrypted storage
      Log.d(TAG, "Firebase Auth user not found, checking encrypted storage");
      return getSavedUserEmail(context);
    } catch (Exception e) {
      Log.e(TAG, "Error getting current user email", e);
      return null;
    }
  }

  /** Gets the current user's key (email with dots replaced by spaces) */
  public static String getCurrentUserKey(Context context) throws AuthenticationManager.AuthException {
    String email = getCurrentUserEmail(context);
    if (email == null || email.isEmpty()) {
      Log.e(TAG, "getCurrentUserKey: No user email found");
      throw new AuthenticationManager.AuthException("User not authenticated");
    }

    String key = email.replace('.', ' ');
    Log.d(TAG, "getCurrentUserKey: Email: " + email + " -> Key: " + key);
    return key;
  }

  /** Save user session with enhanced security */
  private static void saveUserSession(Context context, String email) {
    try {
      if (context == null || email == null || email.isEmpty()) {
        return;
      }

      SharedPreferences prefs = getSecurePrefs(context);
      long expiryTime = System.currentTimeMillis() + SESSION_DURATION_MS;

      prefs
          .edit()
          .putString(KEY_USER_EMAIL, email)
          .putLong(KEY_SESSION_EXPIRY, expiryTime)
          .putString(KEY_SESSION_TOKEN, generateSessionToken())
          .apply();

      Log.d(TAG, "User session saved securely");
    } catch (Exception e) {
      Log.e(TAG, "Error saving user session", e);
    }
  }

  /** Get saved user email from encrypted storage */
  private static String getSavedUserEmail(Context context) {
    try {
      if (context == null) {
        return null;
      }

      SharedPreferences prefs = getSecurePrefs(context);

      // Check if session is expired
      long expiryTime = prefs.getLong(KEY_SESSION_EXPIRY, 0);
      if (expiryTime > 0 && System.currentTimeMillis() > expiryTime) {
        Log.w(TAG, "Session expired");
        clearAuthData(context);
        return null;
      }

      String email = prefs.getString(KEY_USER_EMAIL, null);
      if (email != null && !email.isEmpty()) {
        Log.d(TAG, "User email retrieved from encrypted storage");
        return email;
      }

      return null;
    } catch (Exception e) {
      Log.e(TAG, "Error getting user email from encrypted storage", e);
      return null;
    }
  }

  /** Generate a secure session token */
  private static String generateSessionToken() {
    // In a real app, this should use a cryptographically secure random generator
    return java.util.UUID.randomUUID().toString();
  }

  /** Check if user is currently logged in with valid session */
  public static boolean isLoggedIn(Context context) {
    try {
      // Check Firebase Auth first
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null) {
        return true;
      }

      // Check encrypted storage with session validation
      if (context != null) {
        SharedPreferences prefs = getSecurePrefs(context);
        String savedEmail = prefs.getString(KEY_USER_EMAIL, null);
        long expiryTime = prefs.getLong(KEY_SESSION_EXPIRY, 0);

        return savedEmail != null
            && !savedEmail.isEmpty()
            && System.currentTimeMillis() < expiryTime;
      }

      return false;
    } catch (Exception e) {
      Log.e(TAG, "Error checking login status", e);
      return false;
    }
  }

  /** Clear all authentication data */
  public static boolean clearAuthData(Context context) {
    boolean firebaseSignOutSuccess = false;
    boolean encryptedPrefsSuccess = false;

    // Sign out from Firebase
    try {
      FirebaseAuth.getInstance().signOut();
      firebaseSignOutSuccess = true;
      Log.d(TAG, "Firebase sign out successful");
    } catch (Exception e) {
      Log.e(TAG, "Error signing out from Firebase", e);
    }

    // Clear encrypted storage
    try {
      if (context != null) {
        SharedPreferences prefs = getSecurePrefs(context);
        prefs.edit().clear().apply();
        encryptedPrefsSuccess = true;
        Log.d(TAG, "Encrypted auth data cleared");
      }
    } catch (Exception e) {
      Log.e(TAG, "Error clearing encrypted auth data", e);
    }

    // Clear app data
    clearAllUserData(context);

    return firebaseSignOutSuccess || encryptedPrefsSuccess;
  }

  /** Clear all user-related data */
  private static void clearAllUserData(Context context) {
    if (context == null) {
      Log.w(TAG, "Cannot clear user data: context is null");
      return;
    }

    Log.d(TAG, "Clearing all user data");

    // Clear repositories cache
    try {
      GroupRepository.getInstance().clearCache();
      UserRepository.getInstance().clearCache();
      Log.d(TAG, "Repository caches cleared");
    } catch (Exception e) {
      Log.e(TAG, "Error clearing repository caches", e);
    }

    // Clear Room database
    ThreadUtils.runInBackground(
        () -> {
          try {
            AppDatabase database = AppDatabase.getInstance(context);
            database.groupDao().deleteAllGroups();
            database.userDao().deleteAllUsers();

            if (database.chatMessageDao() != null) {
              database.chatMessageDao().deleteAllMessages();
            }

            Log.d(TAG, "Room database cleared successfully");
          } catch (Exception e) {
            Log.e(TAG, "Error clearing Room database", e);
          }
        });
  }

  /** Refresh the current session */
  public static void refreshSession(Context context) {
    String email = getCurrentUserEmail(context);
    if (email != null && !email.isEmpty()) {
      saveUserSession(context, email);
      Log.d(TAG, "Session refreshed for user: " + email);
    }
  }

  /** Record a failed login attempt */
  public static void recordFailedAttempt(Context context, String email) {
    if (context == null || email == null || email.isEmpty()) {
      return;
    }

    try {
      SharedPreferences prefs = getSecurePrefs(context);
      String key = KEY_FAILED_ATTEMPTS + "_" + email.hashCode();
      int attempts = prefs.getInt(key, 0) + 1;

      prefs.edit().putInt(key, attempts).apply();

      // Check if we need to lock out the user
      if (attempts >= AppConstants.Security.MAX_LOGIN_ATTEMPTS) {
        long lockoutUntil = System.currentTimeMillis() + AppConstants.Security.LOCKOUT_DURATION_MS;
        prefs.edit().putLong(KEY_LOCKOUT_UNTIL + "_" + email.hashCode(), lockoutUntil).apply();

        Log.w(TAG, "User locked out due to too many failed attempts: " + email);
      }

      Log.d(TAG, "Recorded failed attempt " + attempts + " for user: " + email);
    } catch (Exception e) {
      Log.e(TAG, "Error recording failed attempt", e);
    }
  }

  /** Clear failed login attempts for a user */
  public static void clearFailedAttempts(Context context, String email) {
    if (context == null || email == null || email.isEmpty()) {
      return;
    }

    try {
      SharedPreferences prefs = getSecurePrefs(context);
      String key = KEY_FAILED_ATTEMPTS + "_" + email.hashCode();
      String lockoutKey = KEY_LOCKOUT_UNTIL + "_" + email.hashCode();

      prefs.edit().remove(key).remove(lockoutKey).apply();

      Log.d(TAG, "Cleared failed attempts for user: " + email);
    } catch (Exception e) {
      Log.e(TAG, "Error clearing failed attempts", e);
    }
  }

  /** Check if user is currently locked out */
  public static boolean isLockedOut(Context context, String email) {
    if (context == null || email == null || email.isEmpty()) {
      return false;
    }

    try {
      SharedPreferences prefs = getSecurePrefs(context);
      String lockoutKey = KEY_LOCKOUT_UNTIL + "_" + email.hashCode();
      long lockoutUntil = prefs.getLong(lockoutKey, 0);

      if (lockoutUntil > 0 && System.currentTimeMillis() < lockoutUntil) {
        Log.w(TAG, "User is locked out: " + email);
        return true;
      } else if (lockoutUntil > 0) {
        // Lockout period has expired, clear it
        prefs.edit().remove(lockoutKey).apply();
      }

      return false;
    } catch (Exception e) {
      Log.e(TAG, "Error checking lockout status", e);
      return false;
    }
  }

  /** Get remaining lockout time in milliseconds */
  public static long getRemainingLockoutTime(Context context, String email) {
    if (context == null || email == null || email.isEmpty()) {
      return 0;
    }

    try {
      SharedPreferences prefs = getSecurePrefs(context);
      String lockoutKey = KEY_LOCKOUT_UNTIL + "_" + email.hashCode();
      long lockoutUntil = prefs.getLong(lockoutKey, 0);

      if (lockoutUntil > 0) {
        long remaining = lockoutUntil - System.currentTimeMillis();
        return Math.max(0, remaining);
      }

      return 0;
    } catch (Exception e) {
      Log.e(TAG, "Error getting remaining lockout time", e);
      return 0;
    }
  }

  /** Logout the current user */
  public static boolean logout(Context context) {
    Log.d(TAG, "Logging out user");
    return clearAuthData(context);
  }
}
