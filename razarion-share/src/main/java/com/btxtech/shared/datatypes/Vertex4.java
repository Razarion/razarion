package com.btxtech.shared.datatypes;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 05.04.2015.
 */
@Portable
public class Vertex4 {
    private double x;
    private double y;
    private double z;
    private double w;

    // Used by Errai
    public Vertex4() {
    }

    public Vertex4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vertex4(Vertex vertex, double w) {
        this.x = vertex.getX();
        this.y = vertex.getY();
        this.z = vertex.getZ();
        this.w = w;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getW() {
        return w;
    }

    @Override
    public String toString() {
        return "Vertex4{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }
}