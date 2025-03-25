package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 20.03.2016.
 */
public class Line3d {
    private Vertex point;
    private Vertex direction;

    /**
     * Used by Errai marshaller
     */
    public Line3d() {
    }

    public Line3d(Vertex point, Vertex direction) {
        this.point = point;
        this.direction = direction;
    }

    public Vertex getPoint() {
        return point;
    }

    public Vertex getDirection() {
        return direction;
    }

    public Vertex calculatePoint(double multiplier) {
        return point.add(direction.multiply(multiplier));
    }

    public Vertex calculatePositionOnHeightLevel(double height) {
        double m = (height - point.getZ()) / direction.getZ();
        return calculatePoint(m);
    }

    //    public Vertex projectOnInfiniteLine(Vertex source) {
//        Vertex relativePoint2 = point2.sub(point1);
//        Vertex relativeSource = source.sub(point1);
//
//        double dotSource = relativePoint2.dot(relativeSource);
//        double dotLine = relativePoint2.dot(relativePoint2);
//
//        return point1.add(relativePoint2.multiply(dotSource / dotLine));
//    }


//    public Vertex getCross(Line3d line2) {
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Line3d line3d = (Line3d) o;
        return point.equals(line3d.point) && direction.equals(line3d.direction);
    }

    @Override
    public int hashCode() {
        int result = point.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Line3d{" +
                "point=" + point +
                ", direction=" + direction +
                '}';
    }
}
