package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Created by Beat
 * on 15.11.2018.
 */
public class Triangles extends AbstractGeometricPrimitive {

    public Triangles(Node node) {
        super(node);
    }

    public VertexContainerBuffer createVertexContainer(Map<String, Source> sources, Vertices positionVertex) {
        return super.createVertexContainer(sources, positionVertex, 3);
    }
}
