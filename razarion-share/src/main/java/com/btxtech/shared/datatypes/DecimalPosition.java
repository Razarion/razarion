/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.MathHelper;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * User: beat
 * Date: 03.10.2010
 * Time: 13:01:16
 */
@Portable
public class DecimalPosition {
    public static final DecimalPosition NULL = new DecimalPosition(0, 0);
    private double x;
    private double y;

    /**
     * Used by GWT
     */
    public DecimalPosition() {
    }

    public DecimalPosition(double x, double y) {
        if (Double.isInfinite(x) || Double.isNaN(x)) {
            throw new IllegalArgumentException("Can not set x value in DecimalPosition: " + x);
        }
        if (Double.isInfinite(y) || Double.isNaN(y)) {
            throw new IllegalArgumentException("Can not set y value in DecimalPosition: " + y);
        }
        this.x = x;
        this.y = y;
    }

    public DecimalPosition(Index position) {
        this(position.getX(), position.getY());
    }

    public DecimalPosition(DecimalPosition position) {
        x = position.x;
        y = position.y;
    }

    public Index getPositionRound() {
        return new Index((int) Math.round(x), (int) Math.round(y));
    }

    public Index getPosition() {
        return new Index((int) x, (int) y);
    }

    public DecimalPosition getPointWithDistance(double distance, Index directionTo, boolean allowOverrun) {
        double directionDistance = getDistance(directionTo);
        if (!allowOverrun && directionDistance <= distance) {
            return new DecimalPosition(directionTo);
        }
        double dirDeltaX = (double) directionTo.getX() - x;
        double dirDeltaY = (double) directionTo.getY() - y;
        double deltaX = (dirDeltaX * distance) / directionDistance;
        double deltaY = (dirDeltaY * distance) / directionDistance;
        return new DecimalPosition(x + deltaX, y + deltaY);
    }

    public DecimalPosition getPointWithDistance(double distance, DecimalPosition directionTo, boolean allowOverrun) {
        double directionDistance = getDistance(directionTo);
        if (directionDistance == 0.0) {
            throw new IllegalArgumentException("Point are equals. This: " + this + " directionTo: " + directionTo);
        }
        if (!allowOverrun && directionDistance <= distance) {
            return new DecimalPosition(directionTo);
        }
        double dirDeltaX = directionTo.getX() - x;
        double dirDeltaY = directionTo.getY() - y;
        double deltaX = (dirDeltaX * distance) / directionDistance;
        double deltaY = (dirDeltaY * distance) / directionDistance;
        return new DecimalPosition(x + deltaX, y + deltaY);
    }

    public DecimalPosition getPointFromAngelToNord(double angle, double radius) {
        double gk = Math.sin(angle) * radius;
        double ak = Math.cos(angle) * radius;
        return new DecimalPosition(x + ak, y + gk);
    }

    /**
     * Angle from this point to the given point.
     * If the given point is exactly in the north, the angle is 0.0
     * If the given point is exactly in the west, the angle is PI/2 90dec
     * If the given point is exactly in the south, the angle is PI 180dec
     * If the given point is exactly in the east, the angle is 3/2 * PI 270dec
     * <p/>
     * The returned angle is between 0 and 2Pi
     *
     * @param point end point
     * @return positive angle
     */
    @Deprecated
    public double getAngleToNord(DecimalPosition point) {
        if (equals(point)) {
            throw new IllegalArgumentException("Points are equal");
        }
        // Computer screen have different axis than the cartesian coordinate system
        double gk = x - point.x;
        double ak = y - point.y;
        if (ak == 0.0) {
            if (gk > 0.0) {
                return MathHelper.WEST;
            } else {
                return MathHelper.EAST;
            }
        }
        if (gk == 0.0) {
            if (ak > 0.0) {
                return MathHelper.NORTH;
            } else {
                return MathHelper.SOUTH;
            }
        }
        double angle = Math.atan(gk / ak);
        if (ak < 0.0) {
            angle += MathHelper.HALF_RADIANT;
        } else if (gk < 0.0) {
            angle += MathHelper.ONE_RADIANT;
        }
        return angle;
    }

    /**
     * Angle from this point to the given point.
     * If the given point is exactly in the east, the angle is 0.0
     * If the given point is exactly in the north, the angle is PI/2 90dec
     * If the given point is exactly in the west, the angle is PI 180dec
     * If the given point is exactly in the south, the angle is 3/2 * PI 270dec
     * <p/>
     * The returned angle is between 0 and 2Pi
     *
     * @param point end point
     * @return positive angle
     */
    public double getAngleToNorth(DecimalPosition point) {
        if (equals(point)) {
            throw new IllegalArgumentException("Points are equal");
        }
        DecimalPosition normalized = point.sub(this);

        if (normalized.y == 0.0) {
            if (normalized.x > 0.0) {
                return 0;
            } else {
                return MathHelper.HALF_RADIANT;
            }
        }
        if (normalized.x == 0.0) {
            if (normalized.y > 0.0) {
                return MathHelper.QUARTER_RADIANT;
            } else {
                return MathHelper.THREE_QUARTER_RADIANT;
            }
        }
        if (normalized.x > 0 && normalized.y > 0) {
            // Quadrant 1
            return Math.atan(normalized.y / normalized.x);
        } else if (normalized.x < 0 && normalized.y > 0) {
            // Quadrant 2
            return MathHelper.QUARTER_RADIANT + Math.atan(-normalized.x / normalized.y);
        } else if (normalized.x < 0 && normalized.y < 0) {
            // Quadrant 3
            return MathHelper.HALF_RADIANT + Math.atan(-normalized.y / -normalized.x);
        } else {
            // Quadrant 4
            return MathHelper.THREE_QUARTER_RADIANT + Math.atan(normalized.x / -normalized.y);
        }
    }

    public double getAngle() {
        return Math.atan2(y, x);
    }

    public double getAngleToNorth() {
        if (x == 0.0 && y == 0.0) {
            return MathHelper.NORTH;
        }
        if (y == 0.0) {
            if (x <= 0.0) {
                return MathHelper.WEST;
            } else {
                return MathHelper.EAST;
            }
        }
        if (x == 0.0) {
            if (y <= 0.0) {
                return 0.0;
            } else {
                return Math.PI;
            }
        }
        double angle = Math.atan(x / y);
        if (y >= 0.0) {
            angle += Math.PI;
        }
        return angle;
    }

    public DecimalPosition rotateCounterClock(DecimalPosition center, double sinus, double cosines) {
        double normX = center.x - x;
        double normY = center.y - y;
        double newX = -normX * cosines - normY * sinus;
        double newY = -normY * cosines + normX * sinus;
        return new DecimalPosition(center.x + newX, center.y + newY);
    }

    public DecimalPosition rotateCounterClock(DecimalPosition center, double angel) {
        double sinus = Math.sin(angel);
        double cosines = Math.cos(angel);
        return rotateCounterClock(center, sinus, cosines);
    }

    public DecimalPosition add(double weight, DecimalPosition otherDecimalPosition) {
        return new DecimalPosition(x + weight * otherDecimalPosition.x, y + weight * otherDecimalPosition.y);
    }

    public DecimalPosition add(DecimalPosition point) {
        return new DecimalPosition(x + point.x, y + point.y);
    }

    public DecimalPosition add(double deltaX, double deltaY) {
        return new DecimalPosition(x + deltaX, y + deltaY);
    }

    public DecimalPosition sub(DecimalPosition point) {
        return new DecimalPosition(x - point.x, y - point.y);
    }

    public DecimalPosition sub(double deltaX, double deltaY) {
        return new DecimalPosition(x - deltaX, y - deltaY);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public DecimalPosition normalize(double basis) {
        double m = magnitude();
        if (m == 0.0) {
            return getCopy();
        }
        return multiply(basis / m);
    }

    public DecimalPosition truncate(double basis) {
        if (getLength() > basis) {
            return normalize(basis);
        } else {
            return this;
        }
    }


    public DecimalPosition normalize() {
        return normalize(1);
    }

    public DecimalPosition divide(double m) {
        if (m == 0.0) {
            throw new ArithmeticException("Divided by 0");
        }
        return new DecimalPosition(x / m, y / m);
    }

    public DecimalPosition divide(double x, double y) {
        if (x == 0.0) {
            throw new ArithmeticException("Divided by 0 (x)");
        }
        if (y == 0.0) {
            throw new ArithmeticException("Divided by 0 (y)");
        }
        return new DecimalPosition(this.x / x, this.y / y);
    }

    public DecimalPosition divide(DecimalPosition divider) {
        if (divider.x == 0.0) {
            throw new ArithmeticException("Divided by 0 (x)");
        }
        if (divider.y == 0.0) {
            throw new ArithmeticException("Divided by 0 (y)");
        }
        return new DecimalPosition(this.x / divider.x, this.y / divider.y);
    }

    public DecimalPosition multiply(double x, double y) {
        return new DecimalPosition(this.x * x, this.y * y);
    }

    public DecimalPosition multiply(double m) {
        return new DecimalPosition(x * m, y * m);
    }

    public double getDistance(DecimalPosition decimalPosition) {
        double sqrtC = Math.pow(decimalPosition.x - x, 2) + Math.pow(decimalPosition.y - y, 2);
        return Math.sqrt(sqrtC);
    }

    public double getDistance(Index index) {
        double sqrtC = Math.pow((double) index.getX() - x, 2) + Math.pow((double) index.getY() - y, 2);
        return Math.sqrt(sqrtC);
    }

    public double getLength() {
        double sqrtC = Math.pow(x, 2) + Math.pow(y, 2);
        return Math.sqrt(sqrtC);
    }

    public DecimalPosition getCopy() {
        return new DecimalPosition(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isNull() {
        return x == 0.0 && y == 0.0;
    }

    public double determinant(DecimalPosition other) {
        return x * other.y - y * other.x;
    }

    public double dotProduct(DecimalPosition other) {
        return x * other.x + y * other.y;
    }

    public DecimalPosition negate() {
        return new DecimalPosition(-x, -y);
    }

    /**
     * Angle from the given start to the end point with this point as center. The direction is counter clock wise.
     * The returned angle is between 0 and 2Pi
     *
     * @param start start point
     * @param end   end point
     * @return positive angle
     */
    public double getAngle(DecimalPosition start, DecimalPosition end) {
//        start = start.sub(this);
//        end = end.sub(this);
//        return Math.acos(start.dotProduct(end) / (start.magnitude() * end.magnitude()));
        double startAngle = getAngleToNorth(start);
        double endAngle = getAngleToNorth(end);
        return MathHelper.normaliseAngel(endAngle - startAngle);
    }

    public double getIncludedAngle(DecimalPosition point1, DecimalPosition point2) {
        double angle1 = getAngleToNord(point1);
        double angle2 = getAngleToNord(point2);
        return MathHelper.getAngel(angle1, angle2);
    }

    /**
     * The Z value of the vector that would result from a regular 3D cross product of the input vectors,
     * taking their Z values implicitly as 0 (i.e. treating the 2D space as a plane in the 3D space).
     *
     * @param a index a
     * @param b index b
     * @return the magnitude
     */
    public double cross(DecimalPosition a, DecimalPosition b) {
        return (a.getX() - x) * (b.getY() - y) - (a.getY() - y) * (b.getX() - x);
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y;
    }

    public static DecimalPosition createVector(double angle, double distance) {
        return NULL.getPointFromAngelToNord(angle, distance);
    }

    public static DecimalPosition getNearestPoint(DecimalPosition position, Collection<DecimalPosition> intersections) {
        double minDistance = Double.MAX_VALUE;
        DecimalPosition result = null;
        for (DecimalPosition intersection : intersections) {
            double distance = position.getDistance(intersection);
            if (distance < minDistance) {
                minDistance = distance;
                result = intersection;
            }
        }
        return result;
    }

    public static DecimalPosition getFarestPoint(DecimalPosition position, Collection<DecimalPosition> intersections) {
        double maxDistance = 0;
        DecimalPosition result = null;
        for (DecimalPosition intersection : intersections) {
            double distance = position.getDistance(intersection);
            if (distance > maxDistance) {
                maxDistance = distance;
                result = intersection;
            }
        }
        return result;
    }

//    public static List<DecimalPosition> removeBehindPoints(DecimalPosition origin, Collection<DecimalPosition> points) {
//        List<DecimalPosition> pointsOuter = new ArrayList<>(points);
//        List<DecimalPosition> result = new ArrayList<>();
//        while (!pointsOuter.isEmpty()) {
//            DecimalPosition outerPoint = pointsOuter.remove(0);
//            List<DecimalPosition> tmpPoints = new ArrayList<>();
//            tmpPoints.add(outerPoint);
//            double angle = origin.getAngleToNord(outerPoint);
//            List<DecimalPosition> pointsInner = new ArrayList<>(pointsOuter);
//            while (!pointsInner.isEmpty()) {
//                DecimalPosition innerPoint = pointsInner.remove(0);
//                double otherAngle = origin.getAngleToNord(innerPoint);
//                if (MathHelper.compareWithPrecision(angle, otherAngle, CollisionConstants.SAFETY_DISTANCE)) {
//                    tmpPoints.add(innerPoint);
//                    pointsOuter.remove(innerPoint);
//                }
//            }
//            result.add(DecimalPosition.getNearestPoint(origin, tmpPoints));
//        }
//        return result;
//    }

    public static Collection<DecimalPosition> removeSimilarPoints(Collection<DecimalPosition> points, double minDistance) {
        Collection<DecimalPosition> acceptedPoints = new ArrayList<>();
        for (DecimalPosition point : points) {
            boolean accepted = true;
            for (DecimalPosition acceptedPoint : acceptedPoints) {
                if (point.getDistance(acceptedPoint) < minDistance) {
                    accepted = false;
                    break;
                }
            }
            if (accepted) {
                acceptedPoints.add(point);
            }
        }
        return acceptedPoints;
    }

    public static DecimalPosition getSmallestAabb(DecimalPosition... decimalPositions) {
        if (decimalPositions.length == 0) {
            throw new IllegalArgumentException();
        }
        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        for (DecimalPosition decimalPosition : decimalPositions) {
            x = Math.min(decimalPosition.getX(), x);
            y = Math.min(decimalPosition.getX(), y);
        }
        return new DecimalPosition(x, y);
    }

    public static DecimalPosition getBiggestAabb(DecimalPosition... decimalPositions) {
        if (decimalPositions.length == 0) {
            throw new IllegalArgumentException();
        }
        double x = Double.MIN_VALUE;
        double y = Double.MIN_VALUE;
        for (DecimalPosition decimalPosition : decimalPositions) {
            x = Math.max(decimalPosition.getX(), x);
            y = Math.max(decimalPosition.getX(), y);
        }
        return new DecimalPosition(x, y);
    }

    public boolean equalsDeltaZero() {
        return MathHelper.compareWithPrecision(x, 0) && MathHelper.compareWithPrecision(y, 0);
    }

    public boolean equalsDelta(DecimalPosition other) {
        return MathHelper.compareWithPrecision(x, other.x) && MathHelper.compareWithPrecision(y, other.y);
    }

    public boolean equalsDelta(DecimalPosition other, double delta) {
        return MathHelper.compareWithPrecision(x, other.x, delta) && MathHelper.compareWithPrecision(y, other.y, delta);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DecimalPosition that = (DecimalPosition) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Use InstanceStringGenerator
     */
    @Deprecated
    public String testString() {
        return "new DecimalPosition(" + x + ", " + y + ")";
    }

    public static String testString(List<DecimalPosition> indices) {
        StringBuilder builder = new StringBuilder("Arrays.asList(");
        for (int i = 0; i < indices.size(); i++) {
            DecimalPosition index = indices.get(i);
            builder.append(index.testString());
            if (i + 1 < indices.size()) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }

    public static class DecimalPositionComparator implements Comparator<DecimalPosition> {
        @Override
        public int compare(DecimalPosition o1, DecimalPosition o2) {
            if (o1.x == o2.x) {
                return Double.compare(o1.y, o2.y);
            } else {
                return Double.compare(o1.x, o2.x);
            }
        }
    }

}
