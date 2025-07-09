package com.example.partymaker.data.model;

import java.util.HashMap;

public class ChatMessage {
  private String messageText;
  private String messageUser;
  private String messageTime;
  private String MessageKey;
  private String groupId; // Added groupId field
  private String msg1;
  private HashMap<String, Object> messageContent;

  public ChatMessage(
      String messageText, String messageUser, String messageTime, String messageKey) {
    this.messageText = messageText;
    this.messageUser = messageUser;
    this.messageTime = messageTime;
    MessageKey = messageKey;
  }

  public ChatMessage() {}

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
    return MessageKey;
  }

  public void setMessageKey(String messageKey) {
    MessageKey = messageKey;
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
