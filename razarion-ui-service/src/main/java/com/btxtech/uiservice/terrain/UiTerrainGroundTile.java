package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class UiTerrainGroundTile {
    @Inject
    private GroundRenderTask groundRenderTask;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ExceptionHandler exceptionHandler;
    private ModelRenderer modelRenderer;
    private GroundConfig groundConfig;
    private Float32ArrayEmu groundPositions;
    private Object groundNorms;


    public void init(boolean active, Integer groundId, Float32ArrayEmu groundPositions, Object groundNorms) {
        if(groundId != null) {
            groundConfig = terrainTypeService.getGroundConfig(groundId);
        } else {
            groundConfig = terrainTypeService.getGroundConfig(gameUiControl.getPlanetConfig().getGroundConfigId());
        }
        this.groundPositions = groundPositions;
        this.groundNorms = groundNorms;
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

    public Integer getSplattingId() {
        // TODO return groundSkeletonConfig.getSplatting().getId();
        throw new UnsupportedOperationException("TODO");
    }

    public Integer getBottomTextureId() {
        // TODO return groundSkeletonConfig.getBottomTextureId();
        throw new UnsupportedOperationException("TODO");
    }

    public Integer getBottomBmId() {
        // TODO return groundSkeletonConfig.getBottomBmId();
        throw new UnsupportedOperationException("TODO");
    }

    public double getTopTextureScale() {
        // TODO return groundSkeletonConfig.getTopTextureScale();
        throw new UnsupportedOperationException("TODO");
    }

    public double getSplattingScale() {
        // TODO  return groundSkeletonConfig.getSplatting().getScale();
        throw new UnsupportedOperationException("TODO");
    }

    public double getBottomTextureScale() {
        // TODO return groundSkeletonConfig.getBottomTextureScale();
        throw new UnsupportedOperationException("TODO");
    }

    public double getBottomBmScale() {
        // TODO return groundSkeletonConfig.getBottomBmScale();
        throw new UnsupportedOperationException("TODO");
    }

    public double getBottomBmDepth() {
        // TODO return groundSkeletonConfig.getBottomBmDepth();
        throw new UnsupportedOperationException("TODO");
    }

    public double getSplattingGroundBmMultiplicator() {
        // Todo return groundSkeletonConfig.getSplattingGroundBmMultiplicator();
        return 0;
    }
}
