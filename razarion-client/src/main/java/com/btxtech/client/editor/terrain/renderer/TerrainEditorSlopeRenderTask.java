package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.EditorSlopeWrapper;
import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.subtask.AbstractWebGlRenderTask;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import elemental2.webgl.WebGLRenderingContext;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 04.05.2016.
 */
@Dependent
public class TerrainEditorSlopeRenderTask extends AbstractWebGlRenderTask<EditorSlopeWrapper> {
    private static final Color COLOR_HOVER = new Color(1.0, 1.0, 0.0, 1.0);
    private static final Color COLOR_NORMAL = new Color(1.0, 1.0, 1.0, 1.0);
    private EditorSlopeWrapper modifiedSlope;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(EditorSlopeWrapper modifiedSlope) {
        return new WebGlFacadeConfig(Shaders.SHADERS.terrainEditorSlopeCustom())
                .depthTest(false)
                .writeDepthBuffer(false)
                .drawMode(WebGLRenderingContext.LINE_LOOP);
    }

    @Override
    protected void setup(EditorSlopeWrapper modifiedSlope) {
        fillBuffers(modifiedSlope);
        setupUniform("uColor", UniformLocation.Type.COLOR_RGBA, () -> TerrainEditorSlopeRenderTask.this.modifiedSlope.isHover() ? COLOR_HOVER : COLOR_NORMAL);
    }

    public void fillBuffers(EditorSlopeWrapper modifiedSlope) {
        this.modifiedSlope = modifiedSlope;
        List<Vertex> corners = new ArrayList<>();
        for (DecimalPosition position : modifiedSlope.getPolygon().getCorners()) {
            corners.add(new Vertex(position, 0));
        }
        setupVec3VertexPositionArray(corners);
    }

}
