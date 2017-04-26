package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.VisualUiService;
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
    private VisualUiService visualUiService;
    private ModelRenderer modelRenderer;
    private TerrainWaterTile terrainWaterTile;
    private WaterConfig waterConfig;

    public void init(boolean active, TerrainWaterTile terrainWaterTile) {
        waterConfig = visualUiService.getVisualConfig().getWaterConfig();
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

    public int getVertexCount() {
        return terrainWaterTile.getVertexCount();
    }

    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    public double getWaterAnimation() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 0);
    }

    public double getWaterAnimation2() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 500);
    }

    private double getWaterAnimation(long millis, int durationMs, int offsetMs) {
        return Math.sin(((millis % durationMs) / (double) durationMs + ((double) offsetMs / (double) durationMs)) * MathHelper.ONE_RADIANT);
    }

    public void dispose() {
        if (modelRenderer != null) {
            waterRenderTask.remove(modelRenderer);
            modelRenderer.dispose();
        }
    }
}
