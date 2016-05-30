package com.btxtech.client.renderer.engine;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.primitives.Vertex;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent
public class GroundWireRender extends AbstractViewPerspectiveWireRenderer {
    @Inject
    private TerrainSurface terrainSurface;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainSurface.getGroundVertexList().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainSurface.getGroundVertexList().getBarycentric();
    }
}
