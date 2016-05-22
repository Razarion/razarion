package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 19.12.2015.
 */
@Dependent
public class OpaqueTerrainObjectWireRender extends AbstractTerrainObjectWireRender {
    @Override
    protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService) {
        return terrainObjectService.getOpaqueVertexContainer(getId());
    }
}
