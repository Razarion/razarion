package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.DoubleHolder;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
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
    private DecimalPosition position;
    private double radius;
    private TerrainType terrainType;
    private DecimalPosition destination;
    private double distance;
    private PathingAccess pathingAccess;
    private DecimalPosition reachableDestination;
    private PathingNodeWrapper pathingNodeWrapper;

    public TerrainDestinationFinder(DecimalPosition position, DecimalPosition destination, double distance, double radius, TerrainType terrainType, PathingAccess pathingAccess) {
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
                result.setO2(position.getDistance(this.position));
            } else {
                double distance = position.getDistance(this.position);
                if (distance < result.getO2()) {
                    result.setO1(position);
                    result.setO2(distance);
                }
            }
            return false;
        });
        if (result.getO1() == null) {
            throw new IllegalArgumentException("TerrainDestinationFinder.find(): no reachable terrain destination found");
        }
        reachableDestination = result.getO1();
        pathingNodeWrapper = pathingAccess.getPathingNodeWrapper(reachableDestination);
    }

    public DecimalPosition getReachableDestination() {
        return reachableDestination;
    }

    public PathingNodeWrapper getReachableNode() {
        return pathingNodeWrapper;
    }

    public static boolean isAllowed(PathingAccess pathingAccess, double distance, DecimalPosition targetPosition, double radius, TerrainType actorTerrainType, TerrainType targetTerrainType) {
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

    private static void calculatePosition(PathingAccess pathingAccess, double distance, DecimalPosition destination, double radius, TerrainType terrainType, Predicate<DecimalPosition> callback) {
        List<Index> indices = GeometricUtil.rasterizeCircle(new Circle2D(new DecimalPosition(0, 0), distance - 2.0 * TerrainUtil.MIN_SUB_NODE_LENGTH), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        Collection<DecimalPosition> allowedPositions = new ArrayList<>();
        for (Index index : indices) {
            DecimalPosition scanPosition = TerrainUtil.smallestSubNodeAbsolute(index).add(destination);
            if (pathingAccess.isTerrainTypeAllowed(terrainType, scanPosition)) {
                allowedPositions.add(scanPosition);
            }
        }

        for (DecimalPosition allowedPosition : allowedPositions) {
            if (pathingAccess.isTerrainTypeAllowed(terrainType, allowedPosition, radius)) {
                if (!callback.test(allowedPosition)) {
                    return;
                }
                return;
            }
        }
    }
}