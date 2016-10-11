package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.ImageDescriptor;
import com.btxtech.uiservice.renderer.RenderServiceInitEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainUiService {
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainService terrainService;
    private static final double HIGHEST_POINT_IN_VIEW = 200;
    private static final double LOWEST_POINT_IN_VIEW = -20;
    private double highestPointInView; // Should be calculated
    private double lowestPointInView; // Should be calculated
    private ImageDescriptor topTexture = ImageDescriptor.GRASS_TEXTURE_2;
    private ImageDescriptor topBm = ImageDescriptor.GRASS_BM_1;
    private ImageDescriptor splatting = ImageDescriptor.BLEND_3;
    private ImageDescriptor groundTexture = ImageDescriptor.GROUND_6_TEXTURE_2;
    private ImageDescriptor groundBm = ImageDescriptor.GROUND_6_BM_2;
    private MapCollection<TerrainObjectConfig, ModelMatrices> terrainObjectConfigModelMatrices;

    public TerrainUiService() {
        highestPointInView = HIGHEST_POINT_IN_VIEW;
        lowestPointInView = LOWEST_POINT_IN_VIEW;
    }

    public void onRenderServiceInitEvent(@Observes RenderServiceInitEvent renderServiceInitEvent) {
        terrainObjectConfigModelMatrices = new MapCollection<>();
        for (Map.Entry<TerrainObjectConfig, Collection<TerrainObjectPosition>> entry : terrainService.getTerrainObjectPositions().getMap().entrySet()) {
            for (TerrainObjectPosition objectPosition : entry.getValue()) {
                int z = (int) terrainService.getInterpolatedTerrainTriangle(new DecimalPosition(objectPosition.getPosition())).getHeight();
                Matrix4 model = objectPosition.createModelMatrix(z);
                terrainObjectConfigModelMatrices.put(entry.getKey(), new ModelMatrices().setModel(model).setNorm(model.normTransformation()));
            }
        }
    }

    public void setTerrainSlopePositions(Collection<TerrainSlopePosition> terrainSlopePositions) {
        // TODO used in editor this.terrainSlopePositions = terrainSlopePositions;
    }

    public void setSlopeSkeleton(SlopeSkeletonConfig slopeSkeletonConfig) {
        // TODO used in editor
//        slopeSkeletonMap.put(slopeSkeletonConfig.getId(), slopeSkeletonConfig);
//        for (Slope slope : slopeMap.values()) {
//            if (slope.getSlopeSkeletonConfig().getId() == slopeSkeletonConfig.getId()) {
//                slope.updateSlopeSkeleton(slopeSkeletonConfig);
//            }
//        }
    }

    public ImageDescriptor getGroundTexture() {
        return groundTexture;
    }

    public ImageDescriptor getGroundBm() {
        return groundBm;
    }

    public ImageDescriptor getTopTexture() {
        return topTexture;
    }

    public ImageDescriptor getTopBm() {
        return topBm;
    }

    public ImageDescriptor getSplatting() {
        return splatting;
    }

    public double getHighestPointInView() {
        return highestPointInView;
    }

    public double getLowestPointInView() {
        return lowestPointInView;
    }


    public VertexList getGroundVertexList() {
        VertexList vertexList;
        if (terrainService.getGroundMesh() != null) {
            vertexList = terrainService.getGroundMesh().provideVertexList();
        } else {
            vertexList = new VertexList();
        }
        for (Slope slope : terrainService.getSlopes()) {
            if (!slope.hasWater()) {
                vertexList.append(slope.getGroundPlateauConnector().getTopMesh().provideVertexList());
                vertexList.append(slope.getGroundPlateauConnector().getInnerConnectionVertexList());
            }
            vertexList.append(slope.getGroundPlateauConnector().getOuterConnectionVertexList());
        }
        return vertexList;
    }

    public Water getWater() {
        return terrainService.getWater();
    }

    @Deprecated
    public ImageDescriptor getWaterBumpMap() {
        return ImageDescriptor.BUMP_MAP_01;
    }

    public double getWaterAnimation() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 0);
    }

    public double getWaterAnimation2() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 500);
    }

    public double getWaterAnimation(long millis, int durationMs, int offsetMs) {
        return Math.sin(((millis % durationMs) / (double) durationMs + ((double) offsetMs / (double) durationMs)) * MathHelper.ONE_RADIANT);
    }

    @Deprecated
    public Collection<Integer> getSlopeIds() {
        return terrainService.getSlopeIds();
    }

    public Slope getSlope(int id) {
        return terrainService.getSlope(id);
    }

    public GroundSkeletonConfig getGroundSkeleton() {
        return terrainTypeService.getGroundSkeletonConfig();
    }

    public List<ModelMatrices> provideTerrainObjectModelMatrices(TerrainObjectConfig terrainObjectConfig) {
        Collection<ModelMatrices> modelMatrices = terrainObjectConfigModelMatrices.get(terrainObjectConfig);
        if (modelMatrices != null) {
            return new ArrayList<>(modelMatrices);
        } else {
            return Collections.emptyList();
        }
    }
}
