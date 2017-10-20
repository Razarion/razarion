package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;

/**
 * Created by Beat
 * on 19.10.2017.
 */
public class DifferentTerrainTypeDestinationFinder {
    private final TerrainType terrainType;
    private DecimalPosition targetPosition;
    private final double totalRange;
    private final PathingAccess pathingAccess;

    public DifferentTerrainTypeDestinationFinder(TerrainType terrainType, DecimalPosition targetPosition, double totalRange, PathingAccess pathingAccess) {
        this.terrainType = terrainType;
        this.targetPosition = targetPosition;
        this.totalRange = totalRange;
        this.pathingAccess = pathingAccess;
    }

    public DecimalPosition find() {
        DecimalPosition bestPosition = null;
        double minDistance = Double.MAX_VALUE;
        for (Index subNodeIndex : GeometricUtil.rasterizeCircle(new Circle2D(new DecimalPosition(0, 0), totalRange), (int) TerrainUtil.MIN_SUB_NODE_LENGTH)) {
            DecimalPosition scanPosition = TerrainUtil.smallestSubNodeCenter(subNodeIndex).add(targetPosition);
            if (pathingAccess.isTerrainTypeAllowed(terrainType, scanPosition)) {
                double distance = targetPosition.getDistance(scanPosition);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestPosition = scanPosition;
                }
            }
        }
        return bestPosition;
    }
}
