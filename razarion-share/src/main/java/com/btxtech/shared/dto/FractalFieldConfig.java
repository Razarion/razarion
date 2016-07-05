package com.btxtech.shared.dto;

import com.btxtech.shared.utils.MathHelper;
import com.btxtech.shared.utils.MathHelper2;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Bindable
public class FractalFieldConfig {
    // private Logger logger = Logger.getLogger(FractalFieldConfig.class.getName());
    private int xCount;
    private int yCount;
    private double fractalMin;
    private double fractalMax;
    private double fractalRoughness;
    private double clampMin;
    private double clampMax;
    private double[][] fractalField;
    private double[][] clampedFractalField;

    public int getXCount() {
        return xCount;
    }

    public void setXCount(int xCount) {
        this.xCount = xCount;
    }

    public int getYCount() {
        return yCount;
    }

    public void setYCount(int yCount) {
        this.yCount = yCount;
    }

    public double getFractalMin() {
        return fractalMin;
    }

    public void setFractalMin(double fractalMin) {
        this.fractalMin = fractalMin;
    }

    public double getFractalMax() {
        return fractalMax;
    }

    public void setFractalMax(double fractalMax) {
        this.fractalMax = fractalMax;
    }

    public double getFractalRoughness() {
        return fractalRoughness;
    }

    public void setFractalRoughness(double fractalRoughness) {
        this.fractalRoughness = fractalRoughness;
    }

    public Double getClampMin() {
        return clampMin;
    }

    public void setClampMin(Double clampMin) {
        this.clampMin = clampMin;
    }

    public Double getClampMax() {
        return clampMax;
    }

    public void setClampMax(Double clampMax) {
        this.clampMax = clampMax;
    }

    public void setFractalField(double[][] fractalField) {
        this.fractalField = fractalField;
    }

    public double[][] getClampedFractalField() {
        if (clampedFractalField != null) {
            return clampedFractalField;
        } else {
            return fractalField;
        }
    }

    public void clamp() {
        if (this.clampMax < 1.0 || this.clampMin > 0.0) {
            double minEdge = MathHelper2.mix(fractalMin, fractalMax, clampMin);
            double maxEdge = MathHelper2.mix(fractalMin, fractalMax, clampMax);
            clampedFractalField = new double[xCount][yCount];
            for (int x = 0; x < xCount; x++) {
                for (int y = 0; y < yCount; y++) {
                    clampedFractalField[x][y] = MathHelper.clamp(fractalField[x][y], minEdge, maxEdge, fractalMin, fractalMax);
                }
            }
        } else {
            clampedFractalField = null;
        }
    }
}
