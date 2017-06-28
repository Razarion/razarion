package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleSlope extends Obstacle {
    private Line line;

    public ObstacleSlope(Line line) {
        this.line = line;
    }

    @Override
    public DecimalPosition project(DecimalPosition point) {
        return line.getNearestPointOnLine(point);
    }

    @Override
    public boolean isPiercing(Line line) {
        return this.line.getCrossInclusive(line) != null;
    }

    public Line getLine() {
        return line;
    }

    @Override
    public NativeObstacle toNativeObstacle() {
        NativeObstacle nativeObstacle = new NativeObstacle();
        nativeObstacle.x1 = line.getPoint1().getX();
        nativeObstacle.y1 = line.getPoint1().getY();
        nativeObstacle.x2 = line.getPoint2().getX();
        nativeObstacle.y2 = line.getPoint2().getY();
        return nativeObstacle;
    }

    @Override
    public String toString() {
        return "ObstacleSlope{" +
                "line=" + line +
                '}';
    }
}
