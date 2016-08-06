package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlFacade;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.VertexContainerRenderUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Dependent
public class ClientItemUnitRenderer extends VertexContainerRenderUnit {
    private Logger logger = Logger.getLogger(ClientItemUnitRenderer.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private ShadowUiService shadowUiService;
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
        webGlFacade.createProgram(Shaders.INSTANCE.unitVertexShader(), Shaders.INSTANCE.unitFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        textureCoordinateAttribute = webGlFacade.createShaderTextureCoordinateAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
        // TODO webGlFacade.enableReceiveShadow();
    }

    @Override
    public void setupImages() {
    }

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        if (vertexContainer == null || vertexContainer.isEmpty()) {
            logger.warning("No vertices to render: ");
            return;
        }
        if (vertexContainer.checkWrongTextureSize()) {
            logger.warning("TextureCoordinate has not same size as vertices: " + vertexContainer.getShapeElementVertexContainerTag());
            return;
        }
        if (vertexContainer.checkWrongNormSize()) {
            logger.warning("Normal has not same size as vertices: "+ vertexContainer.getShapeElementVertexContainerTag());
            return;
        }
        if (!vertexContainer.hasTextureId()) {
            logger.warning("No texture id: "+ vertexContainer.getShapeElementVertexContainerTag());
            return;
        }

        texture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), "uSampler");
        positions.fillBuffer(vertexContainer.getVertices());
        norms.fillBuffer(vertexContainer.getNorms());
        textureCoordinateAttribute.fillBuffer(vertexContainer.getTextureCoordinates());
        setElementCount(vertexContainer);

        ambient = vertexContainer.getAmbient();
        diffuse = vertexContainer.getDiffuse();
    }

    @Override
    protected void preModelDraw() {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_NORM_MATRIX, camera.createNormMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());

        webGlFacade.uniform3fNoAlpha("uAmbientColor", ambient);
        webGlFacade.uniform3f("uLightingDirection", shadowUiService.getLightDirection());
        webGlFacade.uniform3fNoAlpha("uLightingColor", diffuse);
        webGlFacade.uniform1f("uSpecularHardness", baseItemUiService.getSpecularHardness());
        webGlFacade.uniform1f("uSpecularIntensity", baseItemUiService.getSpecularIntensity());

        // TODO webGlFacade.activateReceiveShadow();

        texture.activate();
        positions.activate();
        norms.activate();
        textureCoordinateAttribute.activate();
    }

    @Override
    protected void modelDraw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.uniformMatrix4fv("uNMMatrix", modelMatrices.getNorm());

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
