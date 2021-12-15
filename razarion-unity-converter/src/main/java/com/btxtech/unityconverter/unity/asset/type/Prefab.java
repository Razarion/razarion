package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.Component;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.IgnoredAssetType;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.MeshRenderer;
import com.btxtech.unityconverter.unity.model.PrefabInstance;
import com.btxtech.unityconverter.unity.model.Reference;
import com.btxtech.unityconverter.unity.model.Transform;
import com.btxtech.unityconverter.unity.model.UnityObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.btxtech.unityconverter.unity.asset.type.UnityYamlScanner.readAllYamlDocuments;

public class Prefab extends AssetType {
    private static final Logger LOGGER = Logger.getLogger(Prefab.class.getName());
    private static final Map<String, Class<? extends UnityObject>> TAG_2_CLASS = new HashMap<>();
    private final List<GameObject> gameObjects = new ArrayList<>();
    private final Map<String, Component> components = new HashMap<>();

    static {
        TAG_2_CLASS.put("!u!1", GameObject.class);
        TAG_2_CLASS.put("!u!4", Transform.class);
        TAG_2_CLASS.put("!u!33", MeshFilter.class);
        TAG_2_CLASS.put("!u!23", MeshRenderer.class);
        TAG_2_CLASS.put("!u!1001", PrefabInstance.class);
        TAG_2_CLASS.put("!u!114", IgnoredAssetType.class); // MonoBehaviour -> Ignore
        TAG_2_CLASS.put("!u!65", IgnoredAssetType.class); // BoxCollider -> Ignore
        TAG_2_CLASS.put("!u!54", IgnoredAssetType.class); // Rigidbody -> Ignore
    }

    public Prefab(Meta meta) {
        super(meta);
        try {
            LOGGER.fine("readPrefab: " + getMeta());
            readAllYamlDocuments(getAssetFile()).stream()
                    .filter(UnityYamlScanner.YamlDocument::hasContent)
                    .forEach(yamlDocument -> {
                        try {
                            Class<? extends UnityObject> unityObjectClass = TAG_2_CLASS.get(yamlDocument.getTag());
                            if (unityObjectClass != null) {
                                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                Map<String, ?> map = mapper.readValue(yamlDocument.getContent(), Map.class);
                                if (map.size() != 1) {
                                    LOGGER.warning("Unknown UnityObject: " + yamlDocument.getContent());
                                }
                                LinkedHashMap<String, ?> unityObjectMap = (LinkedHashMap) map.values().stream().findFirst().orElseThrow(IllegalStateException::new);
                                UnityObject unityObject = mapper.convertValue(unityObjectMap, unityObjectClass);
                                unityObject.setObjectId(yamlDocument.getObjectId());
                                if (unityObject instanceof GameObject) {
                                    gameObjects.add((GameObject) unityObject);
                                } else {
                                    components.put(yamlDocument.getObjectId(), (Component) unityObject);
                                }
                            } else {
                                LOGGER.warning("Unknown Tag for UnityObject: " + yamlDocument.getTag());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    });
        } catch (Exception e) {
            throw new RuntimeException(meta.toString(), e);
        }
    }

    @Override
    public String toString() {
        return "Prefab{" +
                "gameObjects=" + gameObjects +
                ", components=" + components +
                "}";
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public <T extends Component> T getComponent(Reference reference) {
        if (reference.getGuid() != null && !reference.getGuid().equals(getGuid())) {
            throw new IllegalArgumentException("Given GUID does not match this GUID. Given: " + reference.getGuid() + " This: " + getGuid());
        }

        return (T) components.get(reference.getFileID());
    }

    public MeshFilter getMeshFilter(GameObject gameObject) {
        return gameObject.getM_Component()
                .stream()
                .map(componentReference -> getComponent(componentReference.getComponent()))
                .filter(c -> c instanceof MeshFilter)
                .map(c -> (MeshFilter)c)
                .findFirst()
                .orElse(null);
    }

    public MeshRenderer getMeshRenderer(GameObject gameObject) {
        return gameObject.getM_Component()
                .stream()
                .map(componentReference -> getComponent(componentReference.getComponent()))
                .filter(c -> c instanceof MeshRenderer)
                .map(c -> (MeshRenderer)c)
                .findFirst()
                .orElse(null);
    }

    public static class GameObjectHolder implements Holder<GameObject> {
        public GameObject gameObject;

        @Override
        public GameObject getObject() {
            return gameObject;
        }

        @SuppressWarnings("unused")
        public void setGameObject(GameObject gameObject) {
            this.gameObject = gameObject;
        }

        @Override
        public String toString() {
            return "GameObjectHolder{" +
                    "GameObject=" + gameObject +
                    '}';
        }
    }

    public static class TransformHolder implements Holder<Transform> {
        public Transform transform;

        @Override
        public Transform getObject() {
            return transform;
        }

        @SuppressWarnings("unused")
        public void setTransform(Transform transform) {
            this.transform = transform;
        }

        @Override
        public String toString() {
            return "TransformHolder{" +
                    "transform=" + transform +
                    '}';
        }
    }

    public static class MeshFilterHolder implements Holder<MeshFilter> {
        public MeshFilter meshFilter;

        @Override
        public MeshFilter getObject() {
            return meshFilter;
        }

        @SuppressWarnings("unused")
        public void setMeshFilter(MeshFilter meshFilter) {
            this.meshFilter = meshFilter;
        }

        @Override
        public String toString() {
            return "MeshFilterHolder{" +
                    "meshFilter=" + meshFilter +
                    '}';
        }
    }

    public static class MeshRendererHolder implements Holder<MeshRenderer> {
        public MeshRenderer meshRenderer;

        @Override
        public MeshRenderer getObject() {
            return meshRenderer;
        }

        @SuppressWarnings("unused")
        public void setMeshRenderer(MeshRenderer meshRenderer) {
            this.meshRenderer = meshRenderer;
        }

        @Override
        public String toString() {
            return "MeshRendererHolder{" +
                    "meshRenderer=" + meshRenderer +
                    '}';
        }
    }

    public static class PrefabInstanceHolder implements Holder<PrefabInstance> {
        public PrefabInstance prefabInstance;

        @Override
        public PrefabInstance getObject() {
            return prefabInstance;
        }

        @SuppressWarnings("unused")
        public void setPrefabInstance(PrefabInstance prefabInstance) {
            this.prefabInstance = prefabInstance;
        }

        @Override
        public String toString() {
            return "PrefabInstanceHolder{" +
                    "prefabInstance=" + prefabInstance +
                    '}';
        }
    }


}
