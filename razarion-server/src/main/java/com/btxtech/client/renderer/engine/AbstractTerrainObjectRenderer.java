package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ShadowUiService;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;

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
    private VertexShaderAttribute positions;
    private VertexShaderAttribute normals;
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
    @Inject
    private ShadowUiService shadowUiService;

    abstract protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService);

    abstract protected ImageDescriptor getImageDescriptor(TerrainObjectService terrainObjectService);

    abstract protected void preDraw(WebGLRenderingContext webGLRenderingContext);

    protected void postDraw(WebGLRenderingContext ctx3d) {

    }

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainObjectVertexShader(), Shaders.INSTANCE.terrainObjectFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        normals = createVertexShaderAttribute(A_VERTEX_NORMAL);
        textureCoordinate = createShaderTextureCoordinateAttributee(TEXTURE_COORDINATE_ATTRIBUTE_NAME);
    }

    @Override
    public void setupImages() {
        webGLTexture = createWebGLTexture(getImageDescriptor(terrainObjectService), SAMPLER_UNIFORM_NAME);
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

        elementCount = vertexContainer.getVerticesCount();
    }

    @Override
    public void draw() {
        preDraw(gameCanvas.getCtx3d());

        useProgram();

        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, projectionTransformation.createMatrix());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, camera.createMatrix());
        uniform3f(UNIFORM_AMBIENT_COLOR, shadowUiService.getAmbientIntensity(), shadowUiService.getAmbientIntensity(), shadowUiService.getAmbientIntensity());
        Vertex direction = shadowUiService.getLightDirection();
        uniform3f(UNIFORM_LIGHTING_DIRECTION, direction.getX(), direction.getY(), direction.getZ());
        uniform3f(UNIFORM_DIRECTIONAL_COLOR, shadowUiService.getDiffuseIntensity(), shadowUiService.getDiffuseIntensity(), shadowUiService.getDiffuseIntensity());

        positions.activate();
        normals.activate();
        textureCoordinate.activate();

        webGLTexture.activate();

        for (Matrix4 modelMatrix : terrainObjectService.getObjectIdMatrices(getId())) {
            uniformMatrix4fv(MODEL_UNIFORM_NAME, modelMatrix);
            gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
        }

        postDraw(gameCanvas.getCtx3d());
    }
}
