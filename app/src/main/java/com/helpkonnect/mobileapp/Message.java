package com.helpkonnect.mobileapp;

public class Message {
    private String userId;
    private String message;
    private boolean isUserMessage;
    private long timestamp;

    public Message(String userId, String message, boolean isUserMessage, long timestamp) {
        this.userId = userId;
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

