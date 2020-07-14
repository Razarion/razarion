package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;

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

    public static Element3D getElement4MaterialId(Shape3D shape3D, String materialId) {
        if (shape3D.getElement3Ds() != null) {
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    if (vertexContainer.getMaterialId().equals(materialId)) {
                        return element3D;
                    }
                }
            }
        }
        throw new IllegalArgumentException("MaterialId '" + materialId + "' not found in Shape3D: " + shape3D);
    }

    public static VertexContainer getVertexContainer4MaterialId(Shape3D shape3D, String materialId) {
        for (VertexContainer vertexContainer : getElement4MaterialId(shape3D, materialId).getVertexContainers()) {
            if (vertexContainer.getMaterialId().equals(materialId)) {
                return vertexContainer;
            }
        }
        throw new IllegalArgumentException("MaterialId '" + materialId + "' not found in Shape3D: " + shape3D);
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

    public static void updateCharacterRepresenting(Shape3D shape3D, String materialId, boolean characterRepresenting) {
        boolean found = false;
        for (VertexContainer vertexContainer : getAllVertexContainers(shape3D)) {
            if (vertexContainer.getMaterialId() != null && vertexContainer.getMaterialId().equals(materialId)) {
                vertexContainer.setCharacterRepresenting(characterRepresenting);
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("MaterialId not found: " + materialId);
        }
    }

    public static void saveAnimationTriggers(Shape3D source, Shape3D target) {
        if (source.getModelMatrixAnimations() == null || target.getModelMatrixAnimations() == null) {
            return;
        }
        Map<String, AnimationTrigger> sourceAnimationTriggers = new HashMap<>();
        for (ModelMatrixAnimation sourceAnimation : source.getModelMatrixAnimations()) {
            sourceAnimationTriggers.put(sourceAnimation.getId(), sourceAnimation.getAnimationTrigger());
        }
        for (ModelMatrixAnimation targetMatrixAnimation : target.getModelMatrixAnimations()) {
            targetMatrixAnimation.setAnimationTrigger(sourceAnimationTriggers.get(targetMatrixAnimation.getId()));
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

    public static String generateVertexContainerKey(int shape3DId, String element3DId, VertexContainer vertexContainer) {
        return Integer.toString(shape3DId) + "-" + element3DId + "-" + vertexContainer.getMaterialId();
    }
}
