package com.example.partymaker;

import android.app.Application;
import android.util.Log;
import com.example.partymaker.data.api.ConnectivityManager;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.api.NetworkManager;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.infrastructure.system.MemoryManager;
import com.example.partymaker.ui.features.auxiliary.settings.ServerSettingsActivity;
import com.example.partymaker.utils.ui.feedback.NotificationManager;
import com.google.firebase.FirebaseApp;

/** Application class for PartyMaker. Initializes repositories and other app-wide components. */
public class PartyApplication extends Application {
  private static final String TAG = "PartyApplication";

  @Override
  public void onCreate() {
    super.onCreate();

    // Apply saved theme preference
    ServerSettingsActivity.applyThemeFromPreferences(this);
    Log.d(TAG, "Theme preference applied");

    // Initialize Firebase
    try {
      FirebaseApp.initializeApp(this);
      Log.d(TAG, "Firebase initialized successfully");

      // Initialize Firebase references
      DBRef.init();
      Log.d(TAG, "Firebase references initialized successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error initializing Firebase", e);
    }

    // Initialize notification channels
    NotificationManager.createNotificationChannels(this);
    NotificationManager.subscribeToGlobalAnnouncements();

    // Initialize network manager
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.initialize(getApplicationContext());

    // Initialize ConnectivityManager
    ConnectivityManager.getInstance().init(getApplicationContext());
    Log.d(TAG, "ConnectivityManager initialized successfully");

    // Database will be recreated automatically by Room if schema doesn't match

    // Initialize FirebaseServerClient
    try {
      FirebaseServerClient.getInstance().initialize(this);
      Log.d(TAG, "FirebaseServerClient initialized successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error initializing FirebaseServerClient", e);
    }

    // Initialize repositories
    initializeRepositories();

    // Log memory info
    Log.d(TAG, "Initial memory usage: " + MemoryManager.getDetailedMemoryInfo());

    Log.d(TAG, "Application initialized");
  }

  /**
   * Initializes all repositories with application context. This enables caching and offline
   * support.
   */
  private void initializeRepositories() {
    // Initialize Group Repository
    GroupRepository groupRepository = GroupRepository.getInstance();
    groupRepository.initialize(getApplicationContext());

    // Initialize User Repository
    UserRepository userRepository = UserRepository.getInstance();
    userRepository.initialize(getApplicationContext());

    // Add more repositories here as needed

    Log.d(TAG, "Repositories initialized with application context");
  }

  @Override
  public void onTerminate() {
    super.onTerminate();

    // Release resources
    NetworkManager.getInstance().release();

    // Unregister ConnectivityManager callbacks
    ConnectivityManager.getInstance().unregisterNetworkCallback();

    // Clean up FirebaseServerClient
    try {
      FirebaseServerClient.getInstance().cleanup();
      Log.d(TAG, "FirebaseServerClient cleaned up successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error cleaning up FirebaseServerClient", e);
    }

    Log.d(TAG, "Application terminated");
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();

    // Perform memory cleanup
    MemoryManager.performMemoryCleanup(this);

    Log.d(TAG, "Low memory cleanup performed");
  }
}
