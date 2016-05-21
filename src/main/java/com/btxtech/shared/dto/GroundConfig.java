package com.btxtech.shared.dto;

import com.btxtech.client.terrain.FractalFiledConfig;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 07.05.2016.
 */
@Bindable
@Portable
public class GroundConfig {
    private GroundSkeleton groundSkeleton;
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

    public GroundSkeleton getGroundSkeleton() {
        return groundSkeleton;
    }

    public void setGroundSkeleton(GroundSkeleton groundSkeleton) {
        this.groundSkeleton = groundSkeleton;
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

    public FractalFiledConfig toSplattingFractalFiledConfig() {
        FractalFiledConfig fractalFiledConfig = new FractalFiledConfig();
        fractalFiledConfig.setFractalMin(splattingFractalMin);
        fractalFiledConfig.setFractalMax(splattingFractalMax);
        fractalFiledConfig.setClampMin(splattingFractalClampMin);
        fractalFiledConfig.setClampMax(splattingFractalClampMax);
        fractalFiledConfig.setXCount(groundSkeleton.getSplattingXCount());
        fractalFiledConfig.setYCount(groundSkeleton.getSplattingYCount());
        fractalFiledConfig.setFractalRoughness(splattingFractalRoughness);
        fractalFiledConfig.setFractalField(groundSkeleton.getSplattings());
        return fractalFiledConfig;
    }

    public void fromSplattingFractalFiledConfig(FractalFiledConfig fractalFiledConfig) {
        splattingFractalMin = fractalFiledConfig.getFractalMin();
        splattingFractalMax = fractalFiledConfig.getFractalMax();
        splattingFractalClampMin = fractalFiledConfig.getClampMin();
        splattingFractalClampMax = fractalFiledConfig.getClampMax();
        splattingFractalRoughness = fractalFiledConfig.getFractalRoughness();
        groundSkeleton.setSplattingXCount(fractalFiledConfig.getXCount());
        groundSkeleton.setSplattingYCount(fractalFiledConfig.getYCount());
        groundSkeleton.setSplattings(fractalFiledConfig.getClampedFractalField());
    }

    public FractalFiledConfig toHeightFractalFiledConfig() {
        FractalFiledConfig fractalFiledConfig = new FractalFiledConfig();
        fractalFiledConfig.setFractalMin(heightFractalMin);
        fractalFiledConfig.setFractalMax(heightFractalMax);
        fractalFiledConfig.setClampMin(heightFractalClampMin);
        fractalFiledConfig.setClampMax(heightFractalClampMax);
        fractalFiledConfig.setXCount(groundSkeleton.getHeightXCount());
        fractalFiledConfig.setYCount(groundSkeleton.getHeightYCount());
        fractalFiledConfig.setFractalRoughness(heightFractalRoughness);
        fractalFiledConfig.setFractalField(groundSkeleton.getHeights());
        return fractalFiledConfig;
    }

    public void fromHeightFractalFiledConfig(FractalFiledConfig fractalFiledConfig) {
        heightFractalMin = fractalFiledConfig.getFractalMin();
        heightFractalMax = fractalFiledConfig.getFractalMax();
        heightFractalClampMin = fractalFiledConfig.getClampMin();
        heightFractalClampMax = fractalFiledConfig.getClampMax();
        heightFractalRoughness = fractalFiledConfig.getFractalRoughness();
        groundSkeleton.setHeightXCount(fractalFiledConfig.getXCount());
        groundSkeleton.setHeightYCount(fractalFiledConfig.getYCount());
        groundSkeleton.setHeights(fractalFiledConfig.getClampedFractalField());

    }
}
