package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.VertexContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class Element3DRenderer {
    private Element3D element3D;
    private Shape3DRenderer shape3DRenderer;
    private Collection<ProgressAnimation> progressAnimations;

    protected abstract VertexContainerCompositeRenderer createVertexContainerRenderer();

    public void init(Element3D element3D, Shape3DRenderer shape3DRenderer, Collection<ModelMatrixAnimation> modelMatrixAnimations) {
        this.element3D = element3D;
        this.shape3DRenderer = shape3DRenderer;
        if (modelMatrixAnimations != null) {
            progressAnimations = new ArrayList<>();
            for (ModelMatrixAnimation modelMatrixAnimation : modelMatrixAnimations) {
                progressAnimations.add(new ProgressAnimation(modelMatrixAnimation));
            }
        }
    }

    public Shape3DRenderer getShape3DRenderer() {
        return shape3DRenderer;
    }

    public void fillRenderQueue(List<CompositeRenderer> renderQueue) {
        for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
            VertexContainerCompositeRenderer vertexContainerCompositeRenderer = createVertexContainerRenderer();
            vertexContainerCompositeRenderer.init(vertexContainer, this);
            renderQueue.add(vertexContainerCompositeRenderer);
        }
    }

    public ModelMatrices mixAnimation(ModelMatrices modelMatrix) {
        if (progressAnimations == null) {
            return modelMatrix;
        }
        ModelMatrices resultingMatrix = modelMatrix;
        for (ProgressAnimation progressAnimation : progressAnimations) {
            if (progressAnimation.isItemTriggered()) {
                resultingMatrix = progressAnimation.mix(resultingMatrix);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return resultingMatrix;
    }
}
