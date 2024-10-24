package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.DoubleHolder;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Beat
 * on 20.11.2017.
 */
public class TerrainDestinationFinder {
    private final DecimalPosition position;
    private final double radius;
    private final TerrainType terrainType;
    private final DecimalPosition destination;
    private final double distance;
    private final TerrainAnalyzer pathingAccess;
    private PathingNodeWrapper pathingNodeWrapper;

    public TerrainDestinationFinder(DecimalPosition position, DecimalPosition destination, double distance, double radius, TerrainType terrainType, TerrainAnalyzer pathingAccess) {
        this.position = position;
        this.radius = radius;
        this.terrainType = terrainType;
        this.destination = destination;
        this.distance = distance;
        this.pathingAccess = pathingAccess;
    }

    public void find() {
        DoubleHolder<DecimalPosition, Double> result = new DoubleHolder<>();
        calculatePosition(pathingAccess, distance, destination, radius, terrainType, position -> {
            if (result.getO1() == null) {
                result.setO1(position);
                result.setO2(position.getDistance(this.destination));
            } else {
                double distance = position.getDistance(this.destination);
                if (distance < result.getO2()) {
                    result.setO1(position);
                    result.setO2(distance);
                }
            }
            return true;
        });
        if (result.getO1() == null) {
            throw new IllegalArgumentException("TerrainDestinationFinder.find(): no reachable terrain destination found. position: " + position + " destination: " + destination + " distance: " + distance + " radius: " + radius + " terrainType: " + terrainType);
        }
        DecimalPosition reachableDestination = result.getO1();
        pathingNodeWrapper = pathingAccess.getPathingNodeWrapper(reachableDestination);
    }

    public PathingNodeWrapper getReachableNode() {
        return pathingNodeWrapper;
    }

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

    private static void calculatePosition(TerrainAnalyzer pathingAccess, double distance, DecimalPosition destinationPosition, double radius, TerrainType terrainType, Predicate<DecimalPosition> callback) {
        List<Index> indices = GeometricUtil.rasterizeCircle(new Circle2D(new DecimalPosition(0, 0), distance - 2.0 * TerrainUtil.NODE_SIZE), (int) TerrainUtil.NODE_SIZE);
        Collection<Index> allowedNodeIndices = new ArrayList<>();
        Index destination = TerrainUtil.terrainPositionToNodeIndex(destinationPosition);
        for (Index index : indices) {
            Index scanPosition = index.add(destination);
            if (pathingAccess.isTerrainTypeAllowed(terrainType, scanPosition)) {
                allowedNodeIndices.add(scanPosition);
            }
        }

        for (Index allowedPosition : allowedNodeIndices) {
            PathingNodeWrapper pathingNodeWrapper = pathingAccess.getPathingNodeWrapper(allowedPosition);
            if (pathingNodeWrapper.isFree(terrainType)) {
                DecimalPosition aStarPosition = pathingNodeWrapper.getCenter();
                if (pathingAccess.isTerrainTypeAllowed(terrainType, aStarPosition, radius)) {
                    if (!callback.test(aStarPosition)) {
                        return;
                    }
                }
            }
        }
    }
}
