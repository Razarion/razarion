package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;
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

    public ObstacleSlope(NativeObstacle nativeObstacle) {
        point1 = new DecimalPosition(nativeObstacle.x1, nativeObstacle.y1);
        point2 = new DecimalPosition(nativeObstacle.x2, nativeObstacle.y2);
        previousDirection = new DecimalPosition(nativeObstacle.pDx, nativeObstacle.pDy);
        point1Convex = nativeObstacle.p1C;
        point1Direction = new DecimalPosition(nativeObstacle.p1Dx, nativeObstacle.p1Dy);
        point2Convex = nativeObstacle.p2C;
        point2Direction = new DecimalPosition(nativeObstacle.p2Dx, nativeObstacle.p2Dy);
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

    @Override
    public NativeObstacle toNativeObstacle() {
        NativeObstacle nativeObstacle = new NativeObstacle();
        nativeObstacle.x1 = point1.getX();
        nativeObstacle.y1 = point1.getY();
        nativeObstacle.x2 = point2.getX();
        nativeObstacle.y2 = point2.getY();
        nativeObstacle.pDx = previousDirection.getX();
        nativeObstacle.pDy = previousDirection.getY();
        nativeObstacle.p1C = point1Convex;
        nativeObstacle.p1Dx = point1Direction.getX();
        nativeObstacle.p1Dy = point1Direction.getY();
        nativeObstacle.p2C = point2Convex;
        nativeObstacle.p2Dx = point2Direction.getX();
        nativeObstacle.p2Dy = point2Direction.getY();
        return nativeObstacle;
    }

    public static boolean isValidNative(NativeObstacle nativeObstacle) {
        return nativeObstacle.x1 != null && nativeObstacle.y1 != null && nativeObstacle.x2 != null && nativeObstacle.y2 != null
                && nativeObstacle.pDx != null && nativeObstacle.pDy != null && nativeObstacle.p1C != null
                && nativeObstacle.p1Dx != null && nativeObstacle.p1Dy != null && nativeObstacle.p2C != null && nativeObstacle.p2Dx != null && nativeObstacle.p2Dy != null;
    }

    public static void sort(DecimalPosition pivot, List<ObstacleSlope> obstacleSlopes) {
        obstacleSlopes.sort(Comparator.comparingDouble(o -> pivot.getDistance(o.getNearestPoint(pivot))));
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
