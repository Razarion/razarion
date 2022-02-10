package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.IgnoredAssetType;
import com.btxtech.unityconverter.unity.model.Material;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.MeshRenderer;
import com.btxtech.unityconverter.unity.model.PrefabInstance;
import com.btxtech.unityconverter.unity.model.Reference;
import com.btxtech.unityconverter.unity.model.Transform;
import com.btxtech.unityconverter.unity.model.UnityObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.btxtech.unityconverter.unity.asset.type.UnityYamlScanner.readAllYamlDocuments;

public class UnityObjectsAssetType extends AssetType {
    private static final Logger LOGGER = Logger.getLogger(UnityObjectsAssetType.class.getName());

    private static final Map<String, Class<? extends UnityObject>> unityObjectClasses = new HashMap<>();

    static {
        registerTag4Class("!u!1", GameObject.class);
        registerTag4Class("!u!4", Transform.class);
        registerTag4Class("!u!33", MeshFilter.class);
        registerTag4Class("!u!23", MeshRenderer.class);
        registerTag4Class("!u!1001", PrefabInstance.class);
        registerTag4Class("!u!21", Material.class);
        registerTag4Class("!u!114", IgnoredAssetType.class); // MonoBehaviour -> Ignore
        registerTag4Class("!u!65", IgnoredAssetType.class); // BoxCollider -> Ignore
        registerTag4Class("!u!54", IgnoredAssetType.class); // Rigidbody -> Ignore
    }

    private final Map<String, UnityObject> unityObjects = new HashMap<>();

    public UnityObjectsAssetType(Meta meta) {
        super(meta);
    }

    static void registerTag4Class(String tag, Class<? extends UnityObject> unityObjectClass) {
        unityObjectClasses.put(tag, unityObjectClass);
    }

    Collection<? extends UnityObject> getUnityObjects() {
        return unityObjects.values();
    }

    public <E extends UnityObject> E getUnityObject(Reference reference) {
        if (reference.getGuid() != null && !reference.getGuid().equals(getGuid())) {
            throw new IllegalArgumentException("Given GUID does not match this GUID. Given: " + reference.getGuid() + " This: " + getGuid());
        }

        return (E) unityObjects.get(reference.getFileID());
    }

    public UnityObject getFirstUnityObject(Class<? extends UnityObject> unityObjectClass) {
        return unityObjects.values()
                .stream()
                .filter(c -> unityObjectClass.isAssignableFrom(c.getClass()))
                .findFirst()
                .orElse(null);
    }

    public <E extends UnityObject> List<E> getAllUnityObject(Class<E> unityObjectClass) {
        return (List<E>) unityObjects.values()
                .stream()
                .filter(c -> unityObjectClass.isAssignableFrom(c.getClass()))
                .collect(Collectors.toList());
    }

    void loadObjectAndComponents(File assetFile) {
        readAllYamlDocuments(assetFile).stream()
                .filter(UnityYamlScanner.YamlDocument::hasContent)
                .forEach(yamlDocument -> {
                    try {
                        Class<? extends UnityObject> unityObjectClass = unityObjectClasses.get(yamlDocument.getTag());
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
                            unityObjects.put(yamlDocument.getObjectId(), unityObject);
                        } else {
                            LOGGER.warning("Unknown Tag for UnityObject: " + yamlDocument.getTag());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public String toString() {
        return "UnityObjectsAssetType{" +
                "unityObjects=" + unityObjects +
                '}';
    }
}
