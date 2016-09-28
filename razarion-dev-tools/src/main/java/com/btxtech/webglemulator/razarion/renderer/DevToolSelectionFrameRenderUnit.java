package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.selection.AbstractSelectionFrameRenderUnit;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlProgramEmulator;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 27.09.2016.
 */
@ColorBufferRenderer
public class DevToolSelectionFrameRenderUnit extends AbstractSelectionFrameRenderUnit implements VertexShader {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;

    @Override
    protected void fillBuffers(List<Vertex> vertices) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.LINES).setPaint(Color.GREEN).setVertexShader(this);
        webGlProgramEmulator.setDoubles(CollectionUtils.verticesToDoubles(vertices));
    }

    @Override
    protected void draw() {
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix());
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }
}
