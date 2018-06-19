package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleSlope extends Obstacle {
    private Line line;
    private DecimalPosition previous;
    private DecimalPosition next;

    public ObstacleSlope(Line line) {
        this.line = line;
    }

    public void setAdditionPints(DecimalPosition previous, DecimalPosition next) {
        this.previous = previous;
        this.next = next;
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

    public boolean isPoint1Convex() {
        return line.getPoint1().angle(line.getPoint2(), previous) < MathHelper.HALF_RADIANT;
    }

    public boolean isPoint2Convex() {
        return line.getPoint2().angle(next, line.getPoint1()) < MathHelper.HALF_RADIANT;
    }

    public DecimalPosition setupDirection() {
        return line.getPoint2().sub(line.getPoint1()).normalize();
    }

    public DecimalPosition setupPreviousDirection() {
        return next.sub(line.getPoint2()).normalize();
    }

    public DecimalPosition setupNextDirection() {
        return line.getPoint1().sub(previous).normalize();
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
