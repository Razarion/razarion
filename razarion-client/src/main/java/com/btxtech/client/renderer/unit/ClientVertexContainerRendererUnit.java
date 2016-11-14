package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.08.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientVertexContainerRendererUnit extends AbstractVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private ShaderTextureCoordinateAttribute textureCoordinateAttribute;
    private WebGlUniformTexture texture;
    private Color ambient;
    private Color diffuse;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.vertexContainerVertexShader(), Shaders.INSTANCE.vertexContainerFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        textureCoordinateAttribute = webGlFacade.createShaderTextureCoordinateAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
        webGlFacade.enableReceiveShadow();
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer) {
        texture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), "uSampler");
        positions.fillBuffer(vertexContainer.getVertices());
        norms.fillBuffer(vertexContainer.getNorms());
        textureCoordinateAttribute.fillBuffer(vertexContainer.getTextureCoordinates());

        ambient = vertexContainer.getAmbient();
        diffuse = vertexContainer.getDiffuse();
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_NORM_MATRIX, camera.getNormMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());

        webGlFacade.uniform3fNoAlpha("uLightingAmbient", ambient);
        webGlFacade.uniform3f("uLightingDirection", visualUiService.getShape3DLightDirection());
        webGlFacade.uniform3fNoAlpha("uLightingDiffuse", diffuse);
        // webGlFacade.uniform1f("uSpecularHardness", baseItemUiService.getSpecularHardness());
        // webGlFacade.uniform1f("uSpecularIntensity", baseItemUiService.getSpecularIntensity());

        webGlFacade.activateReceiveShadow();

        texture.activate();
        positions.activate();
        norms.activate();
        textureCoordinateAttribute.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.uniformMatrix4fv("uNMMatrix", modelMatrices.getNorm());

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
