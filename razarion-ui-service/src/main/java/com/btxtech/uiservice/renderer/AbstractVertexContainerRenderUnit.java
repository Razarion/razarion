package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
@Deprecated
public abstract class AbstractVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractVertexContainerRenderUnit.class.getName());

    protected abstract void internalFillBuffers(VertexContainer vertexContainer);

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        if (vertexContainer == null) {
            logger.warning("No vertices to render");
            return;
        }

        internalFillBuffers(vertexContainer);

        setElementCount(vertexContainer);
    }

    @Override
    public String helperString() {
        return getRenderData().getKey();
    }
}
