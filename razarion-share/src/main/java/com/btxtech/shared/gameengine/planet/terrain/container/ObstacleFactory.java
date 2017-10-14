package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.slope.DrivewayTerrainTypeHandler;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.List;

/**
 * Created by Beat
 * on 14.10.2017.
 */
public class ObstacleFactory {
    public static void addObstacles(TerrainShape terrainShape, List<DecimalPosition> polygon, DrivewayTerrainTypeHandler drivewayTerrainTypeHandler, boolean isOuter) {
        DecimalPosition last = polygon.get(0);
        boolean inDriveway = false;
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, polygon);
            if (last.equals(next)) {
                continue;
            }
            if (drivewayTerrainTypeHandler.onDrivewayLine(next, isOuter)) {
                if (!inDriveway) {
                    if (isOuter) {
                        // Termination
                        addObstacleSlope(terrainShape, new ObstacleSlope(new Line(next, drivewayTerrainTypeHandler.getInner4Outer(next))));
                    }
                    addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, next)));
                }
                inDriveway = true;
            } else {
                if (inDriveway) {
                    if (isOuter) {
                        // Termination
                        addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, drivewayTerrainTypeHandler.getInner4Outer(last))));
                    }
                    addObstacleSlope(terrainShape, new ObstacleSlope(new Line(last, next)));
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
