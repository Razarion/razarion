package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 12.10.2015.
 */
@Dependent
public class SlopeTerrainSurfaceRenderer extends AbstractTerrainSurfaceRenderer {
    @Inject
    private TerrainSurface terrainSurface;

    @Override
    protected VertexList provideVertexList() {
        return terrainSurface.getSlopeVertexList();
    }
}
