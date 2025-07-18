package com.example.partymaker;

import android.app.Application;
import android.util.Log;

import com.example.partymaker.data.api.ConnectivityManager;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.google.firebase.FirebaseApp;
import com.example.partymaker.utilities.NotificationHelper;

/**
 * Main Application class for PartyMaker.
 * Initializes Firebase and other app-wide components.
 */
public class PartyMakerApplication extends Application {
    private static final String TAG = "PartyMakerApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
        
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this);
        
        // Subscribe to global announcements
        NotificationHelper.subscribeToGlobalAnnouncements();
        
        // Initialize Firebase references
        try {
            DBRef.init();
            Log.d(TAG, "Firebase references initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase references", e);
        }
        
        // Initialize FirebaseServerClient
        try {
            FirebaseServerClient.getInstance().initialize(this);
            Log.d(TAG, "FirebaseServerClient initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FirebaseServerClient", e);
        }
        
        // Initialize ConnectivityManager
        try {
            ConnectivityManager.getInstance().init(this);
            Log.d(TAG, "ConnectivityManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ConnectivityManager", e);
        }
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // Clean up resources
        try {
            FirebaseServerClient.getInstance().cleanup();
            Log.d(TAG, "FirebaseServerClient cleaned up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up FirebaseServerClient", e);
        }
        
        // Clean up ConnectivityManager
        try {
            ConnectivityManager.getInstance().unregisterNetworkCallback();
            Log.d(TAG, "ConnectivityManager cleaned up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up ConnectivityManager", e);
        }
    }
}
