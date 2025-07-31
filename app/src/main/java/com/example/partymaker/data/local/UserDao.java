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
 * local database.
 */
@Dao
public interface UserDao {

    /**
     * Gets a user by their key
     *
     * @param userKey The user key
     * @return The user
     */
    @Query("SELECT * FROM users WHERE userKey = :userKey")
    User getUserByKey(String userKey);

    /**
     * Gets a user by their key as LiveData
     *
     * @param userKey The user key
     * @return LiveData containing the user
     */
    @Query("SELECT * FROM users WHERE userKey = :userKey")
    LiveData<User> observeUserByKey(String userKey);

    /**
     * Gets a user by their email
     *
     * @param email The user's email
     * @return The user
     */
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);

    /**
     * Gets all users
     *
     * @return List of all users
     */
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    /**
     * Gets all users as LiveData
     *
     * @return LiveData containing list of all users
     */
    @Query("SELECT * FROM users")
    LiveData<List<User>> observeAllUsers();

    /**
     * Inserts a user
     *
     * @param user The user to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    /**
     * Inserts multiple users
     *
     * @param users The users to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<User> users);

    /**
     * Updates a user
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
    @Query("DELETE FROM users WHERE userKey = :userKey")
    void deleteUserByKey(String userKey);

    /**
     * Deletes all users
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();
}
