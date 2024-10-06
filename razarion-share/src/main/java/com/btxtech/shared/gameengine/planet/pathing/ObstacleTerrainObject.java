package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;

import java.util.Objects;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleTerrainObject extends Obstacle {
    private Circle2D circle;

    public ObstacleTerrainObject(Circle2D circle) {
        this.circle = circle;
    }

    public Circle2D getCircle() {
        return circle;
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
