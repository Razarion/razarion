package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.asset.Mesh;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.unityconverter.unity.asset.AssetReader;
import com.btxtech.unityconverter.unity.asset.UnityAsset;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.Transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Converter {
    private static final Logger LOGGER = Logger.getLogger(Converter.class.getName());

    public static void main(String[] args) {
        MeshContainer meshContainer = readMeshContainers().get(0);
        meshContainer = meshContainer;
    }

    public static List<MeshContainer> readMeshContainers() {
        List<MeshContainer> meshContainers = new ArrayList<>();
        try {
            UnityAsset unityAsset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta");


            List<Prefab> prefabs = unityAsset.getAssetTypes(Prefab.class);
            Prefab prefab = prefabs.get(2); // Vehicle_11

            MeshContainer meshContainer = createMeshContainer(prefab, unityAsset);
            meshContainer.setId(1);
            meshContainers.add(meshContainer);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error Converter Asset", e);
        }
        return meshContainers;
    }

    private static MeshContainer createMeshContainer(Prefab prefab, UnityAsset unityAsset) {
        List<GameObject> gameObjects = prefab.getGameObjects();
        GameObject mainGameObject = gameObjects.get(0);

        Transform transform = mainGameObject.getM_Component()
                .stream()
                .map(componentReference -> prefab.getComponent(componentReference.getComponent()))
                .filter(c -> c instanceof Transform)
                .map(c -> (Transform) c)
                .findFirst().orElseThrow(IllegalStateException::new);

        Shape3DManager shape3DManager = new Shape3DManager();
        List<MeshContainer> childMeshContainers = new ArrayList<>();
        transform.getM_Children()
                .stream()
                .map(prefab::getComponent)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .map(Transform::getM_CorrespondingSourceObject)
                .forEach(reference -> {
                    Prefab p = unityAsset.getAssetType(reference);
                    MeshContainer meshContainer = new MeshContainer();
                    childMeshContainers.add(meshContainer);
                    if (p != null) {
                        p.getGameObjects().forEach(gameObject -> {
                            MeshFilter meshFilter = p.getMeshFilter(gameObject);
                            if (meshFilter != null) {
                                Fbx fbx = unityAsset.getAssetType(meshFilter.getM_Mesh());
                                String meshName = fbx.getMeshName(meshFilter.getM_Mesh().getFileID());
                                meshContainer.setName(gameObject.getM_Name());
                                meshContainer.setMesh(new Mesh().shape3DId(shape3DManager.getShape3DId4Fbx(fbx)).element3DId(meshName));
                            } else {
                                System.out.println("No MeshFilter: " + gameObject.getM_Name());
                            }
                        });
                    }
                });


        // Errors ---
        System.out.println("--------- ERRORS ---------");
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
        // Errors Ends ---

        return new MeshContainer()
                .name(mainGameObject.getM_Name())
                .children(childMeshContainers);
    }
}
