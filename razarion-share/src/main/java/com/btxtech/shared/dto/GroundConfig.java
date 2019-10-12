package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 07.05.2016.
 */
public class GroundConfig {
    private GroundSkeletonConfig groundSkeletonConfig;
    private double heightFractalRoughness;
    private double heightFractalMin;
    private double heightFractalMax;
    private double heightFractalClampMin;
    private double heightFractalClampMax;

    public GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public void setGroundSkeletonConfig(GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
    }

    public double getHeightFractalMin() {
        return heightFractalMin;
    }

    public void setHeightFractalMin(double heightFractalMin) {
        this.heightFractalMin = heightFractalMin;
    }

    public double getHeightFractalMax() {
        return heightFractalMax;
    }

    public void setHeightFractalMax(double heightFractalMax) {
        this.heightFractalMax = heightFractalMax;
    }

    public double getHeightFractalClampMin() {
        return heightFractalClampMin;
    }

    public void setHeightFractalClampMin(double heightFractalClampMin) {
        this.heightFractalClampMin = heightFractalClampMin;
    }

    public double getHeightFractalClampMax() {
        return heightFractalClampMax;
    }

    public void setHeightFractalClampMax(double heightFractalClampMax) {
        this.heightFractalClampMax = heightFractalClampMax;
    }

    public double getHeightFractalRoughness() {
        return heightFractalRoughness;
    }

    public void setHeightFractalRoughness(double heightFractalRoughness) {
        this.heightFractalRoughness = heightFractalRoughness;
    }

    public FractalFieldConfig toHeightFractalFiledConfig() {
        FractalFieldConfig fractalFieldConfig = new FractalFieldConfig();
        fractalFieldConfig.setFractalMin(heightFractalMin);
        fractalFieldConfig.setFractalMax(heightFractalMax);
        fractalFieldConfig.setClampMin(heightFractalClampMin);
        fractalFieldConfig.setClampMax(heightFractalClampMax);
        fractalFieldConfig.setXCount(groundSkeletonConfig.getHeightXCount());
        fractalFieldConfig.setYCount(groundSkeletonConfig.getHeightYCount());
        fractalFieldConfig.setFractalRoughness(heightFractalRoughness);
        return fractalFieldConfig;
    }

    public void fromHeightFractalFiledConfig(FractalFieldConfig fractalFieldConfig) {
        heightFractalMin = fractalFieldConfig.getFractalMin();
        heightFractalMax = fractalFieldConfig.getFractalMax();
        heightFractalClampMin = fractalFieldConfig.getClampMin();
        heightFractalClampMax = fractalFieldConfig.getClampMax();
        heightFractalRoughness = fractalFieldConfig.getFractalRoughness();
        groundSkeletonConfig.setHeightXCount(fractalFieldConfig.getXCount());
        groundSkeletonConfig.setHeightYCount(fractalFieldConfig.getYCount());
    }
}
