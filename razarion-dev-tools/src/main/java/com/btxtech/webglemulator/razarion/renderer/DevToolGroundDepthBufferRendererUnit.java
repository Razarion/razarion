package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
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
public class DevToolGroundDepthBufferRendererUnit extends AbstractGroundRendererUnit implements VertexShader {
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private WebGlEmulator webGlEmulator;
    private WebGlProgramEmulator webGlProgramEmulator;

    @Override
    public void setupImages() {

    }

    @Override
    protected void fillBuffers(VertexList vertexList, GroundSkeletonConfig groundSkeletonConfig) {
        webGlProgramEmulator = new WebGlProgramEmulator().setRenderMode(RenderMode.TRIANGLES).setPaint(Color.GREEN).setVertexShader(this);
        webGlProgramEmulator.setDoubles(CollectionUtils.verticesToDoubles(vertexList.getVertices()));
    }

    @Override
    public Vertex4 runShader(Vertex vertex) {
        Matrix4 matrix4 = shadowUiService.getDepthProjectionTransformation().multiply(shadowUiService.getDepthViewTransformation());
        return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlEmulator.drawArrays(webGlProgramEmulator);
    }
}
