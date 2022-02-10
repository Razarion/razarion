package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.Material;

import java.util.logging.Logger;

public class MaterialAssetType extends UnityObjectsAssetType {
    private static final Logger LOGGER = Logger.getLogger(MaterialAssetType.class.getName());

    public MaterialAssetType(Meta meta) {
        super(meta);
        try {
            LOGGER.fine("readMaterial: " + getMeta());
            loadObjectAndComponents(getAssetFile());
        } catch (Exception e) {
            throw new RuntimeException(meta.toString(), e);
        }
    }

    public Material getMaterial() {
        return (Material) getFirstUnityObject(Material.class);
    }

    @Override
    public String toString() {
        return "MaterialAssetType{" + super.toString() + "}";
    }
}
