package com.example.partymaker.utils.security;

import android.content.Context;
import android.util.Log;
import com.example.partymaker.data.model.ChatMessage;

/**
 * Manages encryption/decryption of chat messages using EnhancedSecureStorage.
 * Provides seamless integration with existing ChatMessage model.
 */
public class MessageEncryptionManager {
    private static final String TAG = "MessageEncryptionManager";
    
    private final EnhancedSecureStorage secureStorage;
    private final String currentUserId;
    
    /**
     * Constructor
     * @param context Application context
     * @param userId Current user ID for encryption keys
     */
    public MessageEncryptionManager(Context context, String userId) {
        this.currentUserId = userId;
        this.secureStorage = new EnhancedSecureStorage(context, userId);
    }
    
    /**
     * Encrypt a chat message before sending/storing
     * @param message Original message text
     * @return Encrypted message text, or null if encryption fails
     */
    public String encryptMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }
        
        try {
            String encrypted = secureStorage.encrypt(message);
            if (encrypted != null) {
                Log.d(TAG, "Message encrypted successfully for user: " + currentUserId);
                return encrypted;
            } else {
                Log.e(TAG, "Message encryption returned null for user: " + currentUserId);
                return message; // Fallback to original message
            }
        } catch (Exception e) {
            Log.e(TAG, "Message encryption failed for user: " + currentUserId, e);
            return message; // Fallback to original message
        }
    }
    
    /**
     * Decrypt a chat message for display
     * @param encryptedMessage Encrypted message text
     * @return Decrypted message text, or original if decryption fails
     */
    public String decryptMessage(String encryptedMessage) {
        if (encryptedMessage == null || encryptedMessage.trim().isEmpty()) {
            return encryptedMessage;
        }
        
        try {
            String decrypted = secureStorage.decrypt(encryptedMessage);
            if (decrypted != null) {
                Log.d(TAG, "Message decrypted successfully for user: " + currentUserId);
                return decrypted;
            } else {
                Log.w(TAG, "Message decryption returned null, might be plain text");
                return encryptedMessage; // Might be plain text
            }
        } catch (Exception e) {
            Log.w(TAG, "Message decryption failed, treating as plain text", e);
            return encryptedMessage; // Fallback - might be plain text
        }
    }
    
    /**
     * Encrypt a ChatMessage object before sending to server
     * @param chatMessage ChatMessage to encrypt
     * @return Modified ChatMessage with encrypted content
     */
    public ChatMessage encryptChatMessage(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }
        
        // Create copy to avoid modifying original
        ChatMessage encryptedMessage = new ChatMessage();
        encryptedMessage.setMessageKey(chatMessage.getMessageKey());
        encryptedMessage.setGroupKey(chatMessage.getGroupKey());
        encryptedMessage.setSenderKey(chatMessage.getSenderKey());
        encryptedMessage.setMessageTime(chatMessage.getMessageTime());
        encryptedMessage.setImageUrl(chatMessage.getImageUrl());
        
        // Encrypt the message content
        String originalMessage = chatMessage.getMessage();
        String encryptedContent = encryptMessage(originalMessage);
        encryptedMessage.setMessage(encryptedContent);
        
        // Mark as encrypted for proper handling
        encryptedMessage.setEncrypted(true);
        
        return encryptedMessage;
    }
    
    /**
     * Decrypt a ChatMessage object received from server
     * @param encryptedChatMessage ChatMessage to decrypt
     * @return Modified ChatMessage with decrypted content
     */
    public ChatMessage decryptChatMessage(ChatMessage encryptedChatMessage) {
        if (encryptedChatMessage == null) {
            return null;
        }
        
        // Create copy to avoid modifying original
        ChatMessage decryptedMessage = new ChatMessage();
        decryptedMessage.setMessageKey(encryptedChatMessage.getMessageKey());
        decryptedMessage.setGroupKey(encryptedChatMessage.getGroupKey());
        decryptedMessage.setSenderKey(encryptedChatMessage.getSenderKey());
        decryptedMessage.setMessageTime(encryptedChatMessage.getMessageTime());
        decryptedMessage.setImageUrl(encryptedChatMessage.getImageUrl());
        
        // Decrypt the message content
        String encryptedContent = encryptedChatMessage.getMessage();
        String decryptedContent = decryptMessage(encryptedContent);
        decryptedMessage.setMessage(decryptedContent);
        
        // Mark as decrypted
        decryptedMessage.setEncrypted(false);
        
        return decryptedMessage;
    }
    
    /**
     * Check if a message appears to be encrypted (basic heuristic)
     * @param message Message to check
     * @return true if message appears encrypted
     */
    public boolean isMessageEncrypted(String message) {
        if (message == null || message.length() < 20) {
            return false;
        }
        
        // Basic heuristics for encrypted content:
        // - Base64 encoded (only contains Base64 chars)
        // - Reasonable length
        // - No readable text patterns
        return message.matches("^[A-Za-z0-9+/]*={0,2}$") && message.length() > 20;
    }
    
    /**
     * Get encryption status information
     * @return Status string for debugging
     */
    public String getEncryptionStatus() {
        return "User: " + currentUserId + ", " + secureStorage.getEncryptionInfo();
    }
    
    /**
     * Rotate encryption key for enhanced security
     * @param context Application context
     */
    public void rotateEncryptionKey(Context context) {
        try {
            secureStorage.rotateKey(context);
            Log.i(TAG, "Encryption key rotated for user: " + currentUserId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to rotate encryption key for user: " + currentUserId, e);
        }
    }
}