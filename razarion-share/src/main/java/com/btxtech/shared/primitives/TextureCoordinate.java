package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 12.04.2015.
 */
@Portable
public class TextureCoordinate {
    // x Axis
    private double s;
    // y Axis
    private double t;

    // Used by Errai
    public TextureCoordinate() {
    }

    public TextureCoordinate(DecimalPosition decimalPosition) {
        s = decimalPosition.getX();
        t = decimalPosition.getY();
    }

    public TextureCoordinate(double s, double t) {
        if (Double.isInfinite(s) || Double.isNaN(s)) {
            throw new IllegalArgumentException("Can not set s value in DecimalPosition: " + s);
        }
        if (Double.isInfinite(t) || Double.isNaN(t)) {
            throw new IllegalArgumentException("Can not set t value in DecimalPosition: " + t);
        }
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

    public TextureCoordinate add(TextureCoordinate textureCoordinate) {
        return new TextureCoordinate(this.s + textureCoordinate.s, this.t + textureCoordinate.t);
    }

    public TextureCoordinate sub(double s, double t) {
        return new TextureCoordinate(this.s - s, this.t - t);
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

    public DecimalPosition toDecimalPosition() {
        return new DecimalPosition(s, t);
    }

    @Override
    public String toString() {
        return "TextureCoordinate{" +
                "s=" + s +
                ", t=" + t +
                '}';
    }
}
