package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<Element3D, Element3DRenderer> element3DRendererMap = new HashMap<>();
        for (Element3D element3D : shape3D.getElement3Ds()) {
            Element3DRenderer element3DRenderer = createElement3DRenderer(element3D);
            element3DRenderer.init(element3D, this);
            element3DRenderer.fillRenderQueue(renderQueue);
            element3DRendererMap.put(element3D, element3DRenderer);
        }
        List<ModelMatrixAnimation> modelMatrixAnimations = shape3D.getModelMatrixAnimations();
        if(modelMatrixAnimations != null) {
            for (ModelMatrixAnimation modelMatrixAnimation : modelMatrixAnimations) {
                Element3DRenderer element3DRenderer = element3DRendererMap.get(modelMatrixAnimation.getElement3D());
                element3DRenderer.addModelMatrixAnimation(modelMatrixAnimation);
            }
        }
    }
}
