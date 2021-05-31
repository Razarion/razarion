package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.EditorSlopeWrapper;
import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.subtask.AbstractWebGlRenderTask;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.CollectionUtils;
import elemental2.webgl.WebGLRenderingContext;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 04.05.2016.
 */
@Dependent
public class TerrainEditorSlopeDrivewayRenderTask extends AbstractWebGlRenderTask<EditorSlopeWrapper> {
    private static final Color COLOR_SLOPE_HOVER = new Color(0.5, 0.0, 1.0, 1.0);
    private static final Color COLOR_NORMAL = new Color(0.5, 1.0, 0.0, 1.0);

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
        setupUniform("uColor", UniformLocation.Type.COLOR_RGBA, () -> TerrainEditorSlopeDrivewayRenderTask.this.modifiedSlope.isHover() ? COLOR_SLOPE_HOVER : COLOR_NORMAL);

    }

    public void fillBuffers(EditorSlopeWrapper modifiedSlope) {
        this.modifiedSlope = modifiedSlope;
        List<Vertex> corners = new ArrayList<>();
        for (int i = 0; i < modifiedSlope.getPolygon().getCorners().size(); i++) {
            DecimalPosition current = modifiedSlope.getPolygon().getCorners().get(i);
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, modifiedSlope.getPolygon().getCorners());
            if(modifiedSlope.isPositionInDriveway(current) && (modifiedSlope.isPositionInDriveway(next))) {
                corners.add(new Vertex(current, 0));
                corners.add(new Vertex(next, 0));
            }
        }
        setupVec3VertexPositionArray(corners);
    }
}
