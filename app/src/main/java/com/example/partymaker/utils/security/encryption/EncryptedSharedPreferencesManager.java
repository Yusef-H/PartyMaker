package com.example.partymaker.utils.security.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * Enhanced encrypted SharedPreferences manager using AndroidX Security library. Provides secure
 * storage for sensitive application data.
 */
public class EncryptedSharedPreferencesManager {
  private static final String TAG = "EncryptedPrefsManager";
  private static final String PREFS_FILE_NAME = "encrypted_shared_prefs";
  private static final String FALLBACK_PREFS_NAME = "fallback_prefs";
  
  // Encryption constants
  private static final int AES_KEY_SIZE = 256;
  private static final boolean REQUIRE_USER_AUTH = false;
  private static final boolean USE_STRONGBOX = true;

  private static EncryptedSharedPreferencesManager instance;
  private SharedPreferences encryptedPrefs;
  private SharedPreferences fallbackPrefs;
  private final Context context;
  private boolean isEncryptionAvailable = false;

  private EncryptedSharedPreferencesManager(@NonNull Context context) {
    this.context = context.getApplicationContext();
    initializeEncryptedPreferences();
  }

  public static synchronized EncryptedSharedPreferencesManager getInstance(
      @NonNull Context context) {
    if (instance == null) {
      instance = new EncryptedSharedPreferencesManager(context);
    }
    return instance;
  }

  /** Initialize encrypted SharedPreferences with proper error handling */
  private void initializeEncryptedPreferences() {
    try {
      // Create or retrieve master key for encryption
      MasterKey masterKey = createMasterKey();

      // Create encrypted SharedPreferences
      encryptedPrefs =
          EncryptedSharedPreferences.create(
              context,
              PREFS_FILE_NAME,
              masterKey,
              EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
              EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

      isEncryptionAvailable = true;
      Log.i(TAG, "Encrypted SharedPreferences initialized successfully");

    } catch (GeneralSecurityException | IOException e) {
      Log.e(TAG, "Failed to initialize encrypted SharedPreferences", e);
      isEncryptionAvailable = false;

      // Fallback to regular SharedPreferences with warning
      initializeFallbackPreferences();
    }
  }

  /** Creates a secure master key for encryption */
  private MasterKey createMasterKey() throws GeneralSecurityException, IOException {
    KeyGenParameterSpec spec =
        new KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE)
            .setUserAuthenticationRequired(REQUIRE_USER_AUTH) // Set to true if you want user auth
            .build();

    return new MasterKey.Builder(context)
        .setKeyGenParameterSpec(spec)
        .setRequestStrongBoxBacked(USE_STRONGBOX) // Use hardware security module if available
        .build();
  }

  /** Initialize fallback preferences when encryption is not available */
  private void initializeFallbackPreferences() {
    fallbackPrefs = context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE);
    Log.w(TAG, "Using fallback unencrypted SharedPreferences - DATA IS NOT ENCRYPTED!");

    // Log security event
    SecurityAuditLogger.logSecurityEvent(
        SecurityAuditLogger.EventType.ENCRYPTION_FALLBACK,
        "Encrypted SharedPreferences not available, using fallback");
  }

  /** Get the appropriate SharedPreferences instance */
  private SharedPreferences getPreferences() {
    return isEncryptionAvailable ? encryptedPrefs : fallbackPrefs;
  }

  // String operations
  public void putString(@NonNull String key, @Nullable String value) {
    try {
      getPreferences().edit().putString(key, value).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error storing string for key: " + key, e);
      handleStorageError(key, e);
    }
  }

  @Nullable
  public String getString(@NonNull String key, @Nullable String defaultValue) {
    try {
      return getPreferences().getString(key, defaultValue);
    } catch (Exception e) {
      Log.e(TAG, "Error retrieving string for key: " + key, e);
      handleRetrievalError(key, e);
      return defaultValue;
    }
  }

  // Boolean operations
  public void putBoolean(@NonNull String key, boolean value) {
    try {
      getPreferences().edit().putBoolean(key, value).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error storing boolean for key: " + key, e);
      handleStorageError(key, e);
    }
  }

  public boolean getBoolean(@NonNull String key, boolean defaultValue) {
    try {
      return getPreferences().getBoolean(key, defaultValue);
    } catch (Exception e) {
      Log.e(TAG, "Error retrieving boolean for key: " + key, e);
      handleRetrievalError(key, e);
      return defaultValue;
    }
  }

  // Integer operations
  public void putInt(@NonNull String key, int value) {
    try {
      getPreferences().edit().putInt(key, value).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error storing int for key: " + key, e);
      handleStorageError(key, e);
    }
  }

  public int getInt(@NonNull String key, int defaultValue) {
    try {
      return getPreferences().getInt(key, defaultValue);
    } catch (Exception e) {
      Log.e(TAG, "Error retrieving int for key: " + key, e);
      handleRetrievalError(key, e);
      return defaultValue;
    }
  }

  // Long operations
  public void putLong(@NonNull String key, long value) {
    try {
      getPreferences().edit().putLong(key, value).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error storing long for key: " + key, e);
      handleStorageError(key, e);
    }
  }

  public long getLong(@NonNull String key, long defaultValue) {
    try {
      return getPreferences().getLong(key, defaultValue);
    } catch (Exception e) {
      Log.e(TAG, "Error retrieving long for key: " + key, e);
      handleRetrievalError(key, e);
      return defaultValue;
    }
  }

  // Float operations
  public void putFloat(@NonNull String key, float value) {
    try {
      getPreferences().edit().putFloat(key, value).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error storing float for key: " + key, e);
      handleStorageError(key, e);
    }
  }

  public float getFloat(@NonNull String key, float defaultValue) {
    try {
      return getPreferences().getFloat(key, defaultValue);
    } catch (Exception e) {
      Log.e(TAG, "Error retrieving float for key: " + key, e);
      handleRetrievalError(key, e);
      return defaultValue;
    }
  }

  // String Set operations
  public void putStringSet(@NonNull String key, @Nullable Set<String> values) {
    try {
      getPreferences().edit().putStringSet(key, values).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error storing string set for key: " + key, e);
      handleStorageError(key, e);
    }
  }

  @Nullable
  public Set<String> getStringSet(@NonNull String key, @Nullable Set<String> defaultValues) {
    try {
      return getPreferences().getStringSet(key, defaultValues);
    } catch (Exception e) {
      Log.e(TAG, "Error retrieving string set for key: " + key, e);
      handleRetrievalError(key, e);
      return defaultValues;
    }
  }

  // Utility methods
  public boolean contains(@NonNull String key) {
    try {
      return getPreferences().contains(key);
    } catch (Exception e) {
      Log.e(TAG, "Error checking key existence: " + key, e);
      return false;
    }
  }

  public void remove(@NonNull String key) {
    try {
      getPreferences().edit().remove(key).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error removing key: " + key, e);
      handleStorageError(key, e);
    }
  }

  public void clear() {
    try {
      getPreferences().edit().clear().apply();
      Log.i(TAG, "All encrypted preferences cleared");
    } catch (Exception e) {
      Log.e(TAG, "Error clearing preferences", e);
    }
  }

  /** Batch operations for better performance */
  public Editor edit() {
    return new Editor(getPreferences().edit());
  }

  public static class Editor {
    private final SharedPreferences.Editor editor;

    private Editor(SharedPreferences.Editor editor) {
      this.editor = editor;
    }

    public Editor putString(String key, String value) {
      editor.putString(key, value);
      return this;
    }

    public Editor putBoolean(String key, boolean value) {
      editor.putBoolean(key, value);
      return this;
    }

    public Editor putInt(String key, int value) {
      editor.putInt(key, value);
      return this;
    }

    public Editor putLong(String key, long value) {
      editor.putLong(key, value);
      return this;
    }

    public Editor putFloat(String key, float value) {
      editor.putFloat(key, value);
      return this;
    }

    public Editor putStringSet(String key, Set<String> values) {
      editor.putStringSet(key, values);
      return this;
    }

    public Editor remove(String key) {
      editor.remove(key);
      return this;
    }

    public Editor clear() {
      editor.clear();
      return this;
    }

    public void apply() {
      editor.apply();
    }

    public boolean commit() {
      return editor.commit();
    }
  }

  /** Checks if encryption is available and working */
  public boolean isEncryptionAvailable() {
    return isEncryptionAvailable;
  }

  /** Migrates data from regular SharedPreferences to encrypted ones */
  public void migrateFromRegularPreferences(@NonNull String regularPrefsName) {
    if (!isEncryptionAvailable) {
      Log.w(TAG, "Cannot migrate - encryption not available");
      return;
    }

    try {
      SharedPreferences regularPrefs =
          context.getSharedPreferences(regularPrefsName, Context.MODE_PRIVATE);
      Editor editor = edit();

      // Migrate all data
      for (String key : regularPrefs.getAll().keySet()) {
        Object value = regularPrefs.getAll().get(key);
        if (value instanceof String) {
          editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
          editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
          editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
          editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
          editor.putFloat(key, (Float) value);
        } else if (value instanceof Set) {
          editor.putStringSet(key, (Set<String>) value);
        }
      }

      editor.apply();

      // Clear old preferences after successful migration
      regularPrefs.edit().clear().apply();

      Log.i(TAG, "Successfully migrated preferences from: " + regularPrefsName);

    } catch (Exception e) {
      Log.e(TAG, "Error migrating preferences", e);
    }
  }

  /** Error handling for storage operations */
  private void handleStorageError(String key, Exception e) {
    SecurityAuditLogger.logSecurityEvent(
        SecurityAuditLogger.EventType.STORAGE_ERROR,
        "Failed to store data for key: " + key + ", Error: " + e.getMessage());
  }

  /** Error handling for retrieval operations */
  private void handleRetrievalError(String key, Exception e) {
    SecurityAuditLogger.logSecurityEvent(
        SecurityAuditLogger.EventType.RETRIEVAL_ERROR,
        "Failed to retrieve data for key: " + key + ", Error: " + e.getMessage());
  }

  /** Security audit logger helper class */
  private static class SecurityAuditLogger {
    enum EventType {
      ENCRYPTION_FALLBACK,
      STORAGE_ERROR,
      RETRIEVAL_ERROR
    }

    static void logSecurityEvent(EventType eventType, String details) {
      Log.w(TAG, "SECURITY EVENT - " + eventType + ": " + details);
      // In production, send to security monitoring system
    }
  }
}
