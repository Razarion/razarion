package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 08.11.2015.
 */
@Dependent
public class TerrainObjectNormUnitRenderer extends AbstractWebGlUnitRenderer {
    private Logger logger = Logger.getLogger(TerrainObjectNormUnitRenderer.class.getName());
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private VertexShaderAttribute vertices;
    private int terrainObjectId;

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
        terrainObjectId = terrainObjectService.getTerrainObjectId4VertexContainer(getId());
        VertexContainer vertexContainer = terrainObjectService.getVertexContainer(getId());
        if (vertexContainer == null || vertexContainer.isEmpty()) {
            logger.warning("No vertices to render");
            return;
        }
        if (vertexContainer.checkWrongNormSize()) {
            logger.warning("Normal has not same size as vertices");
            return;
        }

        List<Vertex> vectors = new ArrayList<>();
        for (int i = 0; i < vertexContainer.getVertices().size(); i++) {
            Vertex vertex = vertexContainer.getVertices().get(i);
            vectors.add(vertex);
            vectors.add(vertex.add(vertexContainer.getNorms().get(i).multiply(5)));
        }

        vertices.fillBuffer(vectors);
        setElementCount(vertexContainer);
    }

    @Override
    protected void preModelDraw() {
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());

        vertices.activate();

        getCtx3d().lineWidth(30);
    }

    @Override
    protected void modelDraw(ModelMatrices modelMatrices) {
        uniformMatrix4fv(U_MODEL_MATRIX, modelMatrices.getModel());

        drawArrays(WebGLRenderingContext.LINES);
    }
}
