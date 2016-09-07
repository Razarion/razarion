package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderUtil;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlProgramEmulator;
import javafx.scene.paint.Color;

import javax.inject.Inject;

/**
 * Created by Beat
 * 28.08.2016.
 */
@NormRenderer
public class DevToolGroundNormRendererUnit extends AbstractGroundRendererUnit implements VertexShader {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;

    @Override
    protected void fillBuffers(VertexList vertexList) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.LINES).setPaint(Color.BLACK).setVertexShader(this);
        webGlProgramEmulator.setDoubles(RenderUtil.setupNormDoubles(vertexList.getVertices(), vertexList.getNormVertices()));
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix());
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }

    @Override
    public void draw(ModelMatrices modelMatrice) {
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }
}
