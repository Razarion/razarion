package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
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
    @Inject
    private TerrainTypeService terrainTypeService;
    private ModelRenderer modelRenderer;
    private UiTerrainTile uiTerrainTile;
    private SlopeConfig slopeConfig;
    private TerrainSlopeTile terrainSlopeTile;
    private WaterConfig waterConfig;
    private double slopeWaterSplattingFadeThreshold;

    public void init(boolean active, UiTerrainTile uiTerrainTile, TerrainSlopeTile terrainSlopeTile) {
        this.uiTerrainTile = uiTerrainTile;
        slopeConfig = terrainTypeService.getSlopeConfig(terrainSlopeTile.getSlopeConfigId());
        waterConfig = terrainTypeService.getWaterConfig();
        this.terrainSlopeTile = terrainSlopeTile;
        modelRenderer = slopeRenderTask.createModelRenderer(this);
        modelRenderer.setActive(active);
    }

    public void setActive(boolean active) {
        modelRenderer.setActive(active);
    }

    public TerrainSlopeTile getTerrainSlopeTile() {
        return terrainSlopeTile;
    }

    public UiTerrainTile getUiTerrainTile() {
        return uiTerrainTile;
    }

    public SlopeConfig getSlopeConfig() {
        return slopeConfig;
    }

    public void overrideSlopeSkeletonConfig(SlopeConfig slopeConfig) {
        if (slopeConfig.getId() == this.slopeConfig.getId()) {
            this.slopeConfig = slopeConfig;
        }
    }

    @Deprecated
    public int getSlopeVertexCount() {
        throw new UnsupportedOperationException();
    }

    public Integer getTextureId() {
        // TODO return slopeConfig.getSlopeTextureId();
        return 0;
    }

    public Integer getBmId() {
        // TODO return slopeConfig.getSlopeBumpMapId();
        return 0;
    }


    public double getBmDepth() {
        // TODO return slopeConfig.getSlopeBumpMapDepth();
        return 0;
    }

    public boolean hasWater() {
        return slopeConfig.hasWaterConfigId();
    }

    public double getTextureScale() {
        // TODO return slopeConfig.getSlopeTextureScale();
        return 0;
    }

    @Deprecated
    public WaterConfig getWaterConfig() {
        return waterConfig;
    }
    public double getWaterAnimation() {
        return SignalGenerator.sawtooth(System.currentTimeMillis(), (int)(waterConfig.getDistortionDurationSeconds() * 1000.0), 0);
    }

    public void dispose() {
        if (modelRenderer != null) {
            slopeRenderTask.remove(modelRenderer);
            modelRenderer.dispose();
        }
    }
}
