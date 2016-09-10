package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderUtil;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
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
@NormRenderer
public class DevToolWaterNormRendererUnit extends AbstractWaterRendererUnit implements VertexShader {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;

    @Override
    public void setupImages() {

    }

    @Override
    protected void fillInternalBuffers(Water water) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.LINES).setPaint(Color.BLACK).setVertexShader(this);
        webGlProgramEmulator.setDoubles(RenderUtil.setupNormDoubles(water.getVertices(), water.getNorms()));
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix());
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }
}
