package com.btxtech.unityconverter.unity.asset.meta;

import java.util.Map;

public class ModelImporter {
    private Map<String, String> fileIDToRecycleName;

    public Map<String, String> getFileIDToRecycleName() {
        return fileIDToRecycleName;
    }

    public void setFileIDToRecycleName(Map<String, String> fileIDToRecycleName) {
        this.fileIDToRecycleName = fileIDToRecycleName;
    }
}
