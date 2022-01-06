package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.datatypes.asset.Mesh;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.unityconverter.unity.asset.AssetReader;
import com.btxtech.unityconverter.unity.asset.UnityAsset;
import com.btxtech.unityconverter.unity.asset.type.AssetType;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.MaterialAssetType;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.asset.type.ShaderGraphAssetType;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.Material;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.MeshRenderer;
import com.btxtech.unityconverter.unity.model.ModificationContainer;
import com.btxtech.unityconverter.unity.model.PrefabInstance;
import com.btxtech.unityconverter.unity.model.Reference;
import com.btxtech.unityconverter.unity.model.ShaderGraph;
import com.btxtech.unityconverter.unity.model.ShaderGraphData;
import com.btxtech.unityconverter.unity.model.Transform;
import com.btxtech.unityconverter.unity.model.UnityVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UnityAssetConverter {
    private static final Logger LOGGER = Logger.getLogger(UnityAssetConverter.class.getName());

    public static void main(String[] args) {
        String metaFilePath = "C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta";
        try {
            UnityAsset unityAsset = AssetReader.read(metaFilePath);
            MockAssetContext mockAssetContext = new MockAssetContext();

            System.out.println("------------------------------------");
            System.out.println("Unity Asset Guid: " + unityAsset.getGuid());
            System.out.println("Unity Asset Name: " + unityAsset.getName());
            System.out.println("MetaFilePath: " + metaFilePath);


//            System.out.println("------------ Prefabs in Asset ------------");
//            unityAsset.getAssetTypes(Prefab.class).forEach(prefab -> {
//                System.out.println(prefab.getName());
//            });
//            System.out.println("------------ Materials in Asset ------------");
//            unityAsset.getAssetTypes(MaterialAssetType.class).forEach(materialAssetType -> {
//                System.out.println(materialAssetType);
//            });
//            System.out.println("------------ Shader Graphs in Asset ------------");
//            unityAsset.getAssetTypes(ShaderGraphAssetType.class).forEach(shaderGraphAssetType -> {
//                System.out.println(shaderGraphAssetType);
//            });
            System.out.println("------------ Generated Mesh Containers ------------");
            dumpMeshContainers(
                    unityAsset.getAssetTypes(Prefab.class).stream().map(prefab -> createMeshContainer(prefab, unityAsset, mockAssetContext)).collect(Collectors.toList())
                    , "--");
//            System.out.println("------------ Dump single prefab ------------");
//            Prefab prefab = unityAsset.findPrefab("Aaa");
//            System.out.println("Prefab AssetFile: " + prefab.getAssetFile());
//            System.out.println("Prefab: " + prefab);
//            dumpMeshContainer(createMeshContainer(prefab, unityAsset, mockAssetContext), "");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error Converter Asset", e);
        }
    }

    public static AssetConfig createAssetConfig(String assetMetaFile, AssetContext assetContext) {
        UnityAsset unityAsset = AssetReader.read(assetMetaFile);

        return new AssetConfig()
                .unityAssetGuid(unityAsset.getGuid())
                .internalName(unityAsset.getName())
                .assetMetaFileHint(assetMetaFile)
                .meshContainers(unityAsset.getAssetTypes(Prefab.class).stream()
                        .map(prefab -> UnityAssetConverter.createMeshContainer(prefab, unityAsset, assetContext))
                        .collect(Collectors.toList()));
    }

    private static MeshContainer createMeshContainer(Prefab prefab, UnityAsset unityAsset, AssetContext assetContext) {
        List<GameObject> gameObjects = prefab.getGameObjects();
        if (gameObjects.size() == 1) {
            return createMeshContainer(prefab.getName(), prefab.getGuid(), gameObjects.get(0), prefab, unityAsset, assetContext);
        } else if (gameObjects.size() > 1) {
            return new MeshContainer()
                    .guid(prefab.getGuid())
                    .internalName(prefab.getName())
                    .children(gameObjects.stream()
                            .filter(gameObject -> gameObject.getM_Component() != null)
                            .map(gameObject -> createMeshContainer("child: " + gameObject.getM_Name(), null, gameObject, prefab, unityAsset, assetContext))
                            .collect(Collectors.toList()));
        } else {
            LOGGER.warning("No MeshContainer in prefab: " + prefab);
            return null;
        }
    }

    private static MeshContainer createMeshContainer(String name, String guid, GameObject gameObjects, Prefab prefab, UnityAsset unityAsset, AssetContext assetContext) {
        Transform transform = gameObjects.getM_Component()
                .stream()
                .map(componentReference -> prefab.getComponent(componentReference.getComponent()))
                .filter(c -> c instanceof Transform)
                .map(c -> (Transform) c)
                .findFirst().orElseThrow(IllegalStateException::new);

        List<MeshContainer> childMeshContainers = new ArrayList<>();
        transform.getM_Children()
                .stream()
                .map(prefab::getComponent)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .forEach(childTransform -> {
                    Prefab childPrefab = unityAsset.getAssetType(childTransform.getM_CorrespondingSourceObject());
                    PrefabInstance prefabInstance = prefab.getComponent(childTransform.getM_PrefabInstance());
                    MeshContainer meshContainer = new MeshContainer();
                    if (childPrefab != null) {
                        childPrefab.getGameObjects().forEach(gameObject -> {
                            meshContainer.setInternalName("child: " + childPrefab.getName());
                            meshContainer.setMesh(setupMesh(childPrefab, prefabInstance, gameObject, unityAsset, assetContext));
                        });
                    }
                    childMeshContainers.add(meshContainer);
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
                .internalName(name)
                .guid(guid)
                .children(childMeshContainers)
                .mesh(setupMesh(prefab, null, gameObjects, unityAsset, assetContext));
    }

    private static Mesh setupMesh(Prefab prefab, PrefabInstance prefabInstance, GameObject gameObject, UnityAsset unityAsset, AssetContext assetContext) {
        // Check if valid for Mesh creation
        MeshFilter meshFilter = prefab.getMeshFilter(gameObject);
        MeshRenderer meshRenderer = prefab.getMeshRenderer(gameObject);
        if (meshFilter == null && meshRenderer == null) {
            LOGGER.warning("No MeshFilter/MeshRenderer for: " + prefab.getName());
            return null;
        } else if (meshFilter == null) {
            LOGGER.warning("No MeshFilter for: " + prefab.getName());
            return null;
        } else if (meshRenderer == null) {
            LOGGER.warning("No MeshRenderer for: " + prefab.getName());
            return null;
        }

        Fbx fbx = unityAsset.getAssetType(meshFilter.getM_Mesh());
        String meshName = fbx.getMeshName(meshFilter.getM_Mesh().getFileID());
        MaterialInfo materialInfo = setupMaterialInfo(meshRenderer, gameObject, unityAsset);
        return new Mesh()
                .shape3DId(assetContext.getShape3DId4Fbx(fbx, materialInfo))
                .element3DId(meshName)
                .shapeTransform(setupShapeTransform(prefabInstance));
    }

    private static ShapeTransform setupShapeTransform(PrefabInstance prefabInstance) {
        if (prefabInstance == null) {
            return new ShapeTransform().setScaleX(1).setScaleY(1).setScaleZ(1);
        }
        ModificationContainer m_modification = prefabInstance.getM_Modification();
        if (m_modification == null || m_modification.getM_Modifications() == null) {
            return new ShapeTransform().setScaleX(1).setScaleY(1).setScaleZ(1);
        }
        ShapeTransform shapeTransform = new ShapeTransform();
        final SingleHolder<UnityVector> quaternion = new SingleHolder<>();
        UnityVector scale = new UnityVector().x(1).y(1).z(1);

        m_modification.getM_Modifications().forEach(modification -> {
            switch (modification.getPropertyPath().toLowerCase()) {
                case ("m_localposition.z"):
                    shapeTransform.setTranslateX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.x"):
                    shapeTransform.setTranslateY(-Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.y"):
                    shapeTransform.setTranslateZ(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.x"):
                    if (quaternion.isEmpty()) {
                        quaternion.setO(new UnityVector());
                    }
                    quaternion.getO().setX(-Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.y"):
                    if (quaternion.isEmpty()) {
                        quaternion.setO(new UnityVector());
                    }
                    quaternion.getO().setY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.z"):
                    if (quaternion.isEmpty()) {
                        quaternion.setO(new UnityVector());
                    }
                    quaternion.getO().setZ(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.w"):
                    if (quaternion.isEmpty()) {
                        quaternion.setO(new UnityVector());
                    }
                    quaternion.getO().setW(-Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.x"):
                    scale.setX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.y"):
                    scale.setY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.z"):
                    scale.setZ(Double.parseDouble(modification.getValue()));
                    break;
                // Ignore
                case ("m_name"):
                case ("m_rootorder"):
                case ("m_localeulerangleshint.x"):
                case ("m_localeulerangleshint.y"):
                case ("m_localeulerangleshint.z"):
                default:
                    LOGGER.warning("Unknown transformation: " + modification.getPropertyPath().toLowerCase() + ": " + modification.getValue());
            }
        });

        shapeTransform.setScaleX(scale.getX());
        shapeTransform.setScaleY(scale.getY());
        shapeTransform.setScaleZ(scale.getZ());

        UnityVector quaternion2 = UnityVector.createFromAxisAngle(Vertex.Z_NORM, Math.PI / 2);
        UnityVector quaternion3 = UnityVector.createFromAxisAngle(Vertex.Y_NORM, Math.PI / 2);

        quaternion3.quaternionMultiply(quaternion2);
        if (!quaternion.isEmpty()) {
            quaternion3.quaternionMultiply(quaternion.getO());
        }

        shapeTransform.setRotateX(quaternion3.getX());
        shapeTransform.setRotateY(quaternion3.getY());
        shapeTransform.setRotateZ(quaternion3.getZ());
        shapeTransform.setRotateW(quaternion3.getW());

        return shapeTransform;
    }

    private static MaterialInfo setupMaterialInfo(MeshRenderer meshRenderer, GameObject gameObject, UnityAsset unityAsset) {
        if (meshRenderer.getM_Materials().size() == 0) {
            LOGGER.warning("No material  for:  " + gameObject.getM_Name());
            return null;
        }
        if (meshRenderer.getM_Materials().size() > 1) {
            LOGGER.warning("More than one material for:  " + gameObject.getM_Name() + " Last is taken. Material count:" + meshRenderer.getM_Materials().size());
        }
        Reference matReference = meshRenderer.getM_Materials().get(0);
        MaterialAssetType materialAssetType = unityAsset.getAssetType(matReference);
        if (materialAssetType == null) {
            LOGGER.warning("No MaterialAssetType for reference:  " + matReference + " used in: " + gameObject.getM_Name());
            return null;
        }
        Material material = materialAssetType.getMaterial();
        if (material == null) {
            LOGGER.warning("No Material in MaterialAssetType:  " + materialAssetType.getAssetFile());
            return null;
        }

        Reference shaderReference = material.getShader();
        if(shaderReference == null) {
            LOGGER.warning("No Shader Reference in Material: " + material + " used in: " + gameObject.getM_Name());
            return null;
        }
        ShaderGraphAssetType shaderGraphAssetType = unityAsset.getAssetType(shaderReference);
        if (shaderGraphAssetType == null) {
            LOGGER.warning("No ShaderGraphAssetType for reference:  " + shaderReference + " in Material: " + material + " used in: " + gameObject.getM_Name());
            return null;
        }
        ShaderGraph shaderGraph = shaderGraphAssetType.getShaderGraph();
        if (shaderGraph == null) {
            LOGGER.warning("No ShaderGraph in ShaderGraphAssetType " + shaderGraphAssetType + " in Material: " + material + " used in: " + gameObject.getM_Name());
            return null;
        }

        if (material.getSavedProperties() == null) {
            LOGGER.warning("Material has no SavedProperties: " + material + " used in: " + gameObject.getM_Name());
            return null;
        }
        List<MaterialInfo.GuidFile> mainTextures = new ArrayList<>();
        material.getSavedProperties().getTexEnvs()
                .forEach(stringTexture2DMap -> stringTexture2DMap.forEach((key, value) -> {
                    ShaderGraphData shaderGraphData = shaderGraph.findShaderGraphData4tReferenceName(key);
                    if (shaderGraphData == null) {
                        return;
                    }
                    AssetType assetType = unityAsset.getAssetType(value.getTexture());
                    if (assetType == null) {
                        return;
                    }
                    String name = shaderGraphData.getName().toLowerCase();
                    MaterialInfo.GuidFile guidFile = new MaterialInfo.GuidFile()
                            .file(assetType.getAssetFile().getPath())
                            .guid(assetType.getGuid());
                    if (name.startsWith("albedo")) {
                        mainTextures.add(guidFile);
                    }
                }));
        if (mainTextures.size() == 0) {
            LOGGER.warning("No main Texture (albedo) in material found:" + material + " used in: " + gameObject.getM_Name());
            return null;
        }

        if (mainTextures.size() == 1) {
            return new MaterialInfo().mainTexture(mainTextures.get(0));
        } else {
            return new MaterialInfo().mainTexture(mainTextures.get(0)).main2Texture(mainTextures.get(1));
        }
    }

    private static void dumpMeshContainers(List<MeshContainer> meshContainers, String space) {
        meshContainers.forEach(meshContainer -> {
            System.out.println("-------------------------------------");
            dumpMeshContainer(meshContainer, space);
        });
    }

    private static void dumpMeshContainer(MeshContainer meshContainer, String space) {
        if (meshContainer == null) {
            System.out.println("meshContainer == null");
            return;
        }
        System.out.println(space + "InternalName: " + meshContainer.getInternalName());
        System.out.println(space + "Guid: " + meshContainer.getGuid());
        String meshString = "-";
        if (meshContainer.getMesh() != null) {
            meshString = "Shape3DId: " + meshContainer.getMesh().getShape3DId() + ", Element3DId: " + meshContainer.getMesh().getElement3DId();
        }
        System.out.println(space + "Mesh: " + meshString);
        if (meshContainer.getChildren() != null) {
            dumpMeshContainers(meshContainer.getChildren(), space + "--");
        }
    }

    private static class MockAssetContext implements AssetContext {

        @Override
        public Integer getShape3DId4Fbx(Fbx fbx, MaterialInfo materialInfo) {
            return 1234;
        }
    }
}
