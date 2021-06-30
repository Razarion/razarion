package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;

public class ShallowWaterConfig  {
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer textureId;
    private double scale;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer distortionId;
    private double distortionStrength;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer stencilId;
    private double durationSeconds;

    public Integer getTextureId() {
        return textureId;
    }

    public void setTextureId(Integer textureId) {
        this.textureId = textureId;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Integer getDistortionId() {
        return distortionId;
    }

    public void setDistortionId(Integer distortionId) {
        this.distortionId = distortionId;
    }

    public double getDistortionStrength() {
        return distortionStrength;
    }

    public void setDistortionStrength(double distortionStrength) {
        this.distortionStrength = distortionStrength;
    }

    public Integer getStencilId() {
        return stencilId;
    }

    public void setStencilId(Integer stencilId) {
        this.stencilId = stencilId;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public ShallowWaterConfig textureId(Integer textureId) {
        setTextureId(textureId);
        return this;
    }

    public ShallowWaterConfig scale(double scale) {
        setScale(scale);
        return this;
    }

    public ShallowWaterConfig distortionId(Integer distortionId) {
        setDistortionId(distortionId);
        return this;
    }

    public ShallowWaterConfig distortionStrength(double distortionStrength) {
        setDistortionStrength(distortionStrength);
        return this;
    }

    public ShallowWaterConfig stencilId(Integer stencilId) {
        setStencilId(stencilId);
        return this;
    }

    public ShallowWaterConfig durationSeconds(double durationSeconds) {
        setDurationSeconds(durationSeconds);
        return this;
    }
}
