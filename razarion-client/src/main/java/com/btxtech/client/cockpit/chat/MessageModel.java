package com.btxtech.client.cockpit.chat;

/**
 * Created by Beat
 * on 29.12.2017.
 */
public class MessageModel {
    private String userName;
    private String message;

    public String getUserName() {
        return userName;
    }

    public MessageModel setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MessageModel setMessage(String message) {
        this.message = message;
        return this;
    }
}
