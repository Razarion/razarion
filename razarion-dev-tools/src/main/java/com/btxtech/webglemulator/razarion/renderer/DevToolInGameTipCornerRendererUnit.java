package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.tip.AbstractInGameTipCornerRendererUnit;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlProgramEmulator;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 07.12.2016.
 */
@ColorBufferRenderer
public class DevToolInGameTipCornerRendererUnit extends AbstractInGameTipCornerRendererUnit implements VertexShader {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;
    private ModelMatrices modelMatrices;
    private Color cornerColor;

    @Override
    protected void fillBuffers(List<Vertex> vertices) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.LINES).setVertexShader(this);
        webGlProgramEmulator.setDoubles(CollectionUtils.verticesToDoubles(vertices));
    }

    @Override
    public void setupImages() {

    }

    @Override
    protected void prepareDraw(Color cornerColor) {
        this.cornerColor = cornerColor;
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = projectionTransformation.getMatrix().multiply(camera.getMatrix().multiply(modelMatrices.getModel()));
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlProgramEmulator.setPaint(new javafx.scene.paint.Color(cornerColor.getR(), cornerColor.getG(), cornerColor.getB(), cornerColor.getA()));
        this.modelMatrices = modelMatrices;
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }
}
