package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;

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
}
