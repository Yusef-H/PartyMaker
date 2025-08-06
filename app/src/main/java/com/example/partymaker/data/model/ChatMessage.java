package com.example.partymaker.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a chat message in the PartyMaker application. This class is annotated for Room
 * database storage.
 */
@Entity(tableName = "chat_messages")
public class ChatMessage {
  /** The unique key for the message. */
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "messageKey")
  private String messageKey;

  /** The key of the group this message belongs to. */
  @ColumnInfo(name = "groupKey")
  private String groupKey;

  /** The key of the user who sent the message. */
  @ColumnInfo(name = "senderKey")
  private String senderKey;

  /** The display name of the sender. */
  @ColumnInfo(name = "senderName")
  private String senderName;

  /** The message content. */
  @ColumnInfo(name = "message")
  private String message;

  /** The timestamp when the message was sent. */
  @ColumnInfo(name = "timestamp")
  private long timestamp;

  /** URL to an image attached to the message, if any. */
  @ColumnInfo(name = "imageUrl")
  private String imageUrl;

  /** Whether this message is encrypted. */
  @ColumnInfo(name = "encrypted")
  private boolean encrypted = false;

  /** Additional data for the message. */
  @ColumnInfo(name = "metadata")
  private Map<String, Object> metadata;

  // Legacy fields to maintain compatibility with existing code
  private String messageUser;
  private String messageText;
  private String messageTime;
  private String groupId;
  private HashMap<String, Object> messageContent;

  /** Default constructor. */
  public ChatMessage() {
    this.timestamp = System.currentTimeMillis();
    this.metadata = new HashMap<>();
    this.messageContent = new HashMap<>();
    this.messageKey = "";
  }

  /**
   * Constructs a chat message with basic fields.
   *
   * @param groupKey The key of the group
   * @param senderKey The key of the sender
   * @param senderName The name of the sender
   * @param message The message content
   */
  @Ignore
  public ChatMessage(String groupKey, String senderKey, String senderName, String message) {
    this();
    this.groupKey = groupKey;
    this.senderKey = senderKey;
    this.senderName = senderName;
    this.message = message;

    // Set legacy fields for backward compatibility
    this.messageUser = senderName;
    this.messageText = message;
    this.groupId = groupKey;
  }

  /**
   * Constructs a chat message with all fields.
   *
   * @param messageKey The unique key for the message
   * @param groupKey The key of the group
   * @param senderKey The key of the sender
   * @param senderName The name of the sender
   * @param message The message content
   * @param timestamp The timestamp
   * @param imageUrl The image URL
   * @param metadata Additional metadata
   */
  @Ignore
  public ChatMessage(
      @NonNull String messageKey,
      String groupKey,
      String senderKey,
      String senderName,
      String message,
      long timestamp,
      String imageUrl,
      Map<String, Object> metadata) {
    this.messageKey = messageKey;
    this.groupKey = groupKey;
    this.senderKey = senderKey;
    this.senderName = senderName;
    this.message = message;
    this.timestamp = timestamp;
    this.imageUrl = imageUrl;
    this.metadata = metadata != null ? metadata : new HashMap<>();

    // Set legacy fields for backward compatibility
    this.messageUser = senderName;
    this.messageText = message;
    this.groupId = groupKey;
    this.messageContent = new HashMap<>(this.metadata);
  }

  /**
   * Gets the message key.
   *
   * @return The message key
   */
  @NonNull
  public String getMessageKey() {
    return messageKey;
  }

  /**
   * Sets the message key.
   *
   * @param messageKey The message key
   */
  public void setMessageKey(@NonNull String messageKey) {
    this.messageKey = messageKey;
  }

  /**
   * Gets the group key.
   *
   * @return The group key
   */
  public String getGroupKey() {
    return groupKey;
  }

  /**
   * Sets the group key.
   *
   * @param groupKey The group key
   */
  public void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
    this.groupId = groupKey; // For backward compatibility
  }

  /**
   * Gets the sender key.
   *
   * @return The sender key
   */
  public String getSenderKey() {
    return senderKey;
  }

  /**
   * Sets the sender key.
   *
   * @param senderKey The sender key
   */
  public void setSenderKey(String senderKey) {
    this.senderKey = senderKey;
  }

  /**
   * Gets the sender name.
   *
   * @return The sender name
   */
  public String getSenderName() {
    return senderName;
  }

  /**
   * Sets the sender name.
   *
   * @param senderName The sender name
   */
  public void setSenderName(String senderName) {
    this.senderName = senderName;
    this.messageUser = senderName; // For backward compatibility
  }

  /**
   * Gets the message content.
   *
   * @return The message content
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the message content.
   *
   * @param message The message content
   */
  public void setMessage(String message) {
    this.message = message;
    this.messageText = message; // For backward compatibility
  }

  /**
   * Gets the timestamp.
   *
   * @return The timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the timestamp.
   *
   * @param timestamp The timestamp
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Gets the image URL.
   *
   * @return The image URL
   */
  public String getImageUrl() {
    return imageUrl;
  }

  /**
   * Gets whether this message is encrypted.
   *
   * @return true if encrypted, false otherwise
   */
  public boolean isEncrypted() {
    return encrypted;
  }

  /**
   * Sets whether this message is encrypted.
   *
   * @param encrypted true if encrypted, false otherwise
   */
  public void setEncrypted(boolean encrypted) {
    this.encrypted = encrypted;
  }

  /**
   * Sets the image URL.
   *
   * @param imageUrl The image URL
   */
  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  /**
   * Gets the metadata.
   *
   * @return The metadata
   */
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  /**
   * Sets the metadata.
   *
   * @param metadata The metadata
   */
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata != null ? metadata : new HashMap<>();
    this.messageContent = new HashMap<>(this.metadata); // For backward compatibility
  }

  // Legacy methods for backward compatibility

  /**
   * Gets the message user (legacy).
   *
   * @return The sender name
   */
  public String getMessageUser() {
    return senderName;
  }

  /**
   * Sets the message user (legacy).
   *
   * @param messageUser The message user
   */
  public void setMessageUser(String messageUser) {
    this.messageUser = messageUser;
    this.senderName = messageUser;
  }

  /**
   * Gets the message text (legacy).
   *
   * @return The message content
   */
  public String getMessageText() {
    return message;
  }

  /**
   * Sets the message text (legacy).
   *
   * @param messageText The message text
   */
  public void setMessageText(String messageText) {
    this.messageText = messageText;
    this.message = messageText;
  }

  /**
   * Gets the message time (legacy).
   *
   * @return The message time as string
   */
  public String getMessageTime() {
    return messageTime;
  }

  /**
   * Sets the message time (legacy).
   *
   * @param messageTime The message time
   */
  public void setMessageTime(String messageTime) {
    this.messageTime = messageTime;
  }

  /**
   * Gets the group ID (legacy).
   *
   * @return The group key
   */
  public String getGroupId() {
    return groupKey;
  }

  /**
   * Sets the group ID (legacy).
   *
   * @param groupId The group ID
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
    this.groupKey = groupId;
  }

  /**
   * Gets the message content (legacy).
   *
   * @return The message content as a map
   */
  public HashMap<String, Object> getMessageContent() {
    if (messageContent == null) {
      messageContent = new HashMap<>();
    }
    return messageContent;
  }

  /**
   * Sets the message content (legacy).
   *
   * @param messageContent The message content
   */
  public void setMessageContent(HashMap<String, Object> messageContent) {
    this.messageContent = messageContent;
    this.metadata = messageContent;
  }

  /**
   * Creates a copy of this ChatMessage for encryption/decryption operations. Useful to avoid
   * modifying the original message during encryption processing.
   *
   * @return A new ChatMessage instance with the same data
   */
  public ChatMessage createCopy() {
    ChatMessage copy = new ChatMessage();
    copy.setMessageKey(this.messageKey);
    copy.setGroupKey(this.groupKey);
    copy.setSenderKey(this.senderKey);
    copy.setSenderName(this.senderName);
    copy.setMessage(this.message);
    copy.setTimestamp(this.timestamp);
    copy.setImageUrl(this.imageUrl);
    copy.setEncrypted(this.encrypted);
    copy.setMetadata(new HashMap<>(this.metadata != null ? this.metadata : new HashMap<>()));

    // Copy legacy fields
    copy.messageUser = this.messageUser;
    copy.messageText = this.messageText;
    copy.messageTime = this.messageTime;
    copy.groupId = this.groupId;
    copy.messageContent =
        new HashMap<>(this.messageContent != null ? this.messageContent : new HashMap<>());

    return copy;
  }

  /**
   * Checks if the message content appears to be encrypted. This is a basic heuristic check based on
   * content patterns.
   *
   * @return true if message appears encrypted
   */
  public boolean appearEncrypted() {
    if (message == null || message.length() < 20) {
      return false;
    }

    // Check if message looks like Base64 encrypted data
    return message.matches("^[A-Za-z0-9+/]*={0,2}$");
  }

  /** Mark message as encrypted and update metadata. Useful for tracking encryption status. */
  public void markAsEncrypted() {
    this.encrypted = true;

    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }
    this.metadata.put("encryption_timestamp", System.currentTimeMillis());
    this.metadata.put("encryption_version", "group_aes_v1");

    // Update legacy field for compatibility
    if (this.messageContent == null) {
      this.messageContent = new HashMap<>();
    }
    this.messageContent.putAll(this.metadata);
  }

  /** Mark message as decrypted and update metadata. */
  public void markAsDecrypted() {
    this.encrypted = false;

    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }
    this.metadata.put("decryption_timestamp", System.currentTimeMillis());

    // Update legacy field for compatibility
    if (this.messageContent == null) {
      this.messageContent = new HashMap<>();
    }
    this.messageContent.putAll(this.metadata);
  }

  /**
   * Gets encryption metadata for this message.
   *
   * @return Map containing encryption-related metadata
   */
  public Map<String, Object> getEncryptionMetadata() {
    Map<String, Object> encryptionData = new HashMap<>();

    if (this.metadata != null) {
      for (Map.Entry<String, Object> entry : this.metadata.entrySet()) {
        String key = entry.getKey();
        if (key.startsWith("encryption_") || key.startsWith("decryption_")) {
          encryptionData.put(key, entry.getValue());
        }
      }
    }

    encryptionData.put("is_encrypted", this.encrypted);
    encryptionData.put("appears_encrypted", this.appearEncrypted());

    return encryptionData;
  }

  /**
   * Validates that the message is suitable for encryption. Checks for non-empty content and valid
   * group context.
   *
   * @return true if message can be encrypted
   */
  public boolean canBeEncrypted() {
    return message != null
        && !message.trim().isEmpty()
        && groupKey != null
        && !groupKey.trim().isEmpty();
  }

  /**
   * Gets a display-friendly summary of the message for logging. Does not include sensitive content.
   *
   * @return Safe summary string
   */
  public String getLogSafeSummary() {
    return String.format(
        Locale.US,
        "ChatMessage{key='%s', group='%s', sender='%s', encrypted=%s, hasImage=%s, length=%d}",
            messageKey.substring(0, Math.min(8, messageKey.length())) + "...",
        groupKey != null ? groupKey.substring(0, Math.min(8, groupKey.length())) + "..." : "null",
        senderName != null ? senderName : "null",
        encrypted,
        imageUrl != null && !imageUrl.isEmpty(),
        message != null ? message.length() : 0);
  }
}
