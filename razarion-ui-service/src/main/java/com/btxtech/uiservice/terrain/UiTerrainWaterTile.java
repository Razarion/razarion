package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
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
    private ModelRenderer modelRenderer;
    private TerrainWaterTile terrainWaterTile;
    private WaterConfig waterConfig;

    public void init(boolean active, TerrainWaterTile terrainWaterTile) {
        waterConfig = terrainTypeService.getWaterConfig();
        this.terrainWaterTile = terrainWaterTile;
        modelRenderer = waterRenderTask.createModelRenderer(this);
        modelRenderer.setActive(active);
    }

    public void setActive(boolean active) {
        modelRenderer.setActive(active);
    }

    public TerrainWaterTile getTerrainWaterTile() {
        return terrainWaterTile;
    }

    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    public double getWaterAnimation() {
        return SignalGenerator.sawtooth(System.currentTimeMillis(), (int)(waterConfig.getDistortionDurationSeconds() * 1000.0), 0);
    }

    public void dispose() {
        if (modelRenderer != null) {
            waterRenderTask.remove(modelRenderer);
            modelRenderer.dispose();
        }
    }
}
