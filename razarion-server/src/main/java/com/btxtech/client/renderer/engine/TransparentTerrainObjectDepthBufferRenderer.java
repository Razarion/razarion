package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 19.12.2015.
 */
@Dependent
public class TransparentTerrainObjectDepthBufferRenderer extends AbstractTerrainObjectDepthBufferRenderer {
    @Override
    protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService) {
        return terrainObjectService.getTransparentOnlyShadow(getId());
    }
}
