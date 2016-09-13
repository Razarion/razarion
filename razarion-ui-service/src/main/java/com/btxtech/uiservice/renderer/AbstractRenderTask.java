package com.btxtech.uiservice.renderer;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 31.08.2016.
 */
public abstract class AbstractRenderTask<T> {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private RenderService renderService;
    @Inject
    private Instance<ModelRenderer<T, ?, ?, ?>> instance;
    private List<ModelRenderer> modelRenderers = new ArrayList<>();
    private boolean active;

    /**
     * Override in sub classes
     *
     * @return if the task should be rendered
     */
    protected boolean isActive() {
        return true;
    }

    protected void add(ModelRenderer modelRenderer) {
        this.modelRenderers.add(modelRenderer);
    }

    public void removeAll(T model) {
        for (Iterator<ModelRenderer> iterator = modelRenderers.iterator(); iterator.hasNext(); ) {
            ModelRenderer modelRenderer = iterator.next();
            if (model == null) {
                if (modelRenderer.getModel() == null) {
                    iterator.remove();
                }
            } else {
                if (model.equals(modelRenderer.getModel())) {
                    iterator.remove();
                }
            }
        }
    }

    protected List<ModelRenderer> getAll() {
        return modelRenderers;
    }

    protected <T, C extends AbstractRenderComposite<U, D>, U extends AbstractRenderUnit<D>, D> ModelRenderer<T, C, U, D> create() {
        return (ModelRenderer)instance.get();
    }

    public void prepareRender() {
        active = isActive();
        if (active) {
            modelRenderers.forEach(ModelRenderer::setupModelMatrices);
        }
    }

    public void draw(RenderUnitControl renderUnitControl) {
        if (active) {
            modelRenderers.forEach(modelRenderer -> modelRenderer.draw(renderUnitControl));
        }
    }

    public void drawDepthBuffer() {
        if (active) {
            modelRenderers.forEach(ModelRenderer::drawDepthBuffer);
        }
    }

    public void drawNorm() {
        if (active) {
            modelRenderers.forEach(ModelRenderer::drawNorm);
        }
    }

    public void fillBuffers() {
        modelRenderers.forEach(ModelRenderer::fillBuffers);
    }

    public void fillNormBuffer() {
        modelRenderers.forEach(ModelRenderer::fillNormBuffer);
    }
}
