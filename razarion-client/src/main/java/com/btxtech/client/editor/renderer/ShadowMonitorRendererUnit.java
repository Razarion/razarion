package com.btxtech.client.editor.renderer;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.09.2015.
 */
@ColorBufferRenderer
@Dependent
public class ShadowMonitorRendererUnit extends AbstractRenderUnit<Void> {
    private static final int SIDE_LENGTH = 256;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private MonitorRenderTask monitorRenderTask;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec2Float32ArrayShaderAttribute uvs;
   private WebGlUniformTexture colorTexture;
   private WebGlUniformTexture depthTesture;
    private WebGLUniformLocation uDeepMap;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.monitorVertexShader(), Shaders.INSTANCE.monitorFragmentShader()));
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        uvs = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_UV);
        colorTexture = webGlFacade.createEmptyWebGLTexture("uColorSampler");
        depthTesture = webGlFacade.createEmptyWebGLTexture("uDepthSampler");
        uDeepMap = webGlFacade.getUniformLocation("uDepthMap");
    }

    @Override
    public void fillBuffers(Void aVoid) {
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
        this.positions.fillFloat32Array(positions);

        Float32Array uvs = new Float32Array(12);
        // Triangle 1
        uvs.set(new double[]{0, 0}, 0);
        uvs.set(new double[]{1, 0}, 2);
        uvs.set(new double[]{0, 1}, 4);
        // Triangle 2
        uvs.set(new double[]{1, 0}, 6);
        uvs.set(new double[]{1, 1}, 8);
        uvs.set(new double[]{0, 1}, 10);
        this.uvs.fillFloat32Array(uvs);

        setElementCount(6);
    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        if(webGlFacade.canBeSkipped()) {
            return;
        }
        webGlFacade.useProgram();

        webGlFacade.uniform1b(uDeepMap, monitorRenderTask.isShowDeep());

        positions.activate();
        uvs.activate();

        colorTexture.setWebGLTexture(renderService.getColorTexture());
        colorTexture.activate();
        depthTesture.setWebGLTexture(renderService.getDepthTexture());
        depthTesture.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void dispose() {
        positions.deleteBuffer();
        uvs.deleteBuffer();
    }

    @Override
    public String helperString() {
        return "ShadowMonitorRendererUnit";
    }
}
