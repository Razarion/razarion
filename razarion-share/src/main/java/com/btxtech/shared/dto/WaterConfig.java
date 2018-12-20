package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 15.03.2017.
 */
public class WaterConfig {
    private double waterLevel;
    private double transparency;
    private Integer bmId;
    private double bmScale;
    private double bmDepth;
    private double groundLevel;
    private SpecularLightConfig specularLightConfig;

    public double getWaterLevel() {
        return waterLevel;
    }

    public WaterConfig setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

    public double getTransparency() {
        return transparency;
    }

    public WaterConfig setTransparency(double transparency) {
        this.transparency = transparency;
        return this;
    }

    public Integer getBmId() {
        return bmId;
    }

    public WaterConfig setBmId(Integer bmId) {
        this.bmId = bmId;
        return this;
    }

    public double getBmScale() {
        return bmScale;
    }

    public WaterConfig setBmScale(double bmScale) {
        this.bmScale = bmScale;
        return this;
    }

    public double getBmDepth() {
        return bmDepth;
    }

    public WaterConfig setBmDepth(double bmDepth) {
        this.bmDepth = bmDepth;
        return this;
    }

    public double getGroundLevel() {
        return groundLevel;
    }

    public WaterConfig setGroundLevel(double groundLevel) {
        this.groundLevel = groundLevel;
        return this;
    }

    public SpecularLightConfig getSpecularLightConfig() {
        return specularLightConfig;
    }

    public WaterConfig setSpecularLightConfig(SpecularLightConfig specularLightConfig) {
        this.specularLightConfig = specularLightConfig;
        return this;
    }
}
