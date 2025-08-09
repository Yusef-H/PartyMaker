package com.example.partymaker.data.model;

/** Represents a message for GPT-based chat (role + content). */
public class ChatMessageGpt {

  // Role Constants
  public static final String ROLE_USER = "user";
  public static final String ROLE_ASSISTANT = "assistant";
  public static final String ROLE_SYSTEM = "system";

  /** The role of the message (e.g., user, assistant). */
  public final String role;

  /** The content of the message. */
  public final String content;

  /**
   * Constructs a GPT chat message with validation.
   *
   * @param role the role (user/assistant/system)
   * @param content the message content
   * @throws IllegalArgumentException if role or content is null/empty
   */
  public ChatMessageGpt(String role, String content) {
    if (role == null || role.trim().isEmpty()) {
      throw new IllegalArgumentException("Role cannot be null or empty");
    }
    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("Content cannot be null or empty");
    }

    this.role = role.trim();
    this.content = content.trim();
  }

  /**
   * Creates a user message.
   *
   * @param content the message content
   * @return ChatMessageGpt with user role
   */
  public static ChatMessageGpt createUserMessage(String content) {
    return new ChatMessageGpt(ROLE_USER, content);
  }

  /**
   * Creates an assistant message.
   *
   * @param content the message content
   * @return ChatMessageGpt with assistant role
   */
  public static ChatMessageGpt createAssistantMessage(String content) {
    return new ChatMessageGpt(ROLE_ASSISTANT, content);
  }

  /**
   * Creates a system message.
   *
   * @param content the message content
   * @return ChatMessageGpt with system role
   */
  public static ChatMessageGpt createSystemMessage(String content) {
    return new ChatMessageGpt(ROLE_SYSTEM, content);
  }

  /**
   * Checks if this is a user message.
   *
   * @return true if role is user
   */
  public boolean isUserMessage() {
    return ROLE_USER.equals(role);
  }

  /**
   * Checks if this is an assistant message.
   *
   * @return true if role is assistant
   */
  public boolean isAssistantMessage() {
    return ROLE_ASSISTANT.equals(role);
  }

  /**
   * Checks if this is a system message.
   *
   * @return true if role is system
   */
  public boolean isSystemMessage() {
    return ROLE_SYSTEM.equals(role);
  }
}
