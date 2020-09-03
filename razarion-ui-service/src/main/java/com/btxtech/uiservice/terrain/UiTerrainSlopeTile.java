package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeSplattingConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.slope.SlopeRenderTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.04.2017.
 */
@Dependent
public class UiTerrainSlopeTile {
    @Inject
    private SlopeRenderTask slopeRenderTask;
    private ModelRenderer modelRenderer;
    private SlopeConfig slopeConfig;
    private GroundConfig groundConfig;
    private SlopeGeometry slopeGeometry;
    private SlopeSplattingConfig slopeSplattingConfig;

    public void init(boolean active, SlopeConfig slopeConfig, SlopeSplattingConfig slopeSplattingConfig, GroundConfig groundConfig, SlopeGeometry slopeGeometry) {
        this.slopeConfig = slopeConfig;
        this.groundConfig = groundConfig;
        this.slopeGeometry = slopeGeometry;
        this.slopeSplattingConfig = slopeSplattingConfig;
        modelRenderer = slopeRenderTask.createModelRenderer(this);
        modelRenderer.setActive(active);
    }

    public void setActive(boolean active) {
        modelRenderer.setActive(active);
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
        if (modelRenderer != null) {
            slopeRenderTask.destroy(modelRenderer);
        }
    }
}
