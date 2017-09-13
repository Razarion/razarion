package com.btxtech.uiservice.renderer.task.visualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.InGameItemVisualization;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.List;

/**
 * Created by Beat
 * 05.12.2016.
 */
public abstract class AbstractInGameItemCornerRendererUnit extends AbstractRenderUnit<InGameItemVisualization> {
    private InGameItemVisualization inGameItemVisualization;

    protected abstract void fillBuffers(List<Vertex> vertices);

    protected abstract void prepareDraw(Color cornerColor);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(InGameItemVisualization inGameItemVisualization) {
        this.inGameItemVisualization = inGameItemVisualization;
        List<Vertex> vertices = inGameItemVisualization.getCornerVertices();
        fillBuffers(vertices);
        setElementCount(vertices);
    }

    @Override
    protected void prepareDraw() {
        prepareDraw(inGameItemVisualization.getCornerColor());
    }
}
