package com.btxtech.shared.datatypes;

import jsinterop.annotations.JsType;
import org.dominokit.jackson.annotation.JSONMapper;

@JSONMapper
@JsType
public class ChatMessage {
    private String userName;
    private String message;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatMessage userName(String userName) {
        this.userName = userName;
        return this;
    }

    public ChatMessage message(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
