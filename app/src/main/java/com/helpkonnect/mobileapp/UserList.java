package com.helpkonnect.mobileapp;

public class UserList {
    private String userId;
    private String displayName;
    private String imageUrl;

    public UserList(String userId, String displayName, String imageUrl) {
        this.userId = userId;
        this.displayName = displayName;
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
