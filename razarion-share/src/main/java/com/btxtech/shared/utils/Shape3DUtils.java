package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class Shape3DUtils {
    public static Element3D getElement3D(String id, Shape3D shape3D) {
        for (Element3D element3D : shape3D.getElement3Ds()) {
            if (element3D.getId().equalsIgnoreCase(id)) {
                return element3D;
            }
        }
        throw new IllegalArgumentException("No Element3D in Shape3D found for: " + id);
    }

    public static List<VertexContainer> getAllVertexContainers(Shape3D shape3D) {
        List<VertexContainer> vertexContainers = new ArrayList<>();
        if (shape3D.getElement3Ds() != null) {
            for (Element3D element3D : shape3D.getElement3Ds()) {
                vertexContainers.addAll(element3D.getVertexContainers());
            }
        }
        return vertexContainers;
    }

    public static List<VertexContainer> getAllVertexContainer4DiffMaterials(Shape3D shape3D) {
        Map<String, VertexContainer> vertexContainers = new HashMap<>();
        if (shape3D.getElement3Ds() != null) {
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    vertexContainers.put(vertexContainer.getMaterialId(), vertexContainer);
                }
            }
        }
        return new ArrayList<>(vertexContainers.values());
    }

    public static void replaceTextureIds(Shape3D source, Shape3D target) {
        Map<String, Integer> materials = new HashMap<>();
        for (VertexContainer vertexContainer : getAllVertexContainers(source)) {
            materials.put(vertexContainer.getMaterialId(), vertexContainer.getTextureId());
        }
        for (VertexContainer vertexContainer : getAllVertexContainers(target)) {
            vertexContainer.setTextureId(materials.get(vertexContainer.getMaterialId()));
        }
    }

    public static void replaceTextureId(Shape3D shape3D, String materialId, int newImageId) {
        boolean found = false;
        for (VertexContainer vertexContainer : getAllVertexContainers(shape3D)) {
            if (vertexContainer.getMaterialId() != null && vertexContainer.getMaterialId().equals(materialId)) {
                vertexContainer.setTextureId(newImageId);
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("MaterialId not found: " + materialId);
        }
    }

    public static void replaceAnimation(Shape3D shape3D, String animationId, AnimationTrigger animationTrigger) {
        for (ModelMatrixAnimation modelMatrixAnimation : shape3D.getModelMatrixAnimations()) {
            if (modelMatrixAnimation.getId().equals(animationId)) {
                modelMatrixAnimation.setAnimationTrigger(animationTrigger);
                return;
            }
        }
        throw new IllegalArgumentException("AnimationId not found: " + animationId);
    }
}
