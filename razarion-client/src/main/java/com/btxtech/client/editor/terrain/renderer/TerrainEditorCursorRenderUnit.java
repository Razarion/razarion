package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.TerrainEditorImpl;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 04.05.2016.
 */
@ColorBufferRenderer
@Dependent
public class TerrainEditorCursorRenderUnit extends AbstractRenderUnit<Polygon2D> {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainEditorImpl terrainEditor;
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute vertices;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.terrainEditorCursorVertexShader(), Shaders.INSTANCE.terrainEditorCursorFragmentShader());
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void fillBuffers(Polygon2D cursor) {
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

    @Override
    protected void prepareDraw() {

    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        if(!terrainEditor.isCursorVisible()) {
            return;
        }
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, terrainEditor.getCursorModelMatrix());

        vertices.activate();

        webGlFacade.uniform1i(WebGlFacade.U_CURSOR_TYPE, terrainEditor.getCursorType().ordinal());

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLE_FAN);
    }
}
