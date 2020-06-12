package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 15.03.2017.
 */
public class WaterConfig implements Config {
    private int id;
    private String internalName;
    private double waterLevel;
    private double groundLevel;
    private double transparency;
    private Integer reflectionId;
    private double reflectionScale;
    private Integer normMapId;
    private double normMapDepth;
    private Integer distortionId;
    private double distortionScale;
    private double distortionStrength;
    private double distortionDurationSeconds;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
    }

    public double getGroundLevel() {
        return groundLevel;
    }

    public void setGroundLevel(double groundLevel) {
        this.groundLevel = groundLevel;
    }

    public double getTransparency() {
        return transparency;
    }

    public void setTransparency(double transparency) {
        this.transparency = transparency;
    }

    public Integer getReflectionId() {
        return reflectionId;
    }

    public void setReflectionId(Integer reflectionId) {
        this.reflectionId = reflectionId;
    }

    public double getReflectionScale() {
        return reflectionScale;
    }

    public void setReflectionScale(double reflectionScale) {
        this.reflectionScale = reflectionScale;
    }

    public Integer getNormMapId() {
        return normMapId;
    }

    public void setNormMapId(Integer normMapId) {
        this.normMapId = normMapId;
    }

    public double getNormMapDepth() {
        return normMapDepth;
    }

    public void setNormMapDepth(double normMapDepth) {
        this.normMapDepth = normMapDepth;
    }

    public Integer getDistortionId() {
        return distortionId;
    }

    public void setDistortionId(Integer distortionId) {
        this.distortionId = distortionId;
    }

    public double getDistortionScale() {
        return distortionScale;
    }

    public void setDistortionScale(double distortionScale) {
        this.distortionScale = distortionScale;
    }

    public double getDistortionStrength() {
        return distortionStrength;
    }

    public void setDistortionStrength(double distortionStrength) {
        this.distortionStrength = distortionStrength;
    }

    public double getDistortionDurationSeconds() {
        return distortionDurationSeconds;
    }

    public void setDistortionDurationSeconds(double distortionDurationSeconds) {
        this.distortionDurationSeconds = distortionDurationSeconds;
    }

    public WaterConfig id(int id) {
        this.id = id;
        return this;
    }

    public WaterConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public WaterConfig waterLevel(double waterLevel) {
        setWaterLevel(waterLevel);
        return this;
    }

    public WaterConfig groundLevel(double groundLevel) {
        setGroundLevel(groundLevel);
        return this;
    }

    public WaterConfig transparency(double transparency) {
        setTransparency(transparency);
        return this;
    }

    public WaterConfig reflectionId(Integer reflectionId) {
        setReflectionId(reflectionId);
        return this;
    }

    public WaterConfig reflectionScale(double reflectionScale) {
        setReflectionScale(reflectionScale);
        return this;
    }

    public WaterConfig normMapId(Integer normMapId) {
        setNormMapId(normMapId);
        return this;
    }

    public WaterConfig normMapDepth(double normMapDepth) {
        setNormMapDepth(normMapDepth);
        return this;
    }

    public WaterConfig distortionId(Integer distortionId) {
        setDistortionId(distortionId);
        return this;
    }

    public WaterConfig distortionScale(double distortionScale) {
        setDistortionScale(distortionScale);
        return this;
    }

    public WaterConfig distortionStrength(double distortionStrength) {
        setDistortionStrength(distortionStrength);
        return this;
    }

    public WaterConfig distortionDurationSeconds(double distortionDurationSeconds) {
        setDistortionDurationSeconds(distortionDurationSeconds);
        return this;
    }
}
