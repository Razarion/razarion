package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractDemolitionVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.08.2016.
 */
@DepthBufferRenderer
@Dependent
public class ClientDemolitionVertexContainerDepthBufferRendererUnit extends AbstractDemolitionVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ClientShape3DUiService shape3DUiService;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec2Float32ArrayShaderAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private WebGLUniformLocation modelMatrix;
    private WebGLUniformLocation characterRepresenting;

    @Override
    public void init() {
        // TODO webGlFacade.init(new WebGlFacadeConfig(Shaders.INSTANCE.vertexContainerDeptBufferVertexShader(), Shaders.INSTANCE.vertexContainerDeptBufferFragmentShader()).enableShadowTransformation());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        textureCoordinate = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
        modelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_NORM_MATRIX);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer, Integer baseItemDemolitionImageId) {
        positions.fillFloat32Array(shape3DUiService.getVertexFloat32Array(vertexContainer));
        textureCoordinate.fillFloat32Array(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer));
        // webGLTexture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), WebGlFacade.U_TEXTURE);
        characterRepresenting = webGlFacade.getUniformLocation("characterRepresenting");
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        positions.activate();
        textureCoordinate.activate();
        webGLTexture.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices, double health) {
        webGlFacade.uniformMatrix4fv(modelMatrix, modelMatrices.getModel());
        webGlFacade.uniform1b(characterRepresenting, modelMatrices.getColor() != null && getRenderData().getShape3DMaterialConfig().isCharacterRepresenting());

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}