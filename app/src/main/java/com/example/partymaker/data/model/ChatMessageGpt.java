package com.example.partymaker.data.model;

/** Represents a message for GPT-based chat (role + content). */
public class ChatMessageGpt {
  /** The role of the message (e.g., user, assistant). */
  public final String role;
  /** The content of the message. */
  public final String content;

  /**
   * Constructs a GPT chat message.
   *
   * @param role the role (user/assistant)
   * @param content the message content
   */
  public ChatMessageGpt(String role, String content) {
    this.role = role;
    this.content = content;
  }
}
