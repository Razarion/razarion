package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
public abstract class AbstractTerrainObjectRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String A_VERTEX_NORMAL = "aVertexNormal";
    private static final String TEXTURE_COORDINATE_ATTRIBUTE_NAME = "aTextureCoord";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    private static final String SAMPLER_UNIFORM_NAME = "uSampler";
    private static final String UNIFORM_AMBIENT_COLOR = "uAmbientColor";
    private static final String UNIFORM_LIGHTING_DIRECTION = "uLightingDirection";
    private static final String UNIFORM_DIRECTIONAL_COLOR = "uDirectionalColor";
    private ShaderVertexAttribute positions;
    private ShaderVertexAttribute normals;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    // private WebGLTexture webGLTexture;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    @Normal
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;

    abstract protected VertexList getVertexList(TerrainObjectService terrainObjectService);
    abstract protected ImageDescriptor getImageDescriptor(TerrainObjectService terrainObjectService);
    abstract protected void preDraw(WebGLRenderingContext webGLRenderingContext);

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainObjectVertexShader(), Shaders.INSTANCE.terrainObjectFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        normals = createVertexShaderAttribute(A_VERTEX_NORMAL);
        textureCoordinate = createShaderTextureCoordinateAttributee(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
        webGLTexture = createWebGLTexture(getImageDescriptor(terrainObjectService), SAMPLER_UNIFORM_NAME, WebGLRenderingContext.TEXTURE0, 0);
    }


    @Override
    public void fillBuffers() {
        VertexList vertexList = getVertexList(terrainObjectService);
        if (vertexList == null) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(vertexList.getVertices());
        normals.fillBuffer(vertexList.getNormVertices());
        textureCoordinate.fillBuffer(vertexList.getTextureCoordinates());

        elementCount = vertexList.getVerticesCount();
    }


    @Override
    public void draw() {
        preDraw(gameCanvas.getCtx3d());

        useProgram();

        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, projectionTransformation.createMatrix());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, camera.createMatrix());
        uniform3f(UNIFORM_AMBIENT_COLOR, lighting.getAmbientIntensity(), lighting.getAmbientIntensity(), lighting.getAmbientIntensity());
        Vertex direction = lighting.getLightDirection();
        uniform3f(UNIFORM_LIGHTING_DIRECTION, direction.getX(), direction.getY(), direction.getZ());
        uniform3f(UNIFORM_DIRECTIONAL_COLOR, lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity(), lighting.getDiffuseIntensity());


        positions.activate();
        normals.activate();
        textureCoordinate.activate();

        // Texture
        webGLTexture.activate();
        // Draw
        WebGLUniformLocation modelUniform = getUniformLocation(MODEL_UNIFORM_NAME);
        for (Matrix4 matrix4 : terrainObjectService.getPositions()) {
            // Model model transformation uniform
            gameCanvas.getCtx3d().uniformMatrix4fv(modelUniform, false, WebGlUtil.createArrayBufferOfFloat32(matrix4.toWebGlArray()));
            // Draw
            gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
        }
    }
}
