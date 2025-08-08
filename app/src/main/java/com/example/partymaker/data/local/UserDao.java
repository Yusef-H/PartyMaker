package com.example.partymaker.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.partymaker.data.model.User;
import java.util.List;

/**
 * Data Access Object for User entities. Provides methods to query, insert, and update users in the
 * local database using Room persistence library.
 */
@Dao
public interface UserDao {

  // SQL Query Constants
  String QUERY_USER_BY_KEY = "SELECT * FROM users WHERE userKey = :userKey";
  String QUERY_USER_BY_EMAIL = "SELECT * FROM users WHERE email = :email";
  String QUERY_ALL_USERS = "SELECT * FROM users";
  String QUERY_DELETE_USER_BY_KEY = "DELETE FROM users WHERE userKey = :userKey";
  String QUERY_DELETE_ALL_USERS = "DELETE FROM users";

  /**
   * Gets a user by their key
   *
   * @param userKey The user key
   * @return The user, or null if not found
   */
  @Query(QUERY_USER_BY_KEY)
  User getUserByKey(String userKey);

  /**
   * Gets a user by their key as LiveData for reactive updates
   *
   * @param userKey The user key
   * @return LiveData containing the user, automatically updated when data changes
   */
  @Query(QUERY_USER_BY_KEY)
  LiveData<User> observeUserByKey(String userKey);

  /**
   * Gets a user by their email
   *
   * @param email The user's email
   * @return The user, or null if not found
   */
  @Query(QUERY_USER_BY_EMAIL)
  User getUserByEmail(String email);

  /**
   * Gets all users from the local database
   *
   * @return List of all users, empty list if no users found
   */
  @Query(QUERY_ALL_USERS)
  List<User> getAllUsers();

  /**
   * Gets all users as LiveData for reactive updates
   *
   * @return LiveData containing list of all users, automatically updated when data changes
   */
  @Query(QUERY_ALL_USERS)
  LiveData<List<User>> observeAllUsers();

  /**
   * Inserts a user into the local database Uses REPLACE strategy to handle conflicts
   *
   * @param user The user to insert
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertUser(User user);

  /**
   * Inserts multiple users into the local database Uses REPLACE strategy to handle conflicts
   *
   * @param users The list of users to insert
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertUsers(List<User> users);

  /**
   * Updates an existing user in the local database User must exist or operation will be ignored
   *
   * @param user The user to update
   */
  @Update
  void updateUser(User user);

  /**
   * Deletes a user by their key
   *
   * @param userKey The user key
   */
  @Query(QUERY_DELETE_USER_BY_KEY)
  void deleteUserByKey(String userKey);

  /**
   * Deletes all users from the local database Use with caution - this operation cannot be undone
   */
  @Query(QUERY_DELETE_ALL_USERS)
  void deleteAllUsers();
}
