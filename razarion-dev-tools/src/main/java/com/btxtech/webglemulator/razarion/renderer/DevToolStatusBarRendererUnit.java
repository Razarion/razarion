package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.JavaFxHelper;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.selection.AbstractStatusBarRendererUnit;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlProgramEmulator;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 14.02.2017.
 */
@ColorBufferRenderer
public class DevToolStatusBarRendererUnit extends AbstractStatusBarRendererUnit implements VertexShader {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;
    private ModelMatrices modelMatrices;

    @Override
    protected void fillBuffers(List<Vertex> vertices, List<Double> visibilities) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.TRIANGLES).setPaint(JavaFxHelper.toFxColor(Colors.HEALTH_BAR)).setVertexShader(this);
        webGlProgramEmulator.setDoubles(CollectionUtils.verticesToDoubles(vertices));
    }

    @Override
    public void setupImages() {
        // Ignored
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = projectionTransformation.getMatrix().multiply(camera.getMatrix().multiply(DevToolRenderUtil.toMatrix4(modelMatrices.getModel())));
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        this.modelMatrices = modelMatrices;
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }
}
