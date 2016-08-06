package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.VertexContainer;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractVertexContainerRenderUnit extends AbstractRenderUnit {
    private VertexContainer vertexContainer;

    protected abstract void fillBuffers(VertexContainer vertexContainer);

    public void init(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
    }

    @Override
    public void fillBuffers() {
        fillBuffers(vertexContainer);
    }

    @Override
    public String helperString() {
        return vertexContainer.getShapeElementVertexContainerTag();
    }
}
