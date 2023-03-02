package com.btxtech.unityconverter;

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
import com.btxtech.unityconverter.unity.model.IgnoredAssetType;
import com.btxtech.unityconverter.unity.model.Material;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.MeshRenderer;
import com.btxtech.unityconverter.unity.model.ModificationContainer;
import com.btxtech.unityconverter.unity.model.PrefabInstance;
import com.btxtech.unityconverter.unity.model.Reference;
import com.btxtech.unityconverter.unity.model.ShaderGraph;
import com.btxtech.unityconverter.unity.model.ShaderGraphData;
import com.btxtech.unityconverter.unity.model.Transform;
import com.btxtech.unityconverter.unity.model.UnityObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UnityAssetConverter {
    private static final Logger LOGGER = Logger.getLogger(UnityAssetConverter.class.getName());

    public static void main(String[] args) {
        try (InputStream is = UnityAssetConverter.class.getClassLoader().
                getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
//            System.out.println("------------ Generated Mesh Containers ------------");
//            dumpMeshContainers(
//                    unityAsset.getAssetTypes(Prefab.class).stream().map(prefab -> createMeshContainer(prefab, unityAsset, mockAssetContext)).collect(Collectors.toList())
//                    , "--");
            System.out.println("------------ Dump single prefab ------------");
            Prefab prefab = unityAsset.findPrefab("Bbb");
            System.out.println("Prefab AssetFile: " + prefab.getAssetFile());
            System.out.println("Prefab: " + prefab);
            dumpMeshContainer(createRootMeshContainer(prefab, unityAsset, mockAssetContext), "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AssetConfig createAssetConfig(String assetMetaFile, AssetContext assetContext) {
        UnityAsset unityAsset = AssetReader.read(assetMetaFile);

        return new AssetConfig()
                .unityAssetGuid(unityAsset.getGuid())
                .internalName(unityAsset.getName())
                .assetMetaFileHint(assetMetaFile)
                .meshContainers(unityAsset.getAssetTypes(Prefab.class).stream()
                        .map(prefab -> createRootMeshContainer(prefab, unityAsset, assetContext))
                        .collect(Collectors.toList()));
    }

    private static MeshContainer createRootMeshContainer(Prefab prefab, UnityAsset unityAsset, AssetContext assetContext) {
        return createMeshContainer(prefab, null, unityAsset, assetContext);
    }

    private static MeshContainer createMeshContainer(Prefab prefab, List<ShapeTransform> shapeTransforms, UnityAsset unityAsset, AssetContext assetContext) {
        List<GameObject> gameObjects = prefab.getGameObjects();
        if (gameObjects.size() == 1) {
            return createMeshContainerFromGameObject(prefab.getName(), prefab.getGuid(), gameObjects.get(0), prefab, shapeTransforms, unityAsset, assetContext);
        } else if (gameObjects.size() > 1) {
            return new MeshContainer()
                    .guid(prefab.getGuid())
                    .internalName(prefab.getName())
                    .children(gameObjects.stream()
                            .filter(gameObject -> gameObject.getM_Component() != null)
                            .map(gameObject -> createMeshContainerFromGameObject("child: " + gameObject.getM_Name(), null, gameObject, prefab, shapeTransforms, unityAsset, assetContext))
                            .collect(Collectors.toList()));
        } else {
            LOGGER.warning("No MeshContainer in prefab: " + prefab);
            return null;
        }
    }

    private static MeshContainer createMeshContainerFromGameObject(String name, String guid, GameObject gameObject, Prefab prefab, List<ShapeTransform> shapeTransforms, UnityAsset unityAsset, AssetContext assetContext) {
        Transform transform = gameObject.getM_Component()
                .stream()
                .map(componentReference -> prefab.getUnityObject(componentReference.getComponent()))
                .filter(c -> c instanceof Transform)
                .map(c -> (Transform) c)
                .findFirst().orElseThrow(IllegalStateException::new);

        dumpErrors(prefab, unityAsset, transform);

        return new MeshContainer()
                .internalName(name)
                .guid(guid)
                .children(setupChildMeshContainers(prefab, unityAsset, assetContext, transform))
                .mesh(setupMesh(prefab, shapeTransforms, gameObject, unityAsset, assetContext));
    }

    private static List<MeshContainer> setupChildMeshContainers(Prefab prefab, UnityAsset unityAsset, AssetContext assetContext, Transform transform) {
        List<MeshContainer> childMeshContainers = new ArrayList<>();
        transform.getM_Children()
                .stream()
                .map(prefab::getUnityObject)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .forEach(childTransform -> {
                    Prefab childPrefab = unityAsset.getAssetType(childTransform.getM_CorrespondingSourceObject());
                    if (childPrefab != null) {
                        List<ShapeTransform> shapeTransforms = setupShapeTransforms(prefab.getUnityObject(childTransform.getM_PrefabInstance()), unityAsset);
                        childMeshContainers.add(createMeshContainer(childPrefab, shapeTransforms, unityAsset, assetContext));
                    }
                });
        return childMeshContainers;
    }

    private static Mesh setupMesh(Prefab prefab, List<ShapeTransform> shapeTransforms, GameObject gameObject, UnityAsset unityAsset, AssetContext assetContext) {
        // Check if valid for Mesh creation
        MeshFilter meshFilter = prefab.getMeshFilter(gameObject);
        MeshRenderer meshRenderer = prefab.getMeshRenderer(gameObject);
        if (meshFilter == null && meshRenderer == null) {
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
                .threeJsModelId(assetContext.getThreeJsModelId4Fbx(fbx, materialInfo, unityAsset.getName()))
                .element3DId(meshName)
                .shapeTransforms(shapeTransforms);
    }

    private static List<ShapeTransform> setupShapeTransforms(PrefabInstance prefabInstance, UnityAsset unityAsset) {
        if (prefabInstance == null) {
            return Collections.emptyList();
        }
        ModificationContainer m_modification = prefabInstance.getM_Modification();
        if (m_modification == null || m_modification.getM_Modifications() == null) {
            return Collections.emptyList();
        }

        Map<Reference, ShapeTransform> shapeTransforms = new HashMap<>();
        Set<Reference> childReferences = new HashSet<>();
        m_modification.getM_Modifications().forEach(modification -> {
            ShapeTransform shapeTransform = shapeTransforms.get(modification.getTarget());
            if (shapeTransform == null) {
                Prefab targetPrefab = unityAsset.getAssetType(modification.getTarget());
                shapeTransform = createShapeTransform(targetPrefab.getUnityObject(modification.getTarget()));
                if (shapeTransform == null) {
                    return;
                }
                shapeTransforms.put(modification.getTarget(), shapeTransform);
                List<Transform> children = targetPrefab.findTransform4Father(modification.getTarget().getFileID());
                children.forEach(child -> {
                    Reference childReference = new Reference().fileID(child.getObjectId()).guid(targetPrefab.getGuid()).type("3");
                    childReferences.add(childReference);
                    if (!shapeTransforms.containsKey(childReference)) {
                        shapeTransforms.put(childReference, Transform.createShapeTransform(child));
                    }
                });
            }
            switch (modification.getPropertyPath().toLowerCase()) {
                case ("m_localposition.x"):
                    shapeTransform.setTranslateX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.y"):
                    shapeTransform.setTranslateY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.z"):
                    shapeTransform.setTranslateZ(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.x"):
                    shapeTransform.setRotateX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.y"):
                    shapeTransform.setRotateY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.z"):
                    shapeTransform.setRotateZ(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.w"):
                    shapeTransform.setRotateW(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.x"):
                    shapeTransform.setScaleX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.y"):
                    shapeTransform.setScaleY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.z"): {
                    shapeTransform.setScaleZ(Double.parseDouble(modification.getValue()));
                    break;
                }
                // Ignore
                case ("m_name"):
                case ("m_rootorder"):
                case ("m_localeulerangleshint.x"):
                case ("m_localeulerangleshint.y"):
                case ("m_localeulerangleshint.z"):
                    break;
                default:
                    LOGGER.warning("Unknown transformation: " + modification.getPropertyPath().toLowerCase() + ": " + modification.getValue());
            }
        });

        List<ShapeTransform> transformOrdered = childReferences.stream()
                .map(shapeTransforms::remove)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        transformOrdered.addAll(shapeTransforms.values());
        Collections.reverse(transformOrdered);
        return transformOrdered;
    }

    private static ShapeTransform createShapeTransform(UnityObject transformComponent) {
        if (transformComponent instanceof Transform) {
            return Transform.createShapeTransform((Transform) transformComponent);
        } else if (!(transformComponent instanceof IgnoredAssetType) && transformComponent != null) {
            LOGGER.warning("Unknown target Component: " + transformComponent);
        }
        return null;
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
        if (shaderReference == null) {
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
        List<MaterialInfo.GuidFile> normTextures = new ArrayList<>();
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
                    } else if(name.startsWith("normalmap")) {
                        normTextures.add(guidFile);
                    }
                }));
        if (mainTextures.size() == 0) {
            LOGGER.warning("No main Texture (albedo) in material found:" + material + " used in: " + gameObject.getM_Name());
            return null;
        }

        if (mainTextures.size() == 1) {
            return new MaterialInfo()
                    .mainTexture(mainTextures.get(0))
                    .normMap(normTextures.get(0));
        } else {
            return new MaterialInfo()
                    .mainTexture(mainTextures.get(0))
                    .main2Texture(mainTextures.get(1))
                    .normMap(normTextures.get(0))
                    .norm2Map(normTextures.get(1));
        }
    }

    private static void dumpErrors(Prefab prefab, UnityAsset unityAsset, Transform transform) {
        transform.getM_Children().forEach(reference -> {
            if (prefab.getUnityObject(reference) == null) {
                LOGGER.warning("Not found: " + reference);
            }
        });
        transform.getM_Children().stream()
                .map(prefab::getUnityObject)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .forEach(t -> {
                    if (unityAsset.getAssetType(t.getM_CorrespondingSourceObject()) == null) {
                        LOGGER.warning("Transformation Not found: " + t.getM_CorrespondingSourceObject());
                    }
                });
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
            meshString = "ThreeJsModelId: " + meshContainer.getMesh().getThreeJsModelId() + ", Element3DId: " + meshContainer.getMesh().getElement3DId();
            meshString += " ShapeTransform: " + meshContainer.getMesh().getShapeTransforms();
        }
        System.out.println(space + "Mesh: " + meshString);
        if (meshContainer.getChildren() != null) {
            dumpMeshContainers(meshContainer.getChildren(), space + "--");
        }
    }

    private static class MockAssetContext implements AssetContext {

        @Override
        public Integer getThreeJsModelId4Fbx(Fbx fbx, MaterialInfo materialInfo, String assetName) {
            System.out.println("------------------------------------");
            System.out.println("Fbx: " + fbx);
            System.out.println("MaterialInfo: " + materialInfo);
            return 1234;
        }
    }
}
