package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.SpecularLightConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
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
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private TerrainSlopeTile terrainSlopeTile;
    private WaterConfig waterConfig;
    private double slopeWaterSplattingFadeThreshold;

    public void init(boolean active, UiTerrainTile uiTerrainTile, TerrainSlopeTile terrainSlopeTile) {
        this.uiTerrainTile = uiTerrainTile;
        slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopeTile.getSlopeConfigId());
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

    public SlopeSkeletonConfig getSlopeSkeletonConfig() {
        return slopeSkeletonConfig;
    }

    public void overrideSlopeSkeletonConfig(SlopeSkeletonConfig slopeSkeletonConfig) {
        if (slopeSkeletonConfig.getId() == this.slopeSkeletonConfig.getId()) {
            this.slopeSkeletonConfig = slopeSkeletonConfig;
        }
    }

    public int getSlopeVertexCount() {
        return terrainSlopeTile.getSlopeVertexCount();
    }

    public Integer getTextureId() {
        return slopeSkeletonConfig.getSlopeTextureId();
    }

    public Integer getBmId() {
        return slopeSkeletonConfig.getSlopeBumpMapId();
    }


    public SpecularLightConfig getSlopeLightConfig() {
        return slopeSkeletonConfig.getSpecularLightConfig();
    }

    public double getBmDepth() {
        return slopeSkeletonConfig.getSlopeBumpMapDepth();
    }

    public boolean hasWater() {
        return slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER;
    }

    public double getTextureScale() {
        return slopeSkeletonConfig.getSlopeTextureScale();
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
