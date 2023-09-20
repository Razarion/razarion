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

import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: beat
 * Date: 03.10.2010
 * Time: 13:01:16
 */
@JsType
@Embeddable
public class DecimalPosition {
    public static final DecimalPosition NULL = new DecimalPosition(0, 0);
    public static final DecimalPosition EAST = new DecimalPosition(1, 0);
    private double x;
    private double y;

    /**
     * Used by GWT
     */
    @JsIgnore
    public DecimalPosition() {
    }

    @JsIgnore
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

    @JsIgnore
    public DecimalPosition(Index position) {
        this(position.getX(), position.getY());
    }

    @JsIgnore
    public DecimalPosition(DecimalPosition position) {
        x = position.x;
        y = position.y;
    }

    @JsIgnore
    public static DecimalPosition createVector(double angle, double distance) {
        return NULL.getPointWithDistance(angle, distance);
    }

    public static DecimalPosition create(double x, double y) {
        return new DecimalPosition(x, y);
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

    public static DecimalPosition getFurthestPoint(DecimalPosition position, Collection<DecimalPosition> intersections) {
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

    public static List<DecimalPosition> removeSimilarPointsFast(List<DecimalPosition> points, double minDelta) {
        List<DecimalPosition> acceptedPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            DecimalPosition current = points.get(i);
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, points);
            if (!current.equalsDelta(next, minDelta)) {
                acceptedPoints.add(current);
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
            y = Math.min(decimalPosition.getY(), y);
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
            y = Math.max(decimalPosition.getY(), y);
        }
        return new DecimalPosition(x, y);
    }

    public static Collection<DecimalPosition> add(Collection<DecimalPosition> positions, DecimalPosition delta) {
        return positions.stream().map(position -> position.add(delta)).collect(Collectors.toList());
    }

    public static DecimalPosition zeroIfNull(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            return decimalPosition;
        } else {
            return NULL;
        }
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

    public static int getComponentsPerDecimalPosition() {
        return 2;
    }

    public static double[] toArray(List<DecimalPosition> decimalPositions) {
        if (decimalPositions == null) {
            return null;
        }
        double[] array = new double[decimalPositions.size() * getComponentsPerDecimalPosition()];
        for (int i = 0; i < decimalPositions.size(); i++) {
            int arrayIndex = i * getComponentsPerDecimalPosition();
            DecimalPosition decimalPosition = decimalPositions.get(i);
            array[arrayIndex] = decimalPosition.getX();
            array[arrayIndex + 1] = decimalPosition.getY();
        }
        return array;
    }

    public Index toIndexRound() {
        return new Index((int) Math.round(x), (int) Math.round(y));
    }

    public Index toIndexCeil() {
        return new Index((int) Math.ceil(x), (int) Math.ceil(y));
    }

    public Index toIndex() {
        return new Index((int) x, (int) y);
    }

    public Index toIndexFloor() {
        return new Index((int) Math.floor(x), (int) Math.floor(y));
    }

    @JsIgnore
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

    @JsIgnore
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

    @JsIgnore
    public DecimalPosition getPointWithDistance(double angle, double radius) {
        double gk = Math.sin(angle) * radius;
        double ak = Math.cos(angle) * radius;
        return new DecimalPosition(x + ak, y + gk);
    }

    /**
     * Angle from this point to the given point.
     * If the given point is exactly in the east, the angle is 0.0
     * If the given point is exactly in the north, the angle is PI/2 90dec
     * If the given point is exactly in the west, the angle is PI 180dec
     * If the given point is exactly in the south, the angle is 3/2 * PI 270dec
     * <p>
     * The returned angle is between 0 and 2Pi
     *
     * @param point end point
     * @return positive angle
     */
    public double getAngle(DecimalPosition point) {
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

    public double angle() {
        return Math.atan2(y, x);
    }

    @JsIgnore
    public DecimalPosition rotateCounterClock(DecimalPosition center, double sinus, double cosines) {
        double normX = center.x - x;
        double normY = center.y - y;
        double newX = -normX * cosines - normY * sinus;
        double newY = -normY * cosines + normX * sinus;
        return new DecimalPosition(center.x + newX, center.y + newY);
    }

    @JsIgnore
    public DecimalPosition rotateCounterClock(DecimalPosition center, double angle) {
        double sinus = Math.sin(angle);
        double cosines = Math.cos(angle);
        return rotateCounterClock(center, sinus, cosines);
    }

    public DecimalPosition rotateCounterClock90() {
        return new DecimalPosition(-y, x);
    }

    @JsIgnore
    public DecimalPosition add(double weight, DecimalPosition otherDecimalPosition) {
        return new DecimalPosition(x + weight * otherDecimalPosition.x, y + weight * otherDecimalPosition.y);
    }

    @JsIgnore
    public DecimalPosition add(DecimalPosition point) {
        return new DecimalPosition(x + point.x, y + point.y);
    }

    @JsIgnore
    public DecimalPosition add(double deltaX, double deltaY) {
        return new DecimalPosition(x + deltaX, y + deltaY);
    }

    @JsIgnore
    public DecimalPosition sub(DecimalPosition point) {
        return new DecimalPosition(x - point.x, y - point.y);
    }

    @JsIgnore
    public DecimalPosition sub(double deltaX, double deltaY) {
        return new DecimalPosition(x - deltaX, y - deltaY);
    }

    public double magnitude() {
        return Math.sqrt(magnitudeSq());
    }

    public double magnitudeSq() {
        return x * x + y * y;
    }

    @JsIgnore
    public DecimalPosition normalize(double basis) {
        double m = magnitude();
        if (m == 0.0) {
            return new DecimalPosition(this);
        }
        return multiply(basis / m);
    }

    public DecimalPosition truncate(double basis) {
        if (length() > basis) {
            return normalize(basis);
        } else {
            return this;
        }
    }

    @JsIgnore
    public DecimalPosition normalize() {
        return normalize(1);
    }

    @JsIgnore
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

    @JsIgnore
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

    @JsIgnore
    public DecimalPosition multiply(double m) {
        return new DecimalPosition(x * m, y * m);
    }

    public double getDistance(double x, double y) {
        double sqrtC = Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2);
        return Math.sqrt(sqrtC);
    }

    @JsIgnore
    public double getDistance(DecimalPosition decimalPosition) {
        return Math.sqrt(getDistanceSq(decimalPosition));
    }

    @JsIgnore
    public double getDistanceSq(DecimalPosition decimalPosition) {
        return Math.pow(decimalPosition.x - x, 2) + Math.pow(decimalPosition.y - y, 2);
    }

    @JsIgnore
    public double getDistance(Index index) {
        double sqrtC = Math.pow((double) index.getX() - x, 2) + Math.pow((double) index.getY() - y, 2);
        return Math.sqrt(sqrtC);
    }

    public double length() {
        double sqrtC = Math.pow(x, 2) + Math.pow(y, 2);
        return Math.sqrt(sqrtC);
    }

    public double getX() {
        return x;
    }

    /**
     * Should only be called by the frameworks: e.g. Errai, MonogDB etc
     *
     * @param x x value
     */
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    /**
     * Should only be called by the frameworks: e.g. Errai, MonogDB etc
     *
     * @param y y value
     */
    public void setY(double y) {
        this.y = y;
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
    @JsIgnore
    public double angle(DecimalPosition start, DecimalPosition end) {
//        start = start.sub(this);
//        end = end.sub(this);
//        return Math.acos(start.dotProduct(end) / (start.magnitude() * end.magnitude()));
        double startAngle = getAngle(start);
        double endAngle = getAngle(end);
        return MathHelper.normaliseAngle(endAngle - startAngle);
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

    public boolean equalsDeltaZero() {
        return MathHelper.compareWithPrecision(x, 0) && MathHelper.compareWithPrecision(y, 0);
    }

    @JsIgnore
    public boolean equalsDelta(DecimalPosition other) {
        return MathHelper.compareWithPrecision(x, other.x) && MathHelper.compareWithPrecision(y, other.y);
    }

    public boolean equalsDelta(DecimalPosition other, double delta) {
        return MathHelper.compareWithPrecision(x, other.x, delta) && MathHelper.compareWithPrecision(y, other.y, delta);
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y;
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
