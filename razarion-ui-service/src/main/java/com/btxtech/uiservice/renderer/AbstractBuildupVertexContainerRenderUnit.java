package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractBuildupVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractBuildupVertexContainerRenderUnit.class.getName());
    private Matrix4 buildupMatrix;
    private double mayZ;
    private Integer baseItemBuildupImageId;

    protected abstract void internalFillBuffers(VertexContainer vertexContainer, Matrix4 buildupMatrix, int buildupTextureId);

    protected abstract void prepareDraw(Matrix4 buildupMatrix);

    protected abstract void draw(ModelMatrices modelMatrices, double progressZ);

    public AbstractBuildupVertexContainerRenderUnit setMaxZ(double mayZ) {
        this.mayZ = mayZ;
        return this;
    }

    public AbstractBuildupVertexContainerRenderUnit setBaseItemBuildupImageId(Integer baseItemBuildupImageId) {
        this.baseItemBuildupImageId = baseItemBuildupImageId;
        return this;
    }

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: " + vertexContainer.getKey());
            return;
        }
        if (baseItemBuildupImageId == null) {
            // logger.warning("No buildup baseItemBuildupImageId Texture Id set: " + helperString());
            return;
        }

        buildupMatrix = vertexContainer.getShapeTransform().setupMatrix();
        internalFillBuffers(vertexContainer, buildupMatrix, baseItemBuildupImageId);

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
        draw(modelMatrices, setupProgressZ(modelMatrices.getProgress()));
    }

    private double setupProgressZ(double progress) {
        return mayZ * progress;
    }
}
