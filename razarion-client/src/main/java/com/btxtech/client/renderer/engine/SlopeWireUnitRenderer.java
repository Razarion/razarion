package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.datatypes.Vertex;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class SlopeWireUnitRenderer extends AbstractViewPerspectiveWireUnitRenderer {
    @Inject
    private TerrainUiService terrainUiService;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainUiService.getSlope(getId()).getMesh().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainUiService.getSlope(getId()).getMesh().getBarycentric();
    }
}
