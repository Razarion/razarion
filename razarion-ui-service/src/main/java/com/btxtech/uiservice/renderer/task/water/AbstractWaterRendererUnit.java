package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 08.08.2016.
 */
public abstract class AbstractWaterRendererUnit extends AbstractRenderUnit<WaterUi> {
    private Logger logger = Logger.getLogger(AbstractWaterRendererUnit.class.getName());

    protected abstract void fillInternalBuffers(WaterUi waterUi);

    protected abstract void draw(WaterUi waterUi);

    @Override
    public void fillBuffers(WaterUi waterUi) {
        if (waterUi.getBmId() == null) {
            logger.warning("AbstractWaterRendererUnit no BM for water defined");
            return;
        }
        fillInternalBuffers(waterUi);
        setElementCount(waterUi);
    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData());
    }
}