package com.btxtech.client.renderer.engine;

import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent
@Deprecated
public class GroundWireRender extends AbstractViewPerspectiveWireUnitRenderer {

    @Override
    protected List<Vertex> getVertexList() {
        throw new UnsupportedOperationException();
        // return terrainUiService.createGroundVertexList().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        throw new UnsupportedOperationException();
        // return terrainUiService.createGroundVertexList().getBarycentric();
    }
}
