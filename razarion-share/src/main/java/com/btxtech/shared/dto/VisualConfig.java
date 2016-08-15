package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 15.08.2016.
 */
@Portable
@Bindable
public class VisualConfig {
    private double shadowRotationX;
    private double shadowRotationZ;
    private double shadowAlpha;
    private double shape3DLightRotateX;
    private double shape3DLightRotateZ;
    private LightConfig waterLightConfig;
    private double waterTransparency;
    private double waterBmDepth;
    private double waterGroundLevel;

    public double getShadowRotationX() {
        return shadowRotationX;
    }

    public VisualConfig setShadowRotationX(double shadowRotationX) {
        this.shadowRotationX = shadowRotationX;
        return this;
    }

    public double getShadowRotationZ() {
        return shadowRotationZ;
    }

    public VisualConfig setShadowRotationZ(double shadowRotationZ) {
        this.shadowRotationZ = shadowRotationZ;
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

    public LightConfig getWaterLightConfig() {
        return waterLightConfig;
    }

    public VisualConfig setWaterLightConfig(LightConfig waterLightConfig) {
        this.waterLightConfig = waterLightConfig;
        return this;
    }

    public double getWaterTransparency() {
        return waterTransparency;
    }

    public VisualConfig setWaterTransparency(double waterTransparency) {
        this.waterTransparency = waterTransparency;
        return this;
    }

    public double getWaterBmDepth() {
        return waterBmDepth;
    }

    public VisualConfig setWaterBmDepth(double waterBmDepth) {
        this.waterBmDepth = waterBmDepth;
        return this;
    }

    public double getWaterGroundLevel() {
        return waterGroundLevel;
    }

    public VisualConfig setWaterGroundLevel(double waterGroundLevel) {
        this.waterGroundLevel = waterGroundLevel;
        return this;
    }
}
