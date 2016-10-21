package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractGroundRendererUnit extends AbstractRenderUnit<GroundSkeletonConfig> {
    private Logger logger = Logger.getLogger(AbstractGroundRendererUnit.class.getName());
    @Inject
    private TerrainUiService terrainUiService;

    protected abstract void fillBuffers(VertexList vertexList, GroundSkeletonConfig groundSkeletonConfig);

    @Override
    public void fillBuffers(GroundSkeletonConfig groundSkeletonConfig) {
        if(groundSkeletonConfig.getTopTextureId() == null) {
            logger.warning("No TopTextureId in AbstractGroundRendererUnit for: " + helperString());
            return;
        };
        if(groundSkeletonConfig.getTopBmId() == null) {
            logger.warning("No TopBmId in AbstractGroundRendererUnit for: " + helperString());
            return;
        };
        if(groundSkeletonConfig.getSplattingId() == null) {
            logger.warning("No SplattingId in AbstractGroundRendererUnit for: " + helperString());
            return;
        };
        if(groundSkeletonConfig.getBottomTextureId() == null) {
            logger.warning("No BottomTextureId in AbstractGroundRendererUnit for: " + helperString());
            return;
        };
        if(groundSkeletonConfig.getBottomBmId() == null) {
            logger.warning("No BottomBmId in AbstractGroundRendererUnit for: " + helperString());
            return;
        };
        VertexList vertexList = terrainUiService.getGroundVertexList();
        fillBuffers(vertexList, groundSkeletonConfig);
        setElementCount(vertexList);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    public String helperString() {
        return "Ground";
    }
}
