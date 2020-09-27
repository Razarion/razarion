package com.btxtech.uiservice.renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Deprecated
public abstract class AbstractModelRenderTaskRunner<T> extends AbstractRenderTaskRunner {
    private boolean enabled = true;
    @Deprecated
    private List<ModelRenderer<T>> modelRenderers = new ArrayList<>();

    @Override
    public void draw() {
        if (!isActive() || !enabled) {
            return;
        }
        double interpolationFactor = setupInterpolationFactor();
    }

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

    protected void destroyRenderAllTasks() {
        modelRenderers.clear();
    }

    @Deprecated
    protected <T, C extends AbstractRenderComposite<U, D>, U extends AbstractRenderUnit<D>, D> ModelRenderer<T> create() {
        return null;
    }

    public void prepareRender(long timeStamp) {
        if (isActive() && enabled) {
            // interpolationFactor = setupInterpolationFactor();
            preRender(timeStamp);
            modelRenderers.forEach(modelRenderer -> modelRenderer.setupModelMatrices(timeStamp));
        }
    }

}
