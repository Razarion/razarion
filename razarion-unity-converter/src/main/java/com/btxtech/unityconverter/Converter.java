package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.asset.Asset;
import com.btxtech.unityconverter.unity.asset.UnityAsset;
import com.btxtech.unityconverter.unity.asset.AssetReader;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.Transform;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Converter {
    private static final Logger LOGGER = Logger.getLogger(Converter.class.getName());

    public static void main(String[] args) {
        try {
            UnityAsset unityAsset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta");

            Asset asset = new Asset().unityAssetGuid(unityAsset.getGuid()).internalName(unityAsset.getName());

            List<Prefab> prefabs = unityAsset.getAssetTypes(Prefab.class);
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
                        Prefab p = unityAsset.getAssetType(reference);
                        // System.out.println(reference);
                        if (p != null) {
                            p.getGameObjects().stream()
                                    .forEach(gameObject -> {
                                        MeshFilter meshFilter = p.getMeshFilter(gameObject);
                                        if (meshFilter != null) {
                                            Fbx fbx = unityAsset.getAssetType(meshFilter.getM_Mesh());
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
                        if (unityAsset.getAssetType(t.getM_CorrespondingSourceObject()) == null) {
                            System.out.println("Transformation Not found: " + t.getM_CorrespondingSourceObject());
                        }
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error Converter Asset", e);
        }
    }
}
