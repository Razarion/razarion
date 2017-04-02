package com.btxtech.shared.gameengine.planet.pathing;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 21.01.2017.
 */
public class ObstacleContainerNode {
    private Collection<Obstacle> obstacles;
    private Double slopHeight;
    private boolean belongsToSlope;

    public void addObstacle(Obstacle obstacle) {
        if (obstacles == null) {
            obstacles = new ArrayList<>();
        }
        obstacles.add(obstacle);
    }

    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }

    public Double getSlopHeight() {
        return slopHeight;
    }

    public void setSlopHeight(Double slopHeight) {
        this.slopHeight = slopHeight;
    }

    public boolean isInSlope() {
        if (belongsToSlope) {
            return true;
        }
        if (obstacles == null) {
            return false;
        }

        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof ObstacleSlope) {
                return true;
            }
        }
        return false;
    }

    public void setBelongsToSlope() {
        belongsToSlope = true;
    }
}
