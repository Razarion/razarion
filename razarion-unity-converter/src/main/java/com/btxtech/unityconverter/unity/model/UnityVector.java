package com.btxtech.unityconverter.unity.model;

public class UnityVector {
    private double x;
    private double y;
    private double z;
    private double w;

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

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public UnityVector x(double x) {
        setX(x);
        return this;
    }

    public UnityVector y(double y) {
        setY(y);
        return this;
    }

    public UnityVector z(double z) {
        setZ(z);
        return this;
    }

    public UnityVector w(double w) {
        setW(w);
        return this;
    }

    @Override
    public String toString() {
        return "UnityVector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }
}
