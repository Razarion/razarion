package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import javax.inject.Inject;
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
        if(groupSelectionFrame.getRectangle() == null) {
            return;
        }
        List<Vertex> vertices = groupSelectionFrame.generateVertices();
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
}
