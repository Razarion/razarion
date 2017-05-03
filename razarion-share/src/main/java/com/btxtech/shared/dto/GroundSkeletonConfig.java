package com.btxtech.shared.dto;

import com.btxtech.shared.utils.CollectionUtils;

/**
 * Created by Beat
 * 07.05.2016.
 */
public class GroundSkeletonConfig {
    private int id;
    private Integer topTextureId;
    private double topTextureScale;
    private Integer topBmId;
    private double topBmScale;
    private double topBmDepth;
    private Integer bottomTextureId;
    private double bottomTextureScale;
    private Integer bottomBmId;
    private double bottomBmScale;
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
    private Integer splattingId;
    private double splattingScale;

    public int getId() {
        return id;
    }

    public GroundSkeletonConfig setId(int id) {
        this.id = id;
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

    public double[][] getSplattings() {
        return splattings;
    }

    public GroundSkeletonConfig setSplattings(double[][] splattings) {
        this.splattings = splattings;
        return this;
    }

    public int getSplattingXCount() {
        return splattingXCount;
    }

    public GroundSkeletonConfig setSplattingXCount(int splattingXCount) {
        this.splattingXCount = splattingXCount;
        return this;
    }

    public int getSplattingYCount() {
        return splattingYCount;
    }

    public GroundSkeletonConfig setSplattingYCount(int splattingYCount) {
        this.splattingYCount = splattingYCount;
        return this;
    }

    public double getTopBmDepth() {
        return topBmDepth;
    }

    public GroundSkeletonConfig setTopBmDepth(double topBmDepth) {
        this.topBmDepth = topBmDepth;
        return this;
    }

    public double getBottomBmDepth() {
        return bottomBmDepth;
    }

    public GroundSkeletonConfig setBottomBmDepth(double bottomBmDepth) {
        this.bottomBmDepth = bottomBmDepth;
        return this;
    }

    public LightConfig getLightConfig() {
        return lightConfig;
    }

    public GroundSkeletonConfig setLightConfig(LightConfig lightConfig) {
        this.lightConfig = lightConfig;
        return this;
    }

    public Integer getTopTextureId() {
        return topTextureId;
    }

    public GroundSkeletonConfig setTopTextureId(Integer topTextureId) {
        this.topTextureId = topTextureId;
        return this;
    }

    public Integer getTopBmId() {
        return topBmId;
    }

    public GroundSkeletonConfig setTopBmId(Integer topBmId) {
        this.topBmId = topBmId;
        return this;
    }

    public double getTopBmScale() {
        return topBmScale;
    }

    public GroundSkeletonConfig setTopBmScale(double topBmScale) {
        this.topBmScale = topBmScale;
        return this;
    }

    public Integer getSplattingId() {
        return splattingId;
    }

    public GroundSkeletonConfig setSplattingId(Integer splattingId) {
        this.splattingId = splattingId;
        return this;
    }

    public Integer getBottomTextureId() {
        return bottomTextureId;
    }

    public double getSplattingScale() {
        return splattingScale;
    }

    public GroundSkeletonConfig setSplattingScale(double splattingScale) {
        this.splattingScale = splattingScale;
        return this;
    }

    public void setBottomTextureId(Integer bottomTextureId) {
        this.bottomTextureId = bottomTextureId;
    }

    public double getBottomTextureScale() {
        return bottomTextureScale;
    }

    public GroundSkeletonConfig setBottomTextureScale(double bottomTextureScale) {
        this.bottomTextureScale = bottomTextureScale;
        return this;
    }

    public Integer getBottomBmId() {
        return bottomBmId;
    }

    public GroundSkeletonConfig setBottomBmId(Integer bottomBmId) {
        this.bottomBmId = bottomBmId;
        return this;
    }

    public double getBottomBmScale() {
        return bottomBmScale;
    }

    public void setBottomBmScale(double bottomBmScale) {
        this.bottomBmScale = bottomBmScale;
    }

    public double getTopTextureScale() {
        return topTextureScale;
    }

    public GroundSkeletonConfig setTopTextureScale(double topTextureScale) {
        this.topTextureScale = topTextureScale;
        return this;
    }
}
