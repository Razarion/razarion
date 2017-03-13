package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractSlopeRendererUnit extends AbstractRenderUnit<SlopeUi> {
    private Logger logger = Logger.getLogger(AbstractSlopeRendererUnit.class.getName());
    @Inject
    private TerrainUiService terrainUiService;

    protected abstract void fillBuffer(SlopeUi slopeUi, GroundUi groundUi);

    protected abstract void draw(SlopeUi slopeUi, GroundUi groundUi);

    @Override
    public void fillBuffers(SlopeUi slopeUi) {
        if (slopeUi.getTextureId() == null) {
            logger.warning("No Texture Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        }
        if (slopeUi.getBmId() == null) {
            logger.warning("No BM Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        }

        fillBuffer(slopeUi, terrainUiService.getGroundUi());
        setElementCount(slopeUi);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData(), terrainUiService.getGroundUi());
    }

    @Override
    public String helperString() {
        return "Slope: " + getRenderData().getObjectNameId();
    }
}
