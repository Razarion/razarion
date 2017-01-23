package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.cockpit.CockpitMode;
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
        if (groupSelectionFrame.getRectangle() == null) {
            return;
        }
        List<Vertex> vertices = generateVertices(groupSelectionFrame.getRectangle());
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

    public List<Vertex> generateVertices(Rectangle2D rectangle) {
        double z = 0;
        List<Vertex> vertices = new ArrayList<>();
        // Line 1
        vertices.add(new Vertex(rectangle.getStart(), z));
        vertices.add(new Vertex(rectangle.endX(), rectangle.startY(), z));
        // Line 2
        vertices.add(new Vertex(rectangle.endX(), rectangle.startY(), z));
        vertices.add(new Vertex(rectangle.getEnd(), z));
        // Line 3
        vertices.add(new Vertex(rectangle.getEnd(), z));
        vertices.add(new Vertex(rectangle.startX(), rectangle.endY(), z));
        // Line 4
        vertices.add(new Vertex(rectangle.startX(), rectangle.endY(), z));
        vertices.add(new Vertex(rectangle.getStart(), z));
        return vertices;
    }


}
