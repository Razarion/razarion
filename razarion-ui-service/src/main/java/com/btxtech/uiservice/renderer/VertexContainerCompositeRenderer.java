package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.Collection;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class VertexContainerCompositeRenderer extends CompositeRenderer {
    private VertexContainer vertexContainer;
    private Element3DRenderer element3DRenderer;

    protected abstract Collection<ModelMatrices> provideModelMatrices();

    protected abstract void initRenderUnits();

    public void init(VertexContainer vertexContainer, Element3DRenderer element3DRenderer) {
        this.vertexContainer = vertexContainer;
        this.element3DRenderer = element3DRenderer;
        initRenderUnits();
    }

    public VertexContainer getVertexContainer() {
        return vertexContainer;
    }

    protected void draw(AbstractRenderUnit renderUnit) {
        Collection<ModelMatrices> modelMatrices = provideModelMatrices();
        if (modelMatrices == null || modelMatrices.isEmpty()) {
            return;
        }

        renderUnit.preModelDraw();

        for (ModelMatrices modelMatrix : modelMatrices) {
            modelMatrix = element3DRenderer.mixTransformation(modelMatrix, vertexContainer.getShapeTransform());
            renderUnit.modelDraw(modelMatrix);
        }
    }
}
