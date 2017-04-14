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
    private double interpolationFactor;
    private String name;
    private boolean enabled = true;

    /**
     * Override in sub classes
     *
     * @return if the task should be rendered
     */
    protected boolean isActive() {
        return true;
    }

    // Override in subclasses
    protected void preRender(long timeStamp) {

    }

    // Override in subclasses
    protected double setupInterpolationFactor() {
        return 0;
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

    protected void add(ModelRenderer modelRenderer) {
        this.modelRenderers.add(modelRenderer);
    }

    public void remove(ModelRenderer modelRenderer) {
        this.modelRenderers.remove(modelRenderer);
    }

    protected List<ModelRenderer> getAll() {
        return modelRenderers;
    }

    protected void clear() {
        modelRenderers.clear();
    }

    protected <T, C extends AbstractRenderComposite<U, D>, U extends AbstractRenderUnit<D>, D> ModelRenderer<T, C, U, D> create() {
        return (ModelRenderer) instance.get();
    }

    public void prepareRender(long timeStamp) {
        active = isActive() && enabled;
        if (active) {
            interpolationFactor = setupInterpolationFactor();
            preRender(timeStamp);
            modelRenderers.forEach(modelRenderer -> modelRenderer.setupModelMatrices(timeStamp));
        }
    }

    public void draw(RenderUnitControl renderUnitControl) {
        if (active) {
            modelRenderers.forEach(modelRenderer -> modelRenderer.draw(renderUnitControl, interpolationFactor));
        }
    }

    public void drawDepthBuffer() {
        if (active) {
            modelRenderers.forEach(modelRenderer -> modelRenderer.drawDepthBuffer(interpolationFactor));
        }
    }

    public void drawNorm() {
        if (active) {
            modelRenderers.forEach(modelRenderer -> modelRenderer.drawNorm(interpolationFactor));
        }
    }

    public void fillBuffers() {
        modelRenderers.forEach(ModelRenderer::fillBuffers);
    }

    public void fillNormBuffer() {
        modelRenderers.forEach(ModelRenderer::fillNormBuffer);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
