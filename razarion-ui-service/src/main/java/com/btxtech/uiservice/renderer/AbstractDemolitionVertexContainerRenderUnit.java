package com.btxtech.uiservice.renderer;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractDemolitionVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractDemolitionVertexContainerRenderUnit.class.getName());
    private Integer baseItemDemolitionImageId;

    protected abstract void internalFillBuffers(VertexContainer vertexContainer, Integer baseItemDemolitionImageId);

    protected abstract void draw(ModelMatrices modelMatrices, double progressZ);

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        if (vertexContainer == null) {
            logger.warning("No vertices to render");
            return;
        }
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: " + vertexContainer.getKey());
            return;
        }

        internalFillBuffers(vertexContainer, baseItemDemolitionImageId);

        setElementCount(vertexContainer);
    }

    public void setAdditionalData(Integer baseItemDemolitionImageId) {
        this.baseItemDemolitionImageId = baseItemDemolitionImageId;
    }

    @Override
    public String helperString() {
        return getRenderData().getKey();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(modelMatrices, modelMatrices.getProgress());
    }
}
