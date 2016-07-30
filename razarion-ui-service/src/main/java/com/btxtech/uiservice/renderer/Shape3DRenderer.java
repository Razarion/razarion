package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 29.07.2016.
 */
public abstract class Shape3DRenderer {
    private Shape3D shape3D;

    protected abstract Element3DRenderer createElement3DRenderer(Element3D element3D);

    public void init(Shape3D shape3D) {
        this.shape3D = shape3D;
    }

    // Find better name for this method
    public void fillRenderQueue(List<CompositeRenderer> renderQueue) {
        for (Element3D element3D : shape3D.getElement3Ds()) {
            Element3DRenderer element3DRenderer = createElement3DRenderer(element3D);
            element3DRenderer.init(element3D, this, getAnimations(element3D));
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
