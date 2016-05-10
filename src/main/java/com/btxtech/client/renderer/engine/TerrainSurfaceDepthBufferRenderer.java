package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class TerrainSurfaceDepthBufferRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer barycentricBuffer;
    private int barycentricPositionAttribute;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainDepthBufferObjectRenderer.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;

    @PostConstruct
    public void init() {
        Object extension = gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.depthBufferVertexShader(), Shaders.INSTANCE.depthBufferFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        barycentricBuffer = gameCanvas.getCtx3d().createBuffer();
        barycentricPositionAttribute = getAndEnableAttributeLocation(BARYCENTRIC_ATTRIBUTE_NAME);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getGroundVertexList();
        // vertices
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32Doubles(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // barycentric
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32Doubles(vertexList.createBarycentricDoubles()), WebGLRenderingContext.STATIC_DRAW);

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        // Projection uniform
        WebGLUniformLocation perspectiveUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(perspectiveUniform, false, WebGlUtil.createArrayBufferOfFloat32Doubles(lighting.createProjectionTransformation().toWebGlArray()));
        // View transformation uniform
        WebGLUniformLocation viewUniform = getUniformLocation(VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(viewUniform, false, WebGlUtil.createArrayBufferOfFloat32Doubles(lighting.createViewTransformation().toWebGlArray()));
        // Model transformation uniform
        WebGLUniformLocation modelUniform = getUniformLocation(MODEL_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(modelUniform, false, WebGlUtil.createArrayBufferOfFloat32Doubles(Matrix4.createIdentity().toWebGlArray()));
        // set vertices position
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // set the barycentric coordinates
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(barycentricPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);
        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
