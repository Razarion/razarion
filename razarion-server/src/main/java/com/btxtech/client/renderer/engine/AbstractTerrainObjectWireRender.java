package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2015.
 */
abstract public class AbstractTerrainObjectWireRender extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private static final String PERSPECTIVE_UNIFORM_NAME = U_PERSPECTIVE_MATRIX;
    private static final String VIEW_UNIFORM_NAME = U_VIEW_MATRIX;
    private static final String MODEL_UNIFORM_NAME = U_MODEL_MATRIX;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentric;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private int elementCount;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;

    abstract protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService);

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }

        createProgram(Shaders.INSTANCE.modelViewPerspectiveWireVertexShader(), Shaders.INSTANCE.modelViewPerspectiveWireFragmentShader());

        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentric = createVertexShaderAttribute(A_BARYCENTRIC);
        textureCoordinate = createShaderTextureCoordinateAttributee(A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {
        webGLTexture = createWebGLTexture(ImageDescriptor.CHESS_TEXTURE_08, SAMPLER_UNIFORM_NAME);
    }

    @Override
    public void fillBuffers() {
        VertexContainer vertexContainer = getVertexContainer(terrainObjectService);
        if (vertexContainer == null) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(vertexContainer.getVertices());
        barycentric.fillBuffer(vertexContainer.generateBarycentric());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());

        elementCount = vertexContainer.getVerticesCount();
    }

    @Override
    public void draw() {
        if (elementCount == 0) {
            return;
        }
        useProgram();

        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, projectionTransformation.createMatrix());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, camera.createMatrix());

        positions.activate();
        barycentric.activate();
        textureCoordinate.activate();

        webGLTexture.activate();


        if (terrainObjectService.getObjectIdMatrices(getId()) != null) {
            for (ModelMatrices modelMatrix : terrainObjectService.getObjectIdMatrices(getId())) {
                uniformMatrix4fv(MODEL_UNIFORM_NAME, modelMatrix.getModel());
                getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
                WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
            }
        }
    }

}
