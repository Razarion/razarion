package com.btxtech.client.dialog.common;

/**
 * Created by Beat
 * on 30.07.2017.
 */
public class MessageImage {
    private String message;
    private Integer imageId;

    public MessageImage(String message, int imageId) {
        this.message = message;
        this.imageId = imageId;
    }

    public String getMessage() {
        return message;
    }

    public Integer getImageId() {
        return imageId;
    }
}
