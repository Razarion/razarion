package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.utils.Shape3DUtils;

/**
 * Created by Beat
 * 08.03.2017.
 */
public class VertexContainerBuilder {
    private final VertexContainer vertexContainer;
    private final VertexContainerBuffer vertexContainerBuffer;

    public VertexContainerBuilder(VertexContainer vertexContainer, VertexContainerBuffer vertexContainerBuffer) {
        this.vertexContainer = vertexContainer;
        this.vertexContainerBuffer = vertexContainerBuffer;
    }

    public VertexContainer getVertexContainer(int shape3DId, String element3DId) {
        vertexContainer.setKey(Shape3DUtils.generateVertexContainerKey(shape3DId, element3DId, vertexContainer));
        return vertexContainer;
    }

    public VertexContainerBuffer getVertexContainerBuffer(int shape3DId, String element3DId) {
        vertexContainerBuffer.setKey(Shape3DUtils.generateVertexContainerKey(shape3DId, element3DId, vertexContainer));
        return vertexContainerBuffer;
    }
}
