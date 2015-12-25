package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent
public class TerrainSurfaceWireRender extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;
    private WebGLBuffer barycentricBuffer;
    private int barycentricPositionAttribute;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;

    @PostConstruct
    public void init() {
        Object extension = gameCanvas.getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }

        createProgram(Shaders.INSTANCE.terrainSurfaceWireVertexShader(), Shaders.INSTANCE.terrainSurfaceWireFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
        barycentricBuffer = gameCanvas.getCtx3d().createBuffer();
        barycentricPositionAttribute = getAndEnableAttributeLocation(BARYCENTRIC_ATTRIBUTE_NAME);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getVertexList();
        // vertices
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createPositionDoubles()), WebGLRenderingContext.STATIC_DRAW);
        // barycentric
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, barycentricBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(vertexList.createBarycentricDoubles()), WebGLRenderingContext.STATIC_DRAW);

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        useProgram();
        // Projection uniform
        WebGLUniformLocation pUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(pUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // Model model transformation uniform
        WebGLUniformLocation mVUniform = getUniformLocation(VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(mVUniform, false, WebGlUtil.createArrayBufferOfFloat32(camera.createMatrix().toWebGlArray()));
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
