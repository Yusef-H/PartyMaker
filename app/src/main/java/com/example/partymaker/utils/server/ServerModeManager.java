package com.example.partymaker.utils.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class to manage server mode settings for PartyMaker. Allows toggling between server and
 * local modes.
 */
public class ServerModeManager {
  private static final String TAG = "ServerModeManager";
  private static final String PREF_USE_SERVER_MODE = "use_server_mode";
  private static final boolean DEFAULT_SERVER_MODE = true;

  /**
   * Checks if server mode is enabled in SharedPreferences.
   *
   * @param context the context
   * @return true if enabled, false otherwise
   */
  public static boolean isServerModeEnabled(Context context) {
    if (context == null) {
      android.util.Log.w(TAG, "Context is null, returning default server mode");
      return DEFAULT_SERVER_MODE;
    }
    
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    boolean isEnabled = prefs.getBoolean(PREF_USE_SERVER_MODE, DEFAULT_SERVER_MODE);
    android.util.Log.d(TAG, "Server mode enabled: " + isEnabled);
    return isEnabled;
  }

  /**
   * Enables or disables server mode in SharedPreferences.
   *
   * @param context the context
   * @param enabled true to enable, false to disable
   */
  public static void setServerModeEnabled(Context context, boolean enabled) {
    if (context == null) {
      android.util.Log.e(TAG, "Cannot set server mode: context is null");
      return;
    }
    
    try {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putBoolean(PREF_USE_SERVER_MODE, enabled);
      editor.apply();
      
      android.util.Log.d(TAG, "Server mode set to: " + enabled);
    } catch (Exception e) {
      android.util.Log.e(TAG, "Error setting server mode", e);
    }
  }
}
