package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;

import java.io.File;

public class AssetType {
    private final Meta meta;

    public AssetType(Meta meta) {
        this.meta = meta;
    }

    public Meta getMeta() {
        return meta;
    }

    public String getGuid() {
        return meta.getGuid();
    }

    public File getAssetFile() {
        return meta.getAssetFile();
    }
}