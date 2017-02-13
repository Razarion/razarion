package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2017.
 */
public abstract class AbstractSelectedMarkerRendererUnit extends AbstractRenderUnit<Void> {
    private static final int SEGMENT_COUNT = 20;

    protected abstract void fillBuffers(List<Vertex> vertices, List<Double> visibilities);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(Void aVoid) {
        Circle2D circle2D = new Circle2D(new DecimalPosition(0, 0), 1.0);
        List<Vertex> vertexes = circle2D.triangulation(SEGMENT_COUNT, 0);
        List<Double> visibility = new ArrayList<>();
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            visibility.add(0.0);
            visibility.add(1.0);
            visibility.add(1.0);
        }
        fillBuffers(vertexes, visibility);
        setElementCount(vertexes);
    }
}
