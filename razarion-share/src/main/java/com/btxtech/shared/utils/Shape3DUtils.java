package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static Set<Integer> getAllTextures(Collection<Shape3D> shape3Ds) {
        Set<Integer> textureIds = new HashSet<>();
        for (Shape3D shape3D : shape3Ds) {
            if(shape3D.getElement3Ds() == null) {
                continue;
            }
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    if (vertexContainer.getTextureId() != null) {
                        textureIds.add(vertexContainer.getTextureId());
                    }
                }
            }
        }
        return textureIds;
    }

    public static void saveTextureIds(Shape3D source, Shape3D target) {
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

    public static void saveAnimationTriggers(Shape3D source, Shape3D target) {
        if (source.getModelMatrixAnimations() == null) {
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

    public static double getMaxZ(Shape3D shape3D) {
        if (shape3D.getElement3Ds() != null) {
            double maxZ = Double.MIN_VALUE;
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    Matrix4 buildupMatrix = vertexContainer.getShapeTransform().setupMatrix();
                    for (Vertex vertex : vertexContainer.getVertices()) {
                        maxZ = Math.max(buildupMatrix.multiply(vertex, 1.0).getZ(), maxZ);
                    }
                }
            }
            return maxZ;
        }
        throw new IllegalArgumentException("No vertices in vertex container");
    }


    public static double getMinZ(Shape3D shape3D) {
        if (shape3D.getElement3Ds() != null) {
            double minZ = Double.MAX_VALUE;
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    Matrix4 buildupMatrix = vertexContainer.getShapeTransform().setupMatrix();
                    for (Vertex vertex : vertexContainer.getVertices()) {
                        minZ = Math.min(buildupMatrix.multiply(vertex, 1.0).getZ(), minZ);
                    }
                }
            }
            return minZ;
        }
        throw new IllegalArgumentException("No vertices in vertex container");
    }

}
