package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 18.04.2016.
 */
public class Ray3d {
    private Vertex start;
    private Vertex direction;

    public Ray3d(Vertex start, Vertex direction) {
        this.start = start;
        this.direction = direction;
    }

    public Vertex getStart() {
        return start;
    }

    public Vertex getDirection() {
        return direction;
    }

    public Vertex getPoint(double multiplier) {
        return start.add(direction.multiply(multiplier));
    }

    @Override
    public String toString() {
        return "Ray3d{" +
                "start=" + start +
                ", direction=" + direction +
                '}';
    }
}
