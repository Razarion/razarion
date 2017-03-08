package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.AbstractBuildupVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlProgramEmulator;
import javafx.scene.paint.Color;

import javax.inject.Inject;

/**
 * Created by Beat
 * 26.07.2016.
 */
@ColorBufferRenderer
public class DevToolsBuildupVertexContainerRenderUnit extends AbstractBuildupVertexContainerRenderUnit implements VertexShader {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;
    private ModelMatrices modelMatrices;
    private Matrix4 buildupMatrix;
    private double tmpMaxZ;

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.TRIANGLES).setPaint(Color.BLACK).setVertexShader(this);
        webGlProgramEmulator.setDoubles(CollectionUtils.verticesToDoubles(vertexContainer.OLDgetVertices()));
    }

    @Override
    public void setupImages() {
        // Ignored
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        tmpMaxZ = Math.max(tmpMaxZ, buildupMatrix.multiply(vertex, 1.0).getZ());
        Matrix4 matrix4 = projectionTransformation.getMatrix().multiply(camera.getMatrix().multiply(modelMatrices.getModel()));
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }

    @Override
    protected void prepareDraw(Matrix4 buildupMatrix) {
        this.buildupMatrix = buildupMatrix;
    }

    @Override
    protected void draw(ModelMatrices modelMatrices, double progressZ) {
        this.modelMatrices = modelMatrices;
        tmpMaxZ = Double.MIN_VALUE;
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }

}
