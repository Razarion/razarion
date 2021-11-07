package com.btxtech.unityconverter.unity.asset;

import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.Transform;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class AssetReaderTest {
    @Test
    void read() {
        UnityAsset asset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor");

        List<Prefab> prefabs = asset.getAssetTypes(Prefab.class);
        Prefab prefab = prefabs.get(10);
        List<GameObject> gameObjects = prefab.getGameObjects();
        GameObject mainGameObject = gameObjects.get(0);

        System.out.println("--------------------------------------------------------------------");
        System.out.println(mainGameObject.getM_Name());
        Transform transform = mainGameObject.getM_Component()
                .stream()
                .map(componentReference -> prefab.getComponent(componentReference.getComponent()))
                .filter(c -> c instanceof Transform)
                .map(c -> (Transform) c)
                .findFirst().orElseThrow(IllegalStateException::new);

        transform.getM_Children()
                .stream()
                .map(prefab::getComponent)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .map(Transform::getM_CorrespondingSourceObject)
                .forEach(reference -> {
                    Prefab p = asset.getAssetType(reference);
                    // System.out.println(reference);
                    if (p != null) {
                        p.getGameObjects().stream()
                                .forEach(gameObject -> {
                                    MeshFilter meshFilter = p.getMeshFilter(gameObject);
                                    if (meshFilter != null) {
                                        Fbx fbx = asset.getAssetType(meshFilter.getM_Mesh());
                                        String meshName = fbx.getMeshName(meshFilter.getM_Mesh().getFileID());
                                        System.out.println("--" + gameObject.getM_Name() + ": " + meshName + ":" + fbx.getMeta().getAssetFile());
                                    } else {
                                        System.out.println("--" + gameObject.getM_Name());
                                    }
                                });
                    }
                });

        // Errors ---
        transform.getM_Children().forEach(reference -> {
            if (prefab.getComponent(reference) == null) {
                System.out.println("Not found: " + reference);
            }
        });
        transform.getM_Children().stream()
                .map(prefab::getComponent)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .forEach(t -> {
                    if (asset.getAssetType(t.getM_CorrespondingSourceObject()) == null) {
                        System.out.println("Transformation Not found: " + t.getM_CorrespondingSourceObject());
                    }
                });
    }

//    @Test
//    void read2() {
//        UnityAsset asset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor");
//
//        List<Prefab> meshFilterPrefabs = asset.getMeshFilterPrefabs();
//        Prefab meshFilterPrefab = meshFilterPrefabs.get(0);
//
//        System.out.println(meshFilterPrefab.getGameObjects().getM_Name());
//        MeshFilter meshFilter = meshFilterPrefab.getComponents(MeshFilter.class).get(0);
//        System.out.println(meshFilter);
//        Fbx fbx = asset.getAssetType(meshFilter.getM_Mesh());
//        System.out.println(fbx);
//        String meshName = fbx.getMeshName(meshFilter.getM_Mesh().getFileID());
//        System.out.println(meshName);
//        MeshRenderer meshRenderer = meshFilterPrefab.getComponents(MeshRenderer.class).get(0);
//        System.out.println(meshRenderer);
//        MaterialAssetType material = asset.getAssetType(meshRenderer.getM_Materials().get(0));
//        System.out.println(material);
//        System.out.println(material.getMaterial());
//        material.getMaterial().getSavedProperties().getTexEnvs().stream().flatMap(stringTexture2DMap -> stringTexture2DMap.entrySet().stream()).forEach(stringTexture2DEntry -> {
//            System.out.println("  " + stringTexture2DEntry.getKey() + ": " + asset.getAssetType(stringTexture2DEntry.getValue().getTexture()));
//        });
//
//    }
}