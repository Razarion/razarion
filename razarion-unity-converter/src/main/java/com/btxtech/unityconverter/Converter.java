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
        displayMeshContainers("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta");
    }

    public static void displayMeshContainers(String metaFilePath) {
        List<MeshContainer> meshContainers = new ArrayList<>();
        try {
            UnityAsset unityAsset = AssetReader.read(metaFilePath);

            System.out.println("------------ Prefabs in Asset ------------");
            unityAsset.getAssetTypes(Prefab.class).forEach(prefab -> {
                System.out.println(prefab.getGameObjects().get(0).getM_Name());
            });
            System.out.println("------------------------");

            // Prefab prefab = unityAsset.findPrefab("Simple01");
            Prefab prefab = unityAsset.findPrefab("Vehicle_11");
            System.out.println("Prefab: " + prefab.getGameObjects().get(0).getM_Name());

            MeshContainer meshContainer = createMeshContainer(prefab, unityAsset, new AssetContext() {
                @Override
                public Integer getShape3DId4Fbx(Fbx fbx) {
                    return -12345;
                }
            });
            meshContainer.setId(1);
            meshContainers.add(meshContainer);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error Converter Asset", e);
        }
    }

    public static MeshContainer createMeshContainer(Prefab prefab, UnityAsset unityAsset, AssetContext assetContext) {
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
                                meshContainer.setInternalName(gameObject.getM_Name());
                                meshContainer.setMesh(new Mesh()
                                        .shape3DId(assetContext.getShape3DId4Fbx(fbx))
                                        .element3DId(meshName)
                                        .shapeTransform(setupShapeTransform(prefabInstance, baseTransform)));
                            } else {
                                LOGGER.warning("No MeshFilter: " + gameObject.getM_Name());
                            }
                        });
                    }
                });


        // Errors ---
        transform.getM_Children().forEach(reference -> {
            if (prefab.getComponent(reference) == null) {
                LOGGER.warning("Not found: " + reference);
            }
        });
        transform.getM_Children().stream()
                .map(prefab::getComponent)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .forEach(t -> {
                    if (unityAsset.getAssetType(t.getM_CorrespondingSourceObject()) == null) {
                        LOGGER.warning("Transformation Not found: " + t.getM_CorrespondingSourceObject());
                    }
                });
        // Errors Ends ---

        return new MeshContainer()
                .internalName(mainGameObject.getM_Name())
                .guid(prefab.getGuid())
                .children(childMeshContainers);
    }

    private static ShapeTransform setupShapeTransform(PrefabInstance prefabInstance, ShapeTransform baseShapeTransform) {
        if (prefabInstance == null) {
            return null;
        }
        ModificationContainer m_modification = prefabInstance.getM_Modification();
        if (m_modification == null || m_modification.getM_Modifications() == null) {
            return null;
        }
        ShapeTransform shapeTransform = baseShapeTransform.copyTRS();
        m_modification.getM_Modifications().forEach(modification -> {
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
                case ("m_localeulerangleshint.x"): // m_LocalRotation: quaternions to Euler not working. See https://docs.unity3d.com/Manual/QuaternionAndEulerRotationsInUnity.html
                    shapeTransform.setRotateX(shapeTransform.getRotateX() + Math.toRadians(Double.parseDouble(modification.getValue())));
                    break;
                case ("m_localeulerangleshint.y"):  // m_LocalRotation: quaternions to Euler not working. See https://docs.unity3d.com/Manual/QuaternionAndEulerRotationsInUnity.html
                    shapeTransform.setRotateY(shapeTransform.getRotateY() + Math.toRadians(Double.parseDouble(modification.getValue())));
                    break;
                case ("m_localeulerangleshint.z"): // m_LocalRotation: quaternions to Euler not working. See https://docs.unity3d.com/Manual/QuaternionAndEulerRotationsInUnity.html
                    shapeTransform.setRotateZ(shapeTransform.getRotateZ() + Math.toRadians(Double.parseDouble(modification.getValue())));
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
                // Ignore
                case ("m_name"):
                case ("m_rootorder"):
                case ("m_localrotation.x"):
                case ("m_localrotation.y"):
                case ("m_localrotation.z"):
                case ("m_localrotation.w"):
                    break;
                default:
                    LOGGER.warning("Unknown transformation: " + modification.getPropertyPath().toLowerCase() + ": " + modification.getValue());
            }
        });
        return shapeTransform;
    }
}
