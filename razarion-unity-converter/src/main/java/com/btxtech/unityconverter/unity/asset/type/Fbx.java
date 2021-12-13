package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;

import java.io.File;

public class Fbx extends AssetType {
    public Fbx(Meta meta) {
        super(meta);
    }

    public String getMeshName(String filedId) {
        String meshName = getMeta().getModelImporter().getFileIDToRecycleName().get(filedId);
        if (meshName == null) {
            throw new IllegalArgumentException("No mesh name for fileId: " + filedId);
        }
        return meshName;
    }

    public File getColladaFile() {
        return new File(getAssetFile().getParentFile(), getName() + ".dae");
    }

    @Override
    public String toString() {
        return "Fbx: " + getAssetFile();
    }
}
