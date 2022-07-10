package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2016.
 */
@JsType
public class SlopeConfig implements Config {
    private int id;
    private String internalName;
    private List<SlopeShape> slopeShapes;
    private double horizontalSpace;
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    @CollectionReference(CollectionReferenceType.GROUND)
    private Integer groundConfigId;
    @CollectionReference(CollectionReferenceType.WATER)
    private Integer waterConfigId;
    private boolean interpolateNorm;
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    private Integer threeJsMaterial;
    private ShallowWaterConfig shallowWaterConfig;
    private SlopeSplattingConfig outerSlopeSplattingConfig;
    private SlopeSplattingConfig innerSlopeSplattingConfig;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public List<SlopeShape> getSlopeShapes() {
        return slopeShapes;
    }

    public void setSlopeShapes(List<SlopeShape> slopeShapes) {
        this.slopeShapes = slopeShapes;
    }

    public Integer getThreeJsMaterial() {
        return threeJsMaterial;
    }

    public void setThreeJsMaterial(Integer threeJsMaterial) {
        this.threeJsMaterial = threeJsMaterial;
    }

    public double getOuterLineGameEngine() {
        return outerLineGameEngine;
    }

    public void setOuterLineGameEngine(double outerLineGameEngine) {
        this.outerLineGameEngine = outerLineGameEngine;
    }

    public double getInnerLineGameEngine() {
        return innerLineGameEngine;
    }

    public void setInnerLineGameEngine(double innerLineGameEngine) {
        this.innerLineGameEngine = innerLineGameEngine;
    }

    public double getCoastDelimiterLineGameEngine() {
        return coastDelimiterLineGameEngine;
    }

    public void setCoastDelimiterLineGameEngine(double coastDelimiterLineGameEngine) {
        this.coastDelimiterLineGameEngine = coastDelimiterLineGameEngine;
    }

    public double getHorizontalSpace() {
        return horizontalSpace;
    }

    public void setHorizontalSpace(double horizontalSpace) {
        this.horizontalSpace = horizontalSpace;
    }

    public Integer getGroundConfigId() {
        return groundConfigId;
    }

    public void setGroundConfigId(Integer groundConfigId) {
        this.groundConfigId = groundConfigId;
    }

    public Integer getWaterConfigId() {
        return waterConfigId;
    }

    public void setWaterConfigId(Integer waterConfigId) {
        this.waterConfigId = waterConfigId;
    }

    public boolean isInterpolateNorm() {
        return interpolateNorm;
    }

    public void setInterpolateNorm(boolean interpolateNorm) {
        this.interpolateNorm = interpolateNorm;
    }

    public ShallowWaterConfig getShallowWaterConfig() {
        return shallowWaterConfig;
    }

    public void setShallowWaterConfig(ShallowWaterConfig shallowWaterConfig) {
        this.shallowWaterConfig = shallowWaterConfig;
    }

    public SlopeSplattingConfig getOuterSlopeSplattingConfig() {
        return outerSlopeSplattingConfig;
    }

    public void setOuterSlopeSplattingConfig(SlopeSplattingConfig outerSlopeSplattingConfig) {
        this.outerSlopeSplattingConfig = outerSlopeSplattingConfig;
    }

    public SlopeSplattingConfig getInnerSlopeSplattingConfig() {
        return innerSlopeSplattingConfig;
    }

    public void setInnerSlopeSplattingConfig(SlopeSplattingConfig innerSlopeSplattingConfig) {
        this.innerSlopeSplattingConfig = innerSlopeSplattingConfig;
    }

    public SlopeConfig id(Integer id) {
        this.id = id;
        return this;
    }

    public SlopeConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public SlopeConfig waterConfigId(Integer waterConfigId) {
        setWaterConfigId(waterConfigId);
        return this;
    }

    public SlopeConfig slopeShapes(List<SlopeShape> slopeShapes) {
        setSlopeShapes(slopeShapes);
        return this;
    }

    public SlopeConfig horizontalSpace(double horizontalSpace) {
        setHorizontalSpace(horizontalSpace);
        return this;
    }

    public SlopeConfig outerLineGameEngine(double outerLineGameEngine) {
        setOuterLineGameEngine(outerLineGameEngine);
        return this;
    }

    public SlopeConfig innerLineGameEngine(double innerLineGameEngine) {
        setInnerLineGameEngine(innerLineGameEngine);
        return this;
    }

    public SlopeConfig coastDelimiterLineGameEngine(double coastDelimiterLineGameEngine) {
        setCoastDelimiterLineGameEngine(coastDelimiterLineGameEngine);
        return this;
    }

    public SlopeConfig interpolateNorm(boolean interpolateNorm) {
        setInterpolateNorm(interpolateNorm);
        return this;
    }

    public SlopeConfig groundConfigId(Integer groundConfigId) {
        setGroundConfigId(groundConfigId);
        return this;
    }

    public SlopeConfig threeJsMaterial(Integer threeJsMaterial) {
        setThreeJsMaterial(threeJsMaterial);
        return this;
    }

    public SlopeConfig shallowWaterConfig(ShallowWaterConfig shallowWaterConfig) {
        setShallowWaterConfig(shallowWaterConfig);
        return this;
    }

    public SlopeConfig outerSlopeSplattingConfig(SlopeSplattingConfig outerSlopeSplattingConfig) {
        setOuterSlopeSplattingConfig(outerSlopeSplattingConfig);
        return this;
    }

    public SlopeConfig innerSlopeSplattingConfig(SlopeSplattingConfig innerSlopeSplattingConfig) {
        setInnerSlopeSplattingConfig(innerSlopeSplattingConfig);
        return this;
    }

    public boolean hasWaterConfigId() {
        return waterConfigId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeConfig that = (SlopeConfig) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
