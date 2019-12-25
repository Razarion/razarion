package com.btxtech.shared.gameengine.datatypes.config;

public class SlopeGroundSplattingConfig {
    private Integer imageId;
    private Double scale1;
    private Double scale2;
    private Double fadeThreshold;
    private Double offset;

    public Integer getImageId() {
        return imageId;
    }

    public SlopeGroundSplattingConfig setImageId(Integer imageId) {
        this.imageId = imageId;
        return this;
    }

    public Double getScale1() {
        return scale1;
    }

    public SlopeGroundSplattingConfig setScale1(Double scale1) {
        this.scale1 = scale1;
        return this;
    }

    public Double getScale2() {
        return scale2;
    }

    public SlopeGroundSplattingConfig setScale2(Double scale2) {
        this.scale2 = scale2;
        return this;
    }

    public Double getFadeThreshold() {
        return fadeThreshold;
    }

    public SlopeGroundSplattingConfig setFadeThreshold(Double fadeThreshold) {
        this.fadeThreshold = fadeThreshold;
        return this;
    }

    public Double getOffset() {
        return offset;
    }

    public SlopeGroundSplattingConfig setOffset(Double offset) {
        this.offset = offset;
        return this;
    }
}
