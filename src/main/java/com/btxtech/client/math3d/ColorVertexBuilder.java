package com.btxtech.client.math3d;

/**
 * Created by Beat
 * 16.04.2015.
 */
public class ColorVertexBuilder {
    private double x;
    private double y;
    private double z;

    private double r;
    private double g;
    private double b;
    private double a;

    public ColorVertexBuilder(ColorVertex colorVertex) {
        x = colorVertex.getX();
        y = colorVertex.getY();
        z = colorVertex.getZ();
        r = colorVertex.getR();
        g = colorVertex.getG();
        b = colorVertex.getB();
        a = colorVertex.getA();
    }

    public ColorVertexBuilder(double x, double y, double z, double r, double g, double b, double a) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setColor(Color color) {
        r = color.getR();
        g = color.getG();
        b = color.getB();
        a = color.getA();
    }

    public Color getColor() {
        return new Color(r, g, b, a);
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public ColorVertex toColorVertex() {
        return new ColorVertex(x, y, z, r, g, b, a);
    }
}
