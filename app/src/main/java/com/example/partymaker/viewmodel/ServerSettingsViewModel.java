package com.example.partymaker.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import com.example.partymaker.data.api.NetworkManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;

/** ViewModel for ServerSettingsActivity handling server configuration */
public class ServerSettingsViewModel extends BaseViewModel {

  private final MutableLiveData<String> currentServerUrl = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isServerReachable = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isTestingConnection = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isSaved = new MutableLiveData<>(false);

  private final SharedPreferences sharedPreferences;
  private final NetworkManager networkManager;

  private static final String DEFAULT_SERVER_URL = "https://partymaker.onrender.com";
  private static final String SERVER_URL_KEY = "server_url";

  public ServerSettingsViewModel(@NonNull Application application) {
    super(application);
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
    networkManager = NetworkManager.getInstance();
    loadCurrentServerUrl();
  }

  public LiveData<String> getCurrentServerUrl() {
    return currentServerUrl;
  }

  public LiveData<Boolean> getIsServerReachable() {
    return isServerReachable;
  }

  public LiveData<Boolean> getIsTestingConnection() {
    return isTestingConnection;
  }

  public LiveData<Boolean> getIsSaved() {
    return isSaved;
  }

  private void loadCurrentServerUrl() {
    String url = sharedPreferences.getString(SERVER_URL_KEY, DEFAULT_SERVER_URL);
    currentServerUrl.setValue(url);
  }

  public void testConnection(String serverUrl) {
    if (serverUrl == null || serverUrl.trim().isEmpty()) {
      setError("Server URL cannot be empty");
      return;
    }

    if (!isValidUrl(serverUrl)) {
      setError("Invalid URL format");
      return;
    }

    isTestingConnection.setValue(true);
    setLoading(true);

    ThreadUtils.runInBackground(
        () -> {
          try {
            boolean isReachable =
                networkManager.isServerReachable(serverUrl + "/api/firebase/health", 10000);

            ThreadUtils.runOnMainThread(
                () -> {
                  isTestingConnection.setValue(false);
                  setLoading(false);
                  isServerReachable.setValue(isReachable);

                  if (isReachable) {
                    setSuccess("Server is reachable");
                  } else {
                    setError("Server is not reachable");
                  }
                });
          } catch (Exception e) {
            ThreadUtils.runOnMainThread(
                () -> {
                  isTestingConnection.setValue(false);
                  setLoading(false);
                  isServerReachable.setValue(false);
                  setError("Connection test failed: " + e.getMessage());
                });
          }
        });
  }

  public void saveServerUrl(String serverUrl) {
    if (serverUrl == null || serverUrl.trim().isEmpty()) {
      setError("Server URL cannot be empty");
      return;
    }

    if (!isValidUrl(serverUrl)) {
      setError("Invalid URL format");
      return;
    }

    sharedPreferences.edit().putString(SERVER_URL_KEY, serverUrl.trim()).apply();

    currentServerUrl.setValue(serverUrl.trim());
    isSaved.setValue(true);
    setSuccess("Server URL saved successfully");

    // NetworkManager will use the updated SharedPreferences value
  }

  public void resetToDefault() {
    saveServerUrl(DEFAULT_SERVER_URL);
    setSuccess("Reset to default server");
  }

  private boolean isValidUrl(String url) {
    return url != null
        && (url.startsWith("http://") || url.startsWith("https://"))
        && android.util.Patterns.WEB_URL.matcher(url).matches();
  }

  public void resetSavedStatus() {
    isSaved.setValue(false);
  }
}
