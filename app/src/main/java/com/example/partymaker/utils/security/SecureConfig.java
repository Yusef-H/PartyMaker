package com.example.partymaker.utils.security;

import android.content.Context;
import android.util.Log;

import android.content.SharedPreferences;

import com.example.partymaker.BuildConfig;

/**
 * Secure configuration manager for API keys and sensitive configuration
 */
public class SecureConfig {
    private static final String TAG = "SecureConfig";
    private static final String PREFS_FILE_NAME = "secure_prefs";
    
    // Keys for secure storage
    private static final String KEY_OPENAI_API = "openai_api_key";
    private static final String KEY_GOOGLE_MAPS_API = "google_maps_api_key";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_DEFAULT_SERVER_URL = "default_server_url";
    
    private static SecureConfig instance;
    private final SharedPreferences prefs;
    
    private SecureConfig(Context context) {
        // For now, use regular SharedPreferences with MODE_PRIVATE
        // In production, you should use EncryptedSharedPreferences when available
        prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        
        // Initialize with BuildConfig values if available
        initializeDefaults();
    }
    
    public static synchronized SecureConfig getInstance(Context context) {
        if (instance == null) {
            instance = new SecureConfig(context.getApplicationContext());
        }
        return instance;
    }
    
    private void initializeDefaults() {
        // Set default server URL if not already set
        if (!prefs.contains(KEY_DEFAULT_SERVER_URL)) {
            prefs.edit()
                    .putString(KEY_DEFAULT_SERVER_URL, "https://partymaker.onrender.com")
                    .apply();
        }
        
        // Initialize server URL with default if not set
        if (!prefs.contains(KEY_SERVER_URL)) {
            prefs.edit()
                    .putString(KEY_SERVER_URL, getDefaultServerUrl())
                    .apply();
        }
    }
    
    /**
     * Get OpenAI API key
     * @return API key or null if not set
     */
    public String getOpenAiApiKey() {
        try {
            // First check BuildConfig (from local.properties or CI secrets)
            String apiKey = BuildConfig.OPENAI_API_KEY;
            if (apiKey != null && !apiKey.isEmpty()) {
                return apiKey;
            }
        } catch (Exception e) {
            // BuildConfig field might not exist yet
            Log.d(TAG, "BuildConfig.OPENAI_API_KEY not available");
        }
        // Fallback to storage
        return prefs.getString(KEY_OPENAI_API, null);
    }
    
    /**
     * Set OpenAI API key
     */
    public void setOpenAiApiKey(String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            prefs.edit().putString(KEY_OPENAI_API, apiKey).apply();
        }
    }
    
    /**
     * Get Google Maps API key
     * @return API key or placeholder if not set
     */
    public String getGoogleMapsApiKey() {
        try {
            // First check BuildConfig
            String apiKey = BuildConfig.MAPS_API_KEY;
            if (apiKey != null && !apiKey.isEmpty()) {
                return apiKey;
            }
        } catch (Exception e) {
            // BuildConfig field might not exist yet
            Log.d(TAG, "BuildConfig.MAPS_API_KEY not available");
        }
        // Fallback to storage
        String key = prefs.getString(KEY_GOOGLE_MAPS_API, null);
        return key != null ? key : "YOUR_API_KEY_HERE";
    }
    
    /**
     * Set Google Maps API key
     */
    public void setGoogleMapsApiKey(String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            prefs.edit().putString(KEY_GOOGLE_MAPS_API, apiKey).apply();
        }
    }
    
    /**
     * Get server URL
     */
    public String getServerUrl() {
        return prefs.getString(KEY_SERVER_URL, getDefaultServerUrl());
    }
    
    /**
     * Set server URL
     */
    public void setServerUrl(String url) {
        if (url != null && !url.isEmpty()) {
            prefs.edit().putString(KEY_SERVER_URL, url).apply();
        }
    }
    
    /**
     * Get default server URL
     */
    public String getDefaultServerUrl() {
        return prefs.getString(KEY_DEFAULT_SERVER_URL, "https://partymaker.onrender.com");
    }
    
    /**
     * Check if OpenAI API is configured
     */
    public boolean isOpenAiConfigured() {
        String key = getOpenAiApiKey();
        return key != null && !key.isEmpty() && !key.equals("YOUR_API_KEY_HERE");
    }
    
    /**
     * Check if Google Maps is configured
     */
    public boolean isGoogleMapsConfigured() {
        String key = getGoogleMapsApiKey();
        return key != null && !key.isEmpty() && !key.equals("YOUR_API_KEY_HERE");
    }
    
    /**
     * Clear all secure configuration
     */
    public void clearAll() {
        prefs.edit().clear().apply();
        initializeDefaults();
    }
}