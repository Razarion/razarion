package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.VertexList;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 19.12.2015.
 */
@Dependent
public class OpaqueTerrainObjectDepthBufferRenderer extends AbstractTerrainObjectDepthBufferRenderer {
    @Override
    protected VertexList getVertexList(TerrainObjectService terrainObjectService) {
        return terrainObjectService.getOpaqueVertexList();
    }
}
