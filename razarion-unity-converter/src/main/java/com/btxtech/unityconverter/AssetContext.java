package com.btxtech.unityconverter;

import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.model.Material;

public interface AssetContext {
    Integer getShape3DId4Fbx(Fbx fbx, Material material);
}
