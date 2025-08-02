package com.example.partymaker.utils.security.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Simple secure storage implementation using basic encryption For production use, consider using
 * EncryptedSharedPreferences from androidx.security.crypto
 */
public class SimpleSecureStorage {
  private static final String TAG = "SimpleSecureStorage";
  private static final String PREFS_NAME = "secure_prefs";
  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final String KEY_ALGORITHM = "AES";

  private final SharedPreferences prefs;
  private final SecretKeySpec secretKey;
  private final IvParameterSpec ivSpec;

  public SimpleSecureStorage(Context context) {
    this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

    // Generate a key based on package name and a salt
    String keySource = context.getPackageName() + "_secure_key_2024";
    byte[] keyBytes = generateKey(keySource);
    this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);

    // Fixed IV for simplicity (in production, use random IV per encryption)
    byte[] iv = new byte[16];
    System.arraycopy(keyBytes, 0, iv, 0, 16);
    this.ivSpec = new IvParameterSpec(iv);
  }

  /** Store encrypted string */
  public void putString(String key, String value) {
    if (value == null) {
      prefs.edit().remove(key).apply();
      return;
    }

    try {
      String encrypted = encrypt(value);
      prefs.edit().putString(key, encrypted).apply();
    } catch (Exception e) {
      Log.e(TAG, "Error encrypting data", e);
      // Fallback to plain storage in development
      prefs.edit().putString(key, value).apply();
    }
  }

  /** Retrieve and decrypt string */
  public String getString(String key, String defaultValue) {
    String encrypted = prefs.getString(key, null);
    if (encrypted == null) {
      return defaultValue;
    }

    try {
      return decrypt(encrypted);
    } catch (Exception e) {
      Log.e(TAG, "Error decrypting data", e);
      // Return as-is if decryption fails (might be plain text)
      return encrypted;
    }
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

  /** Generate a 256-bit key from string */
  private byte[] generateKey(String source) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(source.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      // Fallback to simple key
      byte[] key = new byte[32];
      byte[] sourceBytes = source.getBytes(StandardCharsets.UTF_8);
      System.arraycopy(sourceBytes, 0, key, 0, Math.min(sourceBytes.length, 32));
      return key;
    }
  }

  /** Encrypt string */
  private String encrypt(String value) throws Exception {
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
    byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
    return Base64.encodeToString(encrypted, Base64.NO_WRAP);
  }

  /** Decrypt string */
  private String decrypt(String encrypted) throws Exception {
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
    byte[] decoded = Base64.decode(encrypted, Base64.NO_WRAP);
    byte[] decrypted = cipher.doFinal(decoded);
    return new String(decrypted, StandardCharsets.UTF_8);
  }

  /** Get SharedPreferences.Editor for batch operations */
  public SharedPreferences.Editor edit() {
    return prefs.edit();
  }
}
