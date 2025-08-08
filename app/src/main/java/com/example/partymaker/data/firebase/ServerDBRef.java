package com.example.partymaker.data.firebase;

import com.example.partymaker.data.api.FirebaseServerClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * This class is a drop-in replacement for DBRef that uses our Spring Boot server instead of
 * directly accessing Firebase. Provides unified access to Firebase services through server API.
 */
public class ServerDBRef {
  private static final String TAG = "ServerDBRef";
  
  // Firebase Storage paths
  private static final String USERS_IMAGE_PROFILE_PATH = "UsersImageProfile";
  
  // Firebase instances
  public static final FirebaseStorage Storage = FirebaseStorage.getInstance();
  public static final StorageReference refStorage = Storage.getReference(USERS_IMAGE_PROFILE_PATH);
  
  // Server client instance for API operations
  private static final FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
  
  // Firebase Auth (kept for authentication operations)
  public static FirebaseAuth Auth = FirebaseAuth.getInstance();
  public static String CurrentUser;

  /**
   * Checks if an image exists in Firebase Storage at the specified path.
   * 
   * @param path The path to check
   * @param listener Callback for the result
   */
  public static void checkImageExists(String path, OnImageExistsListener listener) {
    if (path == null || path.trim().isEmpty()) {
      listener.onImageExists(false);
      return;
    }
    
    StorageReference imageRef = refStorage.child(path.trim());
    imageRef
        .getDownloadUrl()
        .addOnSuccessListener(uri -> {
          android.util.Log.d(TAG, "Image exists at path: " + path);
          listener.onImageExists(true);
        })
        .addOnFailureListener(exception -> {
          android.util.Log.w(TAG, "Image not found at path: " + path);
          listener.onImageExists(false);
        });
  }

  /**
   * Helper method to get the FirebaseServerClient instance.
   * 
   * @return The singleton FirebaseServerClient instance
   */
  public static FirebaseServerClient getServerClient() {
    return serverClient;
  }
  
  /**
   * Gets a storage reference for user images.
   * 
   * @param userPath The user-specific path (e.g., formatted email)
   * @return StorageReference for the user's image
   */
  public static StorageReference getUserImageReference(String userPath) {
    return refStorage.child("Users/" + userPath);
  }
  
  /**
   * Checks if the current user is authenticated.
   * 
   * @return true if user is authenticated, false otherwise
   */
  public static boolean isUserAuthenticated() {
    return Auth != null && Auth.getCurrentUser() != null;
  }

  /**
   * Callback interface for image existence checks.
   */
  public interface OnImageExistsListener {
    /**
     * Called when the image existence check completes.
     * 
     * @param exists true if the image exists, false otherwise
     */
    void onImageExists(boolean exists);
  }
}
