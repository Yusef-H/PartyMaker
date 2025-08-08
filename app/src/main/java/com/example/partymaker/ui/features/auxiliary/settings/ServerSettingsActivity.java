package com.example.partymaker.ui.features.auxiliary.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;
import com.example.partymaker.R;
import com.example.partymaker.utils.media.FileManager;
import com.example.partymaker.utils.server.ServerModeManager;

/**
 * Activity for managing server settings. Allows users to configure the server URL and manage cache
 * settings. Server mode is always enabled in this version.
 */
public class ServerSettingsActivity extends AppCompatActivity {

  // SharedPreferences keys
  private static final String PREF_SERVER_URL = "server_url";

  // Default server configuration
  private static final String DEFAULT_SERVER_URL = "https://partymaker.onrender.com";
  private static final String LOCAL_SERVER_URL = "http://10.0.2.2:8080"; // For emulator development

  // UI messages
  private static final String ACTIVITY_TITLE = "Server Settings";
  private static final String MESSAGE_CLEARING_CACHE = "Clearing cache...";
  private static final String MESSAGE_SETTINGS_SAVED =
      "Settings saved. Please restart the app for changes to take effect.";
  private static final String MESSAGE_CACHE_CLEARED_FORMAT =
      "Cache cleared! Freed up %s of storage.";

  // Log tag
  private static final String TAG = "ServerSettings";

  private SwitchCompat switchServerMode;
  private EditText editServerUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_server_settings);

    setupActionBar();

    initializeViews();
    loadSettings();
    setupEventListeners();
  }

  /** Loads current settings from SharedPreferences and updates UI. */
  private void loadSettings() {
    configureServerModeSwitch();
    loadServerUrl();
  }

  /** Saves the current settings and finishes the activity. */
  private void saveSettings() {
    enableServerMode();
    String serverUrl = getValidatedServerUrl();
    saveServerUrl(serverUrl);

    Log.d(TAG, "Server URL saved: " + serverUrl);
    showSettingsSavedMessage();
    finish();
  }

  /** Clears the application cache and shows feedback to the user. */
  private void clearCache() {
    showCacheCleanupStartMessage();

    long cacheSize = getCacheSizeBeforeClearing();
    String cacheSizeFormatted = FileManager.formatSize(cacheSize);

    logCacheSizeBeforeClearing(cacheSizeFormatted);
    performCacheClearing();
    showCacheCleanupCompletionMessage(cacheSizeFormatted);

    Log.d(TAG, "Cache cleared successfully");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // Private helper methods

  /** Sets up the action bar with back button and title. */
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle(ACTIVITY_TITLE);
    }
  }

  /** Initializes all view components. */
  private void initializeViews() {
    switchServerMode = findViewById(R.id.switch_server_mode);
    editServerUrl = findViewById(R.id.edit_server_url);
  }

  /** Sets up click listeners for buttons. */
  private void setupEventListeners() {
    Button btnSaveSettings = findViewById(R.id.btn_save_settings);
    Button btnClearCache = findViewById(R.id.btn_clear_cache);

    btnSaveSettings.setOnClickListener(v -> saveSettings());
    btnClearCache.setOnClickListener(v -> clearCache());
  }

  /** Configures the server mode switch (always enabled and disabled for user interaction). */
  private void configureServerModeSwitch() {
    switchServerMode.setChecked(true);
    switchServerMode.setEnabled(false);
  }

  /** Loads the server URL from preferences and sets it in the EditText. */
  private void loadServerUrl() {
    String serverUrl =
        PreferenceManager.getDefaultSharedPreferences(this)
            .getString(PREF_SERVER_URL, DEFAULT_SERVER_URL);
    editServerUrl.setText(serverUrl);
  }

  /** Enables server mode using ServerModeManager. */
  private void enableServerMode() {
    ServerModeManager.setServerModeEnabled(this, true);
  }

  /**
   * Gets and validates the server URL from the EditText.
   *
   * @return The validated server URL
   */
  private String getValidatedServerUrl() {
    String serverUrl = editServerUrl.getText().toString().trim();
    return serverUrl.isEmpty() ? DEFAULT_SERVER_URL : serverUrl;
  }

  /**
   * Saves the server URL to SharedPreferences.
   *
   * @param serverUrl The server URL to save
   */
  private void saveServerUrl(String serverUrl) {
    PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putString(PREF_SERVER_URL, serverUrl)
        .apply();
  }

  /** Shows a toast message indicating settings have been saved. */
  private void showSettingsSavedMessage() {
    Toast.makeText(this, MESSAGE_SETTINGS_SAVED, Toast.LENGTH_LONG).show();
  }

  /** Shows initial cache cleanup message. */
  private void showCacheCleanupStartMessage() {
    Toast.makeText(this, MESSAGE_CLEARING_CACHE, Toast.LENGTH_SHORT).show();
  }

  /**
   * Gets the cache size before clearing.
   *
   * @return Cache size in bytes
   */
  private long getCacheSizeBeforeClearing() {
    return FileManager.getSize(getCacheDir());
  }

  /**
   * Logs the cache size before clearing.
   *
   * @param cacheSizeFormatted The formatted cache size string
   */
  private void logCacheSizeBeforeClearing(String cacheSizeFormatted) {
    Log.d(TAG, "Cache size before clearing: " + cacheSizeFormatted);
  }

  /** Performs the actual cache clearing operation. */
  private void performCacheClearing() {
    FileManager.clearCache(this);
  }

  /**
   * Shows cache cleanup completion message with freed space amount.
   *
   * @param cacheSizeFormatted The formatted cache size that was cleared
   */
  private void showCacheCleanupCompletionMessage(String cacheSizeFormatted) {
    String message = String.format(MESSAGE_CACHE_CLEARED_FORMAT, cacheSizeFormatted);
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }
}
