package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.02.2017.
 */
public class StatusBarGeometry {
    private static final double HEIGHT = 0.5;
    private static final double WIDTH = 1.0;

    private List<Vertex> vertexes;
    private List<Double> visibilities;

    public StatusBarGeometry() {
        vertexes = new ArrayList<>();
        visibilities = new ArrayList<>();
        // Triangle 1
        vertexes.add(new Vertex(-WIDTH / 2.0, 0, -HEIGHT / 2.0));
        visibilities.add(0.0);
        vertexes.add(new Vertex(WIDTH / 2.0, 0, -HEIGHT / 2.0));
        visibilities.add(1.0);
        vertexes.add(new Vertex(-WIDTH / 2.0, 0, HEIGHT / 2.0));
        visibilities.add(0.0);
        // Triangle 2
        vertexes.add(new Vertex(WIDTH / 2.0, 0, -HEIGHT / 2.0));
        visibilities.add(1.0);
        vertexes.add(new Vertex(WIDTH / 2.0, 0, HEIGHT / 2.0));
        visibilities.add(1.0);
        vertexes.add(new Vertex(-WIDTH / 2.0, 0, HEIGHT / 2.0));
        visibilities.add(0.0);
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Double> getVisibilities() {
        return visibilities;
    }
}
