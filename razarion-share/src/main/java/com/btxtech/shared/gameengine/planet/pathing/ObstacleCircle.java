package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleCircle extends Obstacle {
    private Circle2D circle;

    public ObstacleCircle(Circle2D circle) {
        this.circle = circle;
    }

    @Override
    public DecimalPosition project(DecimalPosition point) {
        return circle.project(point);
    }

    @Override
    public boolean isPiercing(Line line) {
        return circle.doesLineCut(line);
    }

    public Circle2D getCircle() {
        return circle;
    }

    @Override
    public String toString() {
        return "ObstacleCircle{" +
                "circle=" + circle +
                '}';
    }
}
