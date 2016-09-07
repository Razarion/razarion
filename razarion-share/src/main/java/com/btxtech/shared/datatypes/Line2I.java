package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.MathHelper;

/**
 * User: beat
 * Date: 08.05.2011
 * Time: 16:24:24
 */
public class Line2I {
    private static final double MIN_M_INFINITE = Math.tan(MathHelper.gradToRad(89.99999999));
    private static final double MIN_M_ZERO = Math.tan(MathHelper.gradToRad(0.000000001));
    private Index point1;
    private Index point2;
    private DecimalPosition norm;
    private double m;
    private double c;

    /**
     * Used by GWT
     */
    public Line2I() {

    }

//    public Line2I(Index start, double angel, int length) {
//        this(start, start.getPointWithDistance(angel, length));
//    }

    public Line2I(Index point1, Index point2) {
        if (point1.equals(point2)) {
            throw new IllegalArgumentException("Points are equals: " + point1);
        }
        this.point1 = point1;
        this.point2 = point2;
        m = (double) (this.point2.getY() - this.point1.getY()) / (double) (this.point2.getX() - this.point1.getX());
//        if (Math.abs(m) > MIN_M_INFINITE) {
//            m = Double.POSITIVE_INFINITY;
//            c = Double.NEGATIVE_INFINITY;
//        } else if (Math.abs(m) < MIN_M_ZERO) {
//            m = 0.0;
//            c = this.point1.getY();
//        } else {
        c = this.point1.getY() - (m * this.point1.getX());
//        }
    }

    public Index getPoint1() {
        return point1;
    }

    public Index getPoint2() {
        return point2;
    }

    public DecimalPosition getNorm() {
        return norm;
    }

    public void setNorm(DecimalPosition norm) {
        this.norm = norm;
    }

//    public Line2I translate(double angel, double distance) {
//        return new Line2I(point1.getPointWithDistance(angel, distance), point2.getPointWithDistance(angel, distance));
//    }

    public double getM() {
        return m;
    }

    public double getC() {
        return c;
    }

 /*   public double getShortestDistance(DecimalPosition point) {
        DecimalPosition projection = projectOnInfiniteLine(point);

        double xMin = Math.min(point1.getX(), point2.getX());
        double xMax = Math.max(point1.getX(), point2.getX());
        double yMin = Math.min(point1.getY(), point2.getY());
        double yMax = Math.max(point1.getY(), point2.getY());

        if (projection.getX() < xMin || projection.getY() < yMin || projection.getX() > xMax || projection.getY() > yMax) {
            return Math.min(point.getDistanceDouble(point1), point.getDistanceDouble(point2));
        } else {
            return point.getDistanceDouble(projection);
        }

    }*/

//    public double getShortestDistance(Index point) {
//        DecimalPosition projection = projectOnInfiniteLine(point);
//
//        double xMin = Math.min(point1.getX(), point2.getX());
//        double xMax = Math.max(point1.getX(), point2.getX());
//        double yMin = Math.min(point1.getY(), point2.getY());
//        double yMax = Math.max(point1.getY(), point2.getY());
//
//        if (projection.getX() < xMin || projection.getY() < yMin || projection.getX() > xMax || projection.getY() > yMax) {
//            return Math.min(point.getDistance(point1), point.getDistance(point2));
//        } else {
//            return point.getDistance(projection);
//        }
//
//    }

  /*  public double getShortestDistanceOnInfiniteLine(Index point) {
        Index projection = projectOnInfiniteLine(point);
        return point.getDistanceDouble(projection);
    }*/

    /**
     * Project the given point on this line with infinite length
     * If the projection x or y will be negative, it returns null
     *
     * @param point point to project
     * @return projection
     */
   /* public Index projectOnInfiniteLine(Index point) {
        if (m == 0) {
            return new Index(point.getX(), point1.getY());
        } else if (Double.isInfinite(m)) {
            return new Index(point1.getX(), point.getY());
        }

        // m2 & c2 are the projection line which crosses this line orthogonally
        double m2 = 1.0 / -m;
        double c2 = (double) point.getY() - m2 * (double) point.getX();
        double x = (c2 - c) / (m - m2);
        double y = m2 * x + c2;
        return new Index((int) Math.round(x), (int) Math.round(y));
    }*/

    /**
     * Project the given point on this line with infinite length
     *
     * @param point point to project
     * @return projection
     */
    public DecimalPosition projectOnInfiniteLine(DecimalPosition point) {
        if (m == 0.0) {
            return new DecimalPosition(point.getX(), point1.getY());
        } else if (Double.isInfinite(m)) {
            return new DecimalPosition(point1.getX(), point.getY());
        }

        // mPerpen & cPerpen are the projection line which crosses this line perpendicularly
        double mPerpen = 1.0 / -m;
        double cPerpen = point.getY() - mPerpen * point.getX();
        double x = (cPerpen - c) / (m - mPerpen);
        double y = mPerpen * x + cPerpen;
        return new DecimalPosition(x, y);
    }


    public DecimalPosition getNearestPointOnLine(DecimalPosition point) {
        DecimalPosition projection = projectOnInfiniteLine(point);
        if (isPointInLineInclusive(projection)) {
            return projection;
        }
        if (projection.getDistance(point1) < projection.getDistance(point2)) {
            return new DecimalPosition(point1);
        } else {
            return new DecimalPosition(point2);
        }
    }

    public Index getCrossPoint(Line2I other) {
        DecimalPosition cross = getCrossInfinite(other);
        if (cross == null) {
            return null;
        }
        if (isPointInLineInclusive(cross) && other.isPointInLineInclusive(cross)) {
            return cross.getPositionRound();
        } else {
            return null;
        }
    }

    /**
     * Check if a point is on the line. Both end point are included
     *
     * @param point to check
     * @return true if point is in the line
     */
    public boolean isPointInLineInclusive(DecimalPosition point) {
        if (!Double.isInfinite(m)) {
            double y = calculateY(point.getX());
            if (Math.abs(y - point.getY()) > 0.000001) {
                return false;
            }
        }
        if (!MathHelper.compareWithPrecision(0.0, m)) {
            double x = calculateX(point.getY());
            if (Math.abs(x - point.getX()) > 0.000001) {
                return false;
            }
        }

        double xMin = Math.min(point1.getX(), point2.getX());
        double xMax = Math.max(point1.getX(), point2.getX());
        double yMin = Math.min(point1.getY(), point2.getY());
        double yMax = Math.max(point1.getY(), point2.getY());


        if (!MathHelper.compareWithPrecision(point.getX(), xMin, 0.000001) && point.getX() < xMin) {
            return false;
        }
        if (!MathHelper.compareWithPrecision(point.getY(), yMin, 0.000001) && point.getY() < yMin) {
            return false;
        }
        if (!MathHelper.compareWithPrecision(point.getX(), xMax, 0.000001) && point.getX() > xMax) {
            return false;
        }
        if (!MathHelper.compareWithPrecision(point.getY(), yMax, 0.000001) && point.getY() > yMax) {
            return false;
        }
        return true;
    }

    public double calculateX(double y) {
        if (Double.isInfinite(m)) {
            return point1.getX();
        } else if (Double.compare(0.0, m) == 0 || Double.compare(-0.0, m) == 0) {
            throw new IllegalStateException("Can not calculate X if m is zero");
        } else {
            return (y - c) / m;
        }
    }

    public double calculateY(double x) {
        if (Double.isInfinite(m)) {
            throw new IllegalStateException("Can not calculate Y if m is infinite");
        } else if (Double.compare(0.0, m) == 0 || Double.compare(-0.0, m) == 0) {
            return point1.getY();
        } else {
            return m * x + c;
        }
    }

    public DecimalPosition getCrossInfinite(Line2I line) {
        if (Double.compare(m, line.m) == 0
                || (Double.compare(Math.abs(m), 0.0) == 0 && Double.compare(Math.abs(line.m), 0.0) == 0)
                || (Double.isInfinite(m) && Double.isInfinite(line.m))) {
            return null;
        }

        double x;
        double y;
        if (Double.compare(Math.abs(m), 0.0) == 0) {
            y = point1.getY();
            x = line.calculateX(y);
        } else if (Double.compare(Math.abs(line.m), 0.0) == 0) {
            y = line.point1.getY();
            x = calculateX(y);
        } else if (Double.isInfinite(m)) {
            x = point1.getX();
            y = line.calculateY(x);
        } else if (Double.isInfinite(line.m)) {
            x = line.point1.getX();
            y = calculateY(x);
        } else {
            x = (line.c - c) / (m - line.m);
            y = calculateY(x);
        }

        return new DecimalPosition(x, y);
    }

//    public Line2I shiftParallelN(double distance) {
//        return new Line2I(new DecimalPosition(point1.getX(), point1.getY() - distance), new DecimalPosition(point2.getX(), point2.getY() - distance));
//    }
//
//    public Line2I shiftParallelE(double distance) {
//        return new Line2I(new DecimalPosition(point1.getX() + distance, point1.getY()), new DecimalPosition(point2.getX() + distance, point2.getY()));
//    }
//
//    public Line2I shiftParallelS(double distance) {
//        return new Line2I(new DecimalPosition(point1.getX(), point1.getY() + distance), new DecimalPosition(point2.getX(), point2.getY() + distance));
//    }
//
//    public Line2I shiftParallelW(double distance) {
//        return new Line2I(new DecimalPosition(point1.getX() - distance, point1.getY()), new DecimalPosition(point2.getX() - distance, point2.getY()));
//    }

//    public DecimalPosition getCrossInclusive(Line2I line) {
//        DecimalPosition point = getCrossInfinite(line);
//        if (point == null) {
//            return null;
//        }
//
//        if (!isPointInLineInclusive(point)) {
//            return null;
//        }
//        if (!line.isPointInLineInclusive(point)) {
//            return null;
//        }
//        return point;
//    }

    /**
     * Returns the end-point of this line (counter)clockwise relative to the given reference
     *
     * @return next point
     * @ param reference    reference point
     * @ param counterClock direction
     */
/*    public Index getEndPoint(Index reference, boolean counterClock) {
        double angel = -reference.getAngleToNord(point1);
        Index point1Rot = point1.rotateCounterClock(reference, angel);
        Index point2Rot = point2.rotateCounterClock(reference, angel);
        if (counterClock) {
            if (point1Rot.getX() < point2Rot.getX()) {
                return point1;
            } else {
                return point2;
            }
        } else {
            if (point1Rot.getX() > point2Rot.getX()) {
                return point1;
            } else {
                return point2;
            }
        }
    }*/
    public double getLength() {
        return point1.getDistance(point2);
    }

//    public static Line2I getNearestPoint(DecimalPosition point, Collection<Line2I> lines) {
//        Line2I nearestLineToPoint = null;
//        double minDistance = Double.MAX_VALUE;
//        for (Line2I line : lines) {
//            DecimalPosition nearest = line.getNearestPointOnLine(point);
//            double distance = point.getDistance(nearest);
//            if (minDistance > distance) {
//                minDistance = distance;
//                nearestLineToPoint = line;
//            }
//        }
//        if (nearestLineToPoint == null) {
//            throw new IllegalStateException();
//        }
//        return nearestLineToPoint;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line2I)) return false;

        Line2I line = (Line2I) o;

        return ((point1.equals(line.point1) && point2.equals(line.point2)) || point1.equals(line.point2) && point2.equals(line.point1));
    }

    @Override
    public int hashCode() {
        return point1.hashCode() + point2.hashCode();
    }


//    public boolean isOnNormSide(DecimalPosition position) {
//        if (norm == null) {
//            throw new IllegalStateException("Norm not set");
//        }
//        DecimalPosition projection = projectOnInfiniteLine(position);
//        DecimalPosition positionNorm = position.sub(projection).normalize();
//        return norm.dotProduct(positionNorm) < MathHelper.HALF_RADIANT;
//    }

    @Override
    public String toString() {
        return "Line{" +
                "point1=" + point1 +
                ", point2=" + point2 +
                ", m=" + m +
                ", c=" + c +
                '}';
    }
}
