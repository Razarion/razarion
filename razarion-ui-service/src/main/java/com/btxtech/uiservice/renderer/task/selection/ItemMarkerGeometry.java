package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

public class ItemMarkerGeometry {
    private static final int SEGMENT_COUNT = 20;
    private List<Vertex> vertexes;
    private List<Double> visibilities;

    public ItemMarkerGeometry() {
        Circle2D circle2D = new Circle2D(new DecimalPosition(0, 0), 1.0);
        vertexes = circle2D.triangulation(SEGMENT_COUNT, 0);

        visibilities = new ArrayList<>();
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            visibilities.add(0.0);
            visibilities.add(1.0);
            visibilities.add(1.0);
        }
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Double> getVisibilities() {
        return visibilities;
    }
}
