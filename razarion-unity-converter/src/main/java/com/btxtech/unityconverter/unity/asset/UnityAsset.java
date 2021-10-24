package com.btxtech.unityconverter.unity.asset;

import com.btxtech.unityconverter.unity.asset.type.AssetType;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UnityAsset {
    private final HashMap<String, AssetType> assets = new HashMap<>();

    public void addAssetType(AssetType assetType) {
        assets.put(assetType.getGuid(), assetType);
    }

    public Fbx getFbx(Reference reference) {
        AssetType assetType = assets.get(reference.getGuid());
        if (assetType == null) {
            throw new IllegalArgumentException("No AssetType found with Reference: " + reference);
        }
        return (Fbx) assetType;
    }

    public List<MeshFilter> getMeshFilterPrefabs() {
        return assets.values().stream()
                .filter(assetType -> assetType.getClass().equals(Prefab.class))
                .map(assetType -> (Prefab) assetType)
                .flatMap(prefab -> prefab.getMeshFilters().stream())
                .collect(Collectors.toList());
    }
}
