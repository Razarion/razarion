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
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * User: beat
 * Date: May 22, 2009
 * Time: 11:56:58 AM
 */
@Embeddable
@JsType
public class Index {
    public static final Index ZERO = new Index(0, 0);

    public enum Direction {
        N,
        NE,
        E,
        SE,
        S,
        SW,
        W,
        NW
    }

    private int x;
    private int y;

    /**
     * Used by GWT
     */
    @JsIgnore
    public Index() {
    }

    @JsIgnore
    public Index(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Index create(int x, int y) {
        return new Index(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Index index = (Index) o;

        return x == index.x && y == index.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y;
    }

    public Index copy() {
        return new Index(x, y);
    }

    /**
     * Should only be called by the frameworks: e.g. Errai, MonogDB etc
     *
     * @param x x value
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Should only be called by the frameworks: e.g. Errai, MonogDB etc
     *
     * @param y y value
     */
    public void setY(int y) {
        this.y = y;
    }

    public Index getDelta(Index index) {
        return new Index(index.x - x, index.y - y);
    }

    public int getDistance(Index index) {
        double sqrtC = Math.pow(index.x - x, 2) + Math.pow(index.y - y, 2);
        return (int) Math.round(Math.sqrt(sqrtC));
    }

    public double getDistanceDouble(Index index) {
        double sqrtC = Math.pow(index.x - x, 2) + Math.pow(index.y - y, 2);
        return Math.sqrt(sqrtC);
    }

    public boolean isInRadius(Index index, int radius) {
        return getDistance(index) <= radius;
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
    public double getAngleToNorth(Index point) {
        if (equals(point)) {
            throw new IllegalArgumentException("Points are equal");
        }
        Index normalized = point.sub(this);

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
            return Math.atan((double) normalized.y / (double) normalized.x);
        } else if (normalized.x < 0 && normalized.y > 0) {
            // Quadrant 2
            return MathHelper.QUARTER_RADIANT + Math.atan((double) -normalized.x / (double) normalized.y);
        } else if (normalized.x < 0 && normalized.y < 0) {
            // Quadrant 3
            return MathHelper.HALF_RADIANT + Math.atan((double) -normalized.y / (double) -normalized.x);
        } else {
            // Quadrant 4
            return MathHelper.THREE_QUARTER_RADIANT + Math.atan((double) normalized.x / (double) -normalized.y);
        }
    }

    @Deprecated
    public double getAngleToNord(Index point) {
        if (equals(point)) {
            throw new IllegalArgumentException("Points are equal");
        }
        int gk = x - point.x;
        int ak = y - point.y;
        if (ak == 0) {
            if (gk > 0) {
                return Math.PI / 2;
            } else {
                return -Math.PI / 2;
            }
        }
        if (gk == 0) {
            if (ak > 0) {
                return 0;
            } else {
                return Math.PI;
            }
        }
        double angle = Math.atan((double) gk / (double) ak);
        if (ak < 0) {
            angle += Math.PI;
        }
        return angle;
    }

    /**
     * Angle from the given start to the end point with this point as center. The direction is counter clock wise.
     * The returned angle is between 0 and 2Pi
     *
     * @param start start point
     * @param end   end point
     * @return positive angle
     */
    public double getAngle(Index start, Index end) {
        double startAngle = getAngleToNorth(start);
        double endAngle = getAngleToNorth(end);
        return MathHelper.normaliseAngle(endAngle - startAngle);
    }

    @JsIgnore
    public Index getPointFromAngleRound(double angle, int radius) {
        int gk = (int) Math.round(Math.sin(angle) * (double) radius);
        int ak = (int) Math.round(Math.cos(angle) * (double) radius);
        return new Index(x + ak, y + gk);
    }

    public Index getPointFromAngleRound(double angle, double radius) {
        int gk = (int) Math.round(Math.sin(angle) * radius);
        int ak = (int) Math.round(Math.cos(angle) * radius);
        return new Index(x + ak, y + gk);
    }

    public Index getPointFromAngleRoundUp(double angle, double radius) {
        int gk = (int) Math.ceil(Math.sin(angle) * radius);
        int ak = (int) Math.ceil(Math.cos(angle) * radius);
        return new Index(x + ak, y + gk);
    }

    public Index getPointWithDistance(int distance, Index directionTo, boolean allowOverrun) {
        double directionDistance = getDistance(directionTo);
        if (!allowOverrun && directionDistance <= distance) {
            return directionTo;
        }
        int dirDeltaX = directionTo.x - x;
        int dirDeltaY = directionTo.y - y;
        int deltaX = (int) Math.round((dirDeltaX * distance) / directionDistance);
        int deltaY = (int) Math.round((dirDeltaY * distance) / directionDistance);

        return new Index(x + deltaX, y + deltaY);
    }

    public Rectangle getRegion(int width, int height) {
        int startX = x - width / 2;
        int startY = y - height / 2;
        return new Rectangle(startX, startY, x + width / 2, y + height / 2);
    }

    public Index getSmallestPoint(Index point) {
        int smallestX = Math.min(x, point.x);
        int smallestY = Math.min(y, point.y);
        return new Index(smallestX, smallestY);
    }

    public Index getLargestPoint(Index point) {
        int largestX = Math.max(x, point.x);
        int largestY = Math.max(y, point.y);
        return new Index(largestX, largestY);
    }

    @JsIgnore
    public Index scale(double scale) {
        return new Index((int) (x * scale), (int) (y * scale));
    }

    public Index scale(double scaleX, double scaleY) {
        return new Index((int) (x * scaleX), (int) (y * scaleY));
    }

    public Index scaleInverse(double scale) {
        return new Index((int) (x / scale), (int) (y / scale));
    }

    public DecimalPosition divide(double factor) {
        return new DecimalPosition(x / factor, y / factor);
    }

    public DecimalPosition multiply(double factor) {
        return new DecimalPosition(x * factor, y * factor);
    }

    @JsIgnore
    public Index add(Index point) {
        return new Index(x + point.x, y + point.y);
    }

    public Index add(int deltaX, int deltaY) {
        return new Index(x + deltaX, y + deltaY);
    }

    @JsIgnore
    public Index sub(Index point) {
        return new Index(x - point.x, y - point.y);
    }

    public Index sub(int deltaX, int deltaY) {
        return new Index(x - deltaX, y - deltaY);
    }

    public Index getMiddlePoint(Index other) {
        return new Index((x + other.x) / 2, (y + other.y) / 2);
    }

    public boolean isBigger(Index point) {
        return x > point.x || y > point.y;
    }

    public boolean isSmaller(Index point) {
        return x < point.x || y < point.y;
    }

    public Index rotateCounterClock(Index center, double sinus, double cosines) {
        double normX = center.x - x;
        double normY = center.y - y;
        int newX = (int) Math.round(-normX * cosines - normY * sinus);
        int newY = (int) Math.round(-normY * cosines + normX * sinus);
        return new Index(center.x + newX, center.y + newY);
    }

    @JsIgnore
    public Index rotateCounterClock(Index center, double angle) {
        double sinus = Math.sin(angle);
        double cosines = Math.cos(angle);
        return rotateCounterClock(center, sinus, cosines);
    }

    public Direction getDirection(Index other) {
        if (equals(other)) {
            throw new IllegalArgumentException("Can not determine direction if points are equals: " + this);
        }
        if (x == other.x) {
            if (other.y > y) {
                return Direction.N;
            } else {
                return Direction.S;
            }
        }
        if (y == other.y) {
            if (other.x > x) {
                return Direction.E;
            } else {
                return Direction.W;
            }
        }
        if (other.y > y) {
            if (other.x > x) {
                return Direction.NE;
            } else {
                return Direction.NW;
            }
        } else {
            if (other.x > x) {
                return Direction.SE;
            } else {
                return Direction.SW;
            }
        }
    }

    public String testString() {
        return "new Index(" + getX() + ", " + getY() + ")";
    }

    /**
     * The Z value of the vector that would result from a regular 3D cross product of the input vectors,
     * taking their Z values implicitly as 0 (i.e. treating the 2D space as a plane in the 3D space).
     *
     * @param a index a
     * @param b index b
     * @return the magnitude
     */
    public int cross(Index a, Index b) {
        return (a.getX() - x) * (b.getY() - y) - (a.getY() - y) * (b.getX() - x);
    }

    public static String testString(List<Index> indices) {
        StringBuilder builder = new StringBuilder("Arrays.asList(");
        for (int i = 0; i < indices.size(); i++) {
            Index index = indices.get(i);
            builder.append(index.testString());
            if (i + 1 < indices.size()) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }

    public static Index createSaveIndex(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return new Index(x, y);
    }

    @JsIgnore
    public static Index createSaveIndex(Index index) {
        return createSaveIndex(index.getX(), index.getY());
    }

    public static Index saveCopy(Index index) {
        if (index != null) {
            return index.copy();
        } else {
            return null;
        }
    }

    public static Collection<Index> add(Collection<Index> positions, Index delta) {
        Collection<Index> result = new ArrayList<Index>();
        for (Index position : positions) {
            result.add(position.add(delta));
        }
        return result;
    }

    public static Index calculateMiddle(Collection<Index> positions) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Index position : positions) {
            minX = Math.min(position.getX(), minX);
            minY = Math.min(position.getY(), minY);
            maxX = Math.max(position.getX(), maxX);
            maxY = Math.max(position.getY(), maxY);
        }
        return new Index((minX + maxX) / 2, (minY + maxY) / 2);
    }

    public static class IndexComparator implements Comparator<Index> {
        @Override
        public int compare(Index o1, Index o2) {
            if (o1.x == o2.x) {
                return o1.y - o2.y;
            } else {
                return o1.x - o2.x;
            }
        }
    }

    public static Index createVector(double angle, int radius) {
        return ZERO.getPointFromAngleRound(angle, radius);
    }

    public static String toString(List<Index> pathToDestination) {
        StringBuilder builder = new StringBuilder();
        if (pathToDestination != null) {
            builder.append("{");
            Iterator<Index> iterator = pathToDestination.iterator();
            while (iterator.hasNext()) {
                Index index = iterator.next();
                builder.append(index.toString());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append("}");
        } else {
            builder.append("{-}");
        }
        return builder.toString();
    }

}
