package com.btxtech.uiservice.renderer.task.itemplacer;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.List;

/**
 * Created by Beat
 * 05.09.2016.
 */
public abstract class AbstractBaseItemPlacerCircleRendererUnit extends AbstractRenderUnit<BaseItemPlacer> {
    private BaseItemPlacer baseItemPlacer;

    protected abstract void fillBuffers(List<Vertex> vertices);

    protected abstract void draw(ModelMatrices modelMatrices, Color color);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(BaseItemPlacer baseItemPlacer) {
        this.baseItemPlacer = baseItemPlacer;
        List<Vertex> vertices = baseItemPlacer.getVertexes();
        fillBuffers(vertices);
        setElementCount(vertices);
    }


    @Override
    protected void draw(ModelMatrices modelMatrices) {
        if (baseItemPlacer.isPositionValid()) {
            draw(modelMatrices, Colors.START_POINT_PLACER_VALID);
        } else {
            draw(modelMatrices, Colors.START_POINT_PLACER_IN_VALID);
        }
    }

    @Override
    public String helperString() {
        return "AbstractBaseItemPlacerCircleRendererUnit";
    }
}
