package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent
@Deprecated
public class GroundWireRender extends AbstractViewPerspectiveWireUnitRenderer {
    @Inject
    private TerrainUiService terrainUiService;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainUiService.getGroundVertexList().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainUiService.getGroundVertexList().getBarycentric();
    }
}
