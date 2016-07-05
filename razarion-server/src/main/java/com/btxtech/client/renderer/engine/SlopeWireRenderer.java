package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.shared.datatypes.Vertex;

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
        return terrainSurface.getSlope(getId()).getMesh().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainSurface.getSlope(getId()).getMesh().getBarycentric();
    }
}
