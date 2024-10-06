package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.utils.MathHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleSlope extends Obstacle {
    private DecimalPosition point1;
    private DecimalPosition point2;
    private DecimalPosition previousDirection;
    private boolean point1Convex;
    private DecimalPosition point1Direction;
    private boolean point2Convex;
    private DecimalPosition point2Direction;
    private Line cachedLine;

    public ObstacleSlope(DecimalPosition point1, DecimalPosition point2, DecimalPosition previous, DecimalPosition next) {
        this.point1 = point1;
        this.point2 = point2;
        previousDirection = point1.sub(previous).normalize();
        point1Convex = point1.angle(point2, previous) <= MathHelper.HALF_RADIANT;
        point1Direction = point2.sub(point1).normalize();
        point2Convex = point2.angle(next, point1) <= MathHelper.HALF_RADIANT;
        point2Direction = next.sub(point2).normalize();
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

    public boolean isPoint1Convex() {
        return point1Convex;
    }

    public boolean isPoint2Convex() {
        return point2Convex;
    }

    public boolean isOutside(DecimalPosition position) {
        return point1.sub(position).determinant(point2.sub(point1)) >= 0.0;
    }

    public DecimalPosition getPoint1Direction() {
        return point1Direction;
    }

    public DecimalPosition getPoint2Direction() {
        return point2Direction;
    }

    public DecimalPosition getPreviousDirection() {
        return previousDirection;
    }

    public DecimalPosition getNearestPoint(DecimalPosition position) {
        return createLine().getNearestPointOnLine(position);
    }

    public static void sortObstacleSlope(DecimalPosition pivot, List<ObstacleSlope> obstacleSlopes) {
        obstacleSlopes.sort(Comparator.comparingDouble(o -> pivot.getDistance(o.getNearestPoint(pivot))));
    }

    public static void sortObstacleTerrainObject(DecimalPosition pivot, List<ObstacleTerrainObject> obstacleSlopes) {
        obstacleSlopes.sort(Comparator.comparingDouble(o -> o.getCircle().getDistance(pivot)));
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
                ", previousDirection=" + previousDirection +
                ", point1Convex=" + point1Convex +
                ", point1Direction=" + point1Direction +
                ", point2Convex=" + point2Convex +
                ", point2Direction=" + point2Direction +
                '}';
    }
}
