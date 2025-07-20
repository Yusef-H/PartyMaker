package com.example.partymaker.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a user in the PartyMaker application.
 * This class is annotated for Room database storage.
 */
@Entity(tableName = "users")
public class User {
    /** The user's unique key. */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "userKey")
    private String userKey;
    
    /** The user's display name. */
    @ColumnInfo(name = "username")
    private String username;
    
    /** The user's email address. */
    @ColumnInfo(name = "email")
    private String email;
    
    /** URL to the user's profile image. */
    @ColumnInfo(name = "profile_image_url")
    private String profileImageUrl;
    
    /** Map of friend keys. */
    @ColumnInfo(name = "friend_keys")
    private Map<String, Boolean> friendKeys;

    /** Default constructor. */
    public User() {
        this.friendKeys = new HashMap<>();
    }

    /**
     * Constructs a user with all fields.
     *
     * @param userKey The user's unique key
     * @param username The user's display name
     * @param email The user's email address
     * @param profileImageUrl URL to the user's profile image
     * @param friendKeys Map of friend keys
     */
    @Ignore
    public User(
            @NonNull String userKey,
            String username,
            String email,
            String profileImageUrl,
            Map<String, Boolean> friendKeys) {
        this.userKey = userKey;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.friendKeys = friendKeys != null ? friendKeys : new HashMap<>();
    }
    
    /**
     * Constructs a user with username and email.
     * Generates a random UUID for the userKey.
     *
     * @param username The user's display name
     * @param email The user's email address
     */
    @Ignore
    public User(String username, String email) {
        this(UUID.randomUUID().toString(), username, email, null, new HashMap<>());
    }

    /**
     * Gets the user's unique key.
     *
     * @return The user key
     */
    @NonNull
    public String getUserKey() {
        return userKey;
    }

    /**
     * Sets the user's unique key.
     *
     * @param userKey The user key
     */
    public void setUserKey(@NonNull String userKey) {
        this.userKey = userKey;
    }

    /**
     * Gets the user's display name.
     *
     * @return The username
     */
    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's display name.
     *
     * @param username The username
     */
    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Gets the user's display name (alternative field name).
     * This is for backward compatibility with existing data.
     *
     * @return The username
     */
    @PropertyName("userName")
    public String getUserName() {
        return username;
    }

    /**
     * Sets the user's display name (alternative field name).
     * This is for backward compatibility with existing data.
     *
     * @param userName The username
     */
    @PropertyName("userName")
    public void setUserName(String userName) {
        this.username = userName;
    }

    /**
     * Gets the user's email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the URL to the user's profile image.
     *
     * @return The profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the URL to the user's profile image.
     *
     * @param profileImageUrl The profile image URL
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Gets the map of friend keys.
     *
     * @return The friend keys map
     */
    public Map<String, Boolean> getFriendKeys() {
        return friendKeys;
    }

    /**
     * Sets the map of friend keys.
     *
     * @param friendKeys The friend keys map
     */
    public void setFriendKeys(Map<String, Boolean> friendKeys) {
        this.friendKeys = friendKeys;
    }
}
