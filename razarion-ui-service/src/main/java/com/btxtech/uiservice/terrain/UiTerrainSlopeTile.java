package com.btxtech.uiservice.terrain;

import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.control.GameUiControl;
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
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private GameUiControl gameUiControl;
    private ModelRenderer modelRenderer;
    private UiTerrainTile uiTerrainTile;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private TerrainSlopeTile terrainSlopeTile;
    private double waterLevel;
    private double waterGroundLevel;

    public void init(boolean active, UiTerrainTile uiTerrainTile, TerrainSlopeTile terrainSlopeTile) {
        this.uiTerrainTile = uiTerrainTile;
        slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopeTile.getSlopeSkeletonConfigId());
        waterLevel = gameUiControl.getPlanetConfig().getWaterLevel();
        waterGroundLevel = visualUiService.getVisualConfig().getWaterConfig().getGroundLevel();
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

    public int getSlopeVertexCount() {
        return terrainSlopeTile.getSlopeVertexCount();
    }

    public Integer getTextureId() {
        return slopeSkeletonConfig.getTextureId();
    }

    public Integer getBmId() {
        return slopeSkeletonConfig.getBmId();
    }


    public LightConfig getSlopeLightConfig() {
        return slopeSkeletonConfig.getLightConfig();
    }

    public double getBmDepth() {
        return slopeSkeletonConfig.getBmDepth();
    }

    public boolean isSlopeOriented() {
        return slopeSkeletonConfig.getSlopeOriented();
    }

    public boolean hasWater() {
        return slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER;
    }

    public double getTextureScale() {
        return slopeSkeletonConfig.getTextureScale();
    }

    public double getBmScale() {
        return slopeSkeletonConfig.getBmScale();
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public double getWaterGroundLevel() {
        return waterGroundLevel;
    }
}
