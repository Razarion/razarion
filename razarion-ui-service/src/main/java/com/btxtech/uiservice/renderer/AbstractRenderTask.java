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
    @Inject
    private Instance<ModelRenderer<T>> instance;
    private List<ModelRenderer<T>> modelRenderers = new ArrayList<>();
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
        for (Iterator<ModelRenderer<T>> iterator = modelRenderers.iterator(); iterator.hasNext(); ) {
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

    @Deprecated
    protected void add(ModelRenderer modelRenderer) {
      // Already added to modelRenderers in create
    }

    public void destroy(ModelRenderer modelRenderer) {
        this.modelRenderers.remove(modelRenderer);
        modelRenderer.dispose();
    }

    protected List<ModelRenderer<T>> getAll() {
        return modelRenderers;
    }

    protected void clear() {
        modelRenderers.clear();
    }

    @Deprecated
    protected <T, C extends AbstractRenderComposite<U, D>, U extends AbstractRenderUnit<D>, D> ModelRenderer<T> create() {
        return null;
    }

    protected ModelRenderer<T> createNew() {
        ModelRenderer<T> modelRenderer =  instance.get();
        this.modelRenderers.add(modelRenderer);
        return modelRenderer;
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
