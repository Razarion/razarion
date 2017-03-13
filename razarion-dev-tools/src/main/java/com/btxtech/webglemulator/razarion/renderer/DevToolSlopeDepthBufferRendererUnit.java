package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlProgramEmulator;
import javafx.scene.paint.Color;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.08.2016.
 */
@DepthBufferRenderer
public class DevToolSlopeDepthBufferRendererUnit extends AbstractSlopeRendererUnit implements VertexShader {
    @Inject
    private WebGlEmulator webGlEmulator;
    @Inject
    private ShadowUiService shadowUiService;
    private WebGlProgramEmulator webGlProgramEmulator;

    @Override
    protected void fillBuffer(SlopeUi slopeUi, GroundUi groundUi) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.TRIANGLES).setPaint(Color.GRAY).setVertexShader(this);
        webGlProgramEmulator.setDoubles(DevToolRenderUtil.toDoubles(slopeUi.getVertices()));
    }

    @Override
    protected void draw(SlopeUi slopeUi, GroundUi groundUi) {
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = shadowUiService.getDepthProjectionTransformation().multiply(shadowUiService.getDepthViewTransformation());
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }
}
