package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.VisualUiService;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class AbstractBuildupVertexContainerRenderUnit extends AbstractRenderUnit<VertexContainer> {
    private Logger logger = Logger.getLogger(AbstractBuildupVertexContainerRenderUnit.class.getName());
    @Inject
    private VisualUiService visualUiService;
    private Matrix4 buildupMatrix;
    private double minZ;
    private double mayZ;

    protected abstract void internalFillBuffers(VertexContainer vertexContainer, Matrix4 buildupMatrix, int buildupTextureId);

    protected abstract void prepareDraw(Matrix4 buildupMatrix);

    protected abstract void draw(ModelMatrices modelMatrices, double progressZ);

    public void setMaxZ(double mayZ) {
        this.minZ = minZ;
        this.mayZ = mayZ;
    }

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: " + vertexContainer.getKey());
            return;
        }
        if (visualUiService.getVisualConfig().getBuildupTextureId() == null) {
            logger.warning("Buildup Texture Id from VisualConfig is not set");
            return;
        }

        buildupMatrix = vertexContainer.getShapeTransform().setupMatrix();
        internalFillBuffers(vertexContainer, buildupMatrix, visualUiService.getVisualConfig().getBuildupTextureId());

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
