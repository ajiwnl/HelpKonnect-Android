package com.helpkonnect.mobileapp;

public class ResourceModel {
    private String title;
    private String description;
    private int imageResId; // Image resource ID

    public ResourceModel(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}

