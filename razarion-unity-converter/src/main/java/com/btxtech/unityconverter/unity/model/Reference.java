package com.btxtech.unityconverter.unity.model;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reference reference = (Reference) o;
        return Objects.equals(fileID, reference.fileID) && Objects.equals(guid, reference.guid) && Objects.equals(type, reference.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileID, guid, type);
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