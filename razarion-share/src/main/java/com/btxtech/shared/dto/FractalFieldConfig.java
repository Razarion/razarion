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

    public FractalFieldConfig setXCount(int xCount) {
        this.xCount = xCount;
        return this;
    }

    public int getYCount() {
        return yCount;
    }

    public FractalFieldConfig setYCount(int yCount) {
        this.yCount = yCount;
        return this;
    }

    public double getFractalMin() {
        return fractalMin;
    }

    public FractalFieldConfig setFractalMin(double fractalMin) {
        this.fractalMin = fractalMin;
        return this;
    }

    public double getFractalMax() {
        return fractalMax;
    }

    public FractalFieldConfig setFractalMax(double fractalMax) {
        this.fractalMax = fractalMax;
        return this;
    }

    public double getFractalRoughness() {
        return fractalRoughness;
    }

    public FractalFieldConfig setFractalRoughness(double fractalRoughness) {
        this.fractalRoughness = fractalRoughness;
        return this;
    }

    public Double getClampMin() {
        return clampMin;
    }

    public FractalFieldConfig setClampMin(Double clampMin) {
        this.clampMin = clampMin;
        return this;
    }

    public Double getClampMax() {
        return clampMax;
    }

    public FractalFieldConfig setClampMax(Double clampMax) {
        this.clampMax = clampMax;
        return this;
    }
}
