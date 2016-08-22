package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        for (Element3D element3D : shape3D.getElement3Ds()) {
            vertexContainers.addAll(element3D.getVertexContainers().stream().collect(Collectors.toList()));
        }
        return vertexContainers;
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
        for (VertexContainer vertexContainer : getAllVertexContainers(shape3D)) {
            if(vertexContainer.getMaterialId().equals(materialId)) {
                vertexContainer.setTextureId(newImageId);
                return;
            }
        }
        throw new IllegalArgumentException("MaterialId not found: " + materialId);
    }
}
