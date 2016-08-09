package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
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
public class Shape3DRenderer {
    @Inject
    private Instance<Element3DRenderer> instance;
    private Shape3D shape3D;
    private ModelMatricesProvider modelMatricesProvider;

    public void init(Shape3D shape3D, ModelMatricesProvider modelMatricesProvider) {
        this.shape3D = shape3D;
        this.modelMatricesProvider = modelMatricesProvider;
    }

    // Find better name for this method
    public void fillRenderQueue(List<CompositeRenderer> renderQueue) {
        for (Element3D element3D : shape3D.getElement3Ds()) {
            Element3DRenderer element3DRenderer = instance.get();
            element3DRenderer.init(element3D, getAnimations(element3D), modelMatricesProvider);
            element3DRenderer.fillRenderQueue(renderQueue);
        }
    }

    private Collection<ModelMatrixAnimation> getAnimations(Element3D element3D) {
        List<ModelMatrixAnimation> modelMatrixAnimations = shape3D.getModelMatrixAnimations();
        if (modelMatrixAnimations == null) {
            return null;
        }
        Collection<ModelMatrixAnimation> animations = new ArrayList<>();
        for (ModelMatrixAnimation modelMatrixAnimation : modelMatrixAnimations) {
            if (element3D.equals(modelMatrixAnimation.getElement3D())) {
                animations.add(modelMatrixAnimation);
            }
        }
        if (animations.isEmpty()) {
            return null;
        } else {
            return animations;
        }
    }

}
