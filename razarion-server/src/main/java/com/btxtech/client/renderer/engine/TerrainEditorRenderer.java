package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.client.editor.terrain.TerrainEditorSlopeModifiedEvent;
import com.btxtech.client.editor.terrain.TerrainEditorSlopeSelectedEvent;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Polygon2I;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 04.05.2016.
 */
@Dependent
public class TerrainEditorRenderer extends AbstractRenderer {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainEditor terrainEditor;
    private VertexShaderAttribute vertices;
    private boolean selected;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainEditorVertexShader(), Shaders.INSTANCE.terrainEditorFragmentShader());
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void fillBuffers() {
        fillBuffers(terrainEditor.getSlopePolygon(getId()));
    }

    private void fillBuffers(Polygon2I polygon) {
        List<Vertex> corners = new ArrayList<>();
        for (Index position : polygon.getCorners()) {
            corners.add(new Vertex(position.getX(), position.getY(), 0));
        }
        vertices.fillBuffer(corners);
        setElementCount(corners.size());
    }

    public void onTerrainEditorSlopeSelectedEvent(@Observes TerrainEditorSlopeSelectedEvent terrainEditorSlopeSelectedEvent) {
        selected = getId() == terrainEditorSlopeSelectedEvent.getSlopeId();
    }

    public void onTerrainEditorSlopeModifiedEvent(@Observes TerrainEditorSlopeModifiedEvent terrainEditorSlopeModifiedEvent) {
        if (getId() == terrainEditorSlopeModifiedEvent.getSlopeId()) {
            fillBuffers(terrainEditorSlopeModifiedEvent.getSlope());
        }
    }

    @Override
    public void draw() {
        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());

        uniform1b("uSelected", selected);

        vertices.activate();

        // Draw
        drawArrays(WebGLRenderingContext.LINE_LOOP);
    }
}
