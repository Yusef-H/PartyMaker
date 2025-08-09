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
}
