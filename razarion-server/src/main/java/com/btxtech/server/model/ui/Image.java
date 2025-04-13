package com.btxtech.server.model.ui;

public class Image {
    private final byte[] data;
    private final String type;

    public Image(byte[] data, String type) {
        this.data = data;
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public String getType() {
        return type;
    }
}
