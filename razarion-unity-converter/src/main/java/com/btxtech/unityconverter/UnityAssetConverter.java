package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.datatypes.asset.Mesh;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.unityconverter.unity.asset.AssetReader;
import com.btxtech.unityconverter.unity.asset.UnityAsset;
import com.btxtech.unityconverter.unity.asset.type.AssetType;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import com.btxtech.unityconverter.unity.asset.type.MaterialAssetType;
import com.btxtech.unityconverter.unity.asset.type.Prefab;
import com.btxtech.unityconverter.unity.asset.type.ShaderGraphAssetType;
import com.btxtech.unityconverter.unity.model.Component;
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
            Prefab prefab = unityAsset.findPrefab("Aaa 1");
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

    private static MeshContainer createMeshContainer(Prefab prefab, ShapeTransform shapeTransform, UnityAsset unityAsset, AssetContext assetContext) {
        List<GameObject> gameObjects = prefab.getGameObjects();
        if (gameObjects.size() == 1) {
            return createMeshContainerFromGameObject(prefab.getName(), prefab.getGuid(), gameObjects.get(0), prefab, shapeTransform, unityAsset, assetContext);
        } else if (gameObjects.size() > 1) {
            return new MeshContainer()
                    .guid(prefab.getGuid())
                    .internalName(prefab.getName())
                    .children(gameObjects.stream()
                            .filter(gameObject -> gameObject.getM_Component() != null)
                            .map(gameObject -> createMeshContainerFromGameObject("child: " + gameObject.getM_Name(), null, gameObject, prefab, shapeTransform, unityAsset, assetContext))
                            .collect(Collectors.toList()));
        } else {
            LOGGER.warning("No MeshContainer in prefab: " + prefab);
            return null;
        }
    }

    private static MeshContainer createMeshContainerFromGameObject(String name, String guid, GameObject gameObject, Prefab prefab, ShapeTransform shapeTransform, UnityAsset unityAsset, AssetContext assetContext) {
        Transform transform = gameObject.getM_Component()
                .stream()
                .map(componentReference -> prefab.getComponent(componentReference.getComponent()))
                .filter(c -> c instanceof Transform)
                .map(c -> (Transform) c)
                .findFirst().orElseThrow(IllegalStateException::new);

        dumpErrors(prefab, unityAsset, transform);

        return new MeshContainer()
                .internalName(name)
                .guid(guid)
                .children(setupChildMeshContainers(prefab, unityAsset, assetContext, transform))
                .mesh(setupMesh(prefab, shapeTransform, gameObject, unityAsset, assetContext));
    }

    private static List<MeshContainer> setupChildMeshContainers(Prefab prefab, UnityAsset unityAsset, AssetContext assetContext, Transform transform) {
        List<MeshContainer> childMeshContainers = new ArrayList<>();
        transform.getM_Children()
                .stream()
                .map(prefab::getComponent)
                .filter(Objects::nonNull)
                .map(o -> (Transform) o)
                .forEach(childTransform -> {
                    Prefab childPrefab = unityAsset.getAssetType(childTransform.getM_CorrespondingSourceObject());
                    if (childPrefab != null) {
                        ShapeTransform shapeTransform = setupShapeTransform(prefab.getComponent(childTransform.getM_PrefabInstance()), unityAsset);
                        childMeshContainers.add(createMeshContainer(childPrefab, shapeTransform, unityAsset, assetContext));
                    }
                });
        return childMeshContainers;
    }

    private static Mesh setupMesh(Prefab prefab, ShapeTransform shapeTransform, GameObject gameObject, UnityAsset unityAsset, AssetContext assetContext) {
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
                .shape3DId(assetContext.getShape3DId4Fbx(fbx, materialInfo))
                .element3DId(meshName)
                .shapeTransform(shapeTransform);
    }

    private static ShapeTransform setupShapeTransform(PrefabInstance prefabInstance, UnityAsset unityAsset) {
        if (prefabInstance == null) {
            return new ShapeTransform().setScaleX(1).setScaleY(1).setScaleZ(1);
        }
        ModificationContainer m_modification = prefabInstance.getM_Modification();
        if (m_modification == null || m_modification.getM_Modifications() == null) {
            return new ShapeTransform().setScaleX(1).setScaleY(1).setScaleZ(1);
        }

        Map<Reference, Transform> transforms = new HashMap<>();
        Set<Reference> childReferences = new HashSet<>();
        m_modification.getM_Modifications().forEach(modification -> {
            Transform transform = transforms.get(modification.getTarget());
            if (transform == null) {
                Prefab targetPrefab = unityAsset.getAssetType(modification.getTarget());
                transform = createTransform(targetPrefab.getComponent(modification.getTarget()), targetPrefab);
                if (transform == null) {
                    return;
                }
                transforms.put(modification.getTarget(), transform);
                List<Transform> children = targetPrefab.findTransform4Father(modification.getTarget().getFileID());
                children.forEach(child -> {
                    Reference childReference = new Reference().fileID(child.getObjectId()).guid(targetPrefab.getGuid()).type("3");
                    childReferences.add(childReference);
                    if (!transforms.containsKey(childReference)) {
                        transforms.put(childReference, Transform.copyTransforms(child));
                    }
                });
            }
            switch (modification.getPropertyPath().toLowerCase()) {
                case ("m_localposition.x"):
                    transform.getM_LocalPosition().setX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.y"):
                    transform.getM_LocalPosition().setY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localposition.z"):
                    transform.getM_LocalPosition().setZ(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.x"):
                    transform.getM_LocalRotation().setX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.y"):
                    transform.getM_LocalRotation().setY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.z"):
                    transform.getM_LocalRotation().setZ(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localrotation.w"):
                    transform.getM_LocalRotation().setW(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.x"):
                    transform.getM_LocalScale().setX(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.y"):
                    transform.getM_LocalScale().setY(Double.parseDouble(modification.getValue()));
                    break;
                case ("m_localscale.z"): {
                    transform.getM_LocalScale().setZ(Double.parseDouble(modification.getValue()));
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

        Matrix4 matrix = Matrix4.createIdentity();

        // ---- game engine position
        matrix = matrix.multiply(Matrix4.createTranslation(274, 100, 3));
        // ---- game engine position ends

        Matrix4 unityConversationMatrix = Matrix4.createXRotation(MathHelper.QUARTER_RADIANT);
        unityConversationMatrix = unityConversationMatrix.multiply(Matrix4.createYRotation(MathHelper.QUARTER_RADIANT));
        SingleHolder<Matrix4> transformMatrix = new SingleHolder<>(unityConversationMatrix);

        List<Transform> transformOrdered = childReferences.stream()
                .map(transforms::remove)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        transformOrdered.addAll(transforms.values());
        Collections.reverse(transformOrdered);

        transformOrdered.forEach(transform -> {
            Matrix4 newMatrix = Matrix4.createTranslation(-transform.getM_LocalPosition().getX(), transform.getM_LocalPosition().getY(), transform.getM_LocalPosition().getZ());
            Vertex angles = transform.getM_LocalRotation().quaternion2Angles();
            newMatrix = newMatrix.multiply(Matrix4.createYRotation(-angles.getY()));
            newMatrix = newMatrix.multiply(Matrix4.createXRotation(angles.getX()));
            newMatrix = newMatrix.multiply(Matrix4.createZRotation(-angles.getZ()));
            newMatrix = newMatrix.multiply(Matrix4.createScale(transform.getM_LocalScale().getX(), transform.getM_LocalScale().getY(), transform.getM_LocalScale().getZ()));
            transformMatrix.setO(transformMatrix.getO().multiply(newMatrix));
        });

        matrix = matrix.multiply(transformMatrix.getO());

        ShapeTransform shapeTransform = new ShapeTransform();
        shapeTransform.setStaticMatrix(matrix);
        return shapeTransform;
    }

    private static Transform createTransform(Component transformComponent, Prefab prefab) {
        if (transformComponent instanceof Transform) {
            return Transform.copyTransforms((Transform) transformComponent);
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

    private static void dumpErrors(Prefab prefab, UnityAsset unityAsset, Transform transform) {
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
            meshString += " ShapeTransform: " + meshContainer.getMesh().getShapeTransform();
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
