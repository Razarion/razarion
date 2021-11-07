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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Prefab extends AssetType {
    private static final Logger LOGGER = Logger.getLogger(Prefab.class.getName());
    private final Map<String, Component> components = new HashMap<>();
    private List<GameObject> gameObjects = new ArrayList<>();

    public Prefab(Meta meta) {
        super(meta);
        try {
            LOGGER.fine("readGameObjects: " + getMeta());
            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Constructor constructor = new Constructor();
            // GameObject
            TypeDescription typeDescription = new TypeDescription(GameObjectHolder.class, "tag:unity3d.com,2011:1");
            typeDescription.substituteProperty("GameObject", GameObject.class, "getObject", "setGameObject");
            constructor.addTypeDescription(typeDescription);
            // Transform
            typeDescription = new TypeDescription(TransformHolder.class, "tag:unity3d.com,2011:4");
            typeDescription.substituteProperty("Transform", Transform.class, "getObject", "setTransform");
            constructor.addTypeDescription(typeDescription);
            // MeshFilter
            typeDescription = new TypeDescription(MeshFilterHolder.class, "tag:unity3d.com,2011:33");
            typeDescription.substituteProperty("MeshFilter", MeshFilter.class, "getObject", "setMeshFilter");
            constructor.addTypeDescription(typeDescription);
            // MeshRenderer
            typeDescription = new TypeDescription(MeshRendererHolder.class, "tag:unity3d.com,2011:23");
            typeDescription.substituteProperty("MeshRenderer", MeshRenderer.class, "getObject", "setMeshRenderer");
            constructor.addTypeDescription(typeDescription);
            // PrefabInstance
            typeDescription = new TypeDescription(PrefabInstanceHolder.class, "tag:unity3d.com,2011:1001");
            typeDescription.substituteProperty("PrefabInstance", PrefabInstance.class, "getObject", "setPrefabInstance");
            constructor.addTypeDescription(typeDescription);
            // MonoBehaviour -> Ignore
            typeDescription = new TypeDescription(IgnoredAssetTypeHolder.class, "tag:unity3d.com,2011:114");
            typeDescription.substituteProperty("MeshRenderer", IgnoredAssetType.class, "getObject", "setMeshRenderer");
            constructor.addTypeDescription(typeDescription);
            // BoxCollider -> Ignore
            typeDescription = new TypeDescription(IgnoredAssetTypeHolder.class, "tag:unity3d.com,2011:65");
            typeDescription.substituteProperty("MeshRenderer", IgnoredAssetType.class, "getObject", "setMeshRenderer");
            constructor.addTypeDescription(typeDescription);
            // Rigidbody -> Ignore
            typeDescription = new TypeDescription(IgnoredAssetTypeHolder.class, "tag:unity3d.com,2011:54");
            typeDescription.substituteProperty("MeshRenderer", IgnoredAssetType.class, "getObject", "setMeshRenderer");
            constructor.addTypeDescription(typeDescription);

            Yaml yaml = new Yaml(constructor, representer, new DumperOptions());

            constructor.setComposer(new Composer(new ParserImpl(new StreamReader(new UnicodeReader(removeUnityCrap(getAssetFile())))), new Resolver()));
            yaml.composeAll(new UnicodeReader(removeUnityCrap(getAssetFile()))).forEach(node -> {
                String snippet = node.getStartMark().get_snippet();
                String objectId = readObjectId(snippet);
                UnityObject unityObject = ((Holder<?>) constructor.getData()).getObject();
                if (unityObject != null) {
                    unityObject.setObjectId(objectId);

                    if (unityObject instanceof GameObject) {
                        gameObjects.add((GameObject) unityObject);
                    } else {
                        components.put(objectId, (Component) unityObject);
                    }
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(meta.toString(), e);
        }
    }

    @Override
    public String toString() {
        return "Prefab " + getAssetFile();
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

    private InputStream removeUnityCrap(File assetFile) {
        try (Stream<String> stream = Files.lines(Paths.get(assetFile.toURI()))) {
            return new ByteArrayInputStream(stream.map(s -> {
                if (s.startsWith("---") && s.endsWith(" stripped")) {
                    return s.substring(0, s.length() - " stripped".length());
                } else {
                    return s;
                }
            }).collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Error processing: " + assetFile, e);
        }
    }

    private String readObjectId(String snippet) {
        StringBuilder objectId = new StringBuilder();
        int objectIdStart = snippet.indexOf('&') + 1;
        for (int i = objectIdStart; i < snippet.length(); i++) {
            if (Character.isWhitespace(snippet.charAt(i))) {
                break;
            } else {
                objectId.append(snippet.charAt(i));
            }
        }
        return objectId.toString();
    }

    public interface Holder<T extends UnityObject> {
        T getObject();
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

    public static class IgnoredAssetTypeHolder implements Holder<IgnoredAssetType> {
        public IgnoredAssetType ignoredAssetType;

        @Override
        public IgnoredAssetType getObject() {
            return ignoredAssetType;
        }

        @SuppressWarnings("unused")
        public void setIgnoredAssetType(IgnoredAssetType ignoredAssetType) {
            this.ignoredAssetType = ignoredAssetType;
        }

        @Override
        public String toString() {
            return "MeshRendererHolder{" +
                    "meshRenderer=" + ignoredAssetType +
                    '}';
        }
    }
}
