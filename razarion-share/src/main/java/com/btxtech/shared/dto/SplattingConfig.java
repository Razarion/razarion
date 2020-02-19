package com.btxtech.shared.dto;

public class SplattingConfig {
    private Integer imageId;
    private double scale;
    private double blur;
    private double offset;
    private double amplitude;

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
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

    public double getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    public SplattingConfig imageId(Integer imageId) {
        setImageId(imageId);
        return this;
    }

    public SplattingConfig scale(double scale) {
        setScale(scale);
        return this;
    }

    public SplattingConfig blur(double blur) {
        setBlur(blur);
        return this;
    }

    public SplattingConfig offset(double offset) {
        setOffset(offset);
        return this;
    }

    public SplattingConfig amplitude(double amplitude) {
        setAmplitude(amplitude);
        return this;
    }
}
