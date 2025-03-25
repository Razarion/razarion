package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class Triangle3D {
    private final Vertex pointA;
    private final Vertex pointB;
    private final Vertex pointC;

    public Triangle3D(Vertex pointA, Vertex pointB, Vertex pointC) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
    }

    public Vertex getPointA() {
        return pointA;
    }

    public Vertex getPointB() {
        return pointB;
    }

    public Vertex getPointC() {
        return pointC;
    }

    /**
     * http://geomalgorithms.com/a06-_intersect-2.html
     *
     * @param line line
     * @return position on the triangle or null if not inside
     */
    public Vertex calculateCross(Line3d line) {
        Plane3d plane = new Plane3d(pointA, pointB, pointC);
        Vertex planePosition = plane.crossPoint(line);
        if (planePosition == null) {
            return null;
        }

        Vertex u = pointB.sub(pointA);
        Vertex v = pointC.sub(pointA);
        Vertex w = planePosition.sub(pointA);

        double uv = u.dot(v);
        double wv = w.dot(v);
        double vv = v.dot(v);
        double uu = u.dot(u);
        double wu = w.dot(u);

        double denominator = (uv * uv - uu * vv);

        double s = (uv * wv - vv * wu) / denominator;
        double t = (uv * wu - uu * wv) / denominator;

        if (s >= 0.0 && t >= 0.0 && s + t <= 1.0) {
            return planePosition;
        } else {
            // Not on triangle
            return null;
        }

    }

    @Override
    public String toString() {
        return "Triangle3D{" +
                "pointA=" + pointA +
                ", pointB=" + pointB +
                ", pointC=" + pointC +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Triangle3D that = (Triangle3D) o;

        return pointA.equals(that.pointA) && pointB.equals(that.pointB) && pointC.equals(that.pointC);
    }

    @Override
    public int hashCode() {
        int result = pointA.hashCode();
        result = 31 * result + pointB.hashCode();
        result = 31 * result + pointC.hashCode();
        return result;
    }
}
