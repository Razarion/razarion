package com.btxtech.unityconverter;

import com.btxtech.unityconverter.unity.asset.type.Fbx;

public interface AssetContext {
    Integer getThreeJsModelId4Fbx(Fbx fbx, MaterialInfo materialInfo, String assetName);
}
