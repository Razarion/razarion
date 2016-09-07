package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractGroundRendererUnit extends AbstractRenderUnit<GroundConfig> {
    @Inject
    private TerrainUiService terrainUiService;

    protected abstract void fillBuffers(VertexList vertexList);

    @Override
    public void fillBuffers(GroundConfig groundConfig) {
        VertexList vertexList = terrainUiService.getGroundVertexList();
        fillBuffers(vertexList);
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
