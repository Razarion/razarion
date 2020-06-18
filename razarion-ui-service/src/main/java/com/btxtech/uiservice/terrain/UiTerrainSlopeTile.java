package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.shared.utils.SignalGenerator;
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
    private SlopeGeometry slopeGeometry;
    private WaterConfig waterConfig;
    private double slopeWaterSplattingFadeThreshold;

    public void init(boolean active, SlopeConfig slopeConfig, SlopeGeometry slopeGeometry) {
        this.slopeConfig = slopeConfig;
        this.slopeGeometry = slopeGeometry;
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

    public void overrideSlopeSkeletonConfig(SlopeConfig slopeConfig) {
        if (slopeConfig.getId() == this.slopeConfig.getId()) {
            this.slopeConfig = slopeConfig;
        }
    }

    @Deprecated
    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    public double getWaterAnimation() {
        return SignalGenerator.sawtooth(System.currentTimeMillis(), (int)(waterConfig.getBumpDistortionDurationSeconds() * 1000.0), 0);
    }

    public void dispose() {
        if (modelRenderer != null) {
            slopeRenderTask.remove(modelRenderer);
            modelRenderer.dispose();
        }
    }
}
