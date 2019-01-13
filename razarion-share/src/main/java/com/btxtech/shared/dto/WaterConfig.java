package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;

/**
 * Created by Beat
 * 15.03.2017.
 */
public class WaterConfig {
    private double waterLevel;
    private double transparency;
    private Integer reflectionId;
    private double reflectionScale;
    private Integer bmId;
    private double bmScale;
    private double bmDepth;
    private Integer distortionId;
    private double distortionScale;
    private double distortionStrength;
    private double groundLevel;
    private SpecularLightConfig specularLightConfig;
    private Color color;

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

    public Integer getReflectionId() {
        return reflectionId;
    }

    public WaterConfig setReflectionId(Integer reflectionId) {
        this.reflectionId = reflectionId;
        return this;
    }

    public double getReflectionScale() {
        return reflectionScale;
    }

    public WaterConfig setReflectionScale(double reflectionScale) {
        this.reflectionScale = reflectionScale;
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

    public Integer getDistortionId() {
        return distortionId;
    }

    public WaterConfig setDistortionId(Integer distortionId) {
        this.distortionId = distortionId;
        return this;
    }

    public double getDistortionScale() {
        return distortionScale;
    }

    public WaterConfig setDistortionScale(double distortionScale) {
        this.distortionScale = distortionScale;
        return this;
    }

    public double getDistortionStrength() {
        return distortionStrength;
    }

    public WaterConfig setDistortionStrength(double distortionStrength) {
        this.distortionStrength = distortionStrength;
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

    public Color getColor() {
        return color;
    }

    public WaterConfig setColor(Color color) {
        this.color = color;
        return this;
    }
}
