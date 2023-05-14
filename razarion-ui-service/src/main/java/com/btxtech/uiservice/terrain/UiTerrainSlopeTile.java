package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.uiservice.renderer.task.simple.SlopeRenderTaskRunner;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.04.2017.
 */
@Dependent
@Deprecated
public class UiTerrainSlopeTile {
    @Inject
    private SlopeRenderTaskRunner slopeRenderTaskRunner;
    private SlopeRenderTaskRunner.RenderTask renderTask;
    private SlopeConfig slopeConfig;
    private GroundConfig groundConfig;
    private SlopeGeometry slopeGeometry;

    public void setActive(boolean active) {
        renderTask.setActive(active);
    }

    public SlopeConfig getSlopeConfig() {
        return slopeConfig;
    }

    public SlopeGeometry getSlopeGeometry() {
        return slopeGeometry;
    }

    public GroundConfig getGroundConfig() {
        return groundConfig;
    }

    public void dispose() {
        if (renderTask != null) {
            slopeRenderTaskRunner.destroyRenderTask(renderTask);
            renderTask = null;
        }
    }
}
