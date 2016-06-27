package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.uiservice.units.ItemService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 08.11.2015.
 */
@Dependent
public class UnitNormRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(UnitNormRenderer.class.getName());
    @Inject
    private ItemService itemService;
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
        VertexContainer vertexContainer = itemService.getItemTypeVertexContainer(getId());
        if (vertexContainer == null) {
            return;
        }
        List<Vertex> vertices = vertexContainer.getVertices();
        List<Vertex> norms = vertexContainer.getNorms();
        // List<Vertex> tangents = vertexList.getTangentVertices();

        List<Vertex> vectors = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            // Norm
            vectors.add(vertex);
            vectors.add(vertex.add(norms.get(i).multiply(5)));
            // Tangent
            // vectors.add(vertex);
            // vectors.add(vertex.add(tangents.get(i).multiply(5)));
        }

        this.vertices.fillBuffer(vectors);
        setElementCount(vertexContainer);
    }

    @Override
    public void draw() {
        Collection<ModelMatrices> modelMatrices = itemService.getModelMatrices(getId());
        if (modelMatrices == null || modelMatrices.isEmpty()) {
            return;
        }

        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());

        vertices.activate();

        getCtx3d().lineWidth(30);

        for (ModelMatrices model : modelMatrices) {
            uniformMatrix4fv(U_MODEL_MATRIX, model.getModel());

            drawArrays(WebGLRenderingContext.LINES);
        }
    }
}
