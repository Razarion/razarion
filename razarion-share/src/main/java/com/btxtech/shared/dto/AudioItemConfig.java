package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 25.12.2016.
 */
public class AudioItemConfig implements Config {
    private int id;
    private String type;
    private int size;
    private String internalName;
    private String dataUrl;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AudioItemConfig id(int id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AudioItemConfig type(String type) {
        this.type = type;
        return this;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public AudioItemConfig size(int size) {
        this.size = size;
        return this;
    }

    public AudioItemConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return Config.super.createObjectNameId();
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public AudioItemConfig setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
        return this;
    }
}
