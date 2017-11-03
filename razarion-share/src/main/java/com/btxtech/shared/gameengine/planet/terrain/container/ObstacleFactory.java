package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.slope.Driveway;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.List;

/**
 * Created by Beat
 * on 14.10.2017.
 */
public class ObstacleFactory {
    public static void addObstacles(TerrainShape terrainShape, List<DecimalPosition> polygon, Slope slope, boolean isOuter) {
        // Find offset with no driveway
        int offset = -1;
        for (int i = 0; i < polygon.size(); i++) {
            if (!slope.getDrivewayGameEngineHandler().onFlatLine(polygon.get(i), isOuter)) {
                offset = i;
                break;
            }
        }
        if (offset < 0) {
            throw new IllegalArgumentException("ObstacleFactory.addObstacles(): Can not find start position with no driveway");
        }

        DecimalPosition last = polygon.get(offset);
        boolean inDriveway = false;
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + offset + 1, polygon);
            if (last.equals(next)) {
                continue;
            }
            if (slope.getDrivewayGameEngineHandler().onFlatLine(next, isOuter)) {
                if (!inDriveway) {
                    if (isOuter) {
                        // Termination
                        Driveway driveway = slope.getDriveway(slope.getDrivewayGameEngineHandler().getInner4OuterFlatLine(next));
                        DecimalPosition terminationPosition = driveway.setupInnerStartFlatGrowthPosition(slope.getSlopeSkeletonConfig().getInnerLineGameEngine() - slope.getSlopeSkeletonConfig().getOuterLineGameEngine());
                        addObstacleSlope(terrainShape, new ObstacleSlope(new Line(next, terminationPosition)));
                    }
                    if (isOuter) {
                        addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, next)));
                    } else {
                        Driveway driveway = slope.getDriveway(next);
                        DecimalPosition terminationPosition = driveway.setupInnerStartFlatGrowthPosition(slope.getSlopeSkeletonConfig().getInnerLineGameEngine() - slope.getSlopeSkeletonConfig().getOuterLineGameEngine());
                        // addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, terminationPosition)));
                    }
                }
                inDriveway = true;
            } else {
                if (inDriveway) {
                    if (isOuter) {
                        // Termination
                        Driveway driveway = slope.getDriveway(slope.getDrivewayGameEngineHandler().getInner4OuterFlatLine(last));
                        DecimalPosition terminationPosition = driveway.setupInnerEndFlatGrowthPosition(slope.getSlopeSkeletonConfig().getInnerLineGameEngine() - slope.getSlopeSkeletonConfig().getOuterLineGameEngine());
                        addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, terminationPosition)));
                    }
                    if (isOuter) {
                        addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, next)));
                    } else {
                        Driveway driveway = slope.getDriveway(last);
                        DecimalPosition terminationPosition = driveway.setupInnerEndFlatGrowthPosition(slope.getSlopeSkeletonConfig().getInnerLineGameEngine() - slope.getSlopeSkeletonConfig().getOuterLineGameEngine());
                        //addObstacleSlope(terrainShape, new ObstacleSlope(new Line(terminationPosition, next)));
                    }
                } else {
                    addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, next)));
                }
                inDriveway = false;
            }
            last = next;
        }
    }

    private static void addObstacleSlope(TerrainShape terrainShape, ObstacleSlope obstacleSlope) {
        for (Index nodeIndex : GeometricUtil.rasterizeLine(obstacleSlope.getLine(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH)) {
            TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
            terrainShapeNode.addObstacle(obstacleSlope);
        }
    }


}
