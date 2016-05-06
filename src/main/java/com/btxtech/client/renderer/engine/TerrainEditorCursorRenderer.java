package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.client.editor.terrain.TerrainEditorCursorPositionEvent;
import com.btxtech.client.editor.terrain.TerrainEditorCursorShapeEvent;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Polygon2D;
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
public class TerrainEditorCursorRenderer extends AbstractRenderer {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainEditor terrainEditor;
    private VertexShaderAttribute vertices;
    private int elementCount;
    private Matrix4 modelMatrix = Matrix4.createIdentity();

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainEditorCursorVertexShader(), Shaders.INSTANCE.terrainEditorCursorFragmentShader());
        vertices = createVertexShaderAttribute("aVertexPosition");
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
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
        elementCount = triangleFan.size();
    }

    public void onTerrainEditorCursorPositionEvent(@Observes TerrainEditorCursorPositionEvent terrainEditorCursorPositionEvent) {
        Vertex terrainPosition = terrainEditorCursorPositionEvent.getPosition();
        modelMatrix = Matrix4.createTranslation(terrainPosition.getX(), terrainPosition.getY(), terrainPosition.getZ());
    }

    public void onTerrainEditorCursorShapeEvent(@Observes TerrainEditorCursorShapeEvent terrainEditorCursorShapeEvent) {
        fillBuffer(terrainEditorCursorShapeEvent.getCursor());
    }

    @Override
    public void draw() {
        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uMMatrix", modelMatrix);

        vertices.activate();

        // Draw
        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLE_FAN, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
