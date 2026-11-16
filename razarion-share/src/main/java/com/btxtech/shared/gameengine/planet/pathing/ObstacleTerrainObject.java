package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;

import java.util.Objects;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleTerrainObject extends Obstacle {
    /** See {@link #isPlant()}. */
    public static final double PLANT_MAX_RADIUS = 1.0;
    private final Circle2D circle;

    public ObstacleTerrainObject(Circle2D circle) {
        this.circle = circle;
    }

    public Circle2D getCircle() {
        return circle;
    }

    /**
     * Small enough to be a plant rather than a tree or a rock: ferns, banana plants and palm bushes on
     * PLANET 117 are 0.4-0.85 m, trees and rocks from 1.5 m on.
     */
    public boolean isPlant() {
        return circle.getRadius() < PLANT_MAX_RADIUS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObstacleTerrainObject that = (ObstacleTerrainObject) o;
        return Objects.equals(circle, that.circle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(circle);
    }

    @Override
    public String toString() {
        return "ObstacleTerrainObject{" +
                "circle=" + circle +
                '}';
    }
}
