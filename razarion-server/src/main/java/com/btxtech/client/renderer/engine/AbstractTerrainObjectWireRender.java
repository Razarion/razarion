package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.primitives.Matrix4;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Beat
 * 20.05.2015.
 */
abstract public class AbstractTerrainObjectWireRender extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentric;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    private Collection<Matrix4> modelMatrices;

    abstract protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService);

    @PostConstruct
    public void init() {
        Object extension = gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }

        createProgram(Shaders.INSTANCE.modelViewPerspectiveWireVertexShader(), Shaders.INSTANCE.modelViewPerspectiveWireFragmentShader());

        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentric = createVertexShaderAttribute(BARYCENTRIC_ATTRIBUTE_NAME);
        textureCoordinate = createShaderTextureCoordinateAttributee(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
    }

    @Override
    public void setupImages() {
        webGLTexture = createWebGLTexture(ImageDescriptor.CHESS_TEXTURE_08, SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE0, 0);
    }

    public void updateModelMatrices() {
        modelMatrices = terrainObjectService.getObjectIdMatrices(getId());
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

        updateModelMatrices();

        elementCount = vertexContainer.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();

        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, projectionTransformation.createMatrix());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, camera.createMatrix());

        positions.activate();
        barycentric.activate();
        textureCoordinate.activate();

        webGLTexture.activate();


        if (modelMatrices != null) {
            for (Matrix4 modelMatrix : modelMatrices) {
                uniformMatrix4fv(MODEL_UNIFORM_NAME, modelMatrix);
                gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
                WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
            }
        }
    }

}
