package com.example.partymaker.utils.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.partymaker.data.local.AppDatabase;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.system.ThreadUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Helper class for authentication related operations. Provides methods for getting current user
 * information and clearing auth data.
 */
public class AuthHelper {
  private static final String TAG = "AuthHelper";
  private static final String PREFS_NAME = "PartyMakerPrefs";
  private static final String KEY_USER_EMAIL = "user_email";
  private static final String KEY_SESSION_ACTIVE = "session_active";
  private static final String KEY_LAST_LOGIN_TIME = "last_login_time";
  private static final String KEY_SERVER_MODE_EMAIL = "server_mode_email";
  private static final String KEY_SERVER_MODE_ACTIVE = "server_mode_active";

  /**
   * Gets the current user's email from Firebase Auth or SharedPreferences as fallback
   *
   * @param context Application context
   * @return The user's email or null if not available
   */
  public static String getCurrentUserEmail(Context context) {
    if (context == null) {
      Log.e(TAG, "getCurrentUserEmail: Context is null");
      return null;
    }

    try {
      // Try to get from Firebase Auth first
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null
          && currentUser.getEmail() != null
          && !currentUser.getEmail().isEmpty()) {
        Log.d(TAG, "Firebase Auth user found: " + currentUser.getEmail());

        // Save to SharedPreferences as backup
        saveUserEmail(context, currentUser.getEmail());

        return currentUser.getEmail();
      }

      // If Firebase Auth failed, try SharedPreferences
      Log.d(TAG, "Firebase Auth user not found, checking SharedPreferences");
      return getSavedUserEmail(context);
    } catch (Exception e) {
      Log.e(TAG, "Error getting current user email", e);

      // Last resort - try SharedPreferences
      try {
        return getSavedUserEmail(context);
      } catch (Exception e2) {
        Log.e(TAG, "Failed to get user email from SharedPreferences", e2);
        return null;
      }
    }
  }

  /**
   * Gets the current user's key (email with dots replaced by spaces)
   *
   * @param context Application context
   * @return The user's key or null if not available
   * @throws AuthException if user is not authenticated
   */
  public static String getCurrentUserKey(Context context) throws AuthException {
    String email = getCurrentUserEmail(context);
    if (email == null || email.isEmpty()) {
      Log.e(TAG, "getCurrentUserKey: No user email found");
      throw new AuthException("User not authenticated");
    }

    // Make sure we replace dots with spaces correctly
    String key = email.replace('.', ' ');
    Log.d(TAG, "getCurrentUserKey: Email: " + email + " -> Key: " + key);
    return key;
  }

  /**
   * Saves user email to SharedPreferences
   *
   * @param context Application context
   * @param email User email to save
   */
  private static void saveUserEmail(Context context, String email) {
    try {
      if (context == null || email == null || email.isEmpty()) {
        return;
      }

      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      prefs.edit().putString(KEY_USER_EMAIL, email).apply();
      Log.d(TAG, "User email saved to SharedPreferences: " + email);
    } catch (Exception e) {
      Log.e(TAG, "Error saving user email to SharedPreferences", e);
    }
  }

  /**
   * Gets saved user email from SharedPreferences
   *
   * @param context Application context
   * @return The saved email or null if not available
   */
  private static String getSavedUserEmail(Context context) {
    try {
      if (context == null) {
        return null;
      }

      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      String email = prefs.getString(KEY_USER_EMAIL, null);

      if (email != null && !email.isEmpty()) {
        Log.d(TAG, "User email retrieved from SharedPreferences: " + email);
        return email;
      } else {
        Log.w(TAG, "No user email found in SharedPreferences");
        return null;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error getting user email from SharedPreferences", e);
      return null;
    }
  }

  /**
   * Logs out the current user
   *
   * @param context Application context
   * @return true if logout was successful, false otherwise
   */
  public static boolean logout(Context context) {
    Log.d(TAG, "Logging out user");
    boolean authCleared = clearAuthData(context);
    clearAllUserData(context);
    return authCleared;
  }

  /**
   * Clears all authentication data
   *
   * @param context Application context
   * @return true if successful, false otherwise
   */
  public static boolean clearAuthData(Context context) {
    boolean firebaseSignOutSuccess = false;
    boolean sharedPrefsSuccess = false;

    // Try to sign out from Firebase
    try {
      FirebaseAuth.getInstance().signOut();
      firebaseSignOutSuccess = true;
      Log.d(TAG, "Firebase sign out successful");
    } catch (Exception e) {
      Log.e(TAG, "Error signing out from Firebase", e);
    }

    // Clear SharedPreferences
    try {
      if (context != null) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs
            .edit()
            .remove(KEY_USER_EMAIL)
            .remove(KEY_SESSION_ACTIVE)
            .remove(KEY_LAST_LOGIN_TIME)
            .remove(KEY_SERVER_MODE_EMAIL)
            .remove(KEY_SERVER_MODE_ACTIVE)
            .apply();
        sharedPrefsSuccess = true;
        Log.d(TAG, "SharedPreferences auth data cleared");
      }
    } catch (Exception e) {
      Log.e(TAG, "Error clearing SharedPreferences auth data", e);
    }

    return firebaseSignOutSuccess || sharedPrefsSuccess;
  }

  /**
   * Clears all user-related data including Room database and repository caches
   *
   * @param context Application context
   */
  public static void clearAllUserData(Context context) {
    if (context == null) {
      Log.w(TAG, "Cannot clear user data: context is null");
      return;
    }

    Log.d(TAG, "Clearing all user data including Room database");

    // Clear repositories cache first
    try {
      GroupRepository.getInstance().clearCache();
      UserRepository.getInstance().clearCache();
      Log.d(TAG, "Repository caches cleared");
    } catch (Exception e) {
      Log.e(TAG, "Error clearing repository caches", e);
    }

    // Clear Room database in background thread but wait for completion
    ThreadUtils.runInBackground(
        () -> {
          try {
            AppDatabase database = AppDatabase.getInstance(context);
            database.groupDao().deleteAllGroups();
            database.userDao().deleteAllUsers();

            // Also clear chat messages if needed
            if (database.chatMessageDao() != null) {
              database.chatMessageDao().deleteAllMessages();
            }

            Log.d(TAG, "Room database cleared successfully");

            // Additional verification: check that database is actually empty
            int groupCount = database.groupDao().getAllGroups().size();
            int userCount = database.userDao().getAllUsers().size();
            Log.d(
                TAG,
                "Database verification after clear - Groups: "
                    + groupCount
                    + ", Users: "
                    + userCount);

          } catch (Exception e) {
            Log.e(TAG, "Error clearing Room database", e);
          }
        });
  }

  /**
   * Checks if a user is currently logged in
   *
   * @param context Application context
   * @return true if user is logged in, false otherwise
   */
  public static boolean isLoggedIn(Context context) {
    try {
      // Check Firebase Auth first
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null) {
        return true;
      }

      // Check SharedPreferences as fallback
      String savedEmail = getSavedUserEmail(context);
      return savedEmail != null && !savedEmail.isEmpty();
    } catch (Exception e) {
      Log.e(TAG, "Error checking login status", e);
      return false;
    }
  }

  // ---------- LEGACY METHODS (FOR BACKWARD COMPATIBILITY) ----------

  /**
   * @param context Application context
   * @return true if Firebase Auth is available, false otherwise
   * @deprecated Use isLoggedIn() instead Checks if Firebase Auth is available and working
   */
  @Deprecated
  public static boolean isFirebaseAuthAvailable(Context context) {
    try {
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      return currentUser != null;
    } catch (Exception e) {
      Log.e(TAG, "Error checking Firebase Auth availability", e);
      return false;
    }
  }

  /**
   * @param context Application context
   * @param email User email
   * @deprecated Use saveUserEmail() instead Sets the current user session for server mode
   */
  @Deprecated
  public static void setCurrentUserSession(Context context, String email) {
    try {
      if (context == null || email == null || email.isEmpty()) {
        return;
      }

      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();

      editor.putBoolean(KEY_SERVER_MODE_ACTIVE, true);
      editor.putString(KEY_SERVER_MODE_EMAIL, email);
      editor.putBoolean(KEY_SESSION_ACTIVE, true);
      editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());

      editor.apply();

      // Also save to our new method
      saveUserEmail(context, email);

      Log.d(TAG, "Session set for user: " + email);
    } catch (Exception e) {
      Log.e(TAG, "Error setting user session", e);
    }
  }

  /**
   * @param context Application context
   * @return true if user is authenticated, false otherwise
   * @deprecated Use isLoggedIn() instead Checks if user is authenticated with active session
   */
  @Deprecated
  public static boolean isUserAuthenticated(Context context) {
    return isLoggedIn(context);
  }

  /**
   * @param context Application context
   * @return true if session is valid, false otherwise
   * @deprecated Use isLoggedIn() instead Checks if the current session is valid (not expired)
   */
  @Deprecated
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
   * @param context Application context
   * @deprecated No longer needed with new implementation Refreshes the current session
   */
  @Deprecated
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

  /** Custom exception for authentication errors */
  public static class AuthException extends Exception {
    public AuthException(String message) {
      super(message);
    }

    public AuthException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
