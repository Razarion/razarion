package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.renderer.RenderTask;

public class AbstractModelRenderTask extends com.btxtech.uiservice.renderer.AbstractModelRenderTask<VertexContainer> {
    public interface SubTask extends RenderTask<VertexContainer> {
    }
}
