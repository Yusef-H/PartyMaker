package com.example.partymaker.ui.settings;

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
import com.example.partymaker.utils.server.ServerModeHelper;

public class ServerSettingsActivity extends AppCompatActivity {

  private static final String PREF_SERVER_URL = "server_url";
  // local server is http://10.0.2.2:8080
  private static final String DEFAULT_SERVER_URL =
      "https://partymaker.onrender.com"; // Default to Render URL
  private SwitchCompat switchServerMode;
  private EditText editServerUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_server_settings);

    // Set up the action bar
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Server Settings");
    }

    // Initialize views
    switchServerMode = findViewById(R.id.switch_server_mode);
    editServerUrl = findViewById(R.id.edit_server_url);
    Button btnSaveSettings = findViewById(R.id.btn_save_settings);
    Button btnClearCache = findViewById(R.id.btn_clear_cache);

    // Load current settings
    loadSettings();

    // Set up listeners
    btnSaveSettings.setOnClickListener(v -> saveSettings());
    btnClearCache.setOnClickListener(v -> clearCache());
  }

  private void loadSettings() {
    // Server mode is always enabled
    switchServerMode.setChecked(true);
    switchServerMode.setEnabled(false); // Disable the switch

    // Load server URL
    String serverUrl =
        PreferenceManager.getDefaultSharedPreferences(this)
            .getString(PREF_SERVER_URL, DEFAULT_SERVER_URL);
    editServerUrl.setText(serverUrl);
  }

  private void saveSettings() {
    // Server mode is always enabled
    ServerModeHelper.setServerModeEnabled(this, true);

    // Save server URL
    String serverUrl = editServerUrl.getText().toString().trim();
    if (serverUrl.isEmpty()) {
      serverUrl = DEFAULT_SERVER_URL;
    }

    PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putString(PREF_SERVER_URL, serverUrl)
        .apply();

    Log.d("ServerSettings", "Server URL saved: " + serverUrl);

    Toast.makeText(
            this,
            "Settings saved. Please restart the app for changes to take effect.",
            Toast.LENGTH_LONG)
        .show();

    finish();
  }

  /** Clears the application cache using FileManager */
  private void clearCache() {
    // Show initial toast
    Toast.makeText(this, "Clearing cache...", Toast.LENGTH_SHORT).show();

    // Get cache size before clearing
    long cacheSize = FileManager.getSize(getCacheDir());
    String cacheSizeFormatted = FileManager.formatSize(cacheSize);

    Log.d("ServerSettings", "Cache size before clearing: " + cacheSizeFormatted);

    // Clear cache using FileManager
    FileManager.clearCache(this);

    // Show completion message with cache size that was cleared
    String message = "Cache cleared! Freed up " + cacheSizeFormatted + " of storage.";
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    Log.d("ServerSettings", "Cache cleared successfully");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
