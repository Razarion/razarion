package com.btxtech.shared.datatypes;

import jsinterop.annotations.JsType;
import org.dominokit.jackson.annotation.JSONMapper;

@JSONMapper
@JsType
public class ChatMessage {
    private String userName;
    private String message;
    /** A clip that plays with the message (/rest/chat-controller/video/{id}), or null. */
    private Integer videoId;

    public Integer getVideoId() {
        return videoId;
    }

    public void setVideoId(Integer videoId) {
        this.videoId = videoId;
    }

    public ChatMessage videoId(Integer videoId) {
        this.videoId = videoId;
        return this;
    }

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
