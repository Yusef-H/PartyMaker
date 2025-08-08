package com.example.partymaker.data.firebase;

import android.content.Context;
import android.util.Log;
import com.example.partymaker.data.api.FirebaseServerClient;

// import com.google.firebase.database.DatabaseReference;

/**
 * Centralized manager for Firebase access operations. 
 * Handles server mode configuration and provides unified access to Firebase services.
 */
public class FirebaseAccessManager {
  private static final String TAG = "FirebaseAccessManager";
  
  // Configuration constants
  private static final boolean DEFAULT_SERVER_MODE_ENABLED = true;
  
  private final Context context;

  public FirebaseAccessManager(Context context) {
    this.context = context;
  }

  /**
   * Checks if server mode is enabled for Firebase operations.
   * 
   * @return true if server mode is enabled, false for direct Firebase access
   */
  public boolean isServerModeEnabled() {
    Log.d(TAG, "Server mode is enabled");
    return DEFAULT_SERVER_MODE_ENABLED;
  }

  /**
   * Gets the Firebase server client for groups operations.
   * 
   * @return Initialized FirebaseServerClient instance
   */
  public FirebaseServerClient getGroupsRef() {
    Log.d(TAG, "Using server mode for groups");
    return getInitializedServerClient();
  }

  /**
   * Gets the Firebase server client for users operations.
   * 
   * @return Initialized FirebaseServerClient instance
   */
  public FirebaseServerClient getUsersRef() {
    Log.d(TAG, "Using server mode for users");
    return getInitializedServerClient();
  }

  /**
   * Gets the Firebase server client for messages operations.
   * 
   * @return Initialized FirebaseServerClient instance
   */
  public FirebaseServerClient getMessagesRef() {
    Log.d(TAG, "Using server mode for messages");
    return getInitializedServerClient();
  }
  
  /**
   * Creates and initializes a FirebaseServerClient instance.
   * 
   * @return Initialized FirebaseServerClient
   */
  private FirebaseServerClient getInitializedServerClient() {
    FirebaseServerClient client = FirebaseServerClient.getInstance();
    client.initialize(context);
    return client;
  }
}
