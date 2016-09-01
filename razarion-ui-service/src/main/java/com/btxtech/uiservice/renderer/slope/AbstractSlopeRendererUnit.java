package com.btxtech.uiservice.renderer.slope;

import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractSlopeRendererUnit extends AbstractRenderUnit {
    private Slope slope;

    protected abstract void fillBuffers(Slope slope);

    protected abstract void draw(Slope slope);

    public void setSlope(Slope slope) {
        this.slope = slope;
    }

    @Override
    public void fillBuffers() {
        fillBuffers(slope);
    }

    @Override
    public void draw() {
        draw(slope);
    }

    @Override
    public String helperString() {
        return "Slope id: " + slope.getSlopeSkeletonConfig().getId();
    }
}
