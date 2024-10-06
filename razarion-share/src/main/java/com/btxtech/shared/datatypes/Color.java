package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;

import javax.persistence.Embeddable;
import java.util.List;

/**
 * Created by Beat
 * 11.04.2015.
 */
@Embeddable
public class Color {
    public static final Color GREY = new Color(0.5, 0.5, 0.5);
    double r; // Package private due to dominokit serialization. The variable is still readonly
    double g; // Protected due to dominokit serialization. The variable is still readonly
    double b; // Protected due to dominokit serialization. The variable is still readonly
    double a; // Protected due to dominokit serialization. The variable is still readonly

    public Color() {
    }

    public Color(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(double r, double g, double b) {
        this(r, g, b, 1.0);
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

    public List<Double> appendToColorRGBA(List<Double> doubleList) {
        doubleList.add(r);
        doubleList.add(g);
        doubleList.add(b);
        doubleList.add(a);
        return doubleList;
    }

    public List<Double> appendToColorRGB(List<Double> doubleList) {
        doubleList.add(r);
        doubleList.add(g);
        doubleList.add(b);
        return doubleList;
    }

    public String toHtmlColor() {
        return "#" + doubleToHexString(r) + doubleToHexString(g) + doubleToHexString(b);
    }

    private String doubleToHexString(double aDouble) {
        int intValue = (int) (aDouble * 255.0);
        if (intValue < 0) {
            intValue = 0;
        } else if (intValue > 255) {
            intValue = 255;
        }
        if (intValue < 10) {
            return "0" + Integer.toHexString(intValue).toUpperCase();
        } else {
            return Integer.toHexString(intValue).toUpperCase();
        }
    }

    /**
     * Mixes this color with another
     *
     * @param other other color to mix
     * @param value max value 0..1
     * @return return (1-value)*this + value*other
     */
    public Color mix(Color other, double value) {
        double clamped = MathHelper.clamp(value,0, 1);
        return new Color(InterpolationUtils.mix(r, other.r, clamped), InterpolationUtils.mix(g, other.g, clamped), InterpolationUtils.mix(b, other.b, clamped));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Color color = (Color) o;
        return Double.compare(color.r, r) == 0
                && Double.compare(color.g, g) == 0
                && Double.compare(color.b, b) == 0
                && Double.compare(color.a, a) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(r);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(g);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(b);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(a);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Color{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }

    public static Color fromHtmlColor(String colorStr) {
        return new Color(hexStringToDouble(colorStr.substring(1, 3)), hexStringToDouble(colorStr.substring(3, 5)), hexStringToDouble(colorStr.substring(5, 7)), 1.0);
    }

    private static double hexStringToDouble(String hexString) {
        int intVal = Integer.valueOf(hexString, 16);
        double aDouble = (double) intVal / 255.0;
        if (aDouble < 0) {
            aDouble = 0;
        } else if (aDouble > 1) {
            aDouble = 1;
        }
        return aDouble;
    }

    public static int getComponentsPerColorAlpha() {
        return 4;
    }

    public Color fromAlpha(double a) {
        return new Color(r, g, b, a);
    }
}
