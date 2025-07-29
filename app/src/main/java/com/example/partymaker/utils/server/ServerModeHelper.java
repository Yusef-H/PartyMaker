package com.example.partymaker.utils.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class to manage server mode settings for PartyMaker. Allows toggling between server and
 * local modes.
 */
public class ServerModeHelper {
  private static final String PREF_USE_SERVER_MODE = "use_server_mode";

  /**
   * Checks if server mode is enabled in SharedPreferences.
   *
   * @param context the context
   * @return true if enabled, false otherwise
   */
  public static boolean isServerModeEnabled(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getBoolean(PREF_USE_SERVER_MODE, true);
  }

  /**
   * Enables or disables server mode in SharedPreferences.
   *
   * @param context the context
   * @param enabled true to enable, false to disable
   */
  public static void setServerModeEnabled(Context context, boolean enabled) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean(PREF_USE_SERVER_MODE, enabled);
    editor.apply();
  }
}
