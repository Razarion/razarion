package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 20.03.2016.
 */
public class Line3d {
    private Vertex point;
    private Vertex direction;

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
    public String toString() {
        return "Line3d{" +
                "point=" + point +
                ", direction=" + direction +
                '}';
    }
}
