package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 07.05.2016.
 */
@Bindable
@Portable
public class GroundSkeleton {
    private int id;
    private double topBmDepth;
    private double bottomBmDepth;
    // errai: setter and getter must be available for arrays.Otherwise the array is always null
    private double[][] heights;
    private int heightXCount;
    private int heightYCount;
    // errai: setter and getter must be available for arrays.Otherwise the array is always null
    private double[][] splattings;
    private int splattingXCount;
    private int splattingYCount;
    private LightConfig lightConfig;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double[][] getHeights() {
        return heights;
    }

    public void setHeights(double[][] heights) {
        this.heights = heights;
    }

    public int getHeightXCount() {
        return heightXCount;
    }

    public void setHeightXCount(int heightXCount) {
        this.heightXCount = heightXCount;
    }

    public int getHeightYCount() {
        return heightYCount;
    }

    public void setHeightYCount(int heightYCount) {
        this.heightYCount = heightYCount;
    }

    public double[][] getSplattings() {
        return splattings;
    }

    public void setSplattings(double[][] splattings) {
        this.splattings = splattings;
    }

    public int getSplattingXCount() {
        return splattingXCount;
    }

    public void setSplattingXCount(int splattingXCount) {
        this.splattingXCount = splattingXCount;
    }

    public int getSplattingYCount() {
        return splattingYCount;
    }

    public void setSplattingYCount(int splattingYCount) {
        this.splattingYCount = splattingYCount;
    }

    public double getTopBmDepth() {
        return topBmDepth;
    }

    public void setTopBmDepth(double topBmDepth) {
        this.topBmDepth = topBmDepth;
    }

    public double getBottomBmDepth() {
        return bottomBmDepth;
    }

    public void setBottomBmDepth(double bottomBmDepth) {
        this.bottomBmDepth = bottomBmDepth;
    }

    public LightConfig getLightConfig() {
        return lightConfig;
    }

    public void setLightConfig(LightConfig lightConfig) {
        this.lightConfig = lightConfig;
    }
}
