package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.VertexList;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 19.12.2015.
 */
@Dependent
public class OpaqueTerrainObjectWireRender extends AbstractTerrainObjectWireRender {
    @Override
    protected VertexList getVertexList(TerrainObjectService terrainObjectService) {
        return terrainObjectService.getOpaqueVertexList();
    }
}
