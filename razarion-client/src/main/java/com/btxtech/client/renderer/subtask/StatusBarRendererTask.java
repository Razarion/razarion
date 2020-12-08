package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.task.selection.StatusBarGeometry;
import com.btxtech.uiservice.renderer.task.selection.StatusBarRenderTaskRunner;

import javax.enterprise.context.Dependent;

import static com.btxtech.client.renderer.webgl.WebGlFacade.U_COLOR;

/**
 * Created by Beat
 * 23.01.2017.
 */
@Dependent
public class StatusBarRendererTask extends AbstractWebGlRenderTask<StatusBarGeometry> implements StatusBarRenderTaskRunner.StatusBarRenderTask {
    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(StatusBarGeometry statusBarGeometry) {
        return new WebGlFacadeConfig(Shaders.SHADERS.statusBarCustom())
                .enableTransformation(false)
                .blend(WebGlFacadeConfig.Blend.SOURCE_ALPHA)
                .depthTest(false)
                .writeDepthBuffer(false);
    }

    @Override
    protected void setup(StatusBarGeometry statusBarGeometry) {
        setupVec3VertexPositionArray(statusBarGeometry.getVertexes());
        setupVec1Array("aVisibility", statusBarGeometry.getVisibilities());

        setupModelMatrixUniform(U_COLOR, UniformLocation.Type.COLOR, ModelMatrices::getColor);
        setupModelMatrixUniform("uBgColor", UniformLocation.Type.COLOR, ModelMatrices::getBgColor);
        setupModelMatrixUniform("uProgress", UniformLocation.Type.F, ModelMatrices::getProgress);
    }
}
