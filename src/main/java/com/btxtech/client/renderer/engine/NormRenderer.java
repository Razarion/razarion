package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 08.11.2015.
 */
@Dependent
public class NormRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Camera camera;
    @Inject
    @Normal
    private ProjectionTransformation projectionTransformation;
    // private Logger logger = Logger.getLogger(NormRenderer.class.getName());
    private int elementCount;
    private WebGLBuffer verticesBuffer;
    private int vertexPositionAttribute;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.debugVectorVertexShader(), Shaders.INSTANCE.debugVectorFragmentShader());
        verticesBuffer = gameCanvas.getCtx3d().createBuffer();
        vertexPositionAttribute = getAndEnableAttributeLocation(A_VERTEX_POSITION);
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getVertexList();
        List<Vertex> vertices = vertexList.getVertices();
        List<Vertex> norms = vertexList.getNormVertices();

        List<Double> doubles = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            vertex.appendTo(doubles);
            Vertex norm = norms.get(i);
            vertex.add(norm.multiply(10)).appendTo(doubles);
        }

        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().bufferData(WebGLRenderingContext.ARRAY_BUFFER, WebGlUtil.createArrayBufferOfFloat32(doubles), WebGLRenderingContext.STATIC_DRAW);
        elementCount = vertexList.getVerticesCount() * 2;

        // logger.severe("ALIASED_LINE_WIDTH_RANGE: " + gameCanvas.getCtx3d().getParameter(WebGLRenderingContext.ALIASED_LINE_WIDTH_RANGE));
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        // Projection uniform
        WebGLUniformLocation pUniform = getUniformLocation(PERSPECTIVE_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(pUniform, false, WebGlUtil.createArrayBufferOfFloat32(projectionTransformation.createMatrix().toWebGlArray()));
        // Model model transformation uniform
        WebGLUniformLocation mVUniform = getUniformLocation(VIEW_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(mVUniform, false, WebGlUtil.createArrayBufferOfFloat32(camera.createMatrix().toWebGlArray()));
        // Model transformation uniform
        WebGLUniformLocation modelUniform = getUniformLocation(MODEL_UNIFORM_NAME);
        gameCanvas.getCtx3d().uniformMatrix4fv(modelUniform, false, WebGlUtil.createArrayBufferOfFloat32(Matrix4.createIdentity().toWebGlArray()));
        // set vertices position
        gameCanvas.getCtx3d().bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, verticesBuffer);
        gameCanvas.getCtx3d().vertexAttribPointer(vertexPositionAttribute, Vertex.getComponentsPerVertex(), WebGLRenderingContext.FLOAT, false, 0, 0);

        // Draw
        gameCanvas.getCtx3d().lineWidth(30);
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.LINES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
