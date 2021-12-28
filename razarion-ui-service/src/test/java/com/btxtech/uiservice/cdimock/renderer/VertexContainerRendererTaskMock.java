package com.btxtech.uiservice.cdimock.renderer;

import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.renderer.task.AbstractShape3DRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.progress.ProgressState;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class VertexContainerRendererTaskMock extends AbstractRenderTaskMock<VertexContainer> implements AbstractShape3DRenderTaskRunner.RenderTask {
    @Inject
    private RenderTaskCollector renderTaskCollector;
    private VertexContainer vertexContainer;
    private ProgressState buildupState;

    @PostConstruct
    public void postConstruct() {
        renderTaskCollector.addRendererTask(this);
    }

    @Override
    public void init(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
    }

    @Override
    public void setProgressState(ProgressState buildupState) {
        this.buildupState = buildupState;
    }

    public VertexContainer getVertexContainer() {
        return vertexContainer;
    }
}
