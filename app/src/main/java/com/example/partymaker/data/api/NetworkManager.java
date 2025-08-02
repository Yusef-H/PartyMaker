package com.example.partymaker.data.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.utils.security.network.SSLPinningManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Manager for network operations and connectivity monitoring. Provides utilities for checking
 * network status, monitoring changes, and handling timeouts.
 */
public class NetworkManager {
  private static final String TAG = "NetworkManager";
  private static NetworkManager instance;

  private final MutableLiveData<Boolean> isNetworkAvailable = new MutableLiveData<>();
  private final Executor networkExecutor;
  private final Handler mainHandler;
  private ConnectivityManager connectivityManager;
  private ConnectivityManager.NetworkCallback networkCallback;
  private SSLPinningManager sslPinningManager;

  /** Private constructor for singleton pattern. */
  private NetworkManager() {
    networkExecutor = Executors.newSingleThreadExecutor();
    mainHandler = new Handler(Looper.getMainLooper());
    isNetworkAvailable.setValue(false);
  }

  /**
   * Gets the singleton instance of NetworkManager.
   *
   * @return The NetworkManager instance
   */
  public static synchronized NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
    }
    return instance;
  }

  /**
   * Initializes the NetworkManager with a context.
   *
   * @param context The application context
   */
  public void initialize(Context context) {
    if (context == null) {
      Log.e(TAG, "Cannot initialize with null context");
      return;
    }

    connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    // Initialize SSL Pinning Manager
    initializeSSLPinning();

    // Register network callback
    registerNetworkCallback();

    // Initial check
    checkNetworkAvailability();
  }

  /** Registers a callback for network changes. */
  private void registerNetworkCallback() {
    if (connectivityManager == null) {
      Log.e(TAG, "ConnectivityManager is null, cannot register callback");
      return;
    }

    if (networkCallback != null) {
      // Already registered
      return;
    }

    networkCallback =
        new ConnectivityManager.NetworkCallback() {
          @Override
          public void onAvailable(@NonNull Network network) {
            Log.d(TAG, "Network available");
            updateNetworkStatus(true);
          }

          @Override
          public void onLost(@NonNull Network network) {
            Log.d(TAG, "Network lost");
            updateNetworkStatus(false);
          }

          @Override
          public void onCapabilitiesChanged(
              @NonNull Network network, @NonNull NetworkCapabilities capabilities) {
            boolean hasInternet =
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            Log.d(TAG, "Network capabilities changed, has internet: " + hasInternet);
            updateNetworkStatus(hasInternet);
          }
        };

    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

    try {
      connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
    } catch (Exception e) {
      Log.e(TAG, "Error registering network callback", e);
    }
  }

  /**
   * Updates the network status.
   *
   * @param isAvailable Whether the network is available
   */
  private void updateNetworkStatus(final boolean isAvailable) {
    ThreadUtils.runOnMainThread(
        () -> {
          if (isNetworkAvailable.getValue() == null
              || isNetworkAvailable.getValue() != isAvailable) {
            isNetworkAvailable.setValue(isAvailable);
          }
        });
  }

  /**
   * Checks if the network is currently available.
   *
   * @return true if the network is available, false otherwise
   */
  public boolean isNetworkAvailable() {
    if (connectivityManager == null) {
      return false;
    }

    Network activeNetwork = connectivityManager.getActiveNetwork();
    if (activeNetwork == null) {
      return false;
    }

    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
    return capabilities != null
        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
  }

  /** Checks network availability and updates the LiveData. */
  public void checkNetworkAvailability() {
    updateNetworkStatus(isNetworkAvailable());
  }

  /**
   * Gets a LiveData object for observing network availability.
   *
   * @return LiveData for network availability
   */
  public LiveData<Boolean> getNetworkAvailabilityLiveData() {
    return isNetworkAvailable;
  }

  /**
   * Executes a network request with a timeout.
   *
   * @param runnable The runnable to execute
   * @param timeoutMs The timeout in milliseconds
   * @param timeoutCallback Callback to be called if the request times out
   */
  public void executeWithTimeout(Runnable runnable, long timeoutMs, Runnable timeoutCallback) {
    final boolean[] completed = {false};

    // Timeout handler
    ThreadUtils.runOnMainThreadDelayed(
        () -> {
          if (!completed[0]) {
            Log.d(TAG, "Network request timed out after " + timeoutMs + "ms");
            completed[0] = true;
            if (timeoutCallback != null) {
              timeoutCallback.run();
            }
          }
        },
        timeoutMs);

    // Execute the request
    ThreadUtils.runInBackground(
        () -> {
          try {
            runnable.run();
          } finally {
            completed[0] = true;
          }
        });
  }

  /** Initialize SSL Pinning Manager */
  private void initializeSSLPinning() {
    boolean isProduction = !android.os.Build.TYPE.equals("userdebug");
    sslPinningManager = SSLPinningManager.getInstance(isProduction);
    Log.d(
        TAG,
        "SSL Pinning Manager initialized for " + (isProduction ? "production" : "development"));
  }

  /** Get secure OkHttpClient with SSL pinning */
  public OkHttpClient getSecureClient() {
    if (sslPinningManager != null) {
      return sslPinningManager.createSecureClient();
    } else {
      Log.w(TAG, "SSL Pinning Manager not initialized, returning default client");
      return new OkHttpClient();
    }
  }

  /**
   * Checks if a server is reachable using secure connection.
   *
   * @param url The URL to check
   * @param timeout The timeout in milliseconds
   * @return true if the server is reachable, false otherwise
   */
  public boolean isServerReachable(String url, int timeout) {
    try {
      // Use secure client if available
      if (sslPinningManager != null && (url.startsWith("https://") || url.contains("partymaker"))) {
        return isServerReachableSecure(url, timeout);
      }

      // Fallback to regular HTTP connection
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setRequestMethod("HEAD");
      int responseCode = connection.getResponseCode();
      return (200 <= responseCode && responseCode <= 399);
    } catch (IOException e) {
      Log.e(TAG, "Error checking server reachability", e);
      return false;
    }
  }

  /** Checks server reachability using secure SSL-pinned connection */
  private boolean isServerReachableSecure(String url, int timeout) {
    try {
      OkHttpClient client = sslPinningManager.createSecureClient()
          .newBuilder()
          .connectTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
          .readTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
          .writeTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
          .build();
      Request request = new Request.Builder().url(url).head().build();

      try (Response response = client.newCall(request).execute()) {
        boolean isReachable = response.isSuccessful();
        Log.d(TAG, "Secure server check for " + url + ": " + (isReachable ? "success" : "failed"));
        return isReachable;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error in secure server check for " + url, e);
      return false;
    }
  }

  /** Test SSL connection to verify pinning */
  public boolean testSSLConnection(String url) {
    if (sslPinningManager != null) {
      return sslPinningManager.testSSLConnection(url);
    }
    return false;
  }

  /**
   * Releases resources used by the NetworkManager. Should be called when the application is
   * terminated.
   */
  public void release() {
    if (connectivityManager != null && networkCallback != null) {
      try {
        connectivityManager.unregisterNetworkCallback(networkCallback);
      } catch (Exception e) {
        Log.e(TAG, "Error unregistering network callback", e);
      }
      networkCallback = null;
    }
  }
}
