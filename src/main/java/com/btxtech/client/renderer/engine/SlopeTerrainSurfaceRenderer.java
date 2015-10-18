package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
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

    @Override
    protected ImageDescriptor getTopImageDescriptor() {
        return terrainSurface.getSlopeTopImageDescriptor();
    }

    @Override
    protected ImageDescriptor getBlendImageDescriptor() {
        return terrainSurface.getSlopeBlendImageDescriptor();
    }

    @Override
    protected ImageDescriptor getBottomImageDescriptor() {
        return terrainSurface.getSlopeBottomImageDescriptor();
    }
}
