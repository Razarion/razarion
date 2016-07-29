package com.btxtech.uiservice.renderer;

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
    private Collection<ModelMatrixAnimation> modelMatrixAnimations = new ArrayList<>();

    protected abstract VertexContainerCompositeRenderer createVertexContainerRenderer();

    public void init(Element3D element3D, Shape3DRenderer shape3DRenderer) {
        this.element3D = element3D;
        this.shape3DRenderer = shape3DRenderer;
    }

    public void addModelMatrixAnimation(ModelMatrixAnimation modelMatrixAnimation) {
        modelMatrixAnimations.add(modelMatrixAnimation);
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
}
