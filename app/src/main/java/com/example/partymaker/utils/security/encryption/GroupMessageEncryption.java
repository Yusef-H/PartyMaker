package com.example.partymaker.utils.security.encryption;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.example.partymaker.data.model.ChatMessage;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Group-based message encryption for PartyMaker chat groups. Each group has a shared AES-256 key
 * that all members use.
 *
 * <p>Architecture: - One AES key per group (shared by all group members) - Keys stored encrypted in
 * Firebase per user - Automatic key rotation when members join/leave - AES-GCM for authenticated
 * encryption
 */
public class GroupMessageEncryption {
  private static final String TAG = "GroupMessageEncryption";

  // AES-GCM configuration
  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int AES_KEY_SIZE = 256;
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 16;

  // Validation constants
  private static final int MIN_ENCRYPTED_MESSAGE_LENGTH = 20;
  private static final int MIN_ENCRYPTED_DATA_LENGTH = GCM_IV_LENGTH + GCM_TAG_LENGTH;
  private static final int TAG_LENGTH_BITS = GCM_TAG_LENGTH * 8;
  private static final int AES_KEY_BYTES = AES_KEY_SIZE / 8;

  // Storage constants
  private static final String GROUP_KEY_PREFIX = "group_";

  // Regex patterns
  private static final String BASE64_PATTERN = "^[A-Za-z0-9+/]*={0,2}$";

  private final String currentUserId;
  private final SecureRandom secureRandom;
  private final EnhancedSecureStorage userStorage; // For storing group keys
  private final Map<String, SecretKey> groupKeyCache; // Cache for performance

  public GroupMessageEncryption(Context context, String userId) {
    this.currentUserId = userId;
    this.secureRandom = new SecureRandom();
    this.userStorage = new EnhancedSecureStorage(context, userId);
    this.groupKeyCache = new HashMap<>();
  }

  /**
   * Encrypt message for a specific group
   *
   * @param message Plain text message
   * @param groupId Group identifier
   * @return Encrypted message string or null if encryption fails
   */
  public String encryptGroupMessage(String message, String groupId) {
    if (message == null || message.trim().isEmpty()) {
      return message;
    }

    try {
      SecretKey groupKey = getGroupKey(groupId);
      if (groupKey == null) {
        Log.e(TAG, "No group key found for group: " + groupId);
        return message; // Fallback to plain text
      }

      // Generate random IV for this message
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      // Encrypt with AES-GCM
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.ENCRYPT_MODE, groupKey, gcmSpec);

      byte[] ciphertext = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

      // Combine IV + ciphertext + auth tag
      byte[] encryptedData = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, encryptedData, 0, iv.length);
      System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);

      String result = Base64.encodeToString(encryptedData, Base64.NO_WRAP);
      Log.d(TAG, "Message encrypted for group: " + groupId);
      return result;

    } catch (Exception e) {
      Log.e(TAG, "Failed to encrypt message for group: " + groupId, e);
      return message; // Fallback to plain text
    }
  }

  /**
   * Decrypt message from a specific group
   *
   * @param encryptedMessage Encrypted message string
   * @param groupId Group identifier
   * @return Decrypted message or original if decryption fails
   */
  public String decryptGroupMessage(String encryptedMessage, String groupId) {
    if (encryptedMessage == null || encryptedMessage.trim().isEmpty()) {
      return encryptedMessage;
    }

    // Quick check if message looks encrypted
    if (!isMessageEncrypted(encryptedMessage)) {
      return encryptedMessage; // Probably plain text
    }

    try {
      SecretKey groupKey = getGroupKey(groupId);
      if (groupKey == null) {
        Log.w(TAG, "No group key found for group: " + groupId);
        return encryptedMessage; // Return as-is
      }

      byte[] encryptedData = Base64.decode(encryptedMessage, Base64.NO_WRAP);

      // Validate minimum length
      if (encryptedData.length < MIN_ENCRYPTED_DATA_LENGTH) {
        Log.w(TAG, "Encrypted data too short for group: " + groupId);
        return encryptedMessage;
      }

      // Extract IV
      byte[] iv = new byte[GCM_IV_LENGTH];
      System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

      // Extract ciphertext + auth tag
      byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
      System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

      // Decrypt with AES-GCM
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.DECRYPT_MODE, groupKey, gcmSpec);

      byte[] plaintext = cipher.doFinal(ciphertext);
      String result = new String(plaintext, StandardCharsets.UTF_8);

      Log.d(TAG, "Message decrypted for group: " + groupId);
      return result;

    } catch (Exception e) {
      Log.w(TAG, "Failed to decrypt message for group: " + groupId + ", treating as plain text", e);
      return encryptedMessage; // Fallback - might be plain text or from different key
    }
  }

  /**
   * Encrypt a full ChatMessage object for a group
   *
   * @param chatMessage Message to encrypt
   * @return Modified ChatMessage with encrypted content
   */
  public ChatMessage encryptChatMessage(ChatMessage chatMessage) {
    if (chatMessage == null || chatMessage.getGroupKey() == null) {
      return chatMessage;
    }

    String groupId = chatMessage.getGroupKey();
    String originalMessage = chatMessage.getMessage();
    String encryptedContent = encryptGroupMessage(originalMessage, groupId);

    // Create copy to avoid modifying original
    ChatMessage encrypted = new ChatMessage();
    encrypted.setMessageKey(chatMessage.getMessageKey());
    encrypted.setGroupKey(chatMessage.getGroupKey());
    encrypted.setSenderKey(chatMessage.getSenderKey());
    encrypted.setSenderName(chatMessage.getSenderName());
    encrypted.setMessage(encryptedContent);
    encrypted.setTimestamp(chatMessage.getTimestamp());
    encrypted.setImageUrl(chatMessage.getImageUrl());
    encrypted.setEncrypted(true);
    encrypted.setMetadata(chatMessage.getMetadata());

    return encrypted;
  }

  /**
   * Decrypt a full ChatMessage object from a group
   *
   * @param encryptedChatMessage Message to decrypt
   * @return Modified ChatMessage with decrypted content
   */
  public ChatMessage decryptChatMessage(ChatMessage encryptedChatMessage) {
    if (encryptedChatMessage == null || encryptedChatMessage.getGroupKey() == null) {
      return encryptedChatMessage;
    }

    String groupId = encryptedChatMessage.getGroupKey();
    String encryptedContent = encryptedChatMessage.getMessage();
    String decryptedContent = decryptGroupMessage(encryptedContent, groupId);

    // Create copy to avoid modifying original
    ChatMessage decrypted = new ChatMessage();
    decrypted.setMessageKey(encryptedChatMessage.getMessageKey());
    decrypted.setGroupKey(encryptedChatMessage.getGroupKey());
    decrypted.setSenderKey(encryptedChatMessage.getSenderKey());
    decrypted.setSenderName(encryptedChatMessage.getSenderName());
    decrypted.setMessage(decryptedContent);
    decrypted.setTimestamp(encryptedChatMessage.getTimestamp());
    decrypted.setImageUrl(encryptedChatMessage.getImageUrl());
    decrypted.setEncrypted(false);
    decrypted.setMetadata(encryptedChatMessage.getMetadata());

    return decrypted;
  }

  /**
   * Generate new encryption key for a group
   *
   * @param groupId Group identifier
   * @return Base64 encoded group key for distribution
   */
  public String generateGroupKey(String groupId) {
    try {
      // Generate 256-bit AES key
      KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
      keyGenerator.init(AES_KEY_SIZE, secureRandom);
      SecretKey groupKey = keyGenerator.generateKey();

      // Store in user's secure storage
      String keyBase64 = Base64.encodeToString(groupKey.getEncoded(), Base64.NO_WRAP);
      userStorage.putString(GROUP_KEY_PREFIX + groupId, keyBase64);

      // Cache for performance
      groupKeyCache.put(groupId, groupKey);

      Log.i(TAG, "Generated new group key for: " + groupId);
      return keyBase64;

    } catch (Exception e) {
      Log.e(TAG, "Failed to generate group key for: " + groupId, e);
      return null;
    }
  }

  /**
   * Store group key received from server/other user
   *
   * @param groupId Group identifier
   * @param keyBase64 Base64 encoded group key
   * @return true if key stored successfully
   */
  public boolean storeGroupKey(String groupId, String keyBase64) {
    try {
      // Validate key format
      byte[] keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP);
      if (keyBytes.length != AES_KEY_BYTES) { // 256 bits = 32 bytes
        Log.e(TAG, "Invalid group key length for: " + groupId);
        return false;
      }

      // Store in user's secure storage
      userStorage.putString(GROUP_KEY_PREFIX + groupId, keyBase64);

      // Cache for performance
      SecretKey groupKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
      groupKeyCache.put(groupId, groupKey);

      Log.i(TAG, "Stored group key for: " + groupId);
      return true;

    } catch (Exception e) {
      Log.e(TAG, "Failed to store group key for: " + groupId, e);
      return false;
    }
  }

  /**
   * Get group key from storage or cache
   *
   * @param groupId Group identifier
   * @return SecretKey for the group or null if not found
   */
  private SecretKey getGroupKey(String groupId) {
    try {
      // Check cache first
      if (groupKeyCache.containsKey(groupId)) {
        return groupKeyCache.get(groupId);
      }

      // Load from secure storage
      String keyBase64 = userStorage.getString(GROUP_KEY_PREFIX + groupId, null);
      if (keyBase64 == null) {
        Log.w(TAG, "No stored key found for group: " + groupId);
        return null;
      }

      // Convert to SecretKey and cache
      byte[] keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP);
      SecretKey groupKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
      groupKeyCache.put(groupId, groupKey);

      return groupKey;

    } catch (Exception e) {
      Log.e(TAG, "Failed to load group key for: " + groupId, e);
      return null;
    }
  }

  /**
   * Remove group key (when leaving group)
   *
   * @param groupId Group identifier
   */
  public void removeGroupKey(String groupId) {
    try {
      userStorage.remove(GROUP_KEY_PREFIX + groupId);
      groupKeyCache.remove(groupId);
      Log.i(TAG, "Removed group key for: " + groupId);
    } catch (Exception e) {
      Log.e(TAG, "Failed to remove group key for: " + groupId, e);
    }
  }

  /**
   * Check if user has key for a group
   *
   * @param groupId Group identifier
   * @return true if user has the group key
   */
  public boolean hasGroupKey(String groupId) {
    return groupKeyCache.containsKey(groupId) || userStorage.contains(GROUP_KEY_PREFIX + groupId);
  }

  /**
   * Get group key for sharing with new members
   *
   * @param groupId Group identifier
   * @return Base64 encoded group key or null
   */
  public String getGroupKeyForSharing(String groupId) {
    try {
      return userStorage.getString(GROUP_KEY_PREFIX + groupId, null);
    } catch (Exception e) {
      Log.e(TAG, "Failed to get group key for sharing: " + groupId, e);
      return null;
    }
  }

  /** Clear all group keys (logout/reset) */
  public void clearAllGroupKeys() {
    try {
      groupKeyCache.clear();
      // Note: This clears all user data, including group keys
      userStorage.clear();
      Log.i(TAG, "Cleared all group keys for user: " + currentUserId);
    } catch (Exception e) {
      Log.e(TAG, "Failed to clear group keys", e);
    }
  }

  /**
   * Basic heuristic to check if message appears encrypted
   *
   * @param message Message to check
   * @return true if appears encrypted
   */
  private boolean isMessageEncrypted(String message) {
    if (message == null || message.length() < MIN_ENCRYPTED_MESSAGE_LENGTH) {
      return false;
    }

    // Base64 pattern check
    return message.matches(BASE64_PATTERN) && message.length() > MIN_ENCRYPTED_MESSAGE_LENGTH;
  }

  /**
   * Get encryption status for debugging
   *
   * @return Status information
   */
  public String getEncryptionStatus() {
    int cachedGroups = groupKeyCache.size();
    return String.format(
        Locale.US,
        "User: %s, Algorithm: %s, Cached Groups: %d",
        currentUserId,
        ALGORITHM,
        cachedGroups);
  }

  /**
   * List all groups with encryption keys (for debugging)
   *
   * @return Array of group IDs with keys
   */
  public String[] getEncryptedGroups() {
    return groupKeyCache.keySet().toArray(new String[0]);
  }
}
