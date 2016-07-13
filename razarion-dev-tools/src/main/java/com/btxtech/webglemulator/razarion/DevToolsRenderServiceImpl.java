package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.uiservice.terrain.slope.Mesh;
import com.btxtech.uiservice.units.ItemService;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlEmulatorShadow;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 12.07.2016.
 */
@Singleton
public class DevToolsRenderServiceImpl implements RenderService {
    @Inject
    private WebGlEmulator webGlEmulator;
    @Inject
    private WebGlEmulatorShadow webGlEmulatorShadow;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private ItemService itemService;
    private VertexShader terrainShader = new VertexShader() {
        @Override
        public Vertex4 process(Vertex vertex) {
            Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix());
            return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
        }
    };
    private VertexShader terrainShaderShadow = new VertexShader() {
        @Override
        public Vertex4 process(Vertex vertex) {
            Matrix4 matrix4 = shadowUiService.createDepthProjectionTransformation().multiply(shadowUiService.createDepthViewTransformation());
            return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
        }
    };

    @Override
    public void setupRenderers() {
        // Ground
        webGlEmulator.fillBufferAndShader(RenderMode.TRIANGLES, terrainShader, terrainSurface.getGroundVertexList().createPositionDoubles(), Color.BLUE);
        webGlEmulator.fillBufferAndShader(RenderMode.LINES, terrainShader, setupNormDoubles(terrainSurface.getGroundVertexList().getVertices(), terrainSurface.getGroundVertexList().getNormVertices()), Color.BROWN);
        webGlEmulatorShadow.fillBufferAndShader(RenderMode.TRIANGLES, terrainShaderShadow, terrainSurface.getGroundVertexList().createPositionDoubles(), Color.BLUE);
        // Slopes
        for (Integer slopeId : terrainSurface.getSlopeIds()) {
            Mesh mesh = terrainSurface.getSlope(slopeId).getMesh();
            webGlEmulator.fillBufferAndShader(RenderMode.TRIANGLES, terrainShader, CollectionUtils.verticesToDoubles(mesh.getVertices()), Color.RED);
            webGlEmulator.fillBufferAndShader(RenderMode.LINES, terrainShader, setupNormDoubles(mesh.getVertices(), mesh.getNorms()), Color.GREEN);
            webGlEmulatorShadow.fillBufferAndShader(RenderMode.TRIANGLES, terrainShaderShadow, CollectionUtils.verticesToDoubles(mesh.getVertices()), Color.RED);
        }
        // Items
        final Integer itemTypeId = 1;
        webGlEmulator.fillBufferAndShader(RenderMode.TRIANGLES, new VertexShader() {
            @Override
            public Vertex4 process(Vertex vertex) {
                Collection<ModelMatrices> modelMatrices = itemService.getModelMatrices(itemTypeId);
                ModelMatrices model = CollectionUtils.getFirst(modelMatrices);
                Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix().multiply(model.getModel()));
                return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
            }
        }, CollectionUtils.verticesToDoubles(itemService.getItemTypeVertexContainer(itemTypeId).getVertices()), Color.BLACK);
    }

    @Override
    public void enrollAnimation(int animatedMeshId) {
        System.out.println("enrollAnimation: " + animatedMeshId);
    }

    @Override
    public void disenrollAnimation(int animatedMeshId) {
        System.out.println("disenrollAnimation: " + animatedMeshId);
    }

    private List<Double> setupNormDoubles(List<Vertex> vertices, List<Vertex> norms) {
        List<Double> normDoubles = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            Vertex norm = norms.get(i);
            vertex.appendTo(normDoubles);
            vertex.add(norm.multiply(10)).appendTo(normDoubles);
        }
        return normDoubles;
    }

    @Override
    public void render() {
        webGlEmulatorShadow.drawArrays();
        webGlEmulator.drawArrays();
    }
}
