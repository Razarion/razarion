package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 25.12.2016.
 */
public class AudioItemConfig {
    private int id;
    private String type;
    private int size;
    private String internalName;
    private String dataUrl;

    public int getId() {
        return id;
    }

    public AudioItemConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public AudioItemConfig setType(String type) {
        this.type = type;
        return this;
    }

    public int getSize() {
        return size;
    }

    public AudioItemConfig setSize(int size) {
        this.size = size;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public AudioItemConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public AudioItemConfig setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
        return this;
    }
}
