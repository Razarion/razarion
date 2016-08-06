package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.VertexContainerRenderUnit;
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
public class DevToolsItemUnitRenderer extends VertexContainerRenderUnit implements VertexShader {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;
    private ModelMatrices modelMatrices;

    @Override
    public void fillBuffers(VertexContainer vertexContainer) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.TRIANGLES).setPaint(Color.BLACK).setVertexShader(this);
        webGlProgramEmulator.setDoubles(CollectionUtils.verticesToDoubles(vertexContainer.getVertices()));
        setElementCount(vertexContainer);
    }

    @Override
    public void setupImages() {
        // Ignored
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix().multiply(modelMatrices.getModel()));
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }

    @Override
    protected void preModelDraw() {
        // Ignore
    }

    @Override
    protected void modelDraw(ModelMatrices modelMatrices) {
        this.modelMatrices = modelMatrices;
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }

}
