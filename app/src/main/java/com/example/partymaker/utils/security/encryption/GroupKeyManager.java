package com.example.partymaker.utils.security.encryption;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manages group encryption keys through Firebase. Handles key distribution, rotation, and member
 * management.
 *
 * <p>Firebase Structure: /group_keys/ /{groupId}/ /members/ /{userId}: {encryptedGroupKey}
 * /metadata/ /created: timestamp /version: keyVersion /creator: creatorUserId
 */
public class GroupKeyManager {
  private static final String TAG = "GroupKeyManager";
  private static final String FIREBASE_GROUP_KEYS_PATH = "group_keys";

    private final String currentUserId;
  private final GroupMessageEncryption groupEncryption;
    private final DatabaseReference firebaseRef;

  public GroupKeyManager(Context context, String userId) {
      this.currentUserId = userId;
    this.groupEncryption = new GroupMessageEncryption(context, userId);
      EnhancedSecureStorage userStorage = new EnhancedSecureStorage(context, userId);
    this.firebaseRef = FirebaseDatabase.getInstance().getReference(FIREBASE_GROUP_KEYS_PATH);
  }

  /**
   * Create new group with encryption key
   *
   * @param groupId Group identifier
   * @return CompletableFuture<Boolean> success result
   */
  public CompletableFuture<Boolean> createGroupWithEncryption(String groupId) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    try {
      // Generate new group key
      String groupKeyBase64 = groupEncryption.generateGroupKey(groupId);
      if (groupKeyBase64 == null) {
        future.complete(false);
        return future;
      }

      // Store group key directly (simplified approach)
      // In production, this would be encrypted with each user's public key

      // Prepare Firebase data
      Map<String, Object> groupKeyData = new HashMap<>();
      Map<String, Object> members = new HashMap<>();
      Map<String, Object> metadata = new HashMap<>();

      // Add creator as first member
      members.put(currentUserId, groupKeyBase64);

      // Add metadata
      metadata.put("created", System.currentTimeMillis());
      metadata.put("version", 1);
      metadata.put("creator", currentUserId);

      groupKeyData.put("members", members);
      groupKeyData.put("metadata", metadata);

      // Save to Firebase
      firebaseRef
          .child(groupId)
          .setValue(groupKeyData)
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  Log.i(TAG, "Group encryption created for: " + groupId);
                  future.complete(true);
                } else {
                  Log.e(TAG, "Failed to create group encryption: " + groupId, task.getException());
                  future.complete(false);
                }
              });

    } catch (Exception e) {
      Log.e(TAG, "Error creating group encryption: " + groupId, e);
      future.complete(false);
    }

    return future;
  }

  /**
   * Add user to group encryption (invite new member)
   *
   * @param groupId Group identifier
   * @param newUserId User to add
   * @return CompletableFuture<Boolean> success result
   */
  public CompletableFuture<Boolean> addUserToGroupEncryption(String groupId, String newUserId) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    try {
      // First, get the group key from Firebase (from any existing member)
      firebaseRef
          .child(groupId)
          .child("members")
          .limitToFirst(1)
          .addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  try {
                    String groupKeyBase64 = null;

                    // Get group key from any existing member
                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                      groupKeyBase64 = memberSnapshot.getValue(String.class);
                      break; // Take the first available key
                    }

                    if (groupKeyBase64 == null) {
                      Log.e(TAG, "No existing group key found for: " + groupId);
                      future.complete(false);
                      return;
                    }

                    Log.i(TAG, "Found existing group key, adding user: " + newUserId);

                    // Add new user with the same group key
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("members/" + newUserId, groupKeyBase64);
                    updates.put("metadata/lastUpdated", System.currentTimeMillis());

                    firebaseRef
                        .child(groupId)
                        .updateChildren(updates)
                        .addOnCompleteListener(
                            task -> {
                              if (task.isSuccessful()) {
                                Log.i(
                                    TAG,
                                    "Successfully added user "
                                        + newUserId
                                        + " to group encryption: "
                                        + groupId);
                                future.complete(true);
                              } else {
                                Log.e(
                                    TAG,
                                    "Failed to add user to group encryption",
                                    task.getException());
                                future.complete(false);
                              }
                            });

                  } catch (Exception e) {
                    Log.e(TAG, "Error processing group key for new user", e);
                    future.complete(false);
                  }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                  Log.e(TAG, "Failed to get existing group key", databaseError.toException());
                  future.complete(false);
                }
              });

    } catch (Exception e) {
      Log.e(TAG, "Error adding user to group encryption", e);
      future.complete(false);
    }

    return future;
  }

  /**
   * Remove user from group encryption and rotate key
   *
   * @param groupId Group identifier
   * @param removedUserId User to remove
   * @return CompletableFuture<Boolean> success result
   */
  public CompletableFuture<Boolean> removeUserAndRotateKey(String groupId, String removedUserId) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    try {
      // Generate new group key (key rotation for security)
      String newGroupKeyBase64 = groupEncryption.generateGroupKey(groupId);
      if (newGroupKeyBase64 == null) {
        future.complete(false);
        return future;
      }

      // Get current group members
      firebaseRef
          .child(groupId)
          .child("members")
          .addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  try {
                    Map<String, Object> updates = new HashMap<>();
                    Map<String, Object> newMembers = new HashMap<>();

                    // Re-encrypt new key for all remaining members (except removed user)
                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                      String userId = memberSnapshot.getKey();

                      if (userId != null && !userId.equals(removedUserId)) {
                        // Add remaining member with new key
                        // Simplified: should encrypt per user's personal key
                        newMembers.put(userId, newGroupKeyBase64);
                      }
                    }

                    // Update Firebase
                    updates.put("members", newMembers);
                    updates.put(
                        "metadata/version",
                        System.currentTimeMillis() / 1000); // Use timestamp as version
                    updates.put("metadata/lastRotated", System.currentTimeMillis());

                    firebaseRef
                        .child(groupId)
                        .updateChildren(updates)
                        .addOnCompleteListener(
                            task -> {
                              if (task.isSuccessful()) {
                                Log.i(TAG, "Rotated group key and removed user: " + removedUserId);
                                future.complete(true);
                              } else {
                                Log.e(TAG, "Failed to rotate group key", task.getException());
                                future.complete(false);
                              }
                            });

                  } catch (Exception e) {
                    Log.e(TAG, "Error processing group member removal", e);
                    future.complete(false);
                  }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                  Log.e(TAG, "Failed to get group members", databaseError.toException());
                  future.complete(false);
                }
              });

    } catch (Exception e) {
      Log.e(TAG, "Error removing user and rotating key", e);
      future.complete(false);
    }

    return future;
  }

  /**
   * Load group key from Firebase for current user
   *
   * @param groupId Group identifier
   * @return CompletableFuture<Boolean> success result
   */
  public CompletableFuture<Boolean> loadGroupKey(String groupId) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    try {
      firebaseRef
          .child(groupId)
          .child("members")
          .child(currentUserId)
          .addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  try {
                    String encryptedGroupKey = dataSnapshot.getValue(String.class);
                    if (encryptedGroupKey == null) {
                      Log.w(TAG, "No group key found for user in group: " + groupId);
                      future.complete(false);
                      return;
                    }

                    // Use key directly (simplified approach)
                    // In production, this would be decrypted with user's private key
                    String groupKeyBase64 = encryptedGroupKey;

                    // Store group key locally
                    boolean stored = groupEncryption.storeGroupKey(groupId, groupKeyBase64);

                    if (stored) {
                      Log.i(TAG, "Loaded group key for: " + groupId);
                      future.complete(true);
                    } else {
                      Log.e(TAG, "Failed to store group key locally");
                      future.complete(false);
                    }

                  } catch (Exception e) {
                    Log.e(TAG, "Error processing loaded group key", e);
                    future.complete(false);
                  }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                  Log.e(TAG, "Failed to load group key", databaseError.toException());
                  future.complete(false);
                }
              });

    } catch (Exception e) {
      Log.e(TAG, "Error loading group key", e);
      future.complete(false);
    }

    return future;
  }

  /**
   * Check if user is member of group encryption
   *
   * @param groupId Group identifier
   * @return CompletableFuture<Boolean> membership result
   */
  public CompletableFuture<Boolean> isGroupMember(String groupId) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    try {
      firebaseRef
          .child(groupId)
          .child("members")
          .child(currentUserId)
          .addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  boolean exists = dataSnapshot.exists();
                  future.complete(exists);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                  Log.e(TAG, "Failed to check group membership", databaseError.toException());
                  future.complete(false);
                }
              });

    } catch (Exception e) {
      Log.e(TAG, "Error checking group membership", e);
      future.complete(false);
    }

    return future;
  }

  /**
   * Delete group encryption (when group is deleted)
   *
   * @param groupId Group identifier
   * @return CompletableFuture<Boolean> success result
   */
  public CompletableFuture<Boolean> deleteGroupEncryption(String groupId) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    try {
      firebaseRef
          .child(groupId)
          .removeValue()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  // Also remove local key
                  groupEncryption.removeGroupKey(groupId);
                  Log.i(TAG, "Deleted group encryption: " + groupId);
                  future.complete(true);
                } else {
                  Log.e(TAG, "Failed to delete group encryption", task.getException());
                  future.complete(false);
                }
              });

    } catch (Exception e) {
      Log.e(TAG, "Error deleting group encryption", e);
      future.complete(false);
    }

    return future;
  }

  /**
   * Initialize encryption for user joining existing group
   *
   * @param groupId Group identifier
   */
  public void initializeForExistingGroup(String groupId) {
    // First check if we already have the key locally
    if (groupEncryption.hasGroupKey(groupId)) {
      Log.i(TAG, "Group key already available locally for: " + groupId);
      return;
    }

    // Load from Firebase
    loadGroupKey(groupId)
        .thenAccept(
            success -> {
              if (success) {
                Log.i(TAG, "Successfully initialized encryption for group: " + groupId);
              } else {
                Log.w(TAG, "Failed to initialize encryption for group: " + groupId);
              }
            });
  }

  /**
   * Get encryption manager for message operations
   *
   * @return GroupMessageEncryption instance
   */
  public GroupMessageEncryption getEncryptionManager() {
    return groupEncryption;
  }

  /**
   * Get status information for debugging
   *
   * @return Status string
   */
  public String getStatus() {
    return String.format("User: %s, %s", currentUserId, groupEncryption.getEncryptionStatus());
  }
}
