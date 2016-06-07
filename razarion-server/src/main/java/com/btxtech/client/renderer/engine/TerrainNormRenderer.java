package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.terrain.slope.Mesh;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

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
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers() {
        List<Vertex> vectors = new ArrayList<>();

        VertexList vertexList = terrainSurface.getGroundVertexList();
        appendVectors(vectors, vertexList.getVertices(), vertexList.getNormVertices(), vertexList.getTangentVertices());

        for (int id : terrainSurface.getSlopeIds()) {
            Mesh mesh = terrainSurface.getSlope(id).getMesh();
            appendVectors(vectors, mesh.getVertices(), mesh.getNorms(), mesh.getTangents());
        }

        appendVectors(vectors, terrainSurface.getWater().getVertices(), terrainSurface.getWater().getNorms(), terrainSurface.getWater().getTangents());

        this.vertices.fillBuffer(vectors);
        elementCount = vectors.size();
    }

    private void appendVectors(List<Vertex> vectors, List<Vertex> vertices, List<Vertex> norms, List<Vertex> tangents) {
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            // Norm
            vectors.add(vertex);
            vectors.add(vertex.add(norms.get(i).multiply(20)));
            // Tangent
            vectors.add(vertex);
            vectors.add(vertex.add(tangents.get(i).multiply(20)));
        }
    }

    @Override
    public void draw() {
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv(U_MODEL_MATRIX, Matrix4.createIdentity());

        vertices.activate();

        // Draw
        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.LINES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());
    }
}
