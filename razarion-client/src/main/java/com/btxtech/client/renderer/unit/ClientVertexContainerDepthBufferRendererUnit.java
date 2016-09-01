package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 19.12.2015.
 */
@DepthBufferRenderer
@Dependent
public class ClientVertexContainerDepthBufferRendererUnit extends AbstractVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerDepthBufferRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ShadowUiService shadowUiService;
    private VertexShaderAttribute positions;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.vertexContainerDeptBufferVertexShader(), Shaders.INSTANCE.vertexContainerDeptBufferFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        textureCoordinate = webGlFacade.createShaderTextureCoordinateAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {

    }

    @Override
    protected void fillBuffers(VertexContainer vertexContainer) {
        positions.fillBuffer(vertexContainer.getVertices());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());
        webGLTexture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), "uTexture");
    }

    @Override
    protected void preModelDraw() {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, shadowUiService.createDepthProjectionTransformation());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, shadowUiService.createDepthViewTransformation());

        positions.activate();
        textureCoordinate.activate();
        webGLTexture.activate();
    }

    @Override
    protected void modelDraw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
