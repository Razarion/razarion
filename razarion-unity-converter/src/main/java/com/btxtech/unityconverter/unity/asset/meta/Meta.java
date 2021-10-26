package com.btxtech.unityconverter.unity.asset.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class Meta {
    private String fileExtension;
    private File assetFile;
    private String guid;
    private Boolean folderAsset;
    @JsonProperty("ModelImporter")
    private ModelImporter modelImporter;

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setAssetFile(File assetFile) {
        this.assetFile = assetFile;
    }

    public File getAssetFile() {
        return assetFile;
    }

    public Boolean getFolderAsset() {
        return folderAsset;
    }

    public void setFolderAsset(Boolean folderAsset) {
        this.folderAsset = folderAsset;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public ModelImporter getModelImporter() {
        return modelImporter;
    }

    public void setModelImporter(ModelImporter modelImporter) {
        this.modelImporter = modelImporter;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "fileExtension='" + fileExtension + '\'' +
                ", assetFile=" + assetFile +
                ", guid='" + guid + '\'' +
                ", folderAsset=" + folderAsset +
                '}';
    }

    public Meta fileExtension(String fileExtension) {
        setFileExtension(fileExtension);
        return this;
    }

    public Meta assetFile(File assetFile) {
        setAssetFile(assetFile);
        return this;
    }

    public Meta guid(String guid) {
        setGuid(guid);
        return this;
    }

    public Meta folderAsset(Boolean folderAsset) {
        setFolderAsset(folderAsset);
        return this;
    }
}
