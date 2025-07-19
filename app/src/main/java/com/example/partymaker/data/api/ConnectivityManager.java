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
  private static ConnectivityManager instance;

  private final MutableLiveData<Boolean> isNetworkAvailable = new MutableLiveData<>(false);
  private final MutableLiveData<NetworkUtils.ErrorType> lastNetworkError = new MutableLiveData<>();

  private android.net.ConnectivityManager connectivityManager;
  private android.net.ConnectivityManager.NetworkCallback networkCallback;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  /** Private constructor to enforce singleton pattern */
  private ConnectivityManager() {
    // Private constructor
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

    connectivityManager =
        (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager == null) {
      Log.e(TAG, "ConnectivityManager service not available");
      return;
    }

    // Check initial network state
    boolean hasNetwork = NetworkUtils.isNetworkAvailable(context);
    isNetworkAvailable.postValue(hasNetwork);

    // Register network callback
    registerNetworkCallback();

    Log.d(TAG, "ConnectivityManager initialized, initial network state: " + hasNetwork);
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
            mainHandler.post(() -> isNetworkAvailable.setValue(true));
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
