package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.ShallowWaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.utils.SignalGenerator;
import com.btxtech.uiservice.renderer.task.simple.WaterRenderTaskRunner;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.04.2017.
 */
@Dependent
@Deprecated
public class UiTerrainWaterTile {
    @Inject
    private WaterRenderTaskRunner waterRenderTaskRunner;
    @Inject
    private TerrainTypeService terrainTypeService;
    private Float32ArrayEmu positions;
    private Float32ArrayEmu uvs;
    private WaterRenderTaskRunner.RenderTask renderTask;
    private WaterConfig waterConfig;
    private SlopeConfig slopeConfig;

    public void init(boolean active, SlopeConfig slopeConfig, Float32ArrayEmu positions, Float32ArrayEmu uvs) {
        this.slopeConfig = slopeConfig;
        this.positions = positions;
        this.uvs = uvs;
        waterConfig = terrainTypeService.getWaterConfig(slopeConfig.getWaterConfigId());
        renderTask = waterRenderTaskRunner.createRenderTask(this);
        renderTask.setActive(active);
    }

    public void setActive(boolean active) {
        renderTask.setActive(active);
    }

    public Float32ArrayEmu getPositions() {
        return positions;
    }

    public Float32ArrayEmu getUvs() {
        return uvs;
    }

    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    public ShallowWaterConfig getShallowWaterConfig() {
        return null;
    }

    public double getWaterAnimation() {
        return SignalGenerator.sawtooth(System.currentTimeMillis(), (int) (waterConfig.getDistortionAnimationSeconds() * 1000.0), 0);
    }

    public double getShallowWaterAnimation() {
        return SignalGenerator.sawtooth(System.currentTimeMillis(), (int) (getShallowWaterConfig().getDurationSeconds() * 1000.0), 0);
    }

    public void dispose() {
        if (renderTask != null) {
            waterRenderTaskRunner.destroyRenderTask(renderTask);
        }
    }
}
