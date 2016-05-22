package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.primitives.Vertex;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class WaterWireRenderer extends AbstractViewPerspectiveWireRenderer {
    @Inject
    private TerrainSurface terrainSurface;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainSurface.getWater().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainSurface.getWater().getBarycentric();
    }
}
