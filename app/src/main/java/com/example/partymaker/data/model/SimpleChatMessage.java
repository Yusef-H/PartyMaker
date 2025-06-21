package com.example.partymaker.data.model;

public class SimpleChatMessage {
  public String role;
  public String content;

  public SimpleChatMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }
}
