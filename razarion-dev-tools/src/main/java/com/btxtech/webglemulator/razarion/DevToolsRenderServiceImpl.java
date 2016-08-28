package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.CompositeRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.webglemulator.WebGlEmulatorController;
import com.btxtech.webglemulator.WebGlEmulatorShadowController;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 12.07.2016.
 */
@Singleton
public class DevToolsRenderServiceImpl extends RenderService {
    @Inject
    private WebGlEmulator webGlEmulator;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private WebGlEmulatorController webGlEmulatorController;
    @Inject
    private WebGlEmulatorShadowController shadowController;
    @Inject
    private Instance<Object> instance;
    private VertexShader terrainShader = new VertexShader() {
        @Override
        public Vertex4 runShader(Vertex vertex) {
            Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix());
            return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
        }
    };
    private VertexShader terrainShaderShadow = new VertexShader() {
        @Override
        public Vertex4 runShader(Vertex vertex) {
            Matrix4 matrix4 = shadowUiService.createDepthProjectionTransformation().multiply(shadowUiService.createDepthViewTransformation());
            return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
        }
    };

    @Override
    protected void prepareMainRendering() {
        webGlEmulator.setCanvas(webGlEmulatorController.getCanvas());
        webGlEmulator.clear();
    }

    @Override
    protected void prepareDepthBufferRendering() {
        if (shadowController.isActive()) {
            webGlEmulator.setCanvas(shadowController.getCanvas());
            webGlEmulator.clear();
        } else {
            webGlEmulator.setCanvas(null);
        }
    }

    @Deprecated
    protected void setupRenderers() {
//        // Ground
//        webGlEmulator.fillBufferAndShader(RenderMode.TRIANGLES, terrainShader, terrainUiService.getGroundVertexList().createPositionDoubles(), Color.BLUE);
//       TODO  webGlEmulator.fillBufferAndShader(RenderMode.LINES, terrainShader, setupNormDoubles(terrainUiService.getGroundVertexList().getVertices(), terrainUiService.getGroundVertexList().getNormVertices()), Color.BROWN);
//        webGlEmulatorShadow.fillBufferAndShader(RenderMode.TRIANGLES, terrainShaderShadow, terrainUiService.getGroundVertexList().createPositionDoubles(), Color.BLUE);
//        // Slopes
//        for (Integer slopeId : terrainUiService.getSlopeIds()) {
//            Mesh mesh = terrainUiService.getSlope(slopeId).getMesh();
//            webGlEmulator.fillBufferAndShader(RenderMode.TRIANGLES, terrainShader, CollectionUtils.verticesToDoubles(mesh.getVertices()), Color.RED);
//   TODO         webGlEmulator.fillBufferAndShader(RenderMode.LINES, terrainShader, setupNormDoubles(mesh.getVertices(), mesh.getNorms()), Color.GREEN);
//            webGlEmulatorShadow.fillBufferAndShader(RenderMode.TRIANGLES, terrainShaderShadow, CollectionUtils.verticesToDoubles(mesh.getVertices()), Color.RED);
//        }
    }
}
