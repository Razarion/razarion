package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractLoopUpVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractVertexContainerRenderUnit.class.getName());

    protected abstract void internalFillBuffers(VertexContainer vertexContainer);

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        if (vertexContainer == null || vertexContainer.empty()) {
            logger.warning("No vertices to render");
            return;
        }
        if (vertexContainer.checkWrongTextureSize()) {
            logger.warning("TextureCoordinate has not same size as vertices: " + vertexContainer.createShapeElementVertexContainerTag());
            return;
        }
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: " + vertexContainer.createShapeElementVertexContainerTag());
            return;
        }
        if (!vertexContainer.hasLookUpTextureId()) {
            logger.warning("No look up texture id: " + vertexContainer.createShapeElementVertexContainerTag());
            return;
        }

        internalFillBuffers(vertexContainer);

        setElementCount(vertexContainer);
    }

    @Override
    public String helperString() {
        return getRenderData().createShapeElementVertexContainerTag();
    }
}
