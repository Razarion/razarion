package com.btxtech.client.editor.renderer;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.subtask.AbstractWebGlRenderTask;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import elemental2.core.Float32Array;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static com.btxtech.client.renderer.engine.UniformLocation.Type.B;

/**
 * Created by Beat
 * 11.09.2015.
 */
@Dependent
public class MonitorRenderTask extends AbstractWebGlRenderTask<Void> implements MonitorRenderTaskRunner.RenderTask {
    private static final int SIDE_LENGTH = 256;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientRenderServiceImpl renderService;
     @Inject
     private MonitorRenderTaskRunner monitorRenderTask;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(Void aVoid) {
        return new WebGlFacadeConfig(Shaders.INSTANCE.monitorVertexShader(), Shaders.INSTANCE.monitorFragmentShader());
    }

    @Override
    protected void setup(Void aVoid) {
        double monitorWidth = 2.0 * SIDE_LENGTH / (double) gameCanvas.getWidth();
        double monitorHeight = 2.0 * SIDE_LENGTH / (double) gameCanvas.getHeight();

        Float32Array positions = new Float32Array(18);
        // Triangle 1
        positions.set(new double[]{0, 0, 0}, 0);
        positions.set(new double[]{monitorWidth, 0, 0}, 3);
        positions.set(new double[]{0, monitorHeight, 0}, 6);
        // Triangle 2
        positions.set(new double[]{monitorWidth, 0, 0}, 9);
        positions.set(new double[]{monitorWidth, monitorHeight, 0}, 12);
        positions.set(new double[]{0, monitorHeight, 0}, 15);
        setupVec3PositionArray_(positions);

        Float32Array uvs = new Float32Array(12);
        // Triangle 1
        uvs.set(new double[]{0, 0}, 0);
        uvs.set(new double[]{1, 0}, 2);
        uvs.set(new double[]{0, 1}, 4);
        // Triangle 2
        uvs.set(new double[]{1, 0}, 6);
        uvs.set(new double[]{1, 1}, 8);
        uvs.set(new double[]{0, 1}, 10);
        setupVec2Array(WebGlFacade.A_VERTEX_UV, Js.uncheckedCast(uvs));

        setupUniform("uDepthMap", B, () -> monitorRenderTask.isShowDeep());

        createWebGLTexture("uColorSampler", () -> renderService.getColorTexture());
        createWebGLTexture("uDepthSampler", () -> renderService.getDepthTexture());
    }
}