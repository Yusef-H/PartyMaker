package com.example.partymaker.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DBref {
  public static FirebaseAuth Auth = FirebaseAuth.getInstance();
  public static FirebaseDatabase DataBase = FirebaseDatabase.getInstance();
  public static FirebaseStorage Storage = FirebaseStorage.getInstance();

  // public static String
  // CurrentUser=Auth.getCurrentUser().getEmail().replace('.',' ');
  public static String CurrentUser;
  public static String CurrentGroup;

  public static DatabaseReference refGroups = DataBase.getReference("Groups");
  public static DatabaseReference refUsers = DataBase.getReference("Users");
  public static DatabaseReference refMessages = DataBase.getReference("GroupsMessages");
  public static StorageReference refStorage = Storage.getReference("UsersImagProfile");

  public static void checkImageExists(String path, OnImageExistsListener listener) {
    StorageReference imageRef = refStorage.child(path);
    imageRef.getDownloadUrl()
      .addOnSuccessListener(uri -> listener.onImageExists(true))
      .addOnFailureListener(exception -> listener.onImageExists(false));
  }

  public interface OnImageExistsListener {
    void onImageExists(boolean exists);
  }
}
