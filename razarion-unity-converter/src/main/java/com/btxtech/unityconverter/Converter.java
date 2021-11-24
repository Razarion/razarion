package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.asset.Mesh;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.unityconverter.unity.asset.AssetReader;
import com.btxtech.unityconverter.unity.asset.UnityAsset;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.ModificationContainer;
import com.btxtech.unityconverter.unity.model.PrefabInstance;
import com.btxtech.unityconverter.unity.model.Transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Converter {
    private static final Logger LOGGER = Logger.getLogger(Converter.class.getName());

    public static void main(String[] args) {
        readMeshContainers();
        // MeshContainer meshContainer = readMeshContainers().get(0);
        // meshContainer = meshContainer;
    }

    public static List<MeshContainer> readMeshContainers() {
        List<MeshContainer> meshContainers = new ArrayList<>();
        try {
            UnityAsset unityAsset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta");

            System.out.println("------------ Prefabs in Asset ------------");
            unityAsset.getAssetTypes(Prefab.class).stream().forEach(prefab -> {
                System.out.println(prefab.getGameObjects().get(0).getM_Name());
            });
            System.out.println("------------------------");

            Prefab prefab = unityAsset.findPrefab("Simple01");
            // Prefab prefab = unityAsset.findPrefab("Vehicle_11");
            System.out.println("Prefab: " + prefab.getGameObjects().get(0).getM_Name());

            MeshContainer meshContainer = createMeshContainer(prefab, unityAsset);
            meshContainer.setId(1);
            meshContainers.add(meshContainer);

            return meshContainers;
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

        ShapeTransform baseTransform = new ShapeTransform();
        baseTransform.setTranslateX(transform.getM_LocalPosition().getX());
        baseTransform.setTranslateY(transform.getM_LocalPosition().getY());
        baseTransform.setTranslateZ(transform.getM_LocalPosition().getZ());
        baseTransform.setRotateX(transform.getM_LocalRotation().getX());
        baseTransform.setRotateY(transform.getM_LocalRotation().getY());
        baseTransform.setRotateZ(transform.getM_LocalRotation().getZ());
        baseTransform.setScaleX(transform.getM_LocalScale().getX());
        baseTransform.setScaleY(transform.getM_LocalScale().getY());
        baseTransform.setScaleZ(transform.getM_LocalScale().getZ());

        Shape3DManager shape3DManager = new Shape3DManager();
        List<MeshContainer> childMeshContainers = new ArrayList<>();
        transform.getM_Children()
                .stream()
                .map(prefab::getComponent)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .forEach(childTransform -> {
                    Prefab p = unityAsset.getAssetType(childTransform.getM_CorrespondingSourceObject());
                    PrefabInstance prefabInstance = prefab.getComponent(childTransform.getM_PrefabInstance());
                    MeshContainer meshContainer = new MeshContainer();
                    childMeshContainers.add(meshContainer);
                    if (p != null) {
                        p.getGameObjects().forEach(gameObject -> {
                            MeshFilter meshFilter = p.getMeshFilter(gameObject);
                            if (meshFilter != null) {
                                Fbx fbx = unityAsset.getAssetType(meshFilter.getM_Mesh());
                                String meshName = fbx.getMeshName(meshFilter.getM_Mesh().getFileID());
                                meshContainer.setName(gameObject.getM_Name());
                                meshContainer.setMesh(new Mesh()
                                        .shape3DId(shape3DManager.getShape3DId4Fbx(fbx))
                                        .element3DId(meshName)
                                        .transformation(setupShapeTransform(prefabInstance.getM_Modification(), baseTransform)));
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

    private static ShapeTransform setupShapeTransform(ModificationContainer m_modification, ShapeTransform baseShapeTransform) {
        System.out.println("----- setupShapeTransform");
        if (m_modification == null || m_modification.getM_Modifications() == null) {
            return null;
        }
        ShapeTransform shapeTransform = baseShapeTransform.copyTRS();
        m_modification.getM_Modifications().forEach(modification -> {
            System.out.println("Read: " + modification.getPropertyPath().toLowerCase() + ": " + modification.getValue());
            switch (modification.getPropertyPath().toLowerCase()) {
                case ("m_localposition.x"):
                    shapeTransform.setTranslateX(shapeTransform.getTranslateX() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.y"):
                    shapeTransform.setTranslateY(shapeTransform.getTranslateY() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.z"):
                    shapeTransform.setTranslateZ(shapeTransform.getTranslateZ() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.x"):
                    shapeTransform.setRotateX(shapeTransform.getRotateX() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.y"):
                    shapeTransform.setRotateY(shapeTransform.getRotateY() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.z"):
                    shapeTransform.setRotateZ(shapeTransform.getRotateZ() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.x"):
                    shapeTransform.setScaleX(shapeTransform.getScaleX() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.y"):
                    shapeTransform.setScaleY(shapeTransform.getScaleY() + Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.z"):
                    shapeTransform.setScaleZ(shapeTransform.getScaleZ() + Double.parseDouble(modification.getValue()));
                    break;

                default:
                    System.out.println("Unknown transformation: " + modification.getPropertyPath().toLowerCase() + ": " + modification.getValue());
            }
        });
        System.out.println(shapeTransform);
        return shapeTransform;
    }
}
