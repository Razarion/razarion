package com.btxtech.client.renderer.engine;

import com.btxtech.client.ColladaUiService;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import com.btxtech.shared.primitives.Color;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
public abstract class AbstractTerrainObjectRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(AbstractTerrainObjectRenderer.class.getName());
    private VertexShaderAttribute positions;
    private VertexShaderAttribute normals;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private Color ambient;
    private Color diffuse;
    private int elementCount;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private ColladaUiService colladaUiService;

    abstract protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService);

    abstract protected void preDraw(WebGLRenderingContext webGLRenderingContext);

    protected void postDraw(WebGLRenderingContext ctx3d) {

    }

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainObjectVertexShader(), Shaders.INSTANCE.terrainObjectFragmentShader());
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
        VertexContainer vertexContainer = getVertexContainer(terrainObjectService);
        if (vertexContainer == null) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(vertexContainer.getVertices());
        normals.fillBuffer(vertexContainer.getNorms());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());
        webGLTexture = createWebGLTexture(vertexContainer.getTextureId(), "uTexture");
        elementCount = vertexContainer.getVerticesCount();

        ambient = vertexContainer.getAmbient();
        diffuse = vertexContainer.getDiffuse();
    }

    @Override
    public void draw() {
        preDraw(getCtx3d());

        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv("uNVMatrix", camera.createNormMatrix());
        uniform3fNoAlpha(U_LIGHT_AMBIENT, ambient);
        uniform3f(U_LIGHT_DIRECTION, colladaUiService.getDirection());
        uniform3fNoAlpha(U_LIGHT_DIFFUSE, diffuse);

        activateShadow();
        positions.activate();
        normals.activate();
        textureCoordinate.activate();
        webGLTexture.activate();

        for (ModelMatrices modelMatrix : terrainObjectService.getObjectIdMatrices(getId())) {
            uniformMatrix4fv(U_MODEL_MATRIX, modelMatrix.getModel());
            uniformMatrix4fv(U_MODEL_NORM_MATRIX, modelMatrix.getNorm());
            getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
        }

        postDraw(getCtx3d());
    }
}
