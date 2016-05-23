package com.btxtech.renderer.emulation.razarion;

import com.btxtech.DrawScenes;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.renderer.emulation.webgl.VertexShader;
import com.btxtech.renderer.emulation.webgl.WebGlEmulator;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import com.btxtech.shared.primitives.Vertex4;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 22.05.2016.
 */
@Singleton
public class RazarionEmulator {
    @Inject
    private WebGlEmulator webGlEmulator;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainSurface terrainSurface;

    public void process() {
        setupTerrain();
        projectionTransformation.setAspectRatio(webGlEmulator.getAspectRatio());
        webGlEmulator.setVertexShader(new VertexShader() {
            @Override
            public Vertex4 process(Vertex vertex) {
                Matrix4 matrix4 = projectionTransformation.createMatrix().multiply(camera.createMatrix());
                return new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
            }
        });
        webGlEmulator.fillBuffer(terrainSurface.getGroundVertexList().createPositionDoubles());
        webGlEmulator.drawArrays();
    }

    private void setupTerrain() {
        Gson gson = new Gson();
        SlopeSkeleton slopeSkeletonBeach = gson.fromJson(new InputStreamReader(DrawScenes.class.getResourceAsStream("/SlopeSkeletonBeach.json")), SlopeSkeleton.class);
        SlopeSkeleton slopeSkeletonSlope = gson.fromJson(new InputStreamReader(DrawScenes.class.getResourceAsStream("/SlopeSkeletonSlope.json")), SlopeSkeleton.class);
        GroundSkeleton groundSkeleton = gson.fromJson(new InputStreamReader(DrawScenes.class.getResourceAsStream("/GroundSkeleton.json")), GroundSkeleton.class);
        List<TerrainSlopePosition> terrainSlopePositions = gson.fromJson(new InputStreamReader(DrawScenes.class.getResourceAsStream("/TerrainSlopePositions.json")), new TypeToken<List<TerrainSlopePosition>>(){}.getType());

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
}
