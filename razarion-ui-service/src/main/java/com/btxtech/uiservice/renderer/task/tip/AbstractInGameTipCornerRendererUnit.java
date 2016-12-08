package com.btxtech.uiservice.renderer.task.tip;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import java.util.List;

/**
 * Created by Beat
 * 05.12.2016.
 */
public abstract class AbstractInGameTipCornerRendererUnit extends AbstractRenderUnit<InGameTipVisualization> {
    private InGameTipVisualization inGameTipVisualization;

    protected abstract void fillBuffers(List<Vertex> vertices);

    protected abstract void prepareDraw(Color cornerColor);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(InGameTipVisualization inGameTipVisualization) {
        this.inGameTipVisualization = inGameTipVisualization;
        List<Vertex> vertices = inGameTipVisualization.getCornerVertices();
        fillBuffers(vertices);
        setElementCount(vertices);
    }

    @Override
    protected void prepareDraw() {
        prepareDraw(inGameTipVisualization.getCornerColor());
    }
}
