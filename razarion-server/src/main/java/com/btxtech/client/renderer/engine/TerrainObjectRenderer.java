package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.ColladaUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import com.btxtech.shared.primitives.Color;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class TerrainObjectRenderer extends AbstractRenderer {
    private Logger logger = Logger.getLogger(TerrainObjectRenderer.class.getName());
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private ColladaUiService colladaUiService;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute normals;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private Color ambient;
    private Color diffuse;
    private int terrainObjectId;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.textureTerrainObjectVertexShader(), Shaders.INSTANCE.textureTerrainObjectFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        normals = createVertexShaderAttribute(A_VERTEX_NORMAL);
        textureCoordinate = createShaderTextureCoordinateAttributee(A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {
        enableShadow();
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
        if (vertexContainer.checkWrongNormSize()) {
            logger.warning("Normal has not same size as vertices");
            return;
        }
        if(!vertexContainer.hasTextureId()) {
            logger.warning("No texture id");
            return;
        }
        if(!vertexContainer.hasTextureId()) {
            logger.warning("No texture id");
            return;
        }
        positions.fillBuffer(vertexContainer.getVertices());
        normals.fillBuffer(vertexContainer.getNorms());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());
        webGLTexture = createWebGLTexture(vertexContainer.getTextureId(), "uTexture");
        setElementCount(vertexContainer);

        ambient = vertexContainer.getAmbient();
        diffuse = vertexContainer.getDiffuse();
    }

    @Override
    public void draw() {
        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv(U_VIEW_NORM_MATRIX, camera.createNormMatrix());
        uniform3fNoAlpha(U_LIGHT_AMBIENT, ambient);
        uniform3f(U_LIGHT_DIRECTION, colladaUiService.getDirection());
        uniform3fNoAlpha(U_LIGHT_DIFFUSE, diffuse);

        activateShadow();
        positions.activate();
        normals.activate();
        textureCoordinate.activate();
        webGLTexture.activate();

        for (ModelMatrices modelMatrix : terrainObjectService.getModelMatrices(terrainObjectId)) {
            uniformMatrix4fv(U_MODEL_MATRIX, modelMatrix.getModel());
            uniformMatrix4fv(U_MODEL_NORM_MATRIX, modelMatrix.getNorm());
            drawArrays(WebGLRenderingContext.TRIANGLES);
        }
    }
}
