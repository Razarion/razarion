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
    private static final double MIN_DELTA = 0.1;

    public static void addObstacles(TerrainShape terrainShape, Slope slope) {
        for (List<DecimalPosition> polygon : slope.getObstacleFactoryContext().getPolygons()) {
            List<DecimalPosition> filteredPolygon = DecimalPosition.removeSimilarPointsFast(polygon, MIN_DELTA);
            // dumpPolygonForRvo(filteredPolygon);
            for (int i = 0; i < filteredPolygon.size(); i++) {
                DecimalPosition previous = CollectionUtils.getCorrectedElement(i - 1, filteredPolygon);
                DecimalPosition point1 = filteredPolygon.get(i);
                DecimalPosition point2 = CollectionUtils.getCorrectedElement(i + 1, filteredPolygon);
                DecimalPosition next = CollectionUtils.getCorrectedElement(i + 2, filteredPolygon);
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


    private static void dumpPolygonForRvo(List<DecimalPosition> polygon) {
        System.out.println("-------------------------------------------");
        System.out.print("Arrays.asList(");
        polygon.forEach(position -> {
            System.out.print("new Vector2D(" + position.getX() + ", " + position.getY() + "),");
        });
        System.out.println(")");
    }

}
