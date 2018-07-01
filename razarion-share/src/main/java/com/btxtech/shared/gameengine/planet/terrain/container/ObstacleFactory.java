package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
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
        int offset = CollectionUtils.findStart(polygon, position -> !slope.getDrivewayGameEngineHandler().onFlatLine(position, isOuter));

        DecimalPosition last = polygon.get(offset);
        boolean inDriveway = false;
        ObstacleSlope firstObstacleSlope = null;
        ObstacleSlope previousObstacleSlope = null;
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + offset + 1, polygon);
            if (last.equals(next)) {
                continue;
            }
            if (slope.getDrivewayGameEngineHandler().onFlatLine(next, isOuter)) {
                if (!inDriveway) {
                    if (isOuter) {
                        // Termination
                        previousObstacleSlope = addObstacleSlope(terrainShape, new ObstacleSlope(last, next), previousObstacleSlope);
                        previousObstacleSlope = addObstacleSlope(terrainShape, new ObstacleSlope(next, slope.getDrivewayGameEngineHandler().getInner4OuterTermination(next)), previousObstacleSlope);
                    }
                }
                inDriveway = true;
            } else {
                if (inDriveway) {
                    if (isOuter) {
                        // Termination
                        previousObstacleSlope = addObstacleSlope(terrainShape, new ObstacleSlope(slope.getDrivewayGameEngineHandler().getInner4OuterTermination(last), last), previousObstacleSlope);
                        previousObstacleSlope = addObstacleSlope(terrainShape, new ObstacleSlope(last, next), previousObstacleSlope);
                    }
                } else {
                    previousObstacleSlope = addObstacleSlope(terrainShape, new ObstacleSlope(last, next), previousObstacleSlope);
                }
                inDriveway = false;
            }
            last = next;
            if (firstObstacleSlope == null) {
                firstObstacleSlope = previousObstacleSlope;
            }
        }
        if (previousObstacleSlope != null) {
            firstObstacleSlope.initPrevious(previousObstacleSlope);
            previousObstacleSlope.initNext(firstObstacleSlope);
        }
    }

    private static ObstacleSlope addObstacleSlope(TerrainShape terrainShape, ObstacleSlope obstacleSlope, ObstacleSlope previous) {
        for (Index nodeIndex : GeometricUtil.rasterizeLine(obstacleSlope.createLine(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH)) {
            TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
            terrainShapeNode.addObstacle(obstacleSlope);
        }
        if (previous != null) {
            obstacleSlope.initPrevious(previous);
            previous.initNext(obstacleSlope);
        }
        return obstacleSlope;
    }


}
