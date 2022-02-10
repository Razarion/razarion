package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.GameObject;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import com.btxtech.unityconverter.unity.model.MeshRenderer;
import com.btxtech.unityconverter.unity.model.Transform;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Prefab extends UnityObjectsAssetType {
    private static final Logger LOGGER = Logger.getLogger(Prefab.class.getName());

    public Prefab(Meta meta) {
        super(meta);
        try {
            LOGGER.fine("readPrefab: " + getMeta());
            loadObjectAndComponents(getAssetFile());
        } catch (Exception e) {
            throw new RuntimeException(meta.toString(), e);
        }
    }

    @Override
    public String toString() {
        return "Prefab{" + super.toString() + "}";
    }

    public List<GameObject> getGameObjects() {
        return getAllUnityObject(GameObject.class);
    }

    public MeshFilter getMeshFilter(GameObject gameObject) {
        return gameObject.getM_Component()
                .stream()
                .map(componentReference -> getUnityObject(componentReference.getComponent()))
                .filter(c -> c instanceof MeshFilter)
                .map(c -> (MeshFilter) c)
                .findFirst()
                .orElse(null);
    }

    public MeshRenderer getMeshRenderer(GameObject gameObject) {
        return gameObject.getM_Component()
                .stream()
                .map(componentReference -> getUnityObject(componentReference.getComponent()))
                .filter(c -> c instanceof MeshRenderer)
                .map(c -> (MeshRenderer) c)
                .findFirst()
                .orElse(null);
    }

    public List<Transform> findTransform4Father(String fatherTransformReference) {
        return getUnityObjects()
                .stream()
                .filter(c -> c instanceof Transform)
                .map(c -> (Transform) c)
                .filter(transform -> transform.getM_Father() != null)
                .filter(transform -> transform.getM_Father().isNotNull())
                .filter(transform -> transform.getM_Father().getFileID().equals(fatherTransformReference))
                .collect(Collectors.toList());
    }
}
