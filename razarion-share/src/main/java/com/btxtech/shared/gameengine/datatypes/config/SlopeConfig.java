package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2016.
 */
@JsType
@Deprecated
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
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    private Integer shallowWaterThreeJsMaterial;

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

    public @Nullable Integer getThreeJsMaterial() {
        return threeJsMaterial;
    }

    public void setThreeJsMaterial(@Nullable Integer threeJsMaterial) {
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

    public @Nullable Integer getGroundConfigId() {
        return groundConfigId;
    }

    public void setGroundConfigId(@Nullable Integer groundConfigId) {
        this.groundConfigId = groundConfigId;
    }

    public @Nullable Integer getWaterConfigId() {
        return waterConfigId;
    }

    public void setWaterConfigId(@Nullable Integer waterConfigId) {
        this.waterConfigId = waterConfigId;
    }

    public boolean isInterpolateNorm() {
        return interpolateNorm;
    }

    public void setInterpolateNorm(boolean interpolateNorm) {
        this.interpolateNorm = interpolateNorm;
    }

    public @Nullable Integer getShallowWaterThreeJsMaterial() {
        return shallowWaterThreeJsMaterial;
    }

    public void setShallowWaterThreeJsMaterial(@Nullable Integer shallowWaterThreeJsMaterial) {
        this.shallowWaterThreeJsMaterial = shallowWaterThreeJsMaterial;
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

    public SlopeConfig shallowWaterThreeJsMaterial(Integer shallowWaterThreeJsMaterial) {
        setShallowWaterThreeJsMaterial(shallowWaterThreeJsMaterial);
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
