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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: beat
 * Date: May 22, 2009
 * Time: 11:56:58 AM
 */
public class Vec2dInt implements Serializable {
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
    Vec2dInt() {
    }

    public Vec2dInt(int x, int y) {
        this.x = x;
        this.y = y;
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

        Vec2dInt vec2dInt = (Vec2dInt) o;

        return x == vec2dInt.x && y == vec2dInt.y;

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

    public Vec2dInt getCopy() {
        return new Vec2dInt(x, y);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Vec2dInt getDelta(Vec2dInt vec2dInt) {
        return new Vec2dInt(vec2dInt.x - x, vec2dInt.y - y);
    }

    public int getDistance(Vec2dInt vec2dInt) {
        double sqrtC = Math.pow(vec2dInt.x - x, 2) + Math.pow(vec2dInt.y - y, 2);
        return (int) Math.round(Math.sqrt(sqrtC));
    }

    public double getDistanceDouble(Vec2dInt vec2dInt) {
        double sqrtC = Math.pow(vec2dInt.x - x, 2) + Math.pow(vec2dInt.y - y, 2);
        return Math.sqrt(sqrtC);
    }

    public boolean isInRadius(Vec2dInt vec2dInt, int radius) {
        return getDistance(vec2dInt) <= radius;
    }

    /**
     * The angle from this point to the given point to north
     *
     * @param point point direction
     * @return angle in degrees
     */
    public double getAngleToNord(Vec2dInt point) {
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

    public Vec2dInt getPointFromAngelToNord(double angle, int radius) {
        int gk = (int) Math.round(Math.sin(angle) * (double) radius);
        int ak = (int) Math.round(Math.cos(angle) * (double) radius);
        int newX = x - gk;
        int newY = y - ak;
        return new Vec2dInt(newX, newY);
    }

    public Vec2dInt getPointFromAngelToNord(double angle, double radius) {
        int gk = (int) Math.round(Math.sin(angle) * radius);
        int ak = (int) Math.round(Math.cos(angle) * radius);
        int newX = x - gk;
        int newY = y - ak;
        return new Vec2dInt(newX, newY);
    }

    public Vec2dInt getPointFromAngelToNorthRoundUp(double angle, double radius) {
        int gk = (int) Math.ceil(Math.sin(angle) * radius);
        int ak = (int) Math.ceil(Math.cos(angle) * radius);
        int newX = x - gk;
        int newY = y - ak;
        return new Vec2dInt(newX, newY);
    }

    public Vec2dInt getPointWithDistance(int distance, Vec2dInt directionTo, boolean allowOverrun) {
        double directionDistance = getDistance(directionTo);
        if (!allowOverrun && directionDistance <= distance) {
            return directionTo;
        }
        int dirDeltaX = directionTo.x - x;
        int dirDeltaY = directionTo.y - y;
        int deltaX = (int) Math.round((dirDeltaX * distance) / directionDistance);
        int deltaY = (int) Math.round((dirDeltaY * distance) / directionDistance);

        return new Vec2dInt(x + deltaX, y + deltaY);
    }

//    TODO public Rectangle getRegion(int width, int height) {
//        int startX = x - width / 2;
//        int startY = y - height / 2;
//        return new Rectangle(startX, startY, x + width / 2, y + height / 2);
//    }

    public Vec2dInt getSmallestPoint(Vec2dInt point) {
        int smallestX = Math.min(x, point.x);
        int smallestY = Math.min(y, point.y);
        return new Vec2dInt(smallestX, smallestY);
    }

    public Vec2dInt getLargestPoint(Vec2dInt point) {
        int largestX = Math.max(x, point.x);
        int largestY = Math.max(y, point.y);
        return new Vec2dInt(largestX, largestY);
    }

    public Vec2dInt scale(double scale) {
        return new Vec2dInt((int) (x * scale), (int) (y * scale));
    }

    public Vec2dInt scaleInverse(double scale) {
        return new Vec2dInt((int) (x / scale), (int) (y / scale));
    }

    public Vec2dInt add(Vec2dInt point) {
        return new Vec2dInt(x + point.x, y + point.y);
    }

    public Vec2dInt add(int deltaX, int deltaY) {
        return new Vec2dInt(x + deltaX, y + deltaY);
    }

    public Vec2dInt sub(Vec2dInt point) {
        return new Vec2dInt(x - point.x, y - point.y);
    }

    public Vec2dInt sub(int deltaX, int deltaY) {
        return new Vec2dInt(x - deltaX, y - deltaY);
    }

    public Vec2dInt getMiddlePoint(Vec2dInt other) {
        return new Vec2dInt((x + other.x) / 2, (y + other.y) / 2);
    }

    public boolean isBigger(Vec2dInt point) {
        return x > point.x || y > point.y;
    }

    public boolean isSmaller(Vec2dInt point) {
        return x < point.x || y < point.y;
    }

    public Vec2dInt rotateCounterClock(Vec2dInt center, double sinus, double cosines) {
        double normX = center.x - x;
        double normY = center.y - y;
        int newX = (int) Math.round(-normX * cosines - normY * sinus);
        int newY = (int) Math.round(-normY * cosines + normX * sinus);
        return new Vec2dInt(center.x + newX, center.y + newY);
    }

    public Vec2dInt rotateCounterClock(Vec2dInt center, double angel) {
        double sinus = Math.sin(angel);
        double cosines = Math.cos(angel);
        return rotateCounterClock(center, sinus, cosines);
    }

    public Direction getDirection(Vec2dInt other) {
        if (equals(other)) {
            throw new IllegalArgumentException("Can not determine direction if points are equals: " + this);
        }
        if (x == other.x) {
            if (other.y > y) {
                return Direction.S;
            } else {
                return Direction.N;
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
                return Direction.SE;
            } else {
                return Direction.SW;
            }
        } else {
            if (other.x > x) {
                return Direction.NE;
            } else {
                return Direction.NW;
            }
        }
    }

    public boolean isNull() {
        return x == 0 && y == 0;
    }

    public boolean isNegative() {
        return x < 0 || y < 0;
    }

    public String testString() {
        return "new Index(" + getX() + ", " + getY() + ")";
    }

    public static Vec2dInt createSaveIndex(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return new Vec2dInt(x, y);
    }

    public static Vec2dInt createSaveIndex(Vec2dInt vec2dInt) {
        return createSaveIndex(vec2dInt.getX(), vec2dInt.getY());
    }

    public static Vec2dInt saveCopy(Vec2dInt vec2dInt) {
        if (vec2dInt != null) {
            return vec2dInt.getCopy();
        } else {
            return null;
        }
    }

    public static Collection<Vec2dInt> add(Collection<Vec2dInt> positions, Vec2dInt delta) {
        Collection<Vec2dInt> result = new ArrayList<Vec2dInt>();
        for (Vec2dInt position : positions) {
            result.add(position.add(delta));
        }
        return result;
    }

    public static Vec2dInt calculateMiddle(Collection<Vec2dInt> positions) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Vec2dInt position : positions) {
            minX = Math.min(position.getX(), minX);
            minY = Math.min(position.getY(), minY);
            maxX = Math.max(position.getX(), maxX);
            maxY = Math.max(position.getY(), maxY);
        }
        return new Vec2dInt((minX + maxX) / 2, (minY + maxY) / 2);
    }

}
