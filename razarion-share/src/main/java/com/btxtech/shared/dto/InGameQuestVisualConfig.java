package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 12.09.2017.
 */
@JsType
public class InGameQuestVisualConfig {
    private Integer nodesMaterialId;
    private Integer placeNodesMaterialId;
    private double radius;
    private Integer outOfViewNodesMaterialId;
    public double outOfViewSize;
    public double outOfViewDistanceFromCamera;
    private Color harvestColor;
    private Color attackColor;
    private Color pickColor;

    public Integer getNodesMaterialId() {
        return nodesMaterialId;
    }

    public void setNodesMaterialId(Integer nodesMaterialId) {
        this.nodesMaterialId = nodesMaterialId;
    }

    public Integer getPlaceNodesMaterialId() {
        return placeNodesMaterialId;
    }

    public void setPlaceNodesMaterialId(Integer placeNodesMaterialId) {
        this.placeNodesMaterialId = placeNodesMaterialId;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Integer getOutOfViewNodesMaterialId() {
        return outOfViewNodesMaterialId;
    }

    public void setOutOfViewNodesMaterialId(Integer outOfViewNodesMaterialId) {
        this.outOfViewNodesMaterialId = outOfViewNodesMaterialId;
    }

    public double getOutOfViewSize() {
        return outOfViewSize;
    }

    public void setOutOfViewSize(double outOfViewSize) {
        this.outOfViewSize = outOfViewSize;
    }

    public double getOutOfViewDistanceFromCamera() {
        return outOfViewDistanceFromCamera;
    }

    public void setOutOfViewDistanceFromCamera(double outOfViewDistanceFromCamera) {
        this.outOfViewDistanceFromCamera = outOfViewDistanceFromCamera;
    }

    public Color getHarvestColor() {
        return harvestColor;
    }

    public void setHarvestColor(Color harvestColor) {
        this.harvestColor = harvestColor;
    }

    public Color getAttackColor() {
        return attackColor;
    }

    public void setAttackColor(Color attackColor) {
        this.attackColor = attackColor;
    }

    public Color getPickColor() {
        return pickColor;
    }

    public void setPickColor(Color pickColor) {
        this.pickColor = pickColor;
    }

    @JsIgnore
    public InGameQuestVisualConfig nodesMaterialId(Integer nodesMaterialId) {
        setNodesMaterialId(nodesMaterialId);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig placeNodesMaterialId(Integer nodesMaterialId) {
        setPlaceNodesMaterialId(nodesMaterialId);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig radius(double radius) {
        setRadius(radius);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig outOfViewNodesMaterialId(Integer outOfViewNodesMaterialId) {
        setOutOfViewNodesMaterialId(outOfViewNodesMaterialId);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig outOfViewSize(double outOfViewSize) {
        setOutOfViewSize(outOfViewSize);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig outOfViewDistanceFromCamera(double outOfViewDistanceFromCamera) {
        setOutOfViewDistanceFromCamera(outOfViewDistanceFromCamera);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig harvestColor(Color harvestColor) {
        setHarvestColor(harvestColor);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig attackColor(Color attackColor) {
        setAttackColor(attackColor);
        return this;
    }

    @JsIgnore
    public InGameQuestVisualConfig pickColor(Color pickColor) {
        setPickColor(pickColor);
        return this;
    }
}
