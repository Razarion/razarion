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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: beat
 * Date: May 23, 2009
 * Time: 11:38:26 AM
 */
public class Rectangle2D {
    private DecimalPosition start;
    private DecimalPosition end; // Exclusive

    /**
     * Used by Errai
     */
    public Rectangle2D() {
    }

    public Rectangle2D(DecimalPosition start, DecimalPosition end) {
        if (start.getX() > end.getX() || start.getY() > end.getY()) {
            throw new IllegalArgumentException("Invalid rectangle start: " + start + " end: " + end);
        }
        this.start = start;
        this.end = end;
    }

    public Rectangle2D(double xStart, double yStart, double width, double height) {
        this.start = new DecimalPosition(xStart, yStart);
        this.end = new DecimalPosition(xStart + width, yStart + height);
    }

    public double width() {
        return end.getX() - start.getX();
    }

    public double height() {
        return end.getY() - start.getY();
    }

    public DecimalPosition getStart() {
        return start;
    }

    public DecimalPosition getEnd() {
        return end;
    }

    public double startX() {
        return start.getX();
    }

    public double startY() {
        return start.getY();
    }

    public double endX() {
        return end.getX();
    }

    public double endY() {
        return end.getY();
    }

    public boolean hasMinSize(double minSize) {
        return height() >= minSize || width() >= minSize;
    }

    public DecimalPosition center() {
        double centerX = (end.getX() - start.getX()) / 2;
        double centerY = (end.getY() - start.getY()) / 2;
        return new DecimalPosition(start.getX() + centerX, start.getY() + centerY);
    }

    /**
     * Returns true if the given circle overlaps this rectangle. If the circle just adjoins it returns false.
     *
     * @param center center of circle to check
     * @return true if the position is inside the rectangle
     */
    public boolean adjoinsCircleExclusive(DecimalPosition center, double radius) {
        double distanceX = Math.abs(center.getX() - center().getX());
        double distanceY = Math.abs(center.getY() - center().getY());

        if (distanceX > (width() / 2.0 + radius)) {
            return false;
        }
        if (distanceY > (height() / 2.0 + radius)) {
            return false;
        }

        if (distanceX <= (width() / 2.0)) {
            return true;
        }
        if (distanceY <= (height() / 2.0)) {
            return true;
        }

        double squaredCornerDistance = Math.pow((distanceX - width() / 2.0), 2.0) + Math.pow((distanceY - height() / 2.0), 2.0);

        return squaredCornerDistance <= Math.pow(radius, 2.0);
    }

    /**
     * Returns true if the given position is in the rectangle
     *
     * @param position to check
     * @return true if adjoins or contains position
     */
    public boolean contains(DecimalPosition position) {
        return position.getX() >= start.getX() && position.getY() >= start.getY() && position.getX() <= end.getX() && position.getY() <= end.getY();
    }

    /**
     * Returns the nearest point on the rectangle. Endpoints are inclusive
     *
     * @param point input
     * @return result (exclusive)
     */
    public DecimalPosition getNearestPoint(DecimalPosition point) {
        // 4 Corners
        if (point.getX() <= start.getX() && point.getY() <= start.getY()) {
            return start;
        } else if (point.getX() >= end.getX() && point.getY() >= end.getY()) {
            return end;
        } else if (point.getX() <= start.getX() && point.getY() >= end.getY()) {
            return new DecimalPosition(start.getX(), end.getY());
        } else if (point.getX() >= end.getX() && point.getY() <= start.getY()) {
            return new DecimalPosition(end.getX(), start.getY());
        }

        // Do projection
        if (point.getX() <= start.getX()) {
            return new DecimalPosition(start.getX(), point.getY());
        } else if (point.getX() >= end.getX()) {
            return new DecimalPosition(end.getX(), point.getY());
        } else if (point.getY() <= start.getY()) {
            return new DecimalPosition(point.getX(), start.getY());
        } else if (point.getY() >= end.getY()) {
            return new DecimalPosition(point.getX(), end.getY());
        }

        throw new IllegalArgumentException("The point is inside the rectangle");
    }

    public List<DecimalPosition> toCorners() {
        List<DecimalPosition> corners = new ArrayList<>();
        corners.add(start);
        corners.add(start.add(width(), 0));
        corners.add(end);
        corners.add(end.sub(width(), 0));
        return corners;
    }

    public Polygon2D toPolygon() {
        return new Polygon2D(toCorners());
    }

    @Override
    public String toString() {
        return "Rectangle2D{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    public static Rectangle2D generateRectangleFromMiddlePoint(DecimalPosition middlePoint, double width, double height) {
        DecimalPosition start = middlePoint.sub(width / 2.0, height / 2.0);
        return new Rectangle2D(start.getX(), start.getY(), width, height);
    }
}
