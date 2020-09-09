package com.btxtech.uiservice.renderer;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AbstractSimpleRenderTaskRunner<T> extends AbstractRenderTaskRunner {
    @Inject
    private Instance<RenderTask<T>> instance;
    private List<RenderTask<T>> renderTasks = new ArrayList<>();

    protected RenderTask<T> createRenderTask(Class<? extends RenderTask<T>> clazz, T t) {
        RenderTask<T> renderTask = instance.select(clazz).get();
        renderTask.init(t);
        this.renderTasks.add(renderTask);
        return renderTask;
    }

    @Override
    public void draw() {
        renderTasks.forEach(RenderTask::draw);
    }

    public void destroyRenderTask(RenderTask<T> renderSubTask) {
        renderTasks.remove(renderSubTask);
        renderSubTask.dispose();
    }
}
