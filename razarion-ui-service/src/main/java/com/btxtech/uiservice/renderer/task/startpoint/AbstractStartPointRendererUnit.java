package com.btxtech.uiservice.renderer.task.startpoint;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.List;

/**
 * Created by Beat
 * 05.09.2016.
 */
public abstract class AbstractStartPointRendererUnit extends AbstractRenderUnit<StartPointItemPlacer> {
    private StartPointItemPlacer startPointItemPlacer;

    protected abstract void fillBuffers(List<Vertex> vertices);

    protected abstract void draw(ModelMatrices modelMatrices, Color color);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(StartPointItemPlacer startPointItemPlacer) {
        this.startPointItemPlacer = startPointItemPlacer;
        List<Vertex> vertices = startPointItemPlacer.getVertexes();
        fillBuffers(vertices);
        setElementCount(vertices);
    }


    @Override
    protected void draw(ModelMatrices modelMatrices) {
        if (startPointItemPlacer.isPositionValid()) {
            draw(modelMatrices, Colors.START_POINT_PLACER_VALID);
        } else {
            draw(modelMatrices, Colors.START_POINT_PLACER_IN_VALID);
        }
    }

    @Override
    public String helperString() {
        return "AbstractStartPointRendererUnit";
    }
}
