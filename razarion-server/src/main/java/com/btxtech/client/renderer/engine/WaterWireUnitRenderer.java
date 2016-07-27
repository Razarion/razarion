package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class WaterWireUnitRenderer extends AbstractViewPerspectiveWireUnitRenderer {
    @Inject
    private TerrainUiService terrainUiService;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainUiService.getWater().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainUiService.getWater().getBarycentric();
    }
}
