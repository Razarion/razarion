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
    private Matrix4 buildupMatrix;

    protected abstract void internalFillBuffers(VertexContainer vertexContainer);

    protected abstract void prepareDraw(Matrix4 buildupMatrix);

    protected abstract void draw(ModelMatrices modelMatrices, double progressZ);

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: " + vertexContainer.getKey());
            return;
        }

        buildupMatrix = vertexContainer.getShapeTransform().setupMatrix();
        internalFillBuffers(vertexContainer);

        setElementCount(vertexContainer);
    }

    @Override
    public String helperString() {
        return getRenderData().getKey();
    }

    @Override
    protected void prepareDraw() {
        prepareDraw(buildupMatrix);
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(modelMatrices, modelMatrices.getProgress());
    }
}
