package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
abstract public class AbstractTerrainObjectDepthBufferRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentric;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainDepthBufferObjectRenderer.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;

    abstract protected VertexList getVertexList(TerrainObjectService terrainObjectService);

    @PostConstruct
    public void init() {
        Object extension = gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.depthBufferVertexShader(), Shaders.INSTANCE.depthBufferFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentric = createVertexShaderAttribute(BARYCENTRIC_ATTRIBUTE_NAME);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = getVertexList(terrainObjectService);
        if (vertexList == null) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(vertexList.getVertices());
        barycentric.fillBuffer(vertexList.getBarycentric());

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, lighting.createProjectionTransformation());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, lighting.createViewTransformation());
        uniformMatrix4fv(MODEL_UNIFORM_NAME, Matrix4.createIdentity());

        positions.activate();
        barycentric.activate();

        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
