package com.btxtech.unityconverter.unity.asset;

import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.MaterialAssetType;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.MeshRenderer;
import org.junit.jupiter.api.Test;

import java.util.List;

class AssetReaderTest {
    @Test
    void read() {
        UnityAsset asset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor");

        List<Prefab> meshFilterPrefabs = asset.getMeshFilterPrefabs();
        Prefab meshFilterPrefab = meshFilterPrefabs.get(0);

        System.out.println(meshFilterPrefab.getGameObject().getM_Name());
        MeshFilter meshFilter = meshFilterPrefab.getComponents(MeshFilter.class).get(0);
        System.out.println(meshFilter);
        Fbx fbx = asset.getAssetType(meshFilter.getM_Mesh());
        System.out.println(fbx);
        String meshName = fbx.getMeshName(meshFilter.getM_Mesh().getFileID());
        System.out.println(meshName);
        MeshRenderer meshRenderer = meshFilterPrefab.getComponents(MeshRenderer.class).get(0);
        System.out.println(meshRenderer);
        MaterialAssetType material = asset.getAssetType(meshRenderer.getM_Materials().get(0));
        System.out.println(material);
        System.out.println(material.getMaterial());
        material.getMaterial().getSavedProperties().getTexEnvs().stream().flatMap(stringTexture2DMap -> stringTexture2DMap.entrySet().stream()).forEach(stringTexture2DEntry -> {
            System.out.println("  " + stringTexture2DEntry.getKey() + ": " + asset.getAssetType(stringTexture2DEntry.getValue().getTexture()));
        });

    }
}