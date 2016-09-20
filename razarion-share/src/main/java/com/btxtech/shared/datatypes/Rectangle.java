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
public class Rectangle {
    private Index start;
    private Index endExclusive;

    /**
     * Used by Errai
     */
    public Rectangle() {
    }

    public Rectangle(Index start, Index end) {
        if (start.getX() > end.getX() || start.getY() > end.getY()) {
            throw new IllegalArgumentException("Invalid rectangle start: " + start + " end: " + end);
        }
        this.start = start.copy();
        this.endExclusive = end.copy();
    }

    public Rectangle(int xStart, int yStart, int width, int height) {
        this.start = new Index(xStart, yStart);
        this.endExclusive = new Index(xStart + width, yStart + height);
    }

    public void replace(Rectangle target) {
        start = target.start.copy();
        endExclusive = target.endExclusive.copy();
    }

    public Index getStart() {
        return start.copy();
    }

    public Index getEnd() {
        return endExclusive.copy();
    }

    /**
     * Returns true if the given position is in the rectangle or adjoins the rectangle
     *
     * @param position to check
     * @return true if adjoins or contains position
     */
    public boolean contains(Index position) { // TODO rename: adjoinsOrContains
        return position != null && position.getX() + 1 >= start.getX() && position.getY() + 1 >= start.getY() && position.getX() <= endExclusive.getX() && position.getY() <= endExclusive.getY();
    }

    /**
     * Returns true if the given position is in the rectangle
     *
     * @param position to check
     * @return true if adjoins or contains position
     */
    public boolean contains2(DecimalPosition position) { // TODO rename: ???
        return position != null && position.getX() >= start.getX() && position.getY() >= start.getY() && position.getX() <= endExclusive.getX() && position.getY() <= endExclusive.getY();
    }


    /**
     * Returns true if the given position is in the rectangle. If the rectangle just adjoins it returns false.
     *
     * @param position to check
     * @return true if the position is inside the rectangle
     */
    public boolean containsExclusive(DecimalPosition position) { // TODO rename: contains
        if (position.getX() < start.getX() || position.getY() < start.getY()) {
            return false;
        }
        if (width() > 0) {
            if (position.getX() >= endExclusive.getX()) {
                return false;
            }
        } else {
            if (position.getX() > endExclusive.getX()) {
                return false;
            }
        }

        if (height() > 0) {
            if (position.getY() >= endExclusive.getY()) {
                return false;
            }
        } else {
            if (position.getY() > endExclusive.getY()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the given rectangle is in the rectangle or adjoins the rectangle
     *
     * @param rectangle to check
     * @return true if the position is inside the rectangle
     */
    public boolean adjoins(Rectangle rectangle) {
        int startX = Math.max(start.getX(), rectangle.start.getX());
        int startY = Math.max(start.getY(), rectangle.start.getY());

        int endX = Math.min(endExclusive.getX(), rectangle.endExclusive.getX());
        int endY = Math.min(endExclusive.getY(), rectangle.endExclusive.getY());

        return startX <= endX && startY <= endY;
    }

    /**
     * Returns true if the given rectangle overlaps this rectangle. If the rectangle just adjoins it returns false.
     *
     * @param rectangle to check
     * @return true if the position is inside the rectangle
     */
    public boolean adjoinsEclusive(Rectangle rectangle) {
        int startX = Math.max(start.getX(), rectangle.start.getX());
        int startY = Math.max(start.getY(), rectangle.start.getY());

        int endX = Math.min(endExclusive.getX(), rectangle.endExclusive.getX());
        int endY = Math.min(endExclusive.getY(), rectangle.endExclusive.getY());

        return startX < endX && startY < endY;
    }

    /**
     * Returns true if the given circle overlaps this rectangle. If the circle just adjoins it returns false.
     *
     * @param center center of circle to check
     * @return true if the position is inside the rectangle
     */
    public boolean adjoinsCircleExclusive(DecimalPosition center, int radius) {
        double distanceX = Math.abs(center.getX() - (double) center().getX());
        double distanceY = Math.abs(center.getY() - (double) center().getY());

        if (distanceX > (width() / 2 + radius)) {
            return false;
        }
        if (distanceY > (height() / 2 + radius)) {
            return false;
        }

        if (distanceX <= (width() / 2)) {
            return true;
        }
        if (distanceY <= (height() / 2)) {
            return true;
        }

        double squaredCornerDistance = Math.pow((distanceX - width() / 2), 2) + Math.pow((distanceY - height() / 2), 2);

        return squaredCornerDistance <= Math.pow(radius, 2);
    }

    /**
     * Returns the distance between the circle and the rectangle. If the circle is inside the distance becomes negative
     *
     * @param center center of the circle
     * @param radius radius of the circle
     * @return the distance
     */
    public double getDistanceToCircle(DecimalPosition center, double radius) {
        if (adjoinsCircleExclusive(center, 0)) {
            Line line = nearestLine(center);
            DecimalPosition pointOnRect = line.getNearestPointOnLine(center);
            return -(pointOnRect.getDistance(center) + radius);
        } else {
            DecimalPosition pointOnRect = getNearestPointInclusive(center);
            return pointOnRect.getDistance(center) - radius;
        }
    }

    public Rectangle getCrossSection(Rectangle rectangle) {
        int startX = Math.max(start.getX(), rectangle.start.getX());
        int startY = Math.max(start.getY(), rectangle.start.getY());

        int endX = Math.min(endExclusive.getX(), rectangle.endExclusive.getX());
        int endY = Math.min(endExclusive.getY(), rectangle.endExclusive.getY());

        if (startX > endX || startY > endY) {
            throw new IllegalArgumentException("Rectangles do not overlap");
        }

        return new Rectangle(new Index(startX, startY), new Index(endX, endY));
    }

    /**
     * Checks if the line cuts this rectangle in any way. It returns also true if the line is inside the rectangle
     *
     * @param line point 2 of the line
     * @return true if the line cuts this rectangle
     */
    public boolean doesLineCut(Line line) {
        if (containsExclusive(line.getPoint1())) {
            return true;
        }
        if (containsExclusive(line.getPoint2())) {
            return true;
        }

        double x1 = Math.min(line.getPoint1().getX(), line.getPoint2().getX());
        double x2 = Math.max(line.getPoint1().getX(), line.getPoint2().getX());
        double y1 = Math.min(line.getPoint1().getY(), line.getPoint2().getY());
        double y2 = Math.max(line.getPoint1().getY(), line.getPoint2().getY());

        // y = mx + c
        // x = (y-c)/m
        double m = (line.getPoint2().getY() - line.getPoint1().getY()) / (line.getPoint2().getX() - line.getPoint1().getX());
        double c = line.getPoint1().getY() - (m * line.getPoint1().getX());

        double xNorth = Double.NaN;
        double xSouth = Double.NaN;
        double yWest = Double.NaN;
        double yEast = Double.NaN;
        if (Double.isInfinite(m)) {
            // Vertical line
            xNorth = x1;
            xSouth = x1;
        } else if (m == 0.0) {
            yWest = c;
            yEast = c;
        } else {
            xNorth = ((double) start.getY() - c) / m;
            xSouth = ((double) endExclusive.getY() - 1 - c) / m;
            yWest = m * (double) start.getX() + c;
            yEast = m * (double) endExclusive.getX() - 1 + c;
        }


        // Since both points are outside the rectangle, one crossed edged is enough.

        // Check north
        if (!Double.isNaN(xNorth) && start.getX() <= xNorth && xNorth < endExclusive.getX() && x1 <= xNorth && xNorth <= x2 && y2 > start.getY() && y1 < endExclusive.getY()) {
            return true;
        }
        // Check east
        if (!Double.isNaN(yWest) && start.getY() <= yWest && yWest < endExclusive.getY() && y1 <= yWest && yWest <= y2 && x2 > start.getX() && x1 < endExclusive.getX()) {
            return true;
        }
        // Check south
        if (!Double.isNaN(xSouth) && start.getX() <= xSouth && xSouth < endExclusive.getX() && x1 <= xSouth && xSouth <= x2 && y2 > start.getY() && y1 < endExclusive.getY()) {
            return true;
        }
        // Check west
        return !Double.isNaN(yEast) && start.getY() <= yEast && yEast < endExclusive.getY() && y2 <= yEast && yEast <= y2 && x2 > start.getX() && x1 < endExclusive.getX();
    }

    public Collection<DecimalPosition> getCrossPointsInfiniteLine(Line line) {
        List<DecimalPosition> crossPoints = new ArrayList<>();
        DecimalPosition crossPoint = lineW().getCrossInfinite(line);
        if (crossPoint != null) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineS().getCrossInfinite(line);
        if (crossPoint != null && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineE().getCrossInfinite(line);
        if (crossPoint != null && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        crossPoint = lineN().getCrossInfinite(line);
        if (crossPoint != null && !crossPoints.contains(crossPoint)) {
            crossPoints.add(crossPoint);
        }
        if (crossPoints.size() > 2) {
            throw new IllegalStateException("A rectangle can not be crossed more then twice by a line");
        }
        return crossPoints;
    }


    /**
     * Returns the shortest distance to the line, end is inclusive
     *
     * @ param point1 line point 1
     * @ param point2 line point 2
     * @ return the shortest distance
     */
 /*   public double getShortestDistanceToLine(Index point1, Index point2) {
        if (doesLineCut(point1, point2)) {
            return 0;
        }

        Line line = new Line(point1, point2);
        double d1 = line.getShortestDistance(start);
        double d2 = line.getShortestDistance(new Index(startX(), getEndY()));
        double d3 = line.getShortestDistance(endExclusive);
        double d4 = line.getShortestDistance(new Index(getEndX(), startY()));

        double d5 = getNearestPointInclusive(point1).getDistanceDouble(point1);
        double d6 = getNearestPointInclusive(point2).getDistanceDouble(point2);

        return Math.min(Math.min(Math.min(d1, d2), Math.min(d3, d4)), Math.min(d5, d6));
    }*/

    public Rectangle moveTo(int absX, int absY) {
        return new Rectangle(absX, absY, width(), height());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rectangle rectangle = (Rectangle) o;

        return !(endExclusive != null ? !endExclusive.equals(rectangle.endExclusive) : rectangle.endExclusive != null) && !(start != null ? !start.equals(rectangle.start) : rectangle.start != null);

    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (endExclusive != null ? endExclusive.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Start " + start + " End " + endExclusive + " Width: " + width() + " Height: " + height();
    }

    public int width() {
        return endExclusive.getX() - start.getX();
    }

    public int height() {
        return endExclusive.getY() - start.getY();
    }

    public int startX() {
        return start.getX();
    }

    public int startY() {
        return start.getY();
    }

    public int endX() {
        return endExclusive.getX();
    }

    public int endY() {
        return endExclusive.getY();
    }

    public Index center() {
        int centerX = (endExclusive.getX() - start.getX()) / 2;
        int centerY = (endExclusive.getY() - start.getY()) / 2;
        return new Index(start.getX() + centerX, start.getY() + centerY);
    }

    /**
     * Returns the nearest point on the rectangle. Endpoints are exclusive
     *
     * @param point input
     * @return result (exclusive)
     */
    public DecimalPosition getNearestPoint(DecimalPosition point) {
        // Fist check end point
        int endXCorrection = width() > 0 ? 1 : 0;
        int endYCorrection = height() > 0 ? 1 : 0;

        if (point.getX() <= start.getX() && point.getY() <= start.getY()) {
            return new DecimalPosition(start.copy());
        } else if (point.getX() >= endExclusive.getX() && point.getY() >= endExclusive.getY()) {
            return new DecimalPosition(endExclusive.sub(endXCorrection, endYCorrection));
        } else if (point.getX() <= start.getX() && point.getY() >= endExclusive.getY()) {
            return new DecimalPosition(start.getX(), endExclusive.getY() - endYCorrection);
        } else if (point.getX() >= endExclusive.getX() && point.getY() <= start.getY()) {
            return new DecimalPosition(endExclusive.getX() - endXCorrection, start.getY());
        }

        // Do projection
        if (point.getX() <= start.getX()) {
            return new DecimalPosition(start.getX(), point.getY());
        } else if (point.getX() >= endExclusive.getX()) {
            return new DecimalPosition(endExclusive.getX() - endXCorrection, point.getY());
        } else if (point.getY() <= start.getY()) {
            return new DecimalPosition(point.getX(), start.getY());
        } else if (point.getY() >= endExclusive.getY()) {
            return new DecimalPosition(point.getX(), endExclusive.getY() - endYCorrection);
        }

        throw new IllegalArgumentException("The point is inside the rectangle. Point: " + point + " rectangel: " + this);
    }

    /**
     * Returns the nearest point on the rectangle. Endpoints are inclusive
     *
     * @param point input
     * @return result (exclusive)
     */
    public DecimalPosition getNearestPointInclusive(DecimalPosition point) {
        if (point.getX() <= start.getX() && point.getY() <= start.getY()) {
            return new DecimalPosition(start.copy());
        } else if (point.getX() >= endExclusive.getX() && point.getY() >= endExclusive.getY()) {
            return new DecimalPosition(endExclusive.copy());
        } else if (point.getX() <= start.getX() && point.getY() >= endExclusive.getY()) {
            return new DecimalPosition(start.getX(), endExclusive.getY());
        } else if (point.getX() >= endExclusive.getX() && point.getY() <= start.getY()) {
            return new DecimalPosition(endExclusive.getX(), start.getY());
        }

        // Do projection
        if (point.getX() <= start.getX()) {
            return new DecimalPosition(start.getX(), point.getY());
        } else if (point.getX() >= endExclusive.getX()) {
            return new DecimalPosition(endExclusive.getX(), point.getY());
        } else if (point.getY() <= start.getY()) {
            return new DecimalPosition(point.getX(), start.getY());
        } else if (point.getY() >= endExclusive.getY()) {
            return new DecimalPosition(point.getX(), endExclusive.getY());
        }

        throw new IllegalArgumentException("The point is inside the rectangle");
    }

    public boolean hasMinSize(int minSize) {
        return height() >= minSize || width() >= minSize;
    }

    public boolean isEmpty() {
        return height() == 0 && width() == 0;
    }

    /**
     * Splits the rectangle into smaller rectangles.
     * The result rectangles will always have the given width and height. If the area of this rectangle is not width * height
     * the returning result will be rounded up (e.g. thr returned rectangles have a bigger area then this rectangle).
     *
     * @param width  of the split tiles
     * @param height of the split tiles
     * @return Collection with split rectangles
     */
    public Collection<Rectangle> split(int width, int height) {
        ArrayList<Rectangle> split = new ArrayList<Rectangle>();
        int xCount = (int) Math.ceil((double) width() / (double) width);
        int yCount = (int) Math.ceil((double) height() / (double) height);
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                split.add(new Rectangle(getStart().getX() + x * width, getStart().getY() + y * height, width, height));
            }
        }
        return split;
    }

    public double diagonally() {
        return Math.sqrt(width() * width() + height() * height());
    }

    public double halfDiagonally() {
        return Math.sqrt(width() * width() + height() * height()) / 2.0;
    }

    /**
     * @param center where this rectaggle is turned arount
     * @param angle  to turn thie rectaggle counterclockwise
     * @return the surrounding rectangle if this rectangle is truned
     */
    public Rectangle getSurroundedRectangle(Index center, double angle) {
        double sinus = Math.sin(angle);
        double cosinus = Math.cos(angle);
        Index p1 = start;
        Index p2 = new Index(endExclusive.getX(), start.getY());
        Index p3 = endExclusive;
        Index p4 = new Index(start.getX(), endExclusive.getY());
        Index newP1 = p1.rotateCounterClock(center, sinus, cosinus);
        Index newP2 = p2.rotateCounterClock(center, sinus, cosinus);
        Index newP3 = p3.rotateCounterClock(center, sinus, cosinus);
        Index newP4 = p4.rotateCounterClock(center, sinus, cosinus);
        return generateRectangleFromAnyPoints(newP1, newP2, newP3, newP4);
    }

    public Index cornerNW() {
        return start;
    }

    public Index cornerSW() {
        return start.add(0, height());
    }

    public Index cornerSE() {
        return endExclusive;
    }

    public Index cornerNE() {
        return start.add(width(), 0);
    }

    public Line lineW() {
        return new Line(new DecimalPosition(cornerNW()), new DecimalPosition(cornerSW()));
    }

    public Line lineS() {
        return new Line(new DecimalPosition(cornerSW()), new DecimalPosition(cornerSE()));
    }

    public Line lineSExclusive() {
        return new Line(new DecimalPosition(cornerSW().sub(0, 1)), new DecimalPosition(cornerSE().sub(1, 1)));
    }

    public Line lineE() {
        return new Line(new DecimalPosition(cornerSE()), new DecimalPosition(cornerNE()));
    }

    public Line lineEExclusive() {
        return new Line(new DecimalPosition(cornerSE().sub(1, 1)), new DecimalPosition(cornerNE().sub(1, 0)));
    }

    public Line lineN() {
        return new Line(new DecimalPosition(cornerNE()), new DecimalPosition(cornerNW()));
    }

    public int area() {
        return width() * height();
    }

    public Collection<Line> lines() {
        Collection<Line> lines = new ArrayList<>();
        lines.add(new Line(new DecimalPosition(cornerNW()), new DecimalPosition(cornerSW())));
        lines.add(new Line(new DecimalPosition(cornerSW()), new DecimalPosition(cornerSE())));
        lines.add(new Line(new DecimalPosition(cornerSE()), new DecimalPosition(cornerNE())));
        lines.add(new Line(new DecimalPosition(cornerNE()), new DecimalPosition(cornerNW())));
        return lines;
    }

    public Line nearestLine(DecimalPosition point) {
        double bestDistance = Double.MAX_VALUE;
        Line bestLine = null;
        for (Line line : lines()) {
            double distance = line.getShortestDistance(point);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestLine = line;
            }
        }
        return bestLine;
    }

    public static Rectangle generateRectangleFromAnyPoints(Index point1, Index point2) {
        Index start = point1.getSmallestPoint(point2);
        Index end = point1.getLargestPoint(point2);
        return new Rectangle(start, end);
    }

    public static Rectangle generateRectangleFromAnyPoints(Index point1, Index point2, Index point3, Index point4) {
        Index start = point1.getSmallestPoint(point2);
        start = start.getSmallestPoint(point3);
        start = start.getSmallestPoint(point4);
        Index end = point1.getLargestPoint(point2);
        end = end.getLargestPoint(point3);
        end = end.getLargestPoint(point4);
        return new Rectangle(start, end);
    }

    public static Rectangle generateRectangleFromMiddlePoint(Index middlePoint, int width, int height) {
        Index start = middlePoint.sub(width / 2, height / 2);
        return new Rectangle(start.getX(), start.getY(), width, height);
    }

    public static boolean contains(int x, int y, int width, int height, Index position) {
        return position != null && position.getX() >= x && position.getY() >= y && position.getX() <= x + width && position.getY() <= y + height;
    }

    /**
     * Returns true if one rectangle call to adjoinsExclusive() returns true for one other rectangles in the given list.
     *
     * @param rectangles rectangle to check
     * @return if one rectangle adjoinsExclusively another rectangle
     */
    public static boolean adjoinsExclusive(Collection<Rectangle> rectangles) {
        List<Rectangle> rectanglesList = new ArrayList<Rectangle>(rectangles);
        while (!rectanglesList.isEmpty()) {
            Rectangle rectangle = rectanglesList.remove(0);
            for (Rectangle others : rectanglesList) {
                if (rectangle.adjoinsEclusive(others)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String testString() {
        return "new Rectangle(" + startX() + ", " + startY() + ", " + width() + ", " + height() + ")";
    }
}
