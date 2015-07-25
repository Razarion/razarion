package com.btxtech.client.math3d;

import com.btxtech.game.jsre.client.common.DecimalPosition;

import java.util.List;

/**
 * Created by Beat
 * 12.04.2015.
 */
public class TextureCoordinate {
    // x Axis
    private double s;
    // y Axis
    private double t;

    public TextureCoordinate(DecimalPosition decimalPosition) {
        s = decimalPosition.getX();
        t = decimalPosition.getY();
    }

    public TextureCoordinate(double s, double t) {
        this.s = s;
        this.t = t;
    }

    public List<Double> appendTo(List<Double> doubleList) {
        doubleList.add(s);
        doubleList.add(t);
        return doubleList;
    }

    public TextureCoordinate divide(double pixels) {
        return new TextureCoordinate(s / pixels, t / pixels);
    }

    public TextureCoordinate add(double s, double t) {
        return new TextureCoordinate(this.s + s, this.t + t);
    }

    public static int getComponentCount() {
        return 2;
    }

    public double getS() {
        return s;
    }

    public double getT() {
        return t;
    }
}
