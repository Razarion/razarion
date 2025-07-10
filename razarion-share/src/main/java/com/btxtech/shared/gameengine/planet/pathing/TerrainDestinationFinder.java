package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.DoubleHolder;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

/**
 * Created by Beat
 * on 20.11.2017.
 */
public class TerrainDestinationFinder {
    private final double radius;
    private final TerrainType terrainType;
    private final DecimalPosition destination;
    private final double distance;
    private final TerrainAnalyzer terrainAnalyzer;
    private PathingNodeWrapper pathingNodeWrapper;

    public TerrainDestinationFinder(DecimalPosition destination, double distance, double radius, TerrainType terrainType, TerrainAnalyzer terrainAnalyzer) {
        this.radius = radius;
        this.terrainType = terrainType;
        this.destination = destination;
        this.distance = distance;
        this.terrainAnalyzer = terrainAnalyzer;
    }

    public void find() {
        DoubleHolder<DecimalPosition, Double> result = new DoubleHolder<>();
        TerrainDestinationFinderUtil.calculatePosition(terrainAnalyzer, distance, destination, radius, terrainType, position -> {
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
            pathingNodeWrapper = null;
            return;
        }
        DecimalPosition reachableDestination = result.getO1();
        pathingNodeWrapper = terrainAnalyzer.getPathingNodeWrapper(reachableDestination);
    }

    public PathingNodeWrapper getReachableNode() {
        return pathingNodeWrapper;
    }
}
