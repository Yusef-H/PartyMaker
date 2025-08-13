package com.example.partymaker;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.partymaker.BuildConfig;
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
  private static PartyApplication instance;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;

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

    // Initialize memory management
    MemoryManager.getInstance();

    // Setup memory monitoring in debug builds
    if (BuildConfig.DEBUG) {
      setupMemoryMonitoring();
    }

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

  /**
   * Gets the singleton instance of the application.
   */
  public static PartyApplication getInstance() {
    return instance;
  }

  /**
   * Sets up memory monitoring for debug builds.
   */
  private void setupMemoryMonitoring() {
    // LeakCanary is automatically initialized

    // Additional memory monitoring
    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Log.d("MemoryMonitor", "Activity created: " + activity.getClass().getSimpleName());
        MemoryManager.getInstance().logMemoryStats();
      }

      @Override
      public void onActivityDestroyed(@NonNull Activity activity) {
        Log.d("MemoryMonitor", "Activity destroyed: " + activity.getClass().getSimpleName());
        MemoryManager.getInstance().logMemoryStats();
      }

      // Other lifecycle methods...
      @Override public void onActivityStarted(@NonNull Activity activity) {}
      @Override public void onActivityResumed(@NonNull Activity activity) {}
      @Override public void onActivityPaused(@NonNull Activity activity) {}
      @Override public void onActivityStopped(@NonNull Activity activity) {}
      @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    });
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();

    // Perform memory cleanup using enhanced memory manager
    MemoryManager.getInstance().emergencyCleanup();

    Log.d(TAG, "Low memory cleanup performed");
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    MemoryManager.getInstance().emergencyCleanup();
  }
}
