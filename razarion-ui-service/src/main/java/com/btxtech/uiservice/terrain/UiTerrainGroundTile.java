package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class UiTerrainGroundTile {
    @Inject
    private GroundRenderTask groundRenderTask;
    @Inject
    private ExceptionHandler exceptionHandler;
    private ModelRenderer modelRenderer;
    private GroundConfig groundConfig;
    private Float32ArrayEmu groundPositions;
    private Object groundNorms;


    public void init(boolean active, GroundConfig groundConfig, Float32ArrayEmu groundPositions, Object groundNorms) {
        this.groundPositions = groundPositions;
        this.groundNorms = groundNorms;
        this.groundConfig = groundConfig;
        try {
            modelRenderer = groundRenderTask.createModelRenderer(this);
            modelRenderer.setActive(active);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void setActive(boolean active) {
        if (modelRenderer != null) {
            modelRenderer.setActive(active);
        }
    }

    public void dispose() {
        if (modelRenderer != null) {
            groundRenderTask.remove(modelRenderer);
            modelRenderer.dispose();
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
