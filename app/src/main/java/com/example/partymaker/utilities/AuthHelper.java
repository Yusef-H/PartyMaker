package com.example.partymaker.utilities;

import static com.example.partymaker.utilities.Constants.PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.partymaker.data.firebase.DBRef;
import com.google.firebase.auth.FirebaseUser;

/** Helper class for handling authentication with stable server mode session management */
public class AuthHelper {
  private static final String TAG = "AuthHelper";
  private static final String KEY_SERVER_MODE_ACTIVE = "server_mode_active";
  private static final String KEY_SERVER_MODE_EMAIL = "server_mode_email";
  private static final String KEY_SESSION_ACTIVE = "session_active";
  private static final String KEY_LAST_LOGIN_TIME = "last_login_time";

  /**
   * Gets the current user's email with reliable session management
   *
   * @param context Application context
   * @return User email or null if not authenticated
   */
  public static String getCurrentUserEmail(Context context) {
    try {
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

      // Check if we have an active session
      boolean sessionActive = prefs.getBoolean(KEY_SESSION_ACTIVE, false);
      String serverModeEmail = prefs.getString(KEY_SERVER_MODE_EMAIL, null);

      if (sessionActive && serverModeEmail != null) {
        Log.d(TAG, "Active session found for user: " + serverModeEmail);
        return serverModeEmail;
      }

      // Fallback to Firebase Auth if available
      try {
        FirebaseUser currentUser = DBRef.Auth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
          Log.d(TAG, "Firebase Auth user found: " + currentUser.getEmail());
          return currentUser.getEmail();
        }
      } catch (Exception e) {
        Log.w(TAG, "Firebase Auth not available, using server mode");
      }

      Log.w(TAG, "No active session found");
      return null;
    } catch (Exception e) {
      Log.e(TAG, "Error getting current user email", e);
      return null;
    }
  }

  /**
   * Sets the current user session for server mode
   *
   * @param context Application context
   * @param email User email
   */
  public static void setCurrentUserSession(Context context, String email) {
    try {
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();

      editor.putBoolean(KEY_SERVER_MODE_ACTIVE, true);
      editor.putString(KEY_SERVER_MODE_EMAIL, email);
      editor.putBoolean(KEY_SESSION_ACTIVE, true);
      editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());

      editor.apply();

      Log.d(TAG, "Session set for user: " + email);
    } catch (Exception e) {
      Log.e(TAG, "Error setting user session", e);
    }
  }

  /**
   * Gets the current user's key (email with dots replaced by spaces)
   *
   * @param context Application context
   * @return User key or null if not authenticated
   */
  public static String getCurrentUserKey(Context context) {
    String email = getCurrentUserEmail(context);
    return email != null ? email.replace('.', ' ') : null;
  }

  /**
   * Checks if Firebase Auth is available and working
   *
   * @param context Application context
   * @return true if Firebase Auth is available, false otherwise
   */
  public static boolean isFirebaseAuthAvailable(Context context) {
    try {
      FirebaseUser currentUser = DBRef.Auth.getCurrentUser();
      return currentUser != null;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks if user is authenticated with active session
   *
   * @param context Application context
   * @return true if user is authenticated, false otherwise
   */
  public static boolean isUserAuthenticated(Context context) {
    String email = getCurrentUserEmail(context);
    boolean isAuthenticated = email != null;

    if (isAuthenticated) {
      // Update last access time
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putLong("last_access_time", System.currentTimeMillis());
      editor.apply();
    }

    return isAuthenticated;
  }

  /**
   * Checks if the current session is valid (not expired)
   *
   * @param context Application context
   * @return true if session is valid, false otherwise
   */
  public static boolean isSessionValid(Context context) {
    try {
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      boolean sessionActive = prefs.getBoolean(KEY_SESSION_ACTIVE, false);
      long lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIME, 0);

      if (!sessionActive || lastLoginTime == 0) {
        return false;
      }

      // Session is valid for 30 days
      long sessionDuration = 30L * 24 * 60 * 60 * 1000; // 30 days in milliseconds
      long currentTime = System.currentTimeMillis();

      return (currentTime - lastLoginTime) < sessionDuration;
    } catch (Exception e) {
      Log.e(TAG, "Error checking session validity", e);
      return false;
    }
  }

  /**
   * Refreshes the current session
   *
   * @param context Application context
   */
  public static void refreshSession(Context context) {
    try {
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      String email = prefs.getString(KEY_SERVER_MODE_EMAIL, null);

      if (email != null) {
        setCurrentUserSession(context, email);
        Log.d(TAG, "Session refreshed for user: " + email);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error refreshing session", e);
    }
  }

  /**
   * Clears all authentication data
   *
   * @param context Application context
   */
  public static void clearAuthData(Context context) {
    try {
      // Clear Firebase Auth
      try {
        DBRef.Auth.signOut();
        DBRef.CurrentUser = null;
      } catch (Exception e) {
        Log.w(TAG, "Error signing out from Firebase Auth", e);
      }

      // Clear server mode data
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.remove(KEY_SERVER_MODE_EMAIL);
      editor.remove(KEY_SERVER_MODE_ACTIVE);
      editor.remove(KEY_SESSION_ACTIVE);
      editor.remove(KEY_LAST_LOGIN_TIME);
      editor.remove("last_access_time");
      editor.apply();

      Log.d(TAG, "All authentication data cleared");
    } catch (Exception e) {
      Log.e(TAG, "Error clearing auth data", e);
    }
  }
}
