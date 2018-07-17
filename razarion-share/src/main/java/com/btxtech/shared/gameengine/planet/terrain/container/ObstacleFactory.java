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
    public static void addObstacles(TerrainShape terrainShape, Slope slope) {
        for (List<DecimalPosition> polygon : slope.getObstacleFactoryContext().getPolygons()) {
            for (int i = 0; i < polygon.size(); i++) {
                DecimalPosition previous = CollectionUtils.getCorrectedElement(i - 1, polygon);
                DecimalPosition point1 = polygon.get(i);
                DecimalPosition point2 = CollectionUtils.getCorrectedElement(i + 1, polygon);
                DecimalPosition next = CollectionUtils.getCorrectedElement(i + 2, polygon);
                if (point1.equals(point2)) {
                    // also for previous and next ???
                    continue;
                }
                addObstacleSlope(terrainShape, new ObstacleSlope(point1, point2, previous, next));
            }
        }
    }

    private static void addObstacleSlope(TerrainShape terrainShape, ObstacleSlope obstacleSlope) {
        for (Index nodeIndex : GeometricUtil.rasterizeLine(obstacleSlope.createLine(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH)) {
            TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
            terrainShapeNode.addObstacle(obstacleSlope);
        }
    }


}
