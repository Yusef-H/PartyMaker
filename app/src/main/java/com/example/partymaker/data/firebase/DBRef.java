package com.example.partymaker.data.firebase;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Helper class for Firebase references. Provides static references to Firebase services and methods
 * for working with them.
 */
public class DBRef {
  private static final String TAG = "DBRef";

  private static final String GROUPS_PATH = "Groups";
  private static final String USERS_PATH = "Users";
  private static final String MESSAGES_PATH = "GroupsMessages";

  // Firebase Authentication
  public static FirebaseAuth auth;

  // Firebase Realtime Database
  public static FirebaseDatabase database;
  public static DatabaseReference refGroups;
  public static DatabaseReference refUsers;
  public static DatabaseReference refMessages;

  // Firebase Storage
  public static FirebaseStorage storage;
  public static StorageReference refStorage;

  // Current user
  public static String currentUser;

  /** Initializes all Firebase references. This method should be called in the Application class. */
  public static void init() {
    try {
      initializeAuthentication();
      initializeDatabase();
      initializeStorage();

      Log.d(TAG, "Firebase references initialized successfully");
      Log.d(TAG, "Storage reference path: " + refStorage.getPath());
    } catch (Exception e) {
      Log.e(TAG, "Error initializing Firebase references", e);
      throw new RuntimeException("Failed to initialize Firebase references", e);
    }
  }

  private static void initializeAuthentication() {
    auth = FirebaseAuth.getInstance();
  }

  private static void initializeDatabase() {
    database = FirebaseDatabase.getInstance();
    refGroups = database.getReference(GROUPS_PATH);
    refUsers = database.getReference(USERS_PATH);
    refMessages = database.getReference(MESSAGES_PATH);
  }

  private static void initializeStorage() {
    storage = FirebaseStorage.getInstance();
    refStorage = storage.getReference();
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

  /** Interface for image existence check callbacks. */
  public interface OnImageExistsListener {
    void onImageExists(boolean exists);
  }
}
