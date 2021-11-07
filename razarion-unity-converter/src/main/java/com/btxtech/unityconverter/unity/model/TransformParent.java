package com.btxtech.unityconverter.unity.model;

public class TransformParent {
    private String fileID;

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    @Override
    public String toString() {
        return "TransformParent{" +
                "fileID='" + fileID + '\'' +
                '}';
    }
}
