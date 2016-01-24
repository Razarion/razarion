package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class SlopeWireRenderer extends AbstractViewPerspectiveWireRenderer {
    @Inject
    private TerrainSurface terrainSurface;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainSurface.getPlateau().getMesh().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainSurface.getPlateau().getMesh().getBarycentric();
    }
}
