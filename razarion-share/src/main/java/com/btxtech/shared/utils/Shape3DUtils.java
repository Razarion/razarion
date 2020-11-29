package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.Collection;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class Shape3DUtils {
    public static Element3D getElement3D(String id, Collection<Element3D> element3DS) {
        for (Element3D element3D : element3DS) {
            if (element3D.getId().equalsIgnoreCase(id)) {
                return element3D;
            }
        }
        throw new IllegalArgumentException("No Element3D in Shape3D found for: " + id);
    }

    public static String generateVertexContainerKey(int shape3DId, String element3DId, VertexContainer vertexContainer) {
        return shape3DId + "-" + element3DId + "-" + vertexContainer.getVertexContainerMaterial().getMaterialId();
    }
}
