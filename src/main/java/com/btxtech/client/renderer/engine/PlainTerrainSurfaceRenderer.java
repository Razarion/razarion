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
public class PlainTerrainSurfaceRenderer extends AbstractTerrainSurfaceRenderer {
    @Inject
    private TerrainSurface terrainSurface;

    @Override
    protected VertexList provideVertexList() {
        return terrainSurface.getPlainVertexList();
    }

    @Override
    protected ImageDescriptor getBottomImageDescriptor() {
        return terrainSurface.getTopImageDescriptor();
    }

    @Override
    protected ImageDescriptor getBlendImageDescriptor() {
        return terrainSurface.getBlendImageDescriptor();
    }

    @Override
    protected ImageDescriptor getTopImageDescriptor() {
        return terrainSurface.getBottomImageDescriptor();
    }
}
