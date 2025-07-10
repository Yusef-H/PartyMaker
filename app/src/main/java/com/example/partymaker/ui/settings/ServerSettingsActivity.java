package com.example.partymaker.ui.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.example.partymaker.R;
import com.example.partymaker.utilities.ServerModeHelper;

public class ServerSettingsActivity extends AppCompatActivity {

  private SwitchCompat switchServerMode;
  private EditText editServerUrl;
    private static final String PREF_SERVER_URL = "server_url";
  private static final String DEFAULT_SERVER_URL = "http://10.0.2.2:8080"; // Default for emulator

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_server_settings);

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

    // Load current settings
    loadSettings();

    // Set up listeners
    btnSaveSettings.setOnClickListener(v -> saveSettings());
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

    Toast.makeText(
            this,
            "Settings saved. Please restart the app for changes to take effect.",
            Toast.LENGTH_LONG)
        .show();

    finish();
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
