package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.renderer.task.selection.SelectionFrameRenderTaskRunner;
import elemental2.webgl.WebGLRenderingContext;

import javax.enterprise.context.Dependent;

import static com.btxtech.client.renderer.webgl.WebGlFacade.U_COLOR;

/**
 * Created by Beat
 * 28.09.2016.
 */
@Dependent
public class SelectionFrameRenderTask extends AbstractWebGlRenderTask<GroupSelectionFrame> implements SelectionFrameRenderTaskRunner.RenderTask {
    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(GroupSelectionFrame groupSelectionFrame) {
        return new WebGlFacadeConfig(Shaders.SHADERS.customRgba())
                .blend(WebGlFacadeConfig.Blend.SOURCE_ALPHA)
                .depthTest(false)
                .writeDepthBuffer(false)
                .drawMode(WebGLRenderingContext.LINES);
    }

    @Override
    protected void setup(GroupSelectionFrame groupSelectionFrame) {
        setupVec3VertexPositionArray(groupSelectionFrame.generateVertices());
        setupUniform(U_COLOR, UniformLocation.Type.COLOR, () -> Colors.SELECTION_FRAME);

    }
}
