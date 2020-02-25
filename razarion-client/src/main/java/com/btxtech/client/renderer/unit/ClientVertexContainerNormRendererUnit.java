package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.ClientRenderUtil;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.NormRenderer;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.08.2016.
 */
@NormRenderer
@Dependent
public class ClientVertexContainerNormRendererUnit extends AbstractVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerNormRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ClientShape3DUiService shape3DUiService;
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
    protected void internalFillBuffers(VertexContainer vertexContainer) {
        vertices.fillFloat32Array(ClientRenderUtil.setupNormFloat32Array(shape3DUiService.getVertexFloat32Array(vertexContainer), shape3DUiService.getNormFloat32Array(vertexContainer)));
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        vertices.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(modelMatrix, modelMatrices.getModel());
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }

}
