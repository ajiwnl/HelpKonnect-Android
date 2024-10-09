package com.helpkonnect.mobileapp;

public class CommentModel {
    private String userName;
    private String commentText;

    public CommentModel(String userName, String commentText) {
        this.userName = userName;
        this.commentText = commentText;
    }

    public String getUserName() {
        return userName;
    }

    public String getCommentText() {
        return commentText;
    }
}

