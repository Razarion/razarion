package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;

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
                    if (vertexContainer.getShape3DMaterialConfig().getMaterialId().equals(materialId)) {
                        return element3D;
                    }
                }
            }
        }
        throw new IllegalArgumentException("MaterialId '" + materialId + "' not found in Shape3D: " + shape3D);
    }

    public static VertexContainer getVertexContainer4MaterialId(Shape3D shape3D, String materialId) {
        for (VertexContainer vertexContainer : getElement4MaterialId(shape3D, materialId).getVertexContainers()) {
            if (vertexContainer.getShape3DMaterialConfig().getMaterialId().equals(materialId)) {
                return vertexContainer;
            }
        }
        throw new IllegalArgumentException("MaterialId '" + materialId + "' not found in Shape3D: " + shape3D);
    }

    public static String generateVertexContainerKey(int shape3DId, String element3DId, VertexContainer vertexContainer) {
        return shape3DId + "-" + element3DId + "-" + vertexContainer.getShape3DMaterialConfig().getMaterialId();
    }
}
