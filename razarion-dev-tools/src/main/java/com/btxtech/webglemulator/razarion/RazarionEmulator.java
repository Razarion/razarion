package com.btxtech.webglemulator.razarion;

import com.btxtech.game.jsre.client.common.CollectionUtils;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.ItemType;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import com.btxtech.shared.primitives.Vertex4;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.uiservice.terrain.slope.Mesh;
import com.btxtech.uiservice.units.ItemService;
import com.btxtech.webglemulator.WebGlEmulatorSceneController;
import com.btxtech.webglemulator.webgl.RenderMode;
import com.btxtech.webglemulator.webgl.VertexShader;
import com.btxtech.webglemulator.webgl.WebGlEmulator;
import com.btxtech.webglemulator.webgl.WebGlEmulatorShadow;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 22.05.2016.
 */
@Singleton
public class RazarionEmulator {
    private static final long RENDER_DELAY = 800;
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
    private ItemService itemService;
    @Inject
    private WebGlEmulatorSceneController sceneController;
    @Inject
    private ShadowUiService shadowUiService;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
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

    public void process() {
        setupTerrain();
        setupItems();
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

        start();
    }

    private void start() {
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                             long time = System.currentTimeMillis();
                            webGlEmulatorShadow.drawArrays();
                            webGlEmulator.drawArrays();
                            sceneController.update();
                             System.out.println("Time for render: " + (System.currentTimeMillis() - time));
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });
            }
        }, RENDER_DELAY, RENDER_DELAY, TimeUnit.MILLISECONDS);
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

    private void setupTerrain() {
        Gson gson = new Gson();
        SlopeSkeleton slopeSkeletonBeach = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/SlopeSkeletonBeach.json")), SlopeSkeleton.class);
        SlopeSkeleton slopeSkeletonSlope = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/SlopeSkeletonSlope.json")), SlopeSkeleton.class);
        GroundSkeleton groundSkeleton = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/GroundSkeleton.json")), GroundSkeleton.class);
        // List<TerrainSlopePosition> terrainSlopePositions = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/TerrainSlopePositions.json")), new TypeToken<List<TerrainSlopePosition>>() {
        // }.getType());
        List<TerrainSlopePosition> terrainSlopePositions = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/TerrainSlopePositions.json")), new TypeToken<List<TerrainSlopePosition>>() {
        }.getType());

        // Setup terrain surface
        try {
            terrainSurface.setGroundSkeleton(groundSkeleton);
            terrainSurface.setAllSlopeSkeletons(Arrays.asList(slopeSkeletonSlope, slopeSkeletonBeach));
            terrainSurface.setTerrainSlopePositions(terrainSlopePositions);
            terrainSurface.init();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void setupItems() {
        Gson gson = new Gson();
        List<ItemType> itemTypes = gson.fromJson(new InputStreamReader(RazarionEmulator.class.getResourceAsStream("/ItemType.json")), new TypeToken<List<ItemType>>() {
        }.getType());
        try {
            itemService.setItemTypes(itemTypes);
            itemService.init();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
