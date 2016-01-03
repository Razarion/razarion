package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
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
public class TerrainNormRenderer extends AbstractRenderer {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    // private Logger logger = Logger.getLogger(NormRenderer.class.getName());
    private int elementCount;
    private VertexShaderAttribute vertices;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.debugVectorVertexShader(), Shaders.INSTANCE.debugVectorFragmentShader());
        vertices = createVertexShaderAttribute("aVertexPosition");
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getVertexList();
        List<Vertex> vertices = vertexList.getVertices();
        List<Vertex> norms = vertexList.getNormVertices();
        List<Vertex> tangents = vertexList.getTangentVertices();

        List<Vertex> vectors = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            // Norm
            vectors.add(vertex);
            vectors.add(vertex.add(norms.get(i).multiply(5)));
            // Tangent
            vectors.add(vertex);
            vectors.add(vertex.add(tangents.get(i).multiply(5)));
        }

        this.vertices.fillBuffer(vectors);
        elementCount = vertexList.getVerticesCount() * 2;
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uMMatrix", Matrix4.createIdentity());

        vertices.activate();

        // Draw
        gameCanvas.getCtx3d().lineWidth(30);
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.LINES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
