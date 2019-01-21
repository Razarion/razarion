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
    private double splattingFractalRoughness;
    private double splattingFractalMin;
    private double splattingFractalMax;
    private double splattingFractalClampMin;
    private double splattingFractalClampMax;

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

    public double getSplattingFractalRoughness() {
        return splattingFractalRoughness;
    }

    public void setSplattingFractalRoughness(double splattingFractalRoughness) {
        this.splattingFractalRoughness = splattingFractalRoughness;
    }

    public double getSplattingFractalMin() {
        return splattingFractalMin;
    }

    public void setSplattingFractalMin(double splattingFractalMin) {
        this.splattingFractalMin = splattingFractalMin;
    }

    public double getSplattingFractalMax() {
        return splattingFractalMax;
    }

    public void setSplattingFractalMax(double splattingFractalMax) {
        this.splattingFractalMax = splattingFractalMax;
    }

    public double getSplattingFractalClampMin() {
        return splattingFractalClampMin;
    }

    public void setSplattingFractalClampMin(double splattingFractalClampMin) {
        this.splattingFractalClampMin = splattingFractalClampMin;
    }

    public double getSplattingFractalClampMax() {
        return splattingFractalClampMax;
    }

    public void setSplattingFractalClampMax(double splattingFractalClampMax) {
        this.splattingFractalClampMax = splattingFractalClampMax;
    }

    public FractalFieldConfig toSplattingFractalFiledConfig() {
        FractalFieldConfig fractalFieldConfig = new FractalFieldConfig();
        fractalFieldConfig.setFractalMin(splattingFractalMin);
        fractalFieldConfig.setFractalMax(splattingFractalMax);
        fractalFieldConfig.setClampMin(splattingFractalClampMin);
        fractalFieldConfig.setClampMax(splattingFractalClampMax);
        fractalFieldConfig.setXCount(groundSkeletonConfig.getSplattingXCount());
        fractalFieldConfig.setYCount(groundSkeletonConfig.getSplattingYCount());
        fractalFieldConfig.setFractalRoughness(splattingFractalRoughness);
        return fractalFieldConfig;
    }

    public void fromSplattingFractalFiledConfig(FractalFieldConfig fractalFieldConfig) {
        splattingFractalMin = fractalFieldConfig.getFractalMin();
        splattingFractalMax = fractalFieldConfig.getFractalMax();
        splattingFractalClampMin = fractalFieldConfig.getClampMin();
        splattingFractalClampMax = fractalFieldConfig.getClampMax();
        splattingFractalRoughness = fractalFieldConfig.getFractalRoughness();
        groundSkeletonConfig.setSplattingXCount(fractalFieldConfig.getXCount());
        groundSkeletonConfig.setSplattingYCount(fractalFieldConfig.getYCount());
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
