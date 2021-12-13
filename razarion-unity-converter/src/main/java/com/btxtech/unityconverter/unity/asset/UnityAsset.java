package com.btxtech.unityconverter.unity.asset;

import com.btxtech.unityconverter.unity.asset.type.AssetType;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UnityAsset {
    private String guid;
    private String name;
    private final HashMap<String, AssetType> assets = new HashMap<>();

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, AssetType> getAssets() {
        return assets;
    }

    public void addAssetType(AssetType assetType) {
        assets.put(assetType.getGuid(), assetType);
    }

    public <T extends AssetType> T getAssetType(Reference reference) {
        return (T)assets.get(reference.getGuid());
    }

    public <T extends AssetType> List<T> getAssetTypes(Class<T> clazz) {
        return assets.values().stream()
                .filter(assetType -> assetType.getClass().equals(clazz))
                .map(assetType -> (T) assetType)
                .collect(Collectors.toList());
    }

    public Prefab findPrefab(String name) {
        return assets.values().stream()
                .filter(assetType -> assetType.getClass().equals(Prefab.class))
                .map(assetType -> (Prefab) assetType)
                .filter(prefab -> prefab.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Prefab for: " + name));
    }
}
