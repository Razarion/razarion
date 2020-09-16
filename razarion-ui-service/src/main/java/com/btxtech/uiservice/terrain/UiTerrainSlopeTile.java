package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeSplattingConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.uiservice.renderer.WebGlRenderTask;
import com.btxtech.uiservice.renderer.task.simple.SlopeRenderTaskRunner;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.04.2017.
 */
@Dependent
public class UiTerrainSlopeTile {
    @Inject
    private SlopeRenderTaskRunner slopeRenderTask;
    private WebGlRenderTask renderSubTask;
    private SlopeConfig slopeConfig;
    private GroundConfig groundConfig;
    private SlopeGeometry slopeGeometry;
    private SlopeSplattingConfig slopeSplattingConfig;

    public void init(boolean active, SlopeConfig slopeConfig, SlopeSplattingConfig slopeSplattingConfig, GroundConfig groundConfig, SlopeGeometry slopeGeometry) {
        this.slopeConfig = slopeConfig;
        this.groundConfig = groundConfig;
        this.slopeGeometry = slopeGeometry;
        this.slopeSplattingConfig = slopeSplattingConfig;
        renderSubTask = slopeRenderTask.createRenderTask(this);
        renderSubTask.setActive(active);
    }

    public void setActive(boolean active) {
        renderSubTask.setActive(active);
    }

    public SlopeConfig getSlopeConfig() {
        return slopeConfig;
    }

    public SlopeGeometry getSlopeGeometry() {
        return slopeGeometry;
    }

    public SlopeSplattingConfig getSlopeSplattingConfig() {
        return slopeSplattingConfig;
    }

    public GroundConfig getGroundConfig() {
        return groundConfig;
    }

    public void dispose() {
        if (renderSubTask != null) {
            slopeRenderTask.destroyRenderTask(renderSubTask);
            renderSubTask = null;
        }
    }
}
