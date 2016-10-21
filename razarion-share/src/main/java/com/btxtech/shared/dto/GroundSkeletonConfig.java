package com.btxtech.shared.dto;

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

    public Integer getTopTextureId() {
        return topTextureId;
    }

    public void setTopTextureId(Integer topTextureId) {
        this.topTextureId = topTextureId;
    }

    public Integer getTopBmId() {
        return topBmId;
    }

    public void setTopBmId(Integer topBmId) {
        this.topBmId = topBmId;
    }

    public double getTopBmScale() {
        return topBmScale;
    }

    public void setTopBmScale(double topBmScale) {
        this.topBmScale = topBmScale;
    }

    public Integer getSplattingId() {
        return splattingId;
    }

    public void setSplattingId(Integer splattingId) {
        this.splattingId = splattingId;
    }

    public Integer getBottomTextureId() {
        return bottomTextureId;
    }

    public double getSplattingScale() {
        return splattingScale;
    }

    public void setSplattingScale(double splattingScale) {
        this.splattingScale = splattingScale;
    }

    public void setBottomTextureId(Integer bottomTextureId) {
        this.bottomTextureId = bottomTextureId;
    }

    public double getBottomTextureScale() {
        return bottomTextureScale;
    }

    public void setBottomTextureScale(double bottomTextureScale) {
        this.bottomTextureScale = bottomTextureScale;
    }

    public Integer getBottomBmId() {
        return bottomBmId;
    }

    public void setBottomBmId(Integer bottomBmId) {
        this.bottomBmId = bottomBmId;
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

    public void setTopTextureScale(double topTextureScale) {
        this.topTextureScale = topTextureScale;
    }
}
