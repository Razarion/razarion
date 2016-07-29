package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;

import java.util.Collection;

/**
 * Created by Beat
 * 04.09.2015.
 */
public class CompositeRenderer {
    private AbstractRenderUnit renderUnit;
    private AbstractRenderUnit depthBufferRenderUnit;
    private AbstractRenderUnit wireRenderUnit;
    private AbstractRenderUnit normRenderUnit;
    @Deprecated
    private int id;

    public CompositeRenderer() {
    }

    @Deprecated
    public CompositeRenderer(AbstractRenderUnit renderUnit, AbstractRenderUnit depthBufferRenderUnit, AbstractRenderUnit wireRenderUnit, boolean wire) {
        this.renderUnit = renderUnit;
        this.depthBufferRenderUnit = depthBufferRenderUnit;
        this.wireRenderUnit = wireRenderUnit;
    }

    public void setRenderUnit(AbstractRenderUnit renderUnit) {
        this.renderUnit = renderUnit;
        renderUnit.setCompositeRenderer(this);
    }

    public void setDepthBufferRenderUnit(AbstractRenderUnit depthBufferRenderUnit) {
        this.depthBufferRenderUnit = depthBufferRenderUnit;
        depthBufferRenderUnit.setCompositeRenderer(this);
    }

    public void setWireRenderUnit(AbstractRenderUnit wireRenderUnit) {
        this.wireRenderUnit = wireRenderUnit;
        wireRenderUnit.setCompositeRenderer(this);
    }

    public void setNormRenderUnit(AbstractRenderUnit normRenderUnit) {
        this.normRenderUnit = normRenderUnit;
        normRenderUnit.setCompositeRenderer(this);
    }

    @Deprecated
    public void setId(int id) {
        this.id = id;
    }

    public void draw() {
        if (renderUnit == null || !renderUnit.hasElements()) {
            return;
        }
        draw(renderUnit);
    }

    public void drawDepthBuffer() {
        if (depthBufferRenderUnit == null || !depthBufferRenderUnit.hasElements()) {
            return;
        }
        draw(depthBufferRenderUnit);
    }

    protected void draw(AbstractRenderUnit renderUnit) {
        renderUnit.draw();
    }

    public void drawWire() {
        if (wireRenderUnit != null && wireRenderUnit.hasElements()) {
            wireRenderUnit.draw();
        }
    }

    public void fillBuffers() {
        if (renderUnit != null) {
            renderUnit.fillBuffers();
        }
        if (depthBufferRenderUnit != null) {
            depthBufferRenderUnit.fillBuffers();
        }
        if (wireRenderUnit != null) {
            wireRenderUnit.fillBuffers();
        }
        if (normRenderUnit != null) {
            normRenderUnit.fillBuffers();
        }
    }

    @Deprecated
    public int getId() {
        return id;
    }
}
