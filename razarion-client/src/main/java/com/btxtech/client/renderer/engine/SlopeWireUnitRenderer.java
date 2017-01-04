package com.btxtech.client.renderer.engine;

import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.datatypes.Vertex;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2016.
 */
@Deprecated
public class SlopeWireUnitRenderer extends AbstractViewPerspectiveWireUnitRenderer {
    @Inject
    private TerrainService terrainService;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainService.getSlope(getId()).getMesh().getVertices(); // TODO does not work anymore. TerrainService is in Worker now
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainService.getSlope(getId()).getMesh().getBarycentric(); // TODO does not work anymore. TerrainService is in Worker now
    }
}
