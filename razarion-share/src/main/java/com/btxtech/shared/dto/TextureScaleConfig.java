package com.btxtech.shared.dto;

public class TextureScaleConfig {
    private Integer id;
    private double scale;

    public Integer getId() {
        return id;
    }

    public TextureScaleConfig setId(Integer id) {
        this.id = id;
        return this;
    }

    public double getScale() {
        return scale;
    }

    public TextureScaleConfig setScale(double scale) {
        this.scale = scale;
        return this;
    }
}
