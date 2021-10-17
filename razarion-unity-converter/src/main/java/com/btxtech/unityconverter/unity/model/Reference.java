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

    @Override
    public String toString() {
        return "Reference{" +
                "fileID='" + fileID + '\'' +
                ", guid='" + guid + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}