package com.btxtech.shared.gameengine.datatypes.config;

public class SlopeGroundSplattingConfig {
    private Integer imageId;
    private Double scale;
    private Double fadeThreshold;
    private Double offset;
    private Double impact;

    public Integer getImageId() {
        return imageId;
    }

    public SlopeGroundSplattingConfig setImageId(Integer imageId) {
        this.imageId = imageId;
        return this;
    }

    public Double getScale() {
        return scale;
    }

    public SlopeGroundSplattingConfig setScale(Double scale) {
        this.scale = scale;
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

    public Double getImpact() {
        return impact;
    }

    public SlopeGroundSplattingConfig setImpact(Double impact) {
        this.impact = impact;
        return this;
    }
}
