package com.example.benschat;

public class User {
  private String userId;
  private String displayName;
  private String email;
  private String photoUrl;

  // Default constructor required for calls to DataSnapshot.getValue(User.class)
  public User() {
    // Default constructor
  }

  public User(String userId, String displayName, String email) {
    this.userId = userId;
    this.displayName = displayName;
    this.email = email;
    this.photoUrl = "http://www.gravatar.com/avatar/?d=mp"; // Set the default photo URL value here
  }

  public String getUserId() {
    return userId;
  }


  public String getDisplayName() {
    return this.displayName;
  }


  public String getPhotoUrl() {
    return this.photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }
}
