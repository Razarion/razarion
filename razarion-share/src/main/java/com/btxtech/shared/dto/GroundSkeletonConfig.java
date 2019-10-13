package com.btxtech.shared.dto;

import com.btxtech.shared.utils.CollectionUtils;

/**
 * Created by Beat
 * 07.05.2016.
 */
public class GroundSkeletonConfig {
    private int id;
    private PhongMaterialConfig topTexture;
    private PhongMaterialConfig bottomTexture;
    private ImageScaleConfig splatting;
    private double splattingScale2;
    private double splattingFadeThreshold;
    private double splattingOffset;
    // errai: setter and getter must be available for arrays.Otherwise the array is always null
    @Deprecated
    private double[][] heights;
    @Deprecated
    private int heightXCount;
    @Deprecated
    private int heightYCount;

    public GroundSkeletonConfig setId(int id) {
        this.id = id;
        return this;
    }

    public int getId() {
        return id;
    }

    public PhongMaterialConfig getTopTexture() {
        return topTexture;
    }

    public GroundSkeletonConfig setTopTexture(PhongMaterialConfig topTexture) {
        this.topTexture = topTexture;
        return this;
    }

    public PhongMaterialConfig getBottomTexture() {
        return bottomTexture;
    }

    public GroundSkeletonConfig setBottomTexture(PhongMaterialConfig bottomTexture) {
        this.bottomTexture = bottomTexture;
        return this;
    }

    public ImageScaleConfig getSplatting() {
        return splatting;
    }

    public GroundSkeletonConfig setSplatting(ImageScaleConfig splatting) {
        this.splatting = splatting;
        return this;
    }

    public double getSplattingScale2() {
        return splattingScale2;
    }

    public GroundSkeletonConfig setSplattingScale2(double splattingScale2) {
        this.splattingScale2 = splattingScale2;
        return this;
    }

    public double getSplattingFadeThreshold() {
        return splattingFadeThreshold;
    }

    public GroundSkeletonConfig setSplattingFadeThreshold(double splattingFadeThreshold) {
        this.splattingFadeThreshold = splattingFadeThreshold;
        return this;
    }

    public double getSplattingOffset() {
        return splattingOffset;
    }

    public GroundSkeletonConfig setSplattingOffset(double splattingOffset) {
        this.splattingOffset = splattingOffset;
        return this;
    }

    public double[][] getHeights() {
        return heights;
    }

    public double getHeight(int x, int y) {
        return heights[CollectionUtils.getCorrectedIndex(x, heightXCount)][CollectionUtils.getCorrectedIndexInvert(y, heightYCount)];
    }

    public GroundSkeletonConfig setHeights(double[][] heights) {
        this.heights = heights;
        return this;
    }

    public int getHeightXCount() {
        return heightXCount;
    }

    public GroundSkeletonConfig setHeightXCount(int heightXCount) {
        this.heightXCount = heightXCount;
        return this;
    }

    public int getHeightYCount() {
        return heightYCount;
    }

    public GroundSkeletonConfig setHeightYCount(int heightYCount) {
        this.heightYCount = heightYCount;
        return this;
    }

}
