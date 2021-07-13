package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.config.VertexContainerMaterialConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;

import java.util.Collection;
import java.util.List;

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

    public static String generateVertexContainerKey(int shape3DId, String element3DId) {
        return shape3DId + "-" + element3DId;
    }

    public static void fillMaterialFromSource(List<Element3D> element3Ds, Shape3DConfig source) {
        element3Ds.stream()
                .filter(element3D -> element3D.getVertexContainers() != null)
                .forEach(element3D -> element3D.getVertexContainers().stream()
                        .filter(vertexContainer -> vertexContainer.getVertexContainerMaterial() != null)
                        .forEach(vertexContainer -> {
                            String materialId = vertexContainer.getVertexContainerMaterial().getMaterialId();
                            VertexContainerMaterialConfig sourceMaterial = source.findMaterial(element3D.getId(), materialId);
                            if (vertexContainer.getVertexContainerMaterial().getPhongMaterialConfig() == null) {
                                vertexContainer.getVertexContainerMaterial().setPhongMaterialConfig(new PhongMaterialConfig().scale(1.0));
                            }
                            if (sourceMaterial != null) {
                                vertexContainer.getVertexContainerMaterial().override(sourceMaterial.toVertexContainerMaterial());
                            }
                        }));
    }
}
