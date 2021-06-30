package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;

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
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer reflectionId;
    private double reflectionScale;
    private double fresnelOffset;
    private double fresnelDelta;
    private double shininess;
    private double specularStrength;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer bumpMapId;
    private double bumpDistortionScale;
    private double bumpDistortionDurationSeconds;
    private double bumpMapDepth;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer distortionId;
    private double distortionStrength;

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

    public double getFresnelOffset() {
        return fresnelOffset;
    }

    public void setFresnelOffset(double fresnelOffset) {
        this.fresnelOffset = fresnelOffset;
    }

    public double getFresnelDelta() {
        return fresnelDelta;
    }

    public void setFresnelDelta(double fresnelDelta) {
        this.fresnelDelta = fresnelDelta;
    }

    public double getShininess() {
        return shininess;
    }

    public void setShininess(double shininess) {
        this.shininess = shininess;
    }

    public double getSpecularStrength() {
        return specularStrength;
    }

    public void setSpecularStrength(double specularStrength) {
        this.specularStrength = specularStrength;
    }

    public Integer getBumpMapId() {
        return bumpMapId;
    }

    public void setBumpMapId(Integer bumpMapId) {
        this.bumpMapId = bumpMapId;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public Integer getDistortionId() {
        return distortionId;
    }

    public void setDistortionId(Integer distortionId) {
        this.distortionId = distortionId;
    }

    public double getBumpDistortionScale() {
        return bumpDistortionScale;
    }

    public void setBumpDistortionScale(double bumpDistortionScale) {
        this.bumpDistortionScale = bumpDistortionScale;
    }

    public double getDistortionStrength() {
        return distortionStrength;
    }

    public void setDistortionStrength(double distortionStrength) {
        this.distortionStrength = distortionStrength;
    }

    public double getBumpDistortionDurationSeconds() {
        return bumpDistortionDurationSeconds;
    }

    public void setBumpDistortionDurationSeconds(double bumpDistortionDurationSeconds) {
        this.bumpDistortionDurationSeconds = bumpDistortionDurationSeconds;
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

    public WaterConfig shininess(double shininess) {
        setShininess(shininess);
        return this;
    }

    public WaterConfig specularStrength(double specularStrength) {
        setSpecularStrength(specularStrength);
        return this;
    }

    public WaterConfig reflectionScale(double reflectionScale) {
        setReflectionScale(reflectionScale);
        return this;
    }

    public WaterConfig fresnelOffset(double fresnelOffset) {
        setFresnelOffset(fresnelOffset);
        return this;
    }

    public WaterConfig fresnelDelta(double fresnelDelta) {
        setFresnelDelta(fresnelDelta);
        return this;
    }

    public WaterConfig bumpMapId(Integer normMapId) {
        setBumpMapId(normMapId);
        return this;
    }

    public WaterConfig bumpMapDepth(double normMapDepth) {
        setBumpMapDepth(normMapDepth);
        return this;
    }

    public WaterConfig distortionId(Integer distortionId) {
        setDistortionId(distortionId);
        return this;
    }

    public WaterConfig bumpDistortionScale(double distortionScale) {
        setBumpDistortionScale(distortionScale);
        return this;
    }

    public WaterConfig distortionStrength(double distortionStrength) {
        setDistortionStrength(distortionStrength);
        return this;
    }

    public WaterConfig distortionDurationSeconds(double distortionDurationSeconds) {
        setBumpDistortionDurationSeconds(distortionDurationSeconds);
        return this;
    }
}
