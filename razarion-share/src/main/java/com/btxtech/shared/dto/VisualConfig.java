package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.shape.Shape3D;

import java.util.List;

/**
 * Created by Beat
 * 15.08.2016.
 */
public class VisualConfig {
    // Divide in 3 parts: shadow, water and Shape3D
    private double shadowRotationX;
    private double shadowRotationY;
    private double shadowAlpha;
    private double shape3DLightRotateX;
    private double shape3DLightRotateZ;
    private WaterConfig waterConfig;
    private List<Shape3D> shape3Ds;
    private Integer baseItemDemolitionImageId;
    private Integer buildupTextureId;

    public double getShadowRotationX() {
        return shadowRotationX;
    }

    public VisualConfig setShadowRotationX(double shadowRotationX) {
        this.shadowRotationX = shadowRotationX;
        return this;
    }

    public double getShadowRotationY() {
        return shadowRotationY;
    }

    public VisualConfig setShadowRotationY(double shadowRotationY) {
        this.shadowRotationY = shadowRotationY;
        return this;
    }

    public double getShadowAlpha() {
        return shadowAlpha;
    }

    public VisualConfig setShadowAlpha(double shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
        return this;
    }

    public double getShape3DLightRotateX() {
        return shape3DLightRotateX;
    }

    public VisualConfig setShape3DLightRotateX(double shape3DLightRotateX) {
        this.shape3DLightRotateX = shape3DLightRotateX;
        return this;
    }

    public double getShape3DLightRotateZ() {
        return shape3DLightRotateZ;
    }

    public VisualConfig setShape3DLightRotateZ(double shape3DLightRotateZ) {
        this.shape3DLightRotateZ = shape3DLightRotateZ;
        return this;
    }

    public WaterConfig getWaterConfig() {
        return waterConfig;
    }

    public VisualConfig setWaterConfig(WaterConfig waterConfig) {
        this.waterConfig = waterConfig;
        return this;
    }

    public List<Shape3D> getShape3Ds() {
        return shape3Ds;
    }

    public VisualConfig setShape3Ds(List<Shape3D> shape3Ds) {
        this.shape3Ds = shape3Ds;
        return this;
    }

    public Integer getBaseItemDemolitionImageId() {
        return baseItemDemolitionImageId;
    }

    public VisualConfig setBaseItemDemolitionImageId(Integer baseItemDemolitionImageId) {
        this.baseItemDemolitionImageId = baseItemDemolitionImageId;
        return this;
    }

    public Integer getBuildupTextureId() {
        return buildupTextureId;
    }

    public VisualConfig setBuildupTextureId(Integer buildupTextureId) {
        this.buildupTextureId = buildupTextureId;
        return this;
    }
}
