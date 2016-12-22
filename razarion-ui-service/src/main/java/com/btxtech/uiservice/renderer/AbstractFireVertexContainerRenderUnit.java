package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractFireVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private static final long DURATION_MILLIS = 1000;
    private Logger logger = Logger.getLogger(AbstractVertexContainerRenderUnit.class.getName());
    private Matrix4 heightMatrix;
    private double minHeight;
    private double maxHeight;

    protected abstract void internalFillBuffers(VertexContainer vertexContainer);

    protected abstract void prepareDraw(double yTextureOffset, Matrix4 heightMatrix, double minHeight, double maxHeight);

    public void setAttributes(double minHeight, double maxHeight) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

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

        heightMatrix = vertexContainer.getShapeTransform().setupMatrix();

        internalFillBuffers(vertexContainer);

        setElementCount(vertexContainer);
    }

    @Override
    protected void prepareDraw() {
        double yTextureOffset = (double) (System.currentTimeMillis() % DURATION_MILLIS) / (double) DURATION_MILLIS;
        prepareDraw(yTextureOffset, heightMatrix, minHeight, maxHeight);
    }

    @Override
    public String helperString() {
        return getRenderData().createShapeElementVertexContainerTag();
    }
}
