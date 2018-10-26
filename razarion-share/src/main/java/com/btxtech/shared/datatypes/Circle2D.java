package com.btxtech.shared.datatypes;

import com.btxtech.shared.system.debugtool.DebugHelper;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Beat
 * 05.09.2016.
 */
public class Circle2D {
    private DecimalPosition center;
    private double radius;

    public Circle2D(DecimalPosition center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public List<Vertex> triangulation(int segmentCount, double z) {
        List<Vertex> triangles = new ArrayList<>();
        double segmentAngle = MathHelper.ONE_RADIANT / (double) segmentCount;
        for (int i = 0; i < segmentCount; i++) {
            triangles.add(new Vertex(center, z));
            triangles.add(new Vertex(center.getPointWithDistance(i * segmentAngle, radius), z));
            triangles.add(new Vertex(center.getPointWithDistance((i + 1) * segmentAngle, radius), z));
        }
        return triangles;
    }

    public double getDistance(DecimalPosition point) {
        return center.getDistance(point) - radius;
    }

    public DecimalPosition project(DecimalPosition point) {
        return center.getPointWithDistance(radius, point, true);
    }

    public DecimalPosition getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public Circle2D translate(DecimalPosition translation) {
        return new Circle2D(center.add(translation), radius);
    }

    public boolean doesLineCut(Line line) {
        double distanceP1 = line.getPoint1().getDistance(center);
        if (distanceP1 < radius) {
            return true;
        }
        double distanceP2 = line.getPoint2().getDistance(center);
        if (distanceP2 < radius) {
            return true;
        }

        DecimalPosition nearerPoint;
        double nearerDistance;
        DecimalPosition fartherPoint;
        if (distanceP1 < distanceP2) {
            nearerPoint = line.getPoint1();
            fartherPoint = line.getPoint2();
            nearerDistance = distanceP1;
        } else {
            nearerPoint = line.getPoint2();
            fartherPoint = line.getPoint1();
            nearerDistance = distanceP2;
        }

        double minCircleAngle;
        if (MathHelper.compareWithPrecision(nearerDistance - radius, 0.0)) {
            minCircleAngle = MathHelper.QUARTER_RADIANT;
        } else {
            minCircleAngle = MathHelper.normaliseAngle(Math.asin(radius / nearerDistance));
        }

        double lineAngle = nearerPoint.getAngle(fartherPoint);
        double lineCenterAngle = nearerPoint.getAngle(center);
        double insideAngle = MathHelper.normaliseAngle(MathHelper.getAngle(lineAngle, lineCenterAngle));

        return insideAngle < minCircleAngle;
    }

    public boolean intersects(Rectangle2D rectangle2D) {
        if (rectangle2D.contains(center)) {
            return true;
        }
        DecimalPosition nearestPointOnRect = rectangle2D.getNearestPoint(center);

        return nearestPointOnRect.getDistance(center) <= radius;
    }

    public boolean isIntersect(Circle2D circle2D) {
        return center.getDistance(circle2D.getCenter()) <= radius + circle2D.getRadius();
    }

    public boolean inside(DecimalPosition position) {
        return position.getDistance(center) < radius;
    }

    public boolean inside(Rectangle2D rectangle) {
        for (DecimalPosition corner : rectangle.toCorners()) {
            if (!inside(corner)) {
                return false;
            }
        }
        return true;
    }

    public InsideCheckResult checkInside(Rectangle2D rectangle) {
        if (inside(rectangle)) {
            return InsideCheckResult.INSIDE;
        }
        if (intersects(rectangle)) {
            return InsideCheckResult.PARTLY;
        } else {
            return InsideCheckResult.OUTSIDE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Circle2D circle2D = (Circle2D) o;
        return Double.compare(circle2D.radius, radius) == 0 &&
                Objects.equals(center, circle2D.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, radius);
    }
}
