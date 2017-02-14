package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.02.2017.
 */
public abstract class AbstractStatusBarRendererUnit extends AbstractRenderUnit<Void> {
    private static final double HEIGHT = 0.5;
    private static final double WIDTH = 1.0;

    protected abstract void fillBuffers(List<Vertex> vertices, List<Double> visibilities);

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void fillBuffers(Void aVoid) {
        List<Vertex> vertices = new ArrayList<>();
        List<Double> visibilities = new ArrayList<>();
        // Triangle 1
        vertices.add(new Vertex(-WIDTH / 2.0, 0, -HEIGHT / 2.0));
        visibilities.add(0.0);
        vertices.add(new Vertex(WIDTH / 2.0, 0, -HEIGHT / 2.0));
        visibilities.add(1.0);
        vertices.add(new Vertex(-WIDTH / 2.0, 0, HEIGHT / 2.0));
        visibilities.add(0.0);
        // Triangle 2
        vertices.add(new Vertex(WIDTH / 2.0, 0, -HEIGHT / 2.0));
        visibilities.add(1.0);
        vertices.add(new Vertex(WIDTH / 2.0, 0, HEIGHT / 2.0));
        visibilities.add(1.0);
        vertices.add(new Vertex(-WIDTH / 2.0, 0, HEIGHT / 2.0));
        visibilities.add(0.0);

        fillBuffers(vertices, visibilities);
        setElementCount(vertices);
    }
}
