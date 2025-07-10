package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class TerrainDestinationFinderUtil {
    public static boolean isAllowed(TerrainAnalyzer pathingAccess, double distance, DecimalPosition targetPosition, double radius, TerrainType actorTerrainType, TerrainType targetTerrainType) {
        if (!differentTerrain(actorTerrainType, targetTerrainType)) {
            return true;
        }
        SingleHolder<Boolean> result = new SingleHolder<>(false);
        calculatePosition(pathingAccess, distance, targetPosition, radius, actorTerrainType, position -> {
            result.setO(true);
            return false;
        });
        return result.getO();
    }

    public static boolean differentTerrain(TerrainType actor, TerrainType target) {
        return actor != target;
    }

    static void calculatePosition(TerrainAnalyzer terrainAnalyzer,
                                  double distance,
                                  DecimalPosition destinationPosition,
                                  double radius,
                                  TerrainType terrainType,
                                  Predicate<DecimalPosition> callback) {
        List<Index> indices = GeometricUtil.rasterizeCircle(new Circle2D(new DecimalPosition(0, 0), distance - 2.0 * TerrainUtil.NODE_SIZE), (int) TerrainUtil.NODE_SIZE);
        Collection<Index> allowedNodeIndices = new ArrayList<>();
        Index destination = TerrainUtil.terrainPositionToNodeIndex(destinationPosition);
        for (Index index : indices) {
            Index scanPosition = index.add(destination);
            if (terrainAnalyzer.isTerrainTypeAllowed(terrainType, scanPosition)) {
                allowedNodeIndices.add(scanPosition);
            }
        }

        for (Index allowedIndex : allowedNodeIndices) {
            PathingNodeWrapper pathingNodeWrapper = terrainAnalyzer.getPathingNodeWrapper(allowedIndex);
            if (pathingNodeWrapper.isFree(terrainType)) {
                DecimalPosition aStarPosition = pathingNodeWrapper.getCenter();
                if (terrainAnalyzer.isTerrainTypeAllowed(terrainType, aStarPosition, radius)) {
                    if (!callback.test(aStarPosition)) {
                        return;
                    }
                }
            }
        }
    }
}
