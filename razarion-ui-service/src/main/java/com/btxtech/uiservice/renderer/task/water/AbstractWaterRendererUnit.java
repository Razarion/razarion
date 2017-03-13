package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 08.08.2016.
 */
public abstract class AbstractWaterRendererUnit extends AbstractRenderUnit<WaterUi> {
    private Logger logger = Logger.getLogger(AbstractWaterRendererUnit.class.getName());
    @Inject
    private VisualUiService visualUiService;

    protected abstract void fillInternalBuffers(WaterUi waterUi);

    protected abstract void draw(WaterUi waterUi);

    @Override
    public void fillBuffers(WaterUi waterUi) {
        if (visualUiService.getVisualConfig().getWaterBmId() == null) {
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