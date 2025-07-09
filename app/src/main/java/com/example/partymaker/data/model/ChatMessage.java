package com.example.partymaker.data.model;

import java.util.HashMap;

/** Represents a chat message in a group chat. */
public class ChatMessage {
  /** The text content of the message. */
  private String messageText;
  /** The user who sent the message. */
  private String messageUser;
  /** The time the message was sent. */
  private String messageTime;
  /** The unique key for the message. */
  private String messageKey;
  /** The group ID this message belongs to. */
  private String groupId;
  /** Additional message content (optional). */
  private String msg1;
  /** Map of message content (optional). */
  private HashMap<String, Object> messageContent;

  /** Default constructor. */
  public ChatMessage() {}

  /**
   * Constructs a chat message with main fields.
   *
   * @param messageText the text
   * @param messageUser the user
   * @param messageTime the time
   * @param messageKey the key
   */
  public ChatMessage(
      String messageText, String messageUser, String messageTime, String messageKey) {
    this.messageText = messageText;
    this.messageUser = messageUser;
    this.messageTime = messageTime;
    this.messageKey = messageKey;
  }

  public String getMessageText() {
    return messageText;
  }

  public void setMessageText(String messageText) {
    this.messageText = messageText;
  }

  public String getMessageUser() {
    return messageUser;
  }

  public void setMessageUser(String messageUser) {
    this.messageUser = messageUser;
  }

  public String getMessageTime() {
    return messageTime;
  }

  public void setMessageTime(String messageTime) {
    this.messageTime = messageTime;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public void setMessageKey(String messageKey) {
    this.messageKey = messageKey;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getMsg1() {
    return msg1;
  }

  public void setMsg1(String msg1) {
    this.msg1 = msg1;
  }

  public HashMap<String, Object> getMessageContent() {
    return messageContent;
  }

  public void setMessageContent(HashMap<String, Object> messageContent) {
    this.messageContent = messageContent;
  }
}
