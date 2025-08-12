package com.example.partymaker.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.partymaker.data.model.ChatMessage;
import java.util.List;

/**
 * Data Access Object for ChatMessage entities. Provides methods to query, insert, and update chat
 * messages in the local database. Following Room DAO patterns with proper query optimization.
 */
@Dao
public interface ChatMessageDao {

  // Table and column constants
  String TABLE_NAME = "chat_messages";
  String COLUMN_MESSAGE_KEY = "messageKey";
  String COLUMN_GROUP_KEY = "groupKey";
  String COLUMN_TIMESTAMP = "timestamp";

  // SQL Query constants
  String SELECT_ALL_COLUMNS = "SELECT * FROM " + TABLE_NAME;
  String WHERE_MESSAGE_KEY = " WHERE " + COLUMN_MESSAGE_KEY + " = :messageKey";
  String WHERE_GROUP_KEY = " WHERE " + COLUMN_GROUP_KEY + " = :groupKey";
  String ORDER_BY_TIMESTAMP = " ORDER BY " + COLUMN_TIMESTAMP + " ASC";
  String DELETE_FROM_TABLE = "DELETE FROM " + TABLE_NAME;

  /**
   * Gets a chat message by its key
   *
   * @param messageKey The message key
   * @return The chat message
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_MESSAGE_KEY)
  ChatMessage getMessageByKey(String messageKey);

  /**
   * Gets all messages for a group
   *
   * @param groupKey The group key
   * @return List of messages for the group
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY + ORDER_BY_TIMESTAMP)
  List<ChatMessage> getMessagesForGroup(String groupKey);

  /**
   * Gets all messages for a group as LiveData
   *
   * @param groupKey The group key
   * @return LiveData containing list of messages for the group
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY + ORDER_BY_TIMESTAMP)
  LiveData<List<ChatMessage>> observeMessagesForGroup(String groupKey);
  
  /**
   * Gets messages for a group with pagination
   *
   * @param groupKey The group key
   * @param limit Maximum number of messages to return
   * @return LiveData containing paginated messages
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY + " ORDER BY timestamp DESC LIMIT :limit")
  LiveData<List<ChatMessage>> getGroupMessages(String groupKey, int limit);
  
  /**
   * Gets new messages after a specific timestamp
   *
   * @param groupKey The group key
   * @param lastTimestamp The timestamp after which to fetch messages
   * @param limit Maximum number of messages to return
   * @return LiveData containing new messages
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY + " AND timestamp > :lastTimestamp ORDER BY timestamp DESC LIMIT :limit")
  LiveData<List<ChatMessage>> getNewMessages(String groupKey, long lastTimestamp, int limit);
  
  /**
   * Gets unread message count for a group
   *
   * @param groupKey The group key
   * @param currentUserId The current user ID (to exclude own messages)
   * @return LiveData containing unread message count
   */
  @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE groupKey = :groupKey AND encrypted = 0 AND senderKey != :currentUserId")
  LiveData<Integer> getUnreadMessageCount(String groupKey, String currentUserId);

  /**
   * Inserts a message
   *
   * @param message The message to insert
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertMessage(ChatMessage message);

  /**
   * Inserts multiple messages
   *
   * @param messages The messages to insert
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertMessages(List<ChatMessage> messages);

  /**
   * Updates a message
   *
   * @param message The message to update
   */
  @Update
  void updateMessage(ChatMessage message);
  
  /**
   * Mark all messages as read for a group (simplified - using encrypted field as read status)
   *
   * @param groupKey The group key
   * @param currentUserId The current user ID
   * @return Number of messages marked as read
   */
  @Query("UPDATE " + TABLE_NAME + " SET encrypted = 1 WHERE groupKey = :groupKey AND senderKey != :currentUserId")
  int markAllMessagesAsRead(String groupKey, String currentUserId);

  /**
   * Deletes a message by its key
   *
   * @param messageKey The message key
   */
  @Query(DELETE_FROM_TABLE + WHERE_MESSAGE_KEY)
  void deleteMessageByKey(String messageKey);

  /**
   * Deletes all messages for a group
   *
   * @param groupKey The group key
   */
  @Query(DELETE_FROM_TABLE + WHERE_GROUP_KEY)
  void deleteMessagesForGroup(String groupKey);

  /** Deletes all messages */
  @Query(DELETE_FROM_TABLE)
  void deleteAllMessages();
  
  /**
   * Delete old messages based on timestamp
   *
   * @param cutoffTime Messages older than this will be deleted
   * @return Number of deleted messages
   */
  @Query(DELETE_FROM_TABLE + " WHERE timestamp < :cutoffTime")
  int deleteOldMessages(long cutoffTime);
  
  /**
   * Get latest message for a group
   *
   * @param groupKey The group key
   * @return The latest message for the group
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY + " ORDER BY timestamp DESC LIMIT 1")
  ChatMessage getLatestMessage(String groupKey);
}
