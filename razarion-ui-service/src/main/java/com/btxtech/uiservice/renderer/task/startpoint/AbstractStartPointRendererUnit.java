package com.btxtech.uiservice.renderer.task.startpoint;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.List;

/**
 * Created by Beat
 * 05.09.2016.
 */
public abstract class AbstractStartPointRendererUnit extends AbstractRenderUnit<StartPointItemPlacer> {
    public abstract void fillBuffers(List<Vertex> vertices);

    public abstract void draw(List<Vertex> vertices);


    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(StartPointItemPlacer startPointItemPlacer) {
        List<Vertex> vertices = startPointItemPlacer.getVertexes();
        fillBuffers(vertices);
        setElementCount(vertices);
    }

    // TODO draw ... model ...
}
