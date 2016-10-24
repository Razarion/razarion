package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 08.08.2016.
 */
public abstract class AbstractWaterRendererUnit extends AbstractRenderUnit<Water> {
    private Logger logger = Logger.getLogger(AbstractWaterRendererUnit.class.getName());
    @Inject
    private VisualUiService visualUiService;
    protected abstract void fillInternalBuffers(Water water, VisualConfig visualConfig);

    @Override
    public void fillBuffers(Water water) {
        if(visualUiService.getVisualConfig().getWaterBmId() == null) {
            logger.warning("AbstractWaterRendererUnit no BM for water defined");
            return;
        }
        fillInternalBuffers(water, visualUiService.getVisualConfig());
        setElementCount(water.getVertices().size());
    }

    @Override
    protected void prepareDraw() {

    }
}