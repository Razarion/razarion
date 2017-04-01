package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.ClientRenderUtil;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainTile;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.05.2015.
 */
@NormRenderer
@Dependent
public class ClientGroundNormRendererUnit extends AbstractGroundRendererUnit {
    // private Logger logger = Logger.getLogger(ClientGroundNormRendererUnit.class.getName());
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
    protected void fillBuffersInternal(UiTerrainTile uiTerrainTile) {
        // vertices.fillFloat32Array(ClientRenderUtil.setupNormFloat32Array(uiTerrainTile.getVertices(), uiTerrainTile.getNorms()));
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void draw(UiTerrainTile uiTerrainTile) {
        webGlFacade.useProgram();
        webGlFacade.uniformMatrix4fv(modelMatrix, Matrix4.createIdentity());

        vertices.activate();
        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }
}
