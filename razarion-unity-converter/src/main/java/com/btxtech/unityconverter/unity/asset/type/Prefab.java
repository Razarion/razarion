package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.unityconverter.unity.asset.Meta;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.MeshRenderer;
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

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Prefab extends AssetType {
    private static final Logger LOGGER = Logger.getLogger(Prefab.class.getName());

    public Prefab(Meta meta) {
        super(meta);
    }

    public GameObject readGameObject() {
        try {
            LOGGER.info("readGameObjects: " + getMeta());
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

            DumperOptions dumperOptions = new DumperOptions();
            Yaml yaml = new Yaml(constructor, representer, dumperOptions);


            SingleHolder<GameObject> gameObjectHolder = new SingleHolder<>();
            Map<String, UnityObject> unityObjects = new HashMap<>();

            constructor.setComposer(new Composer(new ParserImpl(new StreamReader(new UnicodeReader(new FileInputStream(getAssetFile())))), new Resolver()));
            yaml.composeAll(new UnicodeReader(new FileInputStream(getAssetFile()))).forEach(node -> {
                String snippet = node.getStartMark().get_snippet();
                String objectId = readObjectId(snippet);
                UnityObject unityObject = ((Holder<?>) constructor.getData()).getObject();
                unityObject.setObjectId(objectId);

                if (gameObjectHolder.isEmpty() && unityObject instanceof GameObject) {
                    gameObjectHolder.setO((GameObject) unityObject);
                } else {
                    unityObjects.put(objectId, unityObject);
                }
            });

            return gameObjectHolder.getO();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        public GameObject getObject() {
            return gameObject;
        }

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

        public Transform getObject() {
            return transform;
        }

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

        public MeshFilter getObject() {
            return meshFilter;
        }

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

        public MeshRenderer getObject() {
            return meshRenderer;
        }

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
}
