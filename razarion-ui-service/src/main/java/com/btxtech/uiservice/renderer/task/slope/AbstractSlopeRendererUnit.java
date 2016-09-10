package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractSlopeRendererUnit extends AbstractRenderUnit<Slope> {
    protected abstract void fillBuffer(Slope slope, Mesh mesh);

    protected abstract void draw(Slope slope);

    @Override
    public void fillBuffers(Slope slope) {
        Mesh mesh = slope.getMesh();
        fillBuffer(slope, mesh);
        setElementCount(mesh);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData());
    }

    @Override
    public String helperString() {
        return "Slope id: " + getRenderData().getSlopeSkeletonConfig().getId();
    }
}
