package com.btxtech.unityconverter.unity.asset;

import com.btxtech.unityconverter.unity.asset.type.AssetType;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.Prefab;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Asset {
    private final HashMap<String, AssetType> assets = new HashMap<>();

    public void addAssetType(AssetType assetType) {
        assets.put(assetType.getGuid(), assetType);
    }

    public List<Fbx> getFbxes() {
        return assets.values().stream()
                .filter(assetType -> assetType.getClass().equals(Fbx.class))
                .map(assetType -> (Fbx) assetType)
                .collect(Collectors.toList());
    }

    public List<Prefab> getPrefabs() {
        return assets.values().stream()
                .filter(assetType -> assetType.getClass().equals(Prefab.class))
                .map(assetType -> (Prefab) assetType)
                .collect(Collectors.toList());
    }
}
