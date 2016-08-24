package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.uiservice.ModelMatricesProvider;
import com.btxtech.uiservice.Shape3DUiService;

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
    @Inject
    private Shape3DUiService shape3DUiService;
    private int shape3DId;
    private ModelMatricesProvider modelMatricesProvider;
    private List<CompositeRenderer> myRenderers = new ArrayList<>();
    private List<CompositeRenderer> renderQueue;

    public void init(Integer shape3DId, ModelMatricesProvider modelMatricesProvider) {
        this.shape3DId = shape3DId;
        this.modelMatricesProvider = modelMatricesProvider;
    }

    // Find better name for this method
    public void fillRenderQueue(List<CompositeRenderer> renderQueue) {
        this.renderQueue = renderQueue;
        shape3DUiService.request(shape3DId, this::onShape3D);
    }

    private void onShape3D(Shape3D shape3D) {
        boolean running = !myRenderers.isEmpty();
        renderQueue.removeAll(myRenderers);
        myRenderers.clear();
        for (Element3D element3D : shape3D.getElement3Ds()) {
            Element3DRenderer element3DRenderer = instance.get();
            element3DRenderer.init(element3D, getAnimations(shape3D, element3D), modelMatricesProvider);
            myRenderers.addAll(element3DRenderer.createCompositeRenderers());
        }
        renderQueue.addAll(myRenderers);
        if (running) {
            for (CompositeRenderer myRenderer : myRenderers) {
                myRenderer.fillBuffers();
            }
        }
    }

    public List<CompositeRenderer> getMyRenderers() {
        return myRenderers;
    }

    private Collection<ModelMatrixAnimation> getAnimations(Shape3D shape3D, Element3D element3D) {
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
