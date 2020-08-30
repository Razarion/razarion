package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractDemolitionVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.08.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientDemolitionVertexContainerRendererUnit extends AbstractDemolitionVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ClientShape3DUiService shape3DUiService;
    @Inject
    private VisualUiService visualUiService;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute norms;
    private Vec2Float32ArrayShaderAttribute textureCoordinateAttribute;
    private WebGlUniformTexture texture;
    private WebGlUniformTexture templateTexture;
    private WebGLUniformLocation modelMatrix;
    private WebGLUniformLocation modelNormMatrix;
    private WebGLUniformLocation uLightingAmbient;
    private WebGLUniformLocation uLightingDirection;
    private WebGLUniformLocation uLightingDiffuse;
    private WebGLUniformLocation uHealth;
    private WebGLUniformLocation characterRepresenting;
    private WebGLUniformLocation characterRepresentingColor;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.demolitionVertexContainerVertexShader(), Shaders.INSTANCE.demolitionVertexContainerFragmentShader()).enableTransformation(true).enableReceiveShadow().enableCastShadow());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        textureCoordinateAttribute = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
        modelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_MATRIX);
        modelNormMatrix = webGlFacade.getUniformLocation("uNMMatrix");
        uLightingAmbient = webGlFacade.getUniformLocation("uLightingAmbient");
        uLightingDirection = webGlFacade.getUniformLocation("uLightingDirection");
        uLightingDiffuse = webGlFacade.getUniformLocation("uLightingDiffuse");
        uHealth = webGlFacade.getUniformLocation("uHealth");
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer, Integer baseItemDemolitionImageId) {
        // texture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), "uSampler");
        templateTexture = webGlFacade.createWebGLTexture(baseItemDemolitionImageId, "uDemolitionSampler");
        positions.fillFloat32Array(shape3DUiService.getVertexFloat32Array(vertexContainer));
        norms.fillFloat32Array(shape3DUiService.getNormFloat32Array(vertexContainer));
        textureCoordinateAttribute.fillFloat32Array(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer));

        characterRepresenting = webGlFacade.getUniformLocation("characterRepresenting");
        characterRepresentingColor = webGlFacade.getUniformLocation("characterRepresentingColor");
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        webGlFacade.uniform3fNoAlpha(uLightingAmbient, visualUiService.getAmbient());
        webGlFacade.uniform3f(uLightingDirection, visualUiService.getLightDirection());
        webGlFacade.uniform3fNoAlpha(uLightingDiffuse, visualUiService.getDiffuse());

        // webGlFacade.uniform1f("uSpecularHardness", baseItemUiService.getSpecularHardness());
        // webGlFacade.uniform1f("uSpecularIntensity", baseItemUiService.getSpecularIntensity());

        // webGlFacade.activateReceiveShadow();

        texture.activate();
        templateTexture.activate();
        positions.activate();
        norms.activate();
        textureCoordinateAttribute.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices, double health) {
        webGlFacade.uniformMatrix4fv(modelMatrix, modelMatrices.getModel());
        webGlFacade.uniformMatrix4fv(modelNormMatrix, modelMatrices.getNorm());
        webGlFacade.uniform1f(uHealth, health);
        if (modelMatrices.getColor() != null && getRenderData().getShape3DMaterialConfig().isCharacterRepresenting()) {
            webGlFacade.uniform1b(characterRepresenting, true);
            webGlFacade.uniform3fNoAlpha(characterRepresentingColor, modelMatrices.getColor());
        } else {
            webGlFacade.uniform1b(characterRepresenting, false);
        }

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
