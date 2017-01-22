package com.btxtech.shared.gameengine.planet.pathing;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 21.01.2017.
 */
public class ObstacleContainerTile {
    private Collection<Obstacle> obstacles;

    public void addObstacle(Obstacle obstacle) {
        if (obstacles == null) {
            obstacles = new ArrayList<>();
        }
        obstacles.add(obstacle);
    }

    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }
}
