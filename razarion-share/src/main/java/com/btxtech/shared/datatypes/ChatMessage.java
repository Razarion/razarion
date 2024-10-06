package com.btxtech.shared.datatypes;

import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@JSONMapper
public class ChatMessage {
    private int userId;
    private String userName;
    private String message;

    public int getUserId() {
        return userId;
    }

    public ChatMessage setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ChatMessage setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ChatMessage setMessage(String message) {
        this.message = message;
        return this;
    }
}
