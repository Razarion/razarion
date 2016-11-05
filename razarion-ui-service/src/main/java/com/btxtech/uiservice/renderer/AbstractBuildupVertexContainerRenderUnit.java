package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractBuildupVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractBuildupVertexContainerRenderUnit.class.getName());
    private double maxZ;
    private Matrix4 buildupMatrix;

    protected abstract void internalFillBuffers(VertexContainer vertexContainer);

    protected abstract void prepareDraw(Matrix4 buildupMatrix);

    protected abstract void draw(ModelMatrices modelMatrices, double progressZ);

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
        if (vertexContainer.checkWrongNormSize()) {
            logger.warning("Normal has not same size as vertices: " + vertexContainer.createShapeElementVertexContainerTag());
            return;
        }
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: " + vertexContainer.createShapeElementVertexContainerTag());
            return;
        }

        maxZ = Double.MIN_VALUE;
        buildupMatrix = vertexContainer.getShapeTransform().getMatrix();
        for (Vertex vertex : vertexContainer.getVertices()) {
            maxZ = Math.max(buildupMatrix.multiply(vertex, 1.0).getZ(), maxZ);
        }
        internalFillBuffers(vertexContainer);

        setElementCount(vertexContainer);
    }

    @Override
    public String helperString() {
        return getRenderData().createShapeElementVertexContainerTag();
    }

    @Override
    protected void prepareDraw() {
        prepareDraw(buildupMatrix);
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(modelMatrices, modelMatrices.getProgress() * maxZ);
    }
}
