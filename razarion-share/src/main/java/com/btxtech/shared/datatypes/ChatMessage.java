package com.btxtech.shared.datatypes;

import jsinterop.annotations.JsType;
import org.dominokit.jackson.annotation.JSONMapper;

@JSONMapper
@JsType
public class ChatMessage {
    private String userName;
    private String message;
    /**
     * A reel on our Instagram the message points to (https://www.instagram.com/reel/.../), or null.
     * The chat shows it behind a button and loads nothing from Instagram before that is tapped.
     */
    private String instagramUrl;
    /** When the message was written, epoch milliseconds; null for a message from before it was sent along. */
    private Long timestamp;

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

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public ChatMessage userName(String userName) {
        this.userName = userName;
        return this;
    }

    public ChatMessage message(String message) {
        this.message = message;
        return this;
    }

    public ChatMessage instagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
        return this;
    }

    public ChatMessage timestamp(Long timestamp) {
        this.timestamp = timestamp;
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
