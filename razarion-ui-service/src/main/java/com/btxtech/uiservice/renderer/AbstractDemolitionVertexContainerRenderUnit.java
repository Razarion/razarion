package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractDemolitionVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractDemolitionVertexContainerRenderUnit.class.getName());
    private Matrix4 heightMatrix;
    private double maxHeight;
    private Integer baseItemDemolitionCuttingImageId;
    private Integer baseItemDemolitionLookUpImageId;

    protected abstract void internalFillBuffers(VertexContainer vertexContainer, Integer baseItemDemolitionCuttingImageId, Integer baseItemDemolitionLookUpImageId);

    protected abstract void prepareDraw(Matrix4 heightMatrix, double maxHeight);

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

        heightMatrix = vertexContainer.getShapeTransform().setupMatrix();
        internalFillBuffers(vertexContainer, baseItemDemolitionCuttingImageId, baseItemDemolitionLookUpImageId);

        setElementCount(vertexContainer);
    }

    public void setAdditionalData(double maxHeight, Integer baseItemDemolitionCuttingImageId, Integer baseItemDemolitionLookUpImageId) {
        this.maxHeight = maxHeight;
        this.baseItemDemolitionCuttingImageId = baseItemDemolitionCuttingImageId;
        this.baseItemDemolitionLookUpImageId = baseItemDemolitionLookUpImageId;
    }

    @Override
    public String helperString() {
        return getRenderData().createShapeElementVertexContainerTag();
    }

    @Override
    protected void prepareDraw() {
        prepareDraw(heightMatrix, maxHeight);
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(modelMatrices, modelMatrices.getProgress());
    }
}
