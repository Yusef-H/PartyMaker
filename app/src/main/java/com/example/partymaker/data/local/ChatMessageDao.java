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
 * messages in the local database.
 */
@Dao
public interface ChatMessageDao {

  /**
   * Gets a chat message by its key
   *
   * @param messageKey The message key
   * @return The chat message
   */
  @Query("SELECT * FROM chat_messages WHERE messageKey = :messageKey")
  ChatMessage getMessageByKey(String messageKey);

  /**
   * Gets all messages for a group
   *
   * @param groupKey The group key
   * @return List of messages for the group
   */
  @Query("SELECT * FROM chat_messages WHERE groupKey = :groupKey ORDER BY timestamp ASC")
  List<ChatMessage> getMessagesForGroup(String groupKey);

  /**
   * Gets all messages for a group as LiveData
   *
   * @param groupKey The group key
   * @return LiveData containing list of messages for the group
   */
  @Query("SELECT * FROM chat_messages WHERE groupKey = :groupKey ORDER BY timestamp ASC")
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
  @Query("DELETE FROM chat_messages WHERE messageKey = :messageKey")
  void deleteMessageByKey(String messageKey);

  /**
   * Deletes all messages for a group
   *
   * @param groupKey The group key
   */
  @Query("DELETE FROM chat_messages WHERE groupKey = :groupKey")
  void deleteMessagesForGroup(String groupKey);

  /** Deletes all messages */
  @Query("DELETE FROM chat_messages")
  void deleteAllMessages();
}
