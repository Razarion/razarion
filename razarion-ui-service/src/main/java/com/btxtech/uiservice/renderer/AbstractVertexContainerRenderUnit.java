package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractVertexContainerRenderUnit extends AbstractRenderUnit {
    private Logger logger = Logger.getLogger(AbstractVertexContainerRenderUnit.class.getName());
    private VertexContainer vertexContainer;

    protected abstract void fillBuffers(VertexContainer vertexContainer);

    public void init(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
    }

    @Override
    public void fillBuffers() {
        if (vertexContainer == null || vertexContainer.isEmpty()) {
            logger.warning("No vertices to render");
            return;
        }
        if (vertexContainer.checkWrongTextureSize()) {
            logger.warning("TextureCoordinate has not same size as vertices: " + vertexContainer.getShapeElementVertexContainerTag());
            return;
        }
        if (vertexContainer.checkWrongNormSize()) {
            logger.warning("Normal has not same size as vertices: "+ vertexContainer.getShapeElementVertexContainerTag());
            return;
        }
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: "+ vertexContainer.getShapeElementVertexContainerTag());
            return;
        }

        fillBuffers(vertexContainer);

        setElementCount(vertexContainer);
    }

    @Override
    public String helperString() {
        return vertexContainer.getShapeElementVertexContainerTag();
    }
}
