package com.btxtech.unityconverter.unity.model;

public class Reference {
    private String fileID;
    private String guid;
    private String type;

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Reference fileID(String fileID) {
        setFileID(fileID);
        return this;
    }

    public Reference guid(String guid) {
        setGuid(guid);
        return this;
    }

    public Reference type(String type) {
        setType(type);
        return this;
    }

    @Override
    public String toString() {
        return "Reference{" +
                "fileID='" + fileID + '\'' +
                ", guid='" + guid + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}