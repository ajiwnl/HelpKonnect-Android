package com.helpkonnect.mobileapp;

public class CommentModel {
    private String username;
    private String comment;
    private float rating;

    public CommentModel(String username, String comment, float rating) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
    }

    public String getUserName() {
        return username;
    }

    public String getCommentText() {
        return comment;
    }

    public float getRating() { return rating; }
}

