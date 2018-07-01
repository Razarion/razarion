package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;
import com.btxtech.shared.utils.MathHelper;

import java.util.Objects;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleSlope extends Obstacle {
    private DecimalPosition point1;
    private DecimalPosition point2;
    private DecimalPosition previous;
    private DecimalPosition next;
    private Line cachedLine;

    public ObstacleSlope(DecimalPosition point1, DecimalPosition point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    public ObstacleSlope(DecimalPosition point1, DecimalPosition point2, DecimalPosition previous, DecimalPosition next) {
        this.point1 = point1;
        this.point2 = point2;
        this.previous = previous;
        this.next = next;
    }

    public void initPrevious(ObstacleSlope previousObstacleSlope) {
        previous = previousObstacleSlope.point1;
    }

    public void initNext(ObstacleSlope nextObstacleSlope) {
        next = nextObstacleSlope.point2;
    }

    @Override
    public boolean isPiercing(Line line) {
        return createLine().getCrossInclusive(line) != null;
    }

    @Override
    public boolean isIntersect(Circle2D circle2D) {
        return circle2D.doesLineCut(createLine());
    }

    public Line createLine() {
        if (cachedLine == null) {
            cachedLine = new Line(point1, point2);
        }
        return cachedLine;
    }

    public DecimalPosition getPoint1() {
        return point1;
    }

    public DecimalPosition getPoint2() {
        return point2;
    }

    public DecimalPosition getPrevious() {
        return previous;
    }

    public DecimalPosition getNext() {
        return next;
    }

    public boolean isPoint1Convex() {
        return point1.angle(point2, previous) < MathHelper.HALF_RADIANT;
    }

    public boolean isPoint2Convex() {
        return point2.angle(next, point1) < MathHelper.HALF_RADIANT;
    }

    public DecimalPosition setupDirection() {
        return point2.sub(point1).normalize();
    }

    public DecimalPosition setupPreviousDirection() {
        return next.sub(point2).normalize();
    }

    public DecimalPosition setupNextDirection() {
        return point1.sub(previous).normalize();
    }

    @Override
    public NativeObstacle toNativeObstacle() {
        NativeObstacle nativeObstacle = new NativeObstacle();
        nativeObstacle.x1 = point1.getX();
        nativeObstacle.y1 = point1.getY();
        nativeObstacle.x2 = point2.getX();
        nativeObstacle.y2 = point2.getY();
        nativeObstacle.xP = previous.getX();
        nativeObstacle.yP = previous.getY();
        nativeObstacle.xN = next.getX();
        nativeObstacle.yN = next.getY();
        return nativeObstacle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObstacleSlope that = (ObstacleSlope) o;
        return Objects.equals(point1, that.point1) &&
                Objects.equals(point2, that.point2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point1, point2);
    }

    @Override
    public String toString() {
        return "ObstacleSlope{" +
                "point1=" + point1 +
                ", point2=" + point2 +
                ", previous=" + previous +
                ", next=" + next +
                '}';
    }
}
