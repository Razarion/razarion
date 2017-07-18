package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 27.09.2016.
 */
public abstract class AbstractSelectionFrameRenderUnit extends AbstractRenderUnit<GroupSelectionFrame> {
    @Inject
    private CockpitMode cockpitMode;

    protected abstract void draw();

    protected abstract void fillBuffers(List<Vertex> vertices);

    @Override
    public void setupImages() {
        // Ignore
    }

    public void fillBuffers(GroupSelectionFrame groupSelectionFrame) {
        if (groupSelectionFrame.getCorners() == null) {
            return;
        }
        List<Vertex> vertices = generateVertices(groupSelectionFrame.getCorners());
        fillBuffers(vertices);
        setElementCount(vertices);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw();
    }

    public List<Vertex> generateVertices(List<Vertex> corners) {
        List<Vertex> vertices = new ArrayList<>();
        // Line 1
        vertices.add(corners.get(0));
        vertices.add(corners.get(1));
        // Line 2
        vertices.add(corners.get(1));
        vertices.add(corners.get(2));
        // Line 3
        vertices.add(corners.get(2));
        vertices.add(corners.get(3));
        // Line 4
        vertices.add(corners.get(3));
        vertices.add(corners.get(0));
        return vertices;
    }


}
