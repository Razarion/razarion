package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
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
@Deprecated
public class GroundNormUnitRenderer extends AbstractWebGlUnitRenderer {
    // private Logger logger = Logger.getLogger(NormRenderer.class.getName());
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
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

        VertexList vertexList = terrainUiService.getGroundVertexList();
        appendVectors(vectors, vertexList.getVertices(), vertexList.getNormVertices(), vertexList.getTangentVertices());

        for (int id : terrainUiService.getSlopeIds()) {
            Mesh mesh = terrainUiService.getSlope(id).getMesh();
            appendVectors(vectors, mesh.getVertices(), mesh.getNorms(), mesh.getTangents());
        }

        appendVectors(vectors, terrainUiService.getWater().getVertices(), terrainUiService.getWater().getNorms(), terrainUiService.getWater().getTangents());

        this.vertices.fillBuffer(vectors);
        setElementCount(vectors.size());
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
        drawArrays(WebGLRenderingContext.LINES);
    }
}
