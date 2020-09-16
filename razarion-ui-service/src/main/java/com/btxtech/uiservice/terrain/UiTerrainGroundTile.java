package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.task.simple.GroundRenderTaskRunner;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class UiTerrainGroundTile {
    @Inject
    private GroundRenderTaskRunner groundRenderTaskRunner;
    @Inject
    private ExceptionHandler exceptionHandler;
    private GroundRenderTaskRunner.RenderTask renderTask;
    private GroundConfig groundConfig;
    private Float32ArrayEmu groundPositions;
    private Object groundNorms;


    public void init(boolean active, GroundConfig groundConfig, Float32ArrayEmu groundPositions, Object groundNorms) {
        this.groundPositions = groundPositions;
        this.groundNorms = groundNorms;
        this.groundConfig = groundConfig;
        try {
            renderTask = groundRenderTaskRunner.createRenderTask(this);
            renderTask.setActive(active);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void setActive(boolean active) {
        if (renderTask != null) {
            renderTask.setActive(active);
        }
    }

    public void dispose() {
        if (renderTask != null) {
            groundRenderTaskRunner.destroyRenderTask(renderTask);
            renderTask = null;
        }
    }

    public Float32ArrayEmu getGroundPositions() {
        return groundPositions;
    }

    public Object getGroundNorms() {
        return groundNorms;
    }

    public GroundConfig getGroundConfig() {
        return groundConfig;
    }
}
