package com.btxtech.unityconverter;

public class MaterialContext {
    private String mainTextureGuid;
    private String mainTextureFile;

    public String getMainTextureGuid() {
        return mainTextureGuid;
    }

    public String getMainTextureFile() {
        return mainTextureFile;
    }

    public void setMainTextureGuid(String mainTextureGuid) {
        this.mainTextureGuid = mainTextureGuid;
    }

    public void setMainTextureFile(String mainTextureFile) {
        this.mainTextureFile = mainTextureFile;
    }

    public boolean isValid() {
        return mainTextureFile != null;
    }
}
