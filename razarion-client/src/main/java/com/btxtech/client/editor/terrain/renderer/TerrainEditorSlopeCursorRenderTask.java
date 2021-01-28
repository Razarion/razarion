package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.TerrainEditorService;
import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.subtask.AbstractWebGlRenderTask;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import elemental2.webgl.WebGLRenderingContext;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 04.05.2016.
 */
@Dependent
public class TerrainEditorSlopeCursorRenderTask extends AbstractWebGlRenderTask<Polygon2D> {
    @Inject
    private TerrainEditorService terrainEditor;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(Polygon2D polygon2D) {
        return new WebGlFacadeConfig(Shaders.SHADERS.terrainEditorCursorCustom())
                .blend(WebGlFacadeConfig.Blend.SOURCE_ALPHA)
                .depthTest(false)
                .writeDepthBuffer(false)
                .drawMode(WebGLRenderingContext.TRIANGLE_FAN);
    }

    @Override
    protected void setup(Polygon2D cursor) {
        fillPositionArray(cursor);
        setupUniform("uCursorType", UniformLocation.Type.I, () -> terrainEditor.getCursorType().ordinal());
    }

    public void changeBuffers(Polygon2D cursor) {
        fillPositionArray(cursor);
    }

    private void fillPositionArray(Polygon2D cursor) {
        List<Vertex> triangleFan = new ArrayList<>();
        triangleFan.add(new Vertex(0, 0, 0));
        List<DecimalPosition> cursorPositions = cursor.getCorners();
        for (DecimalPosition cursorPosition : cursorPositions) {
            triangleFan.add(new Vertex(cursorPosition, 0));
        }
        triangleFan.add(new Vertex(cursorPositions.get(0), 0));
        setupVec3VertexPositionArray(triangleFan);
    }
}
