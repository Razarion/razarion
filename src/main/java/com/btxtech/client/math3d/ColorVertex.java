package com.btxtech.client.math3d;

import java.util.List;

/**
 * Created by Beat
 * 16.04.2015.
 */
public class ColorVertex extends Vertex {
    private double r;
    private double g;
    private double b;
    private double a;

    public ColorVertex(double x, double y, double z, double r, double g, double b, double a) {
        super(x, y, z);
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ColorVertex(double x, double y, double z, double r, double g, double b) {
        this(x, y, z, r, g, b, 1.0);
    }

    public double getR() {
        return r;
    }

    public double getG() {
        return g;
    }

    public double getB() {
        return b;
    }

    public double getA() {
        return a;
    }

    public Color getColor() {
        return new Color(r, g, b, a);
    }

    @Override
    public ColorVertex add(double x, double y, double z) {
        Vertex vertex = super.add(x, y, z);
        return new ColorVertex(vertex.getX(), vertex.getY(), vertex.getZ(), r, g, b, a);
    }

    public ColorVertex add(double x, double y, double z, double r, double g, double b) {
        Vertex vertex = super.add(x, y, z);
        return new ColorVertex(vertex.getX(), vertex.getY(), vertex.getZ(), r, g, b, a);
    }

    public List<Double> appendToColor(List<Double> doubleList) {
        doubleList.add(r);
        doubleList.add(g);
        doubleList.add(b);
        doubleList.add(a);
        return doubleList;
    }
}
