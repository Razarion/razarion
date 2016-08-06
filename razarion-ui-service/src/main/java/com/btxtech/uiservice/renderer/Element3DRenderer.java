package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.datatypes.shape.ShapeTransformTRS;
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
    private Collection<ProgressAnimation> progressAnimations;

    protected abstract VertexContainerCompositeRenderer createVertexContainerRenderer();

    public void init(Element3D element3D, Collection<ModelMatrixAnimation> modelMatrixAnimations) {
        this.element3D = element3D;
        if (modelMatrixAnimations != null) {
            progressAnimations = new ArrayList<>();
            for (ModelMatrixAnimation modelMatrixAnimation : modelMatrixAnimations) {
                progressAnimations.add(new ProgressAnimation(modelMatrixAnimation));
            }
        }
    }

    public void fillRenderQueue(List<CompositeRenderer> renderQueue) {
        for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
            VertexContainerCompositeRenderer vertexContainerCompositeRenderer = createVertexContainerRenderer();
            vertexContainerCompositeRenderer.init(vertexContainer, this);
            renderQueue.add(vertexContainerCompositeRenderer);
        }
    }

    public ModelMatrices mixTransformation(ModelMatrices modelMatrix, ShapeTransform shapeTransform) {
        if (progressAnimations == null) {
            return modelMatrix.multiply(shapeTransform.setupMatrix(), shapeTransform.setupNormMatrix());
        } else {
            ShapeTransformTRS shapeTransformTRS = ((ShapeTransformTRS) shapeTransform).copy();
            for (ProgressAnimation progressAnimation : progressAnimations) {
                if (progressAnimation.isItemTriggered()) {
                    progressAnimation.dispatch(shapeTransformTRS, modelMatrix.getSyncBaseItem().getSpawnProgress());
                } else {
                    throw new UnsupportedOperationException();
                }
            }
            return modelMatrix.multiply(shapeTransformTRS.setupMatrix(), shapeTransformTRS.setupNormMatrix());
        }
    }
}
