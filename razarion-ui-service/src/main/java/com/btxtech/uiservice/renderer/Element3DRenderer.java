package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.datatypes.shape.ShapeTransformTRS;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.ModelMatricesProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 29.07.2016.
 */
@Dependent
public class Element3DRenderer {
    @Inject
    private Instance<VertexContainerCompositeRenderer> instance;
    private Element3D element3D;
    private Collection<ProgressAnimation> progressAnimations;
    private ModelMatricesProvider modelMatricesProvider;

    public void init(Element3D element3D, Collection<ModelMatrixAnimation> modelMatrixAnimations, ModelMatricesProvider modelMatricesProvider) {
        this.element3D = element3D;
        this.modelMatricesProvider = modelMatricesProvider;
        if (modelMatrixAnimations != null) {
            progressAnimations = new ArrayList<>();
            for (ModelMatrixAnimation modelMatrixAnimation : modelMatrixAnimations) {
                progressAnimations.add(new ProgressAnimation(modelMatrixAnimation));
            }
        }
    }

    public List<CompositeRenderer> createCompositeRenderers() {
        List<CompositeRenderer> compositeRenderers = new ArrayList<>();
        for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
            VertexContainerCompositeRenderer vertexContainerCompositeRenderer = instance.get();
            vertexContainerCompositeRenderer.init(vertexContainer, this, modelMatricesProvider);
            compositeRenderers.add(vertexContainerCompositeRenderer);
        }
        return compositeRenderers;
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
