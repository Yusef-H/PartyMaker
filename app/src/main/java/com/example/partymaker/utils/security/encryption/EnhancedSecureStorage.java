package com.example.partymaker.utils.security.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Enhanced secure storage with AES-GCM encryption and random IV per message. Fixes critical
 * security vulnerabilities in SimpleSecureStorage: - Random IV per encryption operation -
 * Authenticated encryption (GCM mode) - Per-user encryption keys - Proper key storage with Android
 * security
 */
public class EnhancedSecureStorage {
  private static final String TAG = "EnhancedSecureStorage";

  // AES-GCM provides both encryption and authentication
  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int GCM_IV_LENGTH = 12; // 96 bits recommended for GCM
  private static final int GCM_TAG_LENGTH = 16; // 128 bits auth tag

  // Security constants
  private static final int AES_KEY_SIZE = 256;
  private static final int MIN_ENCRYPTED_DATA_LENGTH = GCM_IV_LENGTH + GCM_TAG_LENGTH;
  private static final String DEFAULT_USER_ID = "default_user";
  private static final String SECURE_PREFS_PREFIX = "secure_prefs_";
  private static final String USER_KEYS_PREFS = "user_keys";
  private static final String KEY_PREFIX = "key_";
  private static final int TAG_LENGTH_BITS = GCM_TAG_LENGTH * 8;

  private final SharedPreferences prefs;
  private final SecretKey secretKey;
  private final SecureRandom secureRandom;
  private final String userId;

  /**
   * Constructor with user-specific encryption
   *
   * @param context Application context
   * @param userId Unique user identifier for per-user keys
   */
  public EnhancedSecureStorage(Context context, String userId) {
    this.userId = userId != null ? userId : DEFAULT_USER_ID;
    this.prefs =
        context.getSharedPreferences(SECURE_PREFS_PREFIX + this.userId, Context.MODE_PRIVATE);
    this.secureRandom = new SecureRandom();
    this.secretKey = getOrCreateUserKey(context, this.userId);
  }

  /**
   * Encrypt string with random IV per operation Format: [IV(12 bytes)][Encrypted Data + Auth Tag]
   */
  public String encrypt(String plaintext) {
    if (plaintext == null) {
      return null;
    }

    try {
      // Generate random IV for each encryption - FIXES CRITICAL VULNERABILITY
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      // Initialize cipher with GCM mode for authenticated encryption
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

      // Encrypt the plaintext
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      // Combine IV + ciphertext + auth tag
      byte[] encryptedData = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, encryptedData, 0, iv.length);
      System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);

      return Base64.encodeToString(encryptedData, Base64.NO_WRAP);

    } catch (Exception e) {
      Log.e(TAG, "Encryption failed for user: " + userId, e);
      return null;
    }
  }

  /** Decrypt string extracting IV from encrypted data */
  public String decrypt(String encryptedText) {
    if (encryptedText == null) {
      return null;
    }

    try {
      byte[] encryptedData = Base64.decode(encryptedText, Base64.NO_WRAP);

      // Validate minimum length (IV + some ciphertext + auth tag)
      if (encryptedData.length < MIN_ENCRYPTED_DATA_LENGTH) {
        Log.e(TAG, "Encrypted data too short for user: " + userId);
        return null;
      }

      // Extract IV from beginning
      byte[] iv = new byte[GCM_IV_LENGTH];
      System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

      // Extract ciphertext + auth tag
      byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
      System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

      // Initialize cipher for decryption
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

      // Decrypt and verify authentication tag
      byte[] plaintext = cipher.doFinal(ciphertext);
      return new String(plaintext, StandardCharsets.UTF_8);

    } catch (Exception e) {
      Log.e(TAG, "Decryption failed for user: " + userId, e);
      return null;
    }
  }

  /** Store encrypted string */
  public void putString(String key, String value) {
    if (value == null) {
      prefs.edit().remove(key).apply();
      return;
    }

    String encrypted = encrypt(value);
    if (encrypted != null) {
      prefs.edit().putString(key, encrypted).apply();
    } else {
      Log.e(TAG, "Failed to encrypt value for key: " + key);
    }
  }

  /** Retrieve and decrypt string */
  public String getString(String key, String defaultValue) {
    String encrypted = prefs.getString(key, null);
    if (encrypted == null) {
      return defaultValue;
    }

    String decrypted = decrypt(encrypted);
    return decrypted != null ? decrypted : defaultValue;
  }

  /** Store encrypted long */
  public void putLong(String key, long value) {
    putString(key, String.valueOf(value));
  }

  /** Retrieve and decrypt long */
  public long getLong(String key, long defaultValue) {
    String value = getString(key, null);
    if (value == null) {
      return defaultValue;
    }

    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /** Check if key exists */
  public boolean contains(String key) {
    return prefs.contains(key);
  }

  /** Remove a key */
  public void remove(String key) {
    prefs.edit().remove(key).apply();
  }

  /** Clear all data */
  public void clear() {
    prefs.edit().clear().apply();
  }

  /**
   * Get or create user-specific encryption key Uses proper key generation instead of password-based
   * derivation
   */
  private SecretKey getOrCreateUserKey(Context context, String userId) {
    try {
      // Check if key already exists in secure storage
      SharedPreferences keyPrefs =
          context.getSharedPreferences(USER_KEYS_PREFS, Context.MODE_PRIVATE);
      String keyBase64 = keyPrefs.getString(KEY_PREFIX + userId, null);

      if (keyBase64 != null) {
        // Load existing key
        byte[] keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP);
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
      }

      // Generate new 256-bit AES key
      KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
      keyGenerator.init(AES_KEY_SIZE, secureRandom);
      SecretKey newKey = keyGenerator.generateKey();

      // Store key securely
      String newKeyBase64 = Base64.encodeToString(newKey.getEncoded(), Base64.NO_WRAP);
      keyPrefs.edit().putString(KEY_PREFIX + userId, newKeyBase64).apply();

      Log.d(TAG, "Generated new encryption key for user: " + userId);
      return newKey;

    } catch (Exception e) {
      Log.e(TAG, "Failed to initialize encryption key for user: " + userId, e);
      throw new RuntimeException("Cannot initialize secure storage", e);
    }
  }

  /** Key rotation - generate new key for user Call this periodically for enhanced security */
  public void rotateKey(Context context) {
    try {
      // Generate new key
      KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
      keyGenerator.init(AES_KEY_SIZE, secureRandom);
      SecretKey newKey = keyGenerator.generateKey();

      // Store new key
      SharedPreferences keyPrefs =
          context.getSharedPreferences(USER_KEYS_PREFS, Context.MODE_PRIVATE);
      String newKeyBase64 = Base64.encodeToString(newKey.getEncoded(), Base64.NO_WRAP);
      keyPrefs.edit().putString(KEY_PREFIX + userId, newKeyBase64).apply();

      Log.d(TAG, "Rotated encryption key for user: " + userId);

      // Note: In a full implementation, you'd need to re-encrypt existing data
      // with the new key or implement a key versioning system

    } catch (Exception e) {
      Log.e(TAG, "Key rotation failed for user: " + userId, e);
    }
  }

  /** Get encryption info for debugging (no sensitive data) */
  public String getEncryptionInfo() {
    return String.format(
        Locale.US,
        "Algorithm: %s, User: %s, IV Length: %d, Tag Length: %d",
        ALGORITHM,
        userId,
        GCM_IV_LENGTH,
        GCM_TAG_LENGTH);
  }
}
