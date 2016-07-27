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
    private ModelMatricesProvider modelMatricesProvider;
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

    public void setModelMatricesProvider(ModelMatricesProvider modelMatricesProvider) {
        this.modelMatricesProvider = modelMatricesProvider;
    }

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

    private void draw(AbstractRenderUnit renderUnit) {
        if (modelMatricesProvider == null) {
            renderUnit.draw();
        } else {
            Collection<ModelMatrices> modelMatrices = modelMatricesProvider.provideModelMatrices(id);
            if (modelMatrices == null || modelMatrices.isEmpty()) {
                return;
            }

            renderUnit.preModelDraw();

            for (ModelMatrices modelMatrix : modelMatrices) {
                renderUnit.modelDraw(modelMatrix);
            }
        }
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

    public int getId() {
        return id;
    }
}
