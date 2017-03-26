package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.ClientRenderUtil;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@NormRenderer
@Dependent
public class ClientWaterNormRendererUnit extends AbstractWaterRendererUnit {
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    private Vec3Float32ArrayShaderAttribute vertices;
    private WebGLUniformLocation modelMatrix;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.debugVectorVertexShader(), Shaders.INSTANCE.debugVectorFragmentShader()).enableTransformation(false));
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        modelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_MATRIX);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillInternalBuffers(WaterUi waterUi) {
        vertices.fillFloat32ArrayEmu(ClientRenderUtil.setupNormFloat32Array(waterUi.getVertices(), waterUi.getNorms()));
    }

    @Override
    public void draw(WaterUi waterUi) {
        webGlFacade.useProgram();
        webGlFacade.uniformMatrix4fv(modelMatrix, Matrix4.createIdentity());

        vertices.activate();
        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }
}
