package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractGroundRendererUnit extends AbstractRenderUnit<GroundUi> {
    private Logger logger = Logger.getLogger(AbstractGroundRendererUnit.class.getName());

    protected abstract void fillBuffersInternal(GroundUi groundUi);

    protected abstract void draw(GroundUi groundUi);

    @Override
    public void fillBuffers(GroundUi groundUi) {
        if (groundUi.getTopTextureId() == null) {
            logger.warning("No TopTextureId in AbstractGroundRendererUnit for: " + helperString());
            return;
        }
        if (groundUi.getTopBmId() == null) {
            logger.warning("No TopBmId in AbstractGroundRendererUnit for: " + helperString());
            return;
        }
        if (groundUi.getSplattingId() == null) {
            logger.warning("No SplattingId in AbstractGroundRendererUnit for: " + helperString());
            return;
        }
        if (groundUi.getBottomTextureId() == null) {
            logger.warning("No BottomTextureId in AbstractGroundRendererUnit for: " + helperString());
            return;
        }
        if (groundUi.getBottomBmId() == null) {
            logger.warning("No BottomBmId in AbstractGroundRendererUnit for: " + helperString());
            return;
        }
        fillBuffersInternal(groundUi);
        setElementCount(groundUi);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData());
    }

    @Override
    public String helperString() {
        return "Ground";
    }
}
