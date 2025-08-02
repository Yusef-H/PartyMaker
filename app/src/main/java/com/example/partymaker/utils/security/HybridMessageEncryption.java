package com.example.partymaker.utils.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Hybrid encryption system using RSA for key exchange and AES for message encryption.
 * 
 * Architecture:
 * 1. Each user has RSA key pair (2048-bit)
 * 2. Each message encrypted with unique AES key (256-bit)
 * 3. AES key encrypted with recipient's RSA public key
 * 4. Supports group messaging with multiple recipients
 */
public class HybridMessageEncryption {
    private static final String TAG = "HybridMessageEncryption";
    
    // RSA configuration
    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
    private static final int RSA_KEY_SIZE = 2048;
    private static final String RSA_KEY_ALIAS_PREFIX = "partymaker_rsa_";
    
    // AES configuration
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    private final Context context;
    private final String currentUserId;
    private final SecureRandom secureRandom;
    private final KeyStore keyStore;
    
    public HybridMessageEncryption(Context context, String userId) {
        this.context = context;
        this.currentUserId = userId;
        this.secureRandom = new SecureRandom();
        
        try {
            // Initialize Android Keystore
            this.keyStore = KeyStore.getInstance("AndroidKeyStore");
            this.keyStore.load(null);
            
            // Ensure user has RSA key pair
            ensureUserKeyPair();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize hybrid encryption", e);
        }
    }
    
    /**
     * Encrypt message for multiple recipients (group messaging)
     * @param message Plain text message
     * @param recipientPublicKeys Map of userId -> public key (Base64)
     * @return JSON string with encrypted message and keys for each recipient
     */
    public String encryptForGroup(String message, Map<String, String> recipientPublicKeys) {
        try {
            // 1. Generate random AES key for this message
            SecretKey messageKey = generateAESKey();
            
            // 2. Encrypt message with AES
            String encryptedMessage = encryptWithAES(message, messageKey);
            
            // 3. Encrypt AES key for each recipient with their RSA public key
            JSONObject result = new JSONObject();
            result.put("encryptedMessage", encryptedMessage);
            result.put("algorithm", "hybrid-rsa-aes");
            result.put("timestamp", System.currentTimeMillis());
            
            JSONObject encryptedKeys = new JSONObject();
            byte[] aesKeyBytes = messageKey.getEncoded();
            
            for (Map.Entry<String, String> recipient : recipientPublicKeys.entrySet()) {
                String userId = recipient.getKey();
                String publicKeyBase64 = recipient.getValue();
                
                // Convert public key from Base64
                PublicKey recipientPublicKey = parsePublicKey(publicKeyBase64);
                
                // Encrypt AES key with recipient's RSA public key
                String encryptedKeyForRecipient = encryptWithRSA(aesKeyBytes, recipientPublicKey);
                encryptedKeys.put(userId, encryptedKeyForRecipient);
            }
            
            result.put("keys", encryptedKeys);
            
            Log.d(TAG, "Message encrypted for " + recipientPublicKeys.size() + " recipients");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt message for group", e);
            return null;
        }
    }
    
    /**
     * Decrypt message using current user's private key
     * @param encryptedData JSON string from encryptForGroup
     * @return Decrypted message or null if decryption fails
     */
    public String decryptMessage(String encryptedData) {
        try {
            JSONObject data = new JSONObject(encryptedData);
            
            // Get encrypted message
            String encryptedMessage = data.getString("encryptedMessage");
            
            // Get encrypted AES key for current user
            JSONObject keys = data.getJSONObject("keys");
            if (!keys.has(currentUserId)) {
                Log.w(TAG, "No encrypted key found for user: " + currentUserId);
                return null;
            }
            
            String encryptedKeyForMe = keys.getString(currentUserId);
            
            // Decrypt AES key using my RSA private key
            PrivateKey myPrivateKey = getMyPrivateKey();
            byte[] aesKeyBytes = decryptWithRSA(encryptedKeyForMe, myPrivateKey);
            SecretKey messageKey = new SecretKeySpec(aesKeyBytes, "AES");
            
            // Decrypt message using AES key
            String decryptedMessage = decryptWithAES(encryptedMessage, messageKey);
            
            Log.d(TAG, "Message decrypted successfully for user: " + currentUserId);
            return decryptedMessage;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt message for user: " + currentUserId, e);
            return null;
        }
    }
    
    /**
     * Get current user's public key for sharing with others
     * @return Base64 encoded public key
     */
    public String getMyPublicKey() {
        try {
            PublicKey publicKey = getMyPublicKeyObject();
            return Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get public key for user: " + currentUserId, e);
            return null;
        }
    }
    
    /**
     * Generate RSA key pair for current user if not exists
     */
    private void ensureUserKeyPair() throws Exception {
        String keyAlias = RSA_KEY_ALIAS_PREFIX + currentUserId;
        
        if (!keyStore.containsAlias(keyAlias)) {
            Log.d(TAG, "Generating RSA key pair for user: " + currentUserId);
            
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(RSA_KEY_SIZE)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setUserAuthenticationRequired(false) // For now, no biometric required
                .build();
                
            keyPairGenerator.initialize(keyGenParameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            Log.d(TAG, "RSA key pair generated for user: " + currentUserId);
        }
    }
    
    /**
     * Get current user's private key from Android Keystore
     */
    private PrivateKey getMyPrivateKey() throws Exception {
        String keyAlias = RSA_KEY_ALIAS_PREFIX + currentUserId;
        return (PrivateKey) keyStore.getKey(keyAlias, null);
    }
    
    /**
     * Get current user's public key from Android Keystore
     */
    private PublicKey getMyPublicKeyObject() throws Exception {
        String keyAlias = RSA_KEY_ALIAS_PREFIX + currentUserId;
        return keyStore.getCertificate(keyAlias).getPublicKey();
    }
    
    /**
     * Parse public key from Base64 string
     */
    private PublicKey parsePublicKey(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    
    /**
     * Generate random AES key for message encryption
     */
    private SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE, secureRandom);
        return keyGenerator.generateKey();
    }
    
    /**
     * Encrypt data with AES-GCM
     */
    private String encryptWithAES(String plaintext, SecretKey key) throws Exception {
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        // Encrypt
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
        
        // Combine IV + ciphertext
        byte[] encryptedData = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);
        
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
    }
    
    /**
     * Decrypt data with AES-GCM
     */
    private String decryptWithAES(String encryptedText, SecretKey key) throws Exception {
        byte[] encryptedData = Base64.decode(encryptedText, Base64.NO_WRAP);
        
        // Extract IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
        
        // Extract ciphertext
        byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
        
        // Decrypt
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] plaintext = cipher.doFinal(ciphertext);
        
        return new String(plaintext, "UTF-8");
    }
    
    /**
     * Encrypt data with RSA public key
     */
    private String encryptWithRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data);
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
    }
    
    /**
     * Decrypt data with RSA private key
     */
    private byte[] decryptWithRSA(String encryptedData, PrivateKey privateKey) throws Exception {
        byte[] data = Base64.decode(encryptedData, Base64.NO_WRAP);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }
    
    /**
     * Get encryption status for debugging
     */
    public String getEncryptionStatus() {
        try {
            boolean hasKeyPair = keyStore.containsAlias(RSA_KEY_ALIAS_PREFIX + currentUserId);
            return String.format("User: %s, RSA KeyPair: %s, Algorithm: RSA-%d + AES-%d", 
                currentUserId, hasKeyPair ? "✓" : "✗", RSA_KEY_SIZE, AES_KEY_SIZE);
        } catch (Exception e) {
            return "Status check failed: " + e.getMessage();
        }
    }
}