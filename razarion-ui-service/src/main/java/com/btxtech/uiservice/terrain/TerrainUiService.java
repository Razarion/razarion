package com.btxtech.uiservice.terrain;

import com.btxtech.shared.VertexList;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.ColladaUiService;
import com.btxtech.uiservice.ImageDescriptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

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
    @Inject
    private ColladaUiService colladaUiService;
    private static final double HIGHEST_POINT_IN_VIEW = 200;
    private static final double LOWEST_POINT_IN_VIEW = -20;
    private double highestPointInView; // Should be calculated
    private double lowestPointInView; // Should be calculated
    private ImageDescriptor topTexture = ImageDescriptor.GRASS_TEXTURE_2;
    private ImageDescriptor topBm = ImageDescriptor.GRASS_BM_1;
    private ImageDescriptor splatting = ImageDescriptor.BLEND_3;
    private ImageDescriptor groundTexture = ImageDescriptor.GROUND_6_TEXTURE_2;
    private ImageDescriptor groundBm = ImageDescriptor.GROUND_6_BM_2;

    public TerrainUiService() {
        highestPointInView = HIGHEST_POINT_IN_VIEW;
        lowestPointInView = LOWEST_POINT_IN_VIEW;
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

    public Vertex calculatePositionOnZeroLevel(Ray3d worldPickRay) {
        // Find multiplier where the ray hits the ground (z = 0). start + m*direction -> z = 0
        double m = -worldPickRay.getStart().getZ() / worldPickRay.getDirection().getZ();
        return worldPickRay.getPoint(m);
//        logger.severe("Point On Ground: " + pointOnGround);
//        VertexData vertexData = originalGroundMesh.getVertexFromAbsoluteXY(pointOnGround.toXY());
//        if (vertexData != null) {
//            logger.severe("Ground VertexData: " + vertexData);
//        } else {
//            logger.severe("Position not on ground");
//        }
    }

    public Vertex calculatePositionGroundMesh(Ray3d worldPickRay) {
        DecimalPosition zeroLevel = calculatePositionOnZeroLevel(worldPickRay).toXY();
        double height = getInterpolatedTerrainTriangle(zeroLevel).getHeight();
        return new Vertex(zeroLevel, height);
    }

    public VertexList getGroundVertexList() {
        VertexList vertexList = terrainService.getGroundMesh().provideVertexList();
        for (Slope slope : terrainService.getSlopes()) {
            if (!slope.hasWater()) {
                vertexList.append(slope.getGroundPlateauConnector().getTopMesh().provideVertexList());
                vertexList.append(slope.getGroundPlateauConnector().getInnerConnectionVertexList());
            }
            vertexList.append(slope.getGroundPlateauConnector().getOuterConnectionVertexList());
        }
        return vertexList;
    }

    @Deprecated
    public Collection<Obstacle> getAllObstacles() {
        Collection<Obstacle> obstacles = new ArrayList<>();
        for (Slope slope : terrainService.getSlopes()) {
            obstacles.addAll(slope.generateObstacles());
        }
        return obstacles;
    }

    public InterpolatedTerrainTriangle getInterpolatedTerrainTriangle(DecimalPosition absoluteXY) {
        InterpolatedTerrainTriangle interpolatedTerrainTriangle = terrainService.getGroundMesh().getInterpolatedTerrainTriangle(absoluteXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle;
        }

        for (Slope slope : terrainService.getSlopes()) {
            interpolatedTerrainTriangle = slope.getInterpolatedVertexData(absoluteXY);
            if (interpolatedTerrainTriangle != null) {
                return interpolatedTerrainTriangle;
            }
        }

        throw new IllegalArgumentException("No InterpolatedTerrainTriangle at: " + absoluteXY);
    }

    public Water getWater() {
        return terrainService.getWater();
    }

    public ImageDescriptor getWaterBumpMap() {
        return ImageDescriptor.BUMP_MAP_01;
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

    public Collection<ModelMatrices> provideTerrainObjectModelMatrices(TerrainObjectConfig terrainObjectConfig) {
        Collection<ModelMatrices> modelMatrices = new ArrayList<>();
        for (TerrainObjectPosition objectPosition : terrainService.getTerrainObjectPositions(terrainObjectConfig)) {
            int z = (int) getInterpolatedTerrainTriangle(new DecimalPosition(objectPosition.getPosition())).getHeight();
            Matrix4 model = objectPosition.createModelMatrix(z).multiply(Matrix4.createScale(colladaUiService.getGeneralScale(), colladaUiService.getGeneralScale(), colladaUiService.getGeneralScale()));
            modelMatrices.add(new ModelMatrices().setModel(model).setNorm(model.normTransformation()));
        }
        return modelMatrices;
    }
}
