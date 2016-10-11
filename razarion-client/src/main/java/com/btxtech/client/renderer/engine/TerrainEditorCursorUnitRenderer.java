package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.client.editor.terrain.TerrainEditorCursorShapeEvent;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainUiService;
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
public class TerrainEditorCursorUnitRenderer extends AbstractWebGlUnitRenderer {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainEditor terrainEditor;
    private VertexShaderAttribute vertices;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainEditorCursorVertexShader(), Shaders.INSTANCE.terrainEditorCursorFragmentShader());
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    public void fillBuffers() {
        fillBuffer(terrainEditor.getCursor());
    }

    private void fillBuffer(Polygon2D cursor) {
        List<Vertex> triangleFan = new ArrayList<>();
        triangleFan.add(new Vertex(0, 0, 0));
        List<DecimalPosition> cursorPositions = cursor.getCorners();
        for (DecimalPosition cursorPosition : cursorPositions) {
            triangleFan.add(new Vertex(cursorPosition, 0));
        }
        triangleFan.add(new Vertex(cursorPositions.get(0), 0));
        vertices.fillBuffer(triangleFan);
        setElementCount(triangleFan.size());
    }

    public void onTerrainEditorCursorShapeEvent(@Observes TerrainEditorCursorShapeEvent terrainEditorCursorShapeEvent) {
        fillBuffer(terrainEditorCursorShapeEvent.getCursor());
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv(U_MODEL_MATRIX, terrainEditor.getCursorModelMatrix());

        vertices.activate();

        uniform1i(U_CURSOR_TYPE, terrainEditor.getCursorType().ordinal());

        // Draw
        drawArrays(WebGLRenderingContext.TRIANGLE_FAN);
    }
}
