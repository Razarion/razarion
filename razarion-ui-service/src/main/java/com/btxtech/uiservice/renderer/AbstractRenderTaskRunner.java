package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * T Data
 * R RenderTask
 *
 * @param <T>
 */
public abstract class AbstractRenderTaskRunner<T,  R extends WebGlRenderTask<T>> {
    @Inject
    private Instance<WebGlRenderTask<T>> instance;
    private List<WebGlRenderTask<T>> renderTasks = new ArrayList<>();
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

    protected R createModelRenderTask(Class<R> clazz, T t, Function<Long, List<ModelMatrices>> modelMatricesSupplier, Collection<ProgressAnimation> progressAnimations, ShapeTransform shapeTransform) {
        R renderTask = instance.select(clazz).get();
        renderTask.setProgressAnimations(progressAnimations);
        renderTask.setShapeTransform(shapeTransform);
        renderTask.setModelMatricesSupplier(modelMatricesSupplier);
        renderTask.init(t);
        renderTasks.add(renderTask);
        return renderTask;
    }

    protected R createRenderTask(Class<R> clazz, T t) {
        return createModelRenderTask(clazz, t, null, null, null);
    }

    public void draw() {
        double interpolationFactor = setupInterpolationFactor();
        renderTasks.forEach(renderTask -> renderTask.draw(interpolationFactor));
    }

    public void destroyRenderTask(R renderTask) {
        renderTasks.remove(renderTask);
        renderTask.dispose();
    }

    protected void clear() {
        renderTasks.clear();
    }

    // Override in subclasses
    protected double setupInterpolationFactor() {
        return 0;
    }
}
