package com.example.benschat;

public class ChatMessage {
  private String messageId;
  private String userId;
  private String username;
  private String message;
  private long timestamp;

  public ChatMessage() {
    // Empty constructor for Firebase
  }

  public ChatMessage(String messageId, String userId, String username, String message, long timestamp) {
    this.messageId = messageId;
    this.userId = userId;
    this.username = username;
    this.message = message;
    this.timestamp = timestamp;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getMessage() {
    return message;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
