package com.example.partymaker.data.firebase;

import com.example.partymaker.data.api.FirebaseServerClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * This class is a drop-in replacement for DBRef that uses our Spring Boot server instead of
 * directly accessing Firebase.
 */
public class ServerDBRef {
  private static final String TAG = "ServerDBRef";

  // Keep Firebase Auth and Storage for now as they're not part of our server implementation
  public static FirebaseAuth Auth = FirebaseAuth.getInstance();
  public static FirebaseStorage Storage = FirebaseStorage.getInstance();
  public static String CurrentUser;
  public static StorageReference refStorage = Storage.getReference("UsersImageProfile");

  // Our server client instance
  private static final FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

  public static void checkImageExists(String path, OnImageExistsListener listener) {
    StorageReference imageRef = refStorage.child(path);
    imageRef
        .getDownloadUrl()
        .addOnSuccessListener(uri -> listener.onImageExists(true))
        .addOnFailureListener(exception -> listener.onImageExists(false));
  }

  public interface OnImageExistsListener {
    void onImageExists(boolean exists);
  }

  /** Helper method to get the FirebaseServerClient instance */
  public static FirebaseServerClient getServerClient() {
    return serverClient;
  }
}
