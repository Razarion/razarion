package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 15.03.2017.
 */
@JsType
public class WaterConfig implements Config {
    private int id;
    private String internalName;
    private double waterLevel;
    private double groundLevel;
    private double transparency;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer reflectionId;
    private double shininess;
    private double specularStrength;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer normalMapId;
    private double normalMapDepth;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer distortionId;
    private double distortionStrength;
    private double distortionAnimationSeconds;
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    private Integer material;
    private String color;

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

    public Integer getNormalMapId() {
        return normalMapId;
    }

    public void setNormalMapId(Integer normalMapId) {
        this.normalMapId = normalMapId;
    }

    public double getNormalMapDepth() {
        return normalMapDepth;
    }

    public void setNormalMapDepth(double normalMapDepth) {
        this.normalMapDepth = normalMapDepth;
    }

    public Integer getDistortionId() {
        return distortionId;
    }

    public void setDistortionId(Integer distortionId) {
        this.distortionId = distortionId;
    }

    public double getDistortionStrength() {
        return distortionStrength;
    }

    public void setDistortionStrength(double distortionStrength) {
        this.distortionStrength = distortionStrength;
    }

    public double getDistortionAnimationSeconds() {
        return distortionAnimationSeconds;
    }

    public void setDistortionAnimationSeconds(double distortionAnimationSeconds) {
        this.distortionAnimationSeconds = distortionAnimationSeconds;
    }

    public Integer getMaterial() {
        return material;
    }

    public void setMaterial(Integer material) {
        this.material = material;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public WaterConfig normalMapId(Integer normalMapId) {
        setNormalMapId(normalMapId);
        return this;
    }

    public WaterConfig normalMapDepth(double normalMapDepth) {
        setNormalMapDepth(normalMapDepth);
        return this;
    }

    public WaterConfig distortionId(Integer distortionId) {
        setDistortionId(distortionId);
        return this;
    }

    public WaterConfig distortionStrength(double distortionStrength) {
        setDistortionStrength(distortionStrength);
        return this;
    }

    public WaterConfig distortionAnimationSeconds(double distortionAnimationSeconds) {
        setDistortionAnimationSeconds(distortionAnimationSeconds);
        return this;
    }

    public WaterConfig material(Integer material) {
        setMaterial(material);
        return this;
    }

    public WaterConfig color(String color) {
        setColor(color);
        return this;
    }
}
