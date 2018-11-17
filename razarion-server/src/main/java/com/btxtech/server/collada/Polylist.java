package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 15.08.2015.
 */
public class Polylist extends AbstractGeometricPrimitive {
    private List<Integer> polygonPrimitiveCounts;

    public Polylist(Node node) {
        super(node);
        // count = getAttributeAsInt(node, ATTRIBUTE_COUNT);

        polygonPrimitiveCounts = getElementAsIntegerList(getChild(node, ELEMENT_VCOUNT));
    }

    public VertexContainerBuffer createVertexContainer(Map<String, Source> sources, Vertices positionVertex) {
        for (Integer polygonPrimitiveCount : polygonPrimitiveCounts) {
            if (polygonPrimitiveCount != 3) {
                throw new ColladaRuntimeException("Only polygon with 3 vertices supported (triangle). Given vertices: " + polygonPrimitiveCount);
            }
        }

        return super.createVertexContainer(sources, positionVertex, 3);
    }
}
