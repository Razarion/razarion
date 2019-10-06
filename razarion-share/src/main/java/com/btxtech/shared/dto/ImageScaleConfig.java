package com.btxtech.shared.dto;

public class ImageScaleConfig {
    private Integer id;
    private double scale;

    public Integer getId() {
        return id;
    }

    public ImageScaleConfig setId(Integer id) {
        this.id = id;
        return this;
    }

    public double getScale() {
        return scale;
    }

    public ImageScaleConfig setScale(double scale) {
        this.scale = scale;
        return this;
    }
}
