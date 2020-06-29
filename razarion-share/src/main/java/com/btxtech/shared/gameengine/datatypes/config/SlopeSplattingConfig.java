package com.btxtech.shared.gameengine.datatypes.config;

public class SlopeSplattingConfig {
    private Integer textureId;
    private double scale;
    private double impact;
    private double blur;
    private double offset;

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

    public double getImpact() {
        return impact;
    }

    public void setImpact(double impact) {
        this.impact = impact;
    }

    public double getBlur() {
        return blur;
    }

    public void setBlur(double blur) {
        this.blur = blur;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public SlopeSplattingConfig textureId(Integer textureId) {
        setTextureId(textureId);
        return this;
    }

    public SlopeSplattingConfig scale(double scale) {
        setScale(scale);
        return this;
    }

    public SlopeSplattingConfig impact(double impact) {
        setImpact(impact);
        return this;
    }

    public SlopeSplattingConfig blur(double blur) {
        setBlur(blur);
        return this;
    }

    public SlopeSplattingConfig offset(double offset) {
        setOffset(offset);
        return this;
    }
}
