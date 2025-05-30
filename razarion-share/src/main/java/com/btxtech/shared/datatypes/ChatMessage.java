package com.btxtech.shared.datatypes;

import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@JSONMapper
public class ChatMessage {
    private String userId;
    private String userName;
    private String message;

    public String getUserId() {
        return userId;
    }

    public ChatMessage setUserId(String userId) {
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
