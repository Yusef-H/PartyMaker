package com.example.partymaker.data.api;

import android.content.Context;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Manager class for monitoring network connectivity. Provides LiveData for observing network state
 * changes.
 */
public class ConnectivityManager {
  private static final String TAG = "ConnectivityManager";
  
  // Network check constants
  private static final String NETWORK_TEST_URL = "https://www.google.com";
  private static final int NETWORK_TIMEOUT_MS = 3000;
  private static final int NETWORK_CHECK_INTERVAL_MS = 30000;
  private static final int HTTP_SUCCESS_CODE = 200;
  
  // Instance management
  private static ConnectivityManager instance;

  private final MutableLiveData<Boolean> isNetworkAvailable = new MutableLiveData<>(false);
  private final MutableLiveData<NetworkUtils.ErrorType> lastNetworkError = new MutableLiveData<>();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private android.net.ConnectivityManager connectivityManager;
  private android.net.ConnectivityManager.NetworkCallback networkCallback;
  private Context appContext;

  /** Private constructor to enforce singleton pattern */
  private ConnectivityManager() {
    // Prevent instantiation
  }

  /**
   * Gets the singleton instance
   *
   * @return The ConnectivityManager instance
   */
  public static synchronized ConnectivityManager getInstance() {
    if (instance == null) {
      instance = new ConnectivityManager();
    }
    return instance;
  }

  /**
   * Initializes the connectivity manager
   *
   * @param context Application context
   */
  public void init(Context context) {
    if (context == null) {
      Log.e(TAG, "Context is null, cannot initialize ConnectivityManager");
      return;
    }

    // Save application context for later use
    this.appContext = context.getApplicationContext();

    connectivityManager =
        (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager == null) {
      Log.e(TAG, "ConnectivityManager service not available");
      return;
    }

    // Check initial network state
    boolean hasNetwork = NetworkUtils.isNetworkAvailable(context);
    isNetworkAvailable.postValue(hasNetwork);

    // Log initial state
    Log.d(TAG, "Initial network availability: " + hasNetwork);

    // Register network callback
    registerNetworkCallback();

    Log.d(TAG, "ConnectivityManager initialized, initial network state: " + hasNetwork);

    // Perform an active network check to ensure we have the correct state
    performActiveNetworkCheck();

    // Schedule periodic network checks
    schedulePeriodicNetworkChecks();
  }

  /** Schedules periodic network checks to ensure connectivity status is accurate */
  private void schedulePeriodicNetworkChecks() {
    mainHandler.postDelayed(createPeriodicCheckRunnable(), NETWORK_CHECK_INTERVAL_MS);
  }
  
  /**
   * Creates a runnable for periodic network checks
   * 
   * @return Runnable for periodic checks
   */
  private Runnable createPeriodicCheckRunnable() {
    return new Runnable() {
      @Override
      public void run() {
        if (appContext != null) {
          performActiveNetworkCheck();
        }
        // Schedule the next check
        mainHandler.postDelayed(this, NETWORK_CHECK_INTERVAL_MS);
      }
    };
  }

  /** Forces a refresh of the network connectivity status */
  public void refreshNetworkStatus() {
    if (appContext != null) {
      performActiveNetworkCheck();
    } else {
      Log.e(TAG, "Cannot refresh network status: appContext is null");
    }
  }

  /** Performs an active check of the network connectivity */
  private void performActiveNetworkCheck() {
    new Thread(this::checkNetworkConnection).start();
  }
  
  /**
   * Checks network connection by attempting to connect to a known server
   */
  private void checkNetworkConnection() {
    try {
      boolean isConnected = testNetworkConnection();
      Log.d(TAG, "Active network check result: " + isConnected);
      updateNetworkStateOnMainThread(isConnected, null);
    } catch (Exception e) {
      Log.e(TAG, "Error performing active network check", e);
      updateNetworkStateOnMainThread(false, NetworkUtils.ErrorType.NO_NETWORK);
    }
  }
  
  /**
   * Tests network connection to a known server
   * 
   * @return true if connection successful
   * @throws Exception if connection fails
   */
  private boolean testNetworkConnection() throws Exception {
    java.net.URL url = new java.net.URL(NETWORK_TEST_URL);
    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(NETWORK_TIMEOUT_MS);
    connection.connect();
    boolean isConnected = connection.getResponseCode() == HTTP_SUCCESS_CODE;
    connection.disconnect();
    return isConnected;
  }
  
  /**
   * Updates network state on the main thread
   * 
   * @param isConnected Whether network is connected
   * @param errorType Error type if connection failed
   */
  private void updateNetworkStateOnMainThread(boolean isConnected, NetworkUtils.ErrorType errorType) {
    mainHandler.post(() -> {
      isNetworkAvailable.setValue(isConnected);
      if (!isConnected && errorType != null) {
        lastNetworkError.setValue(errorType);
      } else if (isConnected) {
        // Clear error if we're now connected
        lastNetworkError.setValue(null);
      }
    });
  }

  /** Registers a callback for network changes */
  private void registerNetworkCallback() {
    if (connectivityManager == null) {
      Log.e(TAG, "ConnectivityManager not initialized");
      return;
    }

    NetworkRequest networkRequest =
        new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build();

    networkCallback =
        new android.net.ConnectivityManager.NetworkCallback() {
          @Override
          public void onAvailable(@NonNull Network network) {
            Log.d(TAG, "Network available");
            mainHandler.post(
                () -> {
                  isNetworkAvailable.setValue(true);
                  // Verify with an active check
                  if (appContext != null) {
                    performActiveNetworkCheck();
                  }
                });
          }

          @Override
          public void onLost(@NonNull Network network) {
            Log.d(TAG, "Network lost");
            mainHandler.post(
                () -> {
                  isNetworkAvailable.setValue(false);
                  lastNetworkError.setValue(NetworkUtils.ErrorType.NO_NETWORK);
                });
          }

          @Override
          public void onCapabilitiesChanged(
              @NonNull Network network, @NonNull NetworkCapabilities capabilities) {
            boolean hasInternet =
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

            Log.d(TAG, "Network capabilities changed, has internet: " + hasInternet);
            mainHandler.post(() -> isNetworkAvailable.setValue(hasInternet));
          }
        };

    try {
      connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
      Log.d(TAG, "Network callback registered");
    } catch (Exception e) {
      Log.e(TAG, "Error registering network callback", e);
    }
  }

  /** Unregisters the network callback */
  public void unregisterNetworkCallback() {
    if (connectivityManager != null && networkCallback != null) {
      try {
        connectivityManager.unregisterNetworkCallback(networkCallback);
        Log.d(TAG, "Network callback unregistered");
      } catch (Exception e) {
        Log.e(TAG, "Error unregistering network callback", e);
      }
    }
  }

  /**
   * Gets the network availability as LiveData
   *
   * @return LiveData for network availability
   */
  public LiveData<Boolean> getNetworkAvailability() {
    return isNetworkAvailable;
  }

  /**
   * Gets the last network error as LiveData
   *
   * @return LiveData for last network error
   */
  public LiveData<NetworkUtils.ErrorType> getLastNetworkError() {
    return lastNetworkError;
  }

  /**
   * Sets the last network error
   *
   * @param errorType The error type
   */
  public void setLastNetworkError(NetworkUtils.ErrorType errorType) {
    lastNetworkError.postValue(errorType);
  }
}
