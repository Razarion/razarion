package com.btxtech.shared.gameengine.datatypes.config;

public class ShallowWaterConfig  {
    private Integer textureId;
    private Double scale;
    private Integer distortionId;
    private Double distortionStrength;
    private Integer stencilId;
    private Double durationSeconds;

    public Integer getTextureId() {
        return textureId;
    }

    public void setTextureId(Integer textureId) {
        this.textureId = textureId;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Integer getDistortionId() {
        return distortionId;
    }

    public void setDistortionId(Integer distortionId) {
        this.distortionId = distortionId;
    }

    public Double getDistortionStrength() {
        return distortionStrength;
    }

    public void setDistortionStrength(Double distortionStrength) {
        this.distortionStrength = distortionStrength;
    }

    public Integer getStencilId() {
        return stencilId;
    }

    public void setStencilId(Integer stencilId) {
        this.stencilId = stencilId;
    }

    public Double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public ShallowWaterConfig textureId(Integer textureId) {
        setTextureId(textureId);
        return this;
    }

    public ShallowWaterConfig scale(Double scale) {
        setScale(scale);
        return this;
    }

    public ShallowWaterConfig distortionId(Integer distortionId) {
        setDistortionId(distortionId);
        return this;
    }

    public ShallowWaterConfig distortionStrength(Double distortionStrength) {
        setDistortionStrength(distortionStrength);
        return this;
    }

    public ShallowWaterConfig stencilId(Integer stencilId) {
        setStencilId(stencilId);
        return this;
    }

    public ShallowWaterConfig durationSeconds(Double durationSeconds) {
        setDurationSeconds(durationSeconds);
        return this;
    }
}
