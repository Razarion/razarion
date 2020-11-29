package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractRenderTaskRunner {
    @Inject
    private Instance<WebGlRenderTask<?>> instance;
    private List<WebGlRenderTask<?>> renderTasks = new ArrayList<>();
    private boolean enabled = true;
    private String name;

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

    /**
     * D Data
     * R RenderTask
     *
     * @param d data
     * @param clazz render task
     */
    protected <R extends WebGlRenderTask<D>, D> R createModelRenderTask(Class<R> clazz, D d, Function<Long, List<ModelMatrices>> modelMatricesSupplier, Collection<ProgressAnimation> progressAnimations, ShapeTransform shapeTransform, Consumer<R> preInit) {
        R renderTask = instance.select(clazz).get();
        renderTask.setProgressAnimations(progressAnimations);
        renderTask.setShapeTransform(shapeTransform);
        renderTask.setModelMatricesSupplier(modelMatricesSupplier);
        if(preInit != null) {
            preInit.accept(renderTask);
        }
        renderTask.init(d);
        renderTasks.add(renderTask);
        return renderTask;
    }

    protected <R extends WebGlRenderTask<D>, D> R createRenderTask(Class<R> clazz, D d) {
        return createModelRenderTask(clazz, d, null, null, null, null);
    }

    public void draw() {
        double interpolationFactor = setupInterpolationFactor();
        renderTasks.forEach(renderTask -> renderTask.draw(interpolationFactor));
    }

    public void destroyRenderTask(WebGlRenderTask<?> renderTask) {
        renderTasks.remove(renderTask);
        renderTask.dispose();
    }

    protected void destroyRenderAllTasks() {
        renderTasks.forEach(WebGlRenderTask::dispose);
        renderTasks.clear();
    }

    // Override in subclasses
    protected double setupInterpolationFactor() {
        return 0;
    }
}
