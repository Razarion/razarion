package com.btxtech.shared.dto;

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
    private double heightFractalShift;
    private double splattingFractalRoughness;
    private double splattingFractalMin;
    private double splattingFractalMax;

    public GroundSkeleton getGroundSkeleton() {
        return groundSkeleton;
    }

    public void setGroundSkeleton(GroundSkeleton groundSkeleton) {
        this.groundSkeleton = groundSkeleton;
    }

    public double getHeightFractalRoughness() {
        return heightFractalRoughness;
    }

    public void setHeightFractalRoughness(double heightFractalRoughness) {
        this.heightFractalRoughness = heightFractalRoughness;
    }

    public double getHeightFractalShift() {
        return heightFractalShift;
    }

    public void setHeightFractalShift(double heightFractalShift) {
        this.heightFractalShift = heightFractalShift;
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
}
