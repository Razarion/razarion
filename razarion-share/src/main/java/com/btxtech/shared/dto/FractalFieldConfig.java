package com.btxtech.shared.dto;

import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 20.05.2016.
 */
public class FractalFieldConfig {
    // private static Logger logger = Logger.getLogger(FractalFieldConfig.class.getName());
    private int xCount;
    private int yCount;
    private double fractalMin;
    private double fractalMax;
    private double fractalRoughness;
    private double clampMin;
    private double clampMax;

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
}
