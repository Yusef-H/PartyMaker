package com.example.partymaker.data.firebase;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Helper class for Firebase references.
 * Provides static references to Firebase services and methods for working with them.
 */
public class DBRef {
  private static final String TAG = "DBRef";
  
  // Firebase Authentication
  public static FirebaseAuth Auth;
  
  // Firebase Realtime Database
  public static FirebaseDatabase DataBase;
  public static DatabaseReference refGroups;
  public static DatabaseReference refUsers;
  public static DatabaseReference refMessages;
  
  // Firebase Storage
  public static FirebaseStorage Storage;
  public static StorageReference refStorage;
  
  // Current user
  public static String CurrentUser;
  
  /**
   * Initializes all Firebase references.
   * This method should be called in the Application class.
   */
  public static void init() {
    try {
      // Initialize Firebase Authentication
      Auth = FirebaseAuth.getInstance();
      
      // Initialize Firebase Realtime Database
      DataBase = FirebaseDatabase.getInstance();
      refGroups = DataBase.getReference("Groups");
      refUsers = DataBase.getReference("Users");
      refMessages = DataBase.getReference("GroupsMessages");
      
      // Initialize Firebase Storage
      Storage = FirebaseStorage.getInstance();
      refStorage = Storage.getReference("UsersImageProfile");
      
      Log.d(TAG, "Firebase references initialized successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error initializing Firebase references", e);
      throw new RuntimeException("Failed to initialize Firebase references", e);
    }
  }

  /**
   * Checks if an image exists in Firebase Storage.
   *
   * @param path The path to the image
   * @param listener Callback for the result
   */
  public static void checkImageExists(String path, OnImageExistsListener listener) {
    if (refStorage == null) {
      Log.e(TAG, "Storage reference is null. Call init() first.");
      listener.onImageExists(false);
      return;
    }
    
    StorageReference imageRef = refStorage.child(path);
    imageRef
        .getDownloadUrl()
        .addOnSuccessListener(uri -> listener.onImageExists(true))
        .addOnFailureListener(exception -> listener.onImageExists(false));
  }

  /**
   * Interface for image existence check callbacks.
   */
  public interface OnImageExistsListener {
    void onImageExists(boolean exists);
  }
}
