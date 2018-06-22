package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleTerrainObject extends Obstacle {
    private Circle2D circle;

    public ObstacleTerrainObject(Circle2D circle) {
        this.circle = circle;
    }

    @Override
    public boolean isPiercing(Line line) {
        return circle.doesLineCut(line);
    }

    public Circle2D getCircle() {
        return circle;
    }

    @Override
    public NativeObstacle toNativeObstacle() {
        NativeObstacle nativeObstacle = new NativeObstacle();
        nativeObstacle.xC = circle.getCenter().getX();
        nativeObstacle.yC = circle.getCenter().getY();
        nativeObstacle.r = circle.getRadius();
        return nativeObstacle;
    }

    @Override
    public String toString() {
        return "ObstacleTerrainObject{" +
                "circle=" + circle +
                '}';
    }
}
