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

import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: beat
 * Date: May 23, 2009
 * Time: 11:38:26 AM
 */
@Embeddable
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

    public double area() {
        return width() * height();
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
     * Returns true if one of the given positions is in the rectangle
     *
     * @param positions to check
     * @return true if one is inside
     */
    public boolean contains(Collection<DecimalPosition> positions) {
        for (DecimalPosition position : positions) {
            if (contains(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if all of the given positions are in the rectangle
     *
     * @param positions to check
     * @return true if all are inside
     */
    public boolean containsAll(Collection<DecimalPosition> positions) {
        for (DecimalPosition position : positions) {
            if (!contains(position)) {
                return false;
            }
        }
        return true;
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

    public Collection<DecimalPosition> getCrossPointsInfiniteLine(Line line) {
        Collection<DecimalPosition> crossPoints = new ArrayList<>();
        DecimalPosition crossPoint = lineW().getCrossInfinite(line);
        if (crossPoint != null && lineW().isPointInLineInclusive(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineS().getCrossInfinite(line);
        if (crossPoint != null && lineS().isPointInLineInclusive(crossPoint) && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineE().getCrossInfinite(line);
        if (crossPoint != null && lineE().isPointInLineInclusive(crossPoint) && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineN().getCrossInfinite(line);
        if (crossPoint != null && lineN().isPointInLineInclusive(crossPoint) && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoints = DecimalPosition.removeSimilarPoints(crossPoints, 0.001);

        if (crossPoints.size() > 2) {
            throw new IllegalStateException("A rectangle can not be crossed more then twice by a line");
        }
        return crossPoints;
    }


    public Collection<DecimalPosition> getCrossPointsLine(Line line) {
        List<DecimalPosition> crossPoints = new ArrayList<>();
        DecimalPosition crossPoint = lineW().getCrossInclusive(line);
        if (crossPoint != null) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineS().getCrossInclusive(line);
        if (crossPoint != null && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineE().getCrossInclusive(line);
        if (crossPoint != null && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineN().getCrossInclusive(line);
        if (crossPoint != null && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        if (crossPoints.size() > 2) {
            throw new IllegalStateException("A rectangle can not be crossed more then twice by a line");
        }
        return crossPoints;
    }

    public boolean isLineInside(Line line) {
        return contains(line.getPoint1()) && contains(line.getPoint2());
    }

    public Line lineW() {
        return new Line(new DecimalPosition(cornerTopLeft()), new DecimalPosition(cornerBottomLeft()));
    }

    public Line lineS() {
        return new Line(new DecimalPosition(cornerBottomLeft()), new DecimalPosition(cornerBottomRight()));
    }

    public Line lineE() {
        return new Line(new DecimalPosition(cornerBottomRight()), new DecimalPosition(cornerTopRight()));
    }

    public Line lineN() {
        return new Line(new DecimalPosition(cornerTopRight()), new DecimalPosition(cornerTopLeft()));
    }

    public List<Line> toLines() {
        return Arrays.asList(lineN(), lineE(), lineS(), lineW());
    }

    public boolean isLineCrossing(List<Line> lines) {
        Line lineW = lineW();
        Line lineS = lineS();
        Line lineE = lineE();
        Line lineN = lineN();

        for (Line line : lines) {
            if (lineW.getCrossInclusive(line) != null) {
                return true;
            }
            if (lineS.getCrossInclusive(line) != null) {
                return true;
            }
            if (lineE.getCrossInclusive(line) != null) {
                return true;
            }
            if (lineN.getCrossInclusive(line) != null) {
                return true;
            }

        }
        return false;
    }


    public DecimalPosition cornerBottomLeft() {
        return start;
    }

    public DecimalPosition cornerBottomRight() {
        return start.add(width(), 0);
    }

    public DecimalPosition cornerTopRight() {
        return end;
    }

    public DecimalPosition cornerTopLeft() {
        return start.add(0, height());
    }

    public List<DecimalPosition> toCorners() {
        List<DecimalPosition> corners = new ArrayList<>();
        corners.add(cornerBottomLeft());
        corners.add(cornerBottomRight());
        corners.add(cornerTopRight());
        corners.add(cornerTopLeft());
        return corners;
    }

    public Polygon2D toPolygon() {
        return new Polygon2D(toCorners());
    }

    /**
     * Returns true if the given rectangle is in the rectangle or adjoins the rectangle
     *
     * @param rectangle to check
     * @return true if the position is inside the rectangle
     */
    public boolean adjoins(Rectangle2D rectangle) {
        double startX = Math.max(start.getX(), rectangle.start.getX());
        double startY = Math.max(start.getY(), rectangle.start.getY());

        double endX = Math.min(end.getX(), rectangle.end.getX());
        double endY = Math.min(end.getY(), rectangle.end.getY());

        return startX <= endX && startY <= endY;
    }

    /**
     * The cross section inside this rectangle with the given rectangle
     *
     * @param rectangle given rectangle
     * @return cross section inside this rectangle
     */
    public Rectangle2D calculateCrossSection(Rectangle2D rectangle) {
        double startX = Math.max(start.getX(), rectangle.start.getX());
        double startY = Math.max(start.getY(), rectangle.start.getY());

        double endX = Math.min(end.getX(), rectangle.end.getX());
        double endY = Math.min(end.getY(), rectangle.end.getY());

        if (startX > endX || startY > endY) {
            return null;
        }

        return new Rectangle2D(new DecimalPosition(startX, startY), new DecimalPosition(endX, endY));
    }

    /**
     * Calculate the cover ratio of two rectangles
     * e.g. 100% means the given rectangle is fully inside this rectangle
     *
     * @param other given rectangle
     * @return cover ratio
     */
    public double coverRatio(Rectangle2D other) {
        Rectangle2D cross = calculateCrossSection(other);
        if (cross == null) {
            return 0;
        } else {
            return cross.area() / other.area();
        }
    }

    public Rectangle2D translate(double deltaX, double deltaY) {
        return new Rectangle2D(startX() + deltaX, startY() + deltaY, width(), height());
    }


    public Rectangle2D shrink(double shrink) {
        return new Rectangle2D(startX() + shrink, startY() + shrink, width() - 2.0 * shrink, height() - 2.0 * shrink);
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

    public static Rectangle2D generateRectangleFromAnyPoints(DecimalPosition... points) {
        DecimalPosition start = DecimalPosition.getSmallestAabb(points);
        DecimalPosition end = DecimalPosition.getBiggestAabb(points);
        return new Rectangle2D(start, end);
    }

    public String testString() {
        return "new Rectangle2D(" + startX() + ", " + startY() + ", " + width() + ", " + height() + ")";
    }
}
