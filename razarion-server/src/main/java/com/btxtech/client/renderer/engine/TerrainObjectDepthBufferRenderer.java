package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 19.12.2015.
 */
@Dependent
public class TerrainObjectDepthBufferRenderer extends AbstractRenderer {
    private Logger logger = Logger.getLogger(TerrainObjectDepthBufferRenderer.class.getName());
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ShadowUiService shadowUiService;
    private VertexShaderAttribute positions;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private int terrainObjectId;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.textureTerrainObjectDepthVertexShader(), Shaders.INSTANCE.textureTerrainObjectDepthFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        textureCoordinate = createShaderTextureCoordinateAttributee(A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers() {
        terrainObjectId = terrainObjectService.getTerrainObjectId4VertexContainer(getId());
        VertexContainer vertexContainer = terrainObjectService.getVertexContainer(getId());
        if (vertexContainer == null || vertexContainer.isEmpty()) {
            logger.warning("No vertices to render");
            return;
        }
        if (vertexContainer.checkWrongTextureSize()) {
            logger.warning("TextureCoordinate has not same size as vertices");
            return;
        }
        if(!vertexContainer.hasTextureId()) {
            logger.warning("No texture id");
            return;
        }
        positions.fillBuffer(vertexContainer.getVertices());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());
        webGLTexture = createWebGLTexture(vertexContainer.getTextureId(), "uTexture");
        setElementCount(vertexContainer);
    }

    @Override
    public void draw() {
        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, shadowUiService.createDepthProjectionTransformation());
        uniformMatrix4fv(U_VIEW_MATRIX, shadowUiService.createDepthViewTransformation());

        positions.activate();
        textureCoordinate.activate();
        webGLTexture.activate();

        for (ModelMatrices modelMatrix : terrainObjectService.getModelMatrices(terrainObjectId)) {
            uniformMatrix4fv(U_MODEL_MATRIX, modelMatrix.getModel());
            drawArrays(WebGLRenderingContext.TRIANGLES);
        }
    }
}
