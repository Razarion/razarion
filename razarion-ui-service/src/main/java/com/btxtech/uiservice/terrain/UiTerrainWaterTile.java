package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.utils.SignalGenerator;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.water.WaterRenderTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.04.2017.
 */
@Dependent
public class UiTerrainWaterTile {
    @Inject
    private WaterRenderTask waterRenderTask;
    @Inject
    private TerrainTypeService terrainTypeService;
    private Float32ArrayEmu positions;
    private Float32ArrayEmu uvs;
    private ModelRenderer modelRenderer;
    private WaterConfig waterConfig;

    public void init(boolean active, SlopeConfig slopeConfig, Float32ArrayEmu positions, Float32ArrayEmu uvs) {
        this.positions = positions;
        this.uvs = uvs;
        waterConfig = terrainTypeService.getWaterConfig(slopeConfig.getWaterConfigId());
        modelRenderer = waterRenderTask.createModelRenderer(this);
        modelRenderer.setActive(active);
    }

    public void setActive(boolean active) {
        modelRenderer.setActive(active);
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

    public double getWaterAnimation() {
        return SignalGenerator.sawtooth(System.currentTimeMillis(), (int) (waterConfig.getBumpDistortionDurationSeconds() * 1000.0), 0);
    }

    public void dispose() {
        if (modelRenderer != null) {
            waterRenderTask.remove(modelRenderer);
            modelRenderer.dispose();
        }
    }
}
