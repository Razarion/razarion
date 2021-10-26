package com.btxtech.unityconverter.unity.asset;

import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

class AssetReaderTest {
    @Test
    void read() {
        UnityAsset asset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor");

        List<Prefab> meshFilterPrefabs = asset.getMeshFilterPrefabs();
        Prefab meshFilterPrefab = meshFilterPrefabs.get(0);

        System.out.println(meshFilterPrefab.getGameObject().getM_Name());
        MeshFilter meshFilter = meshFilterPrefab.getMeshFilters().get(0);
        System.out.println(meshFilter);
        Fbx fbx = asset.getFbx(meshFilter.getM_Mesh());
        System.out.println(fbx);
        String meshName = fbx.getMeshName(meshFilter.getM_Mesh().getFileID());
        System.out.println(meshName);
    }
}