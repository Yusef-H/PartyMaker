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
import com.example.partymaker.utils.infrastructure.PerformanceMonitor;
import com.example.partymaker.ui.features.auxiliary.settings.ServerSettingsActivity;
import com.example.partymaker.utils.ui.feedback.NotificationManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.example.partymaker.utils.media.ImageOptimizationManager;

/** Application class for PartyMaker. Initializes repositories and other app-wide components. */
public class PartyApplication extends Application {
  private static final String TAG = "PartyApplication";
  private static PartyApplication instance;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;

    // Start performance monitoring
    PerformanceMonitor.startTiming("Application.onCreate");
    PerformanceMonitor.trackMemoryUsage("Application.onCreate.start");

    // Apply saved theme preference
    ServerSettingsActivity.applyThemeFromPreferences(this);
    Log.d(TAG, "Theme preference applied");

    // Initialize Firebase
    try {
      PerformanceMonitor.startTiming("Firebase.init");
      FirebaseApp.initializeApp(this);
      Log.d(TAG, "Firebase initialized successfully");
      
      // Initialize Firebase App Check for security
      initializeAppCheck();
      
      // Initialize Crashlytics
      initializeCrashlytics();

      // Initialize Firebase references
      DBRef.init();
      Log.d(TAG, "Firebase references initialized successfully");
      PerformanceMonitor.endTiming("Firebase.init");
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

    // Setup global exception handler
    setupGlobalExceptionHandler();
    
    // Optimize image loading configuration
    ImageOptimizationManager.optimizeGlideConfiguration(this);
    Log.d(TAG, "Image loading optimized");

    // End application initialization timing
    PerformanceMonitor.trackMemoryUsage("Application.onCreate.end");
    PerformanceMonitor.endTiming("Application.onCreate");
    
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
   * Initializes Firebase App Check for enhanced security.
   * Uses Play Integrity for production and Debug provider for testing.
   */
  private void initializeAppCheck() {
    try {
      FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
      
      if (BuildConfig.DEBUG) {
        // Use debug provider for testing
        Log.d(TAG, "Initializing App Check with Debug provider");
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance());
      } else {
        // Use Play Integrity for production
        Log.d(TAG, "Initializing App Check with Play Integrity provider");
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance());
      }
      
      Log.d(TAG, "Firebase App Check initialized successfully");
    } catch (Exception e) {
      Log.e(TAG, "Failed to initialize App Check - continuing without it", e);
      // App can still function without App Check, but with reduced security
    }
  }

  /**
   * Initializes Firebase Crashlytics for crash reporting.
   */
  private void initializeCrashlytics() {
    try {
      FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
      
      // Enable/disable based on build type
      crashlytics.setCrashlyticsCollectionEnabled(BuildConfig.ENABLE_CRASHLYTICS);
      
      if (BuildConfig.ENABLE_CRASHLYTICS) {
        Log.d(TAG, "Crashlytics enabled for crash reporting");
        
        // Set user properties
        crashlytics.setUserId("user_" + System.currentTimeMillis());
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME);
        crashlytics.setCustomKey("debug_mode", BuildConfig.DEBUG);
        
        // Test log (only in debug)
        if (BuildConfig.DEBUG) {
          crashlytics.log("PartyApplication initialized successfully");
        }
      } else {
        Log.d(TAG, "Crashlytics disabled for debug builds");
      }
      
    } catch (Exception e) {
      Log.e(TAG, "Failed to initialize Crashlytics", e);
    }
  }

  /**
   * Sets up global exception handler for uncaught exceptions.
   */
  private void setupGlobalExceptionHandler() {
    // Store the original handler before replacing it
    final Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        Log.e(TAG, "Uncaught exception in thread: " + thread.getName(), throwable);
        
        // Log crash information
        Log.e(TAG, "Crash details - Message: " + throwable.getMessage());
        Log.e(TAG, "Crash details - Cause: " + (throwable.getCause() != null ? throwable.getCause().toString() : "None"));
        
        // Send to Crashlytics if enabled
        try {
          if (BuildConfig.ENABLE_CRASHLYTICS) {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.recordException(throwable);
            crashlytics.log("Uncaught exception in thread: " + thread.getName());
          }
        } catch (Exception e) {
          Log.e(TAG, "Failed to report crash to Crashlytics", e);
        }
        
        // In debug builds, show additional information
        if (BuildConfig.DEBUG) {
          Log.e(TAG, "Stack trace: ", throwable);
          Log.e(TAG, "Thread state: " + thread.getState());
          Log.e(TAG, "Memory info: " + MemoryManager.getDetailedMemoryInfo());
        }
        
        // Attempt graceful cleanup
        try {
          MemoryManager.getInstance().emergencyCleanup();
        } catch (Exception e) {
          Log.e(TAG, "Error during emergency cleanup", e);
        }
        
        // Call the original handler to terminate the app
        if (originalHandler != null) {
          originalHandler.uncaughtException(thread, throwable);
        } else {
          // Fallback termination
          System.exit(1);
        }
      }
    });
    
    Log.d(TAG, "Global exception handler configured");
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
    // Also trim image cache memory
    ImageOptimizationManager.trimMemory(this, level);
  }
}
