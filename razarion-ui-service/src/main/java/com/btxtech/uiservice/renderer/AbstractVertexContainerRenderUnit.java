package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractVertexContainerRenderUnit.class.getName());

    protected abstract void internalFillBuffers(VertexContainer vertexContainer);

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
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

        internalFillBuffers(vertexContainer);

        setElementCount(vertexContainer);
    }

    @Override
    public String helperString() {
        return getRenderData().getShapeElementVertexContainerTag();
    }
}
