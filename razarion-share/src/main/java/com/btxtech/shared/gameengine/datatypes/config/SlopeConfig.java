package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeShape;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeConfig implements Config {
    private int id;
    private String internalName;
    private List<SlopeShape> slopeShapes;
    private double horizontalSpace;
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    private Integer groundConfigId;
    private Integer waterConfigId;
    private boolean interpolateNorm;
//    private Integer slopeTextureId;
//    private double slopeTextureScale;
//    private Integer slopeBumpMapId;
//    private double slopeBumpMapDepth;
//    private double slopeShininess;
//    private double slopeSpecularStrength;
//    private Integer slopeFoamTextureId;
//    private Integer slopeFoamDistortionId;
//    private Double slopeFoamDistortionStrength;
//    private Double slopeFoamAnimationDuration;
    private Double waterLevel;
//    private Double waterFresnelOffset;
//    private Double waterFresnelDelta;
//    private Double waterShininess;
//    private Integer waterReflectionId;
//    private Double waterSpecularStrength;
//    private Double waterReflectionScale;
//    private Double waterMapScale;
//    private Integer waterDistortionId;
//    private Double waterDistortionStrength;
//    private Integer waterBumpMapId;
//    private Double waterBumpMapDepth;
//    private Double waterTransparency;
//    private Double waterAnimationDuration;
//    private Integer shallowWaterTextureId;
//    private Double shallowWaterTextureScale;
//    private Integer shallowWaterDistortionId;
//    private Double shallowWaterDistortionStrength;
//    private Double shallowWaterAnimation;
//    private Integer shallowWaterStencilId;

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

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

//    public double getSlopeBumpMapDepth() {
//        return slopeBumpMapDepth;
//    }
//
//    public SlopeConfig setSlopeBumpMapDepth(double slopeBumpMapDepth) {
//        this.slopeBumpMapDepth = slopeBumpMapDepth;
//        return this;
//    }
//
//    public double getSlopeShininess() {
//        return slopeShininess;
//    }
//
//    public SlopeConfig setSlopeShininess(double slopeShininess) {
//        this.slopeShininess = slopeShininess;
//        return this;
//    }
//
//    public double getSlopeSpecularStrength() {
//        return slopeSpecularStrength;
//    }
//
//    public SlopeConfig setSlopeSpecularStrength(double slopeSpecularStrength) {
//        this.slopeSpecularStrength = slopeSpecularStrength;
//        return this;
//    }

//    public Integer getSlopeFoamTextureId() {
//        return slopeFoamTextureId;
//    }
//
//    public SlopeConfig setSlopeFoamTextureId(Integer slopeFoamTextureId) {
//        this.slopeFoamTextureId = slopeFoamTextureId;
//        return this;
//    }
//
//    public Integer getSlopeFoamDistortionId() {
//        return slopeFoamDistortionId;
//    }
//
//    public SlopeConfig setSlopeFoamDistortionId(Integer slopeFoamDistortionId) {
//        this.slopeFoamDistortionId = slopeFoamDistortionId;
//        return this;
//    }
//
//    public Double getSlopeFoamDistortionStrength() {
//        return slopeFoamDistortionStrength;
//    }
//
//    public SlopeConfig setSlopeFoamDistortionStrength(Double slopeFoamDistortionStrength) {
//        this.slopeFoamDistortionStrength = slopeFoamDistortionStrength;
//        return this;
//    }
//
//    public Double getSlopeFoamAnimationDuration() {
//        return slopeFoamAnimationDuration;
//    }
//
//    public SlopeConfig setSlopeFoamAnimationDuration(Double slopeFoamAnimationDuration) {
//        this.slopeFoamAnimationDuration = slopeFoamAnimationDuration;
//        return this;
//    }

    public double getOuterLineGameEngine() {
        return outerLineGameEngine;
    }

    public SlopeConfig setOuterLineGameEngine(double outerLineGameEngine) {
        this.outerLineGameEngine = outerLineGameEngine;
        return this;
    }

    public double getInnerLineGameEngine() {
        return innerLineGameEngine;
    }

    public SlopeConfig setInnerLineGameEngine(double innerLineGameEngine) {
        this.innerLineGameEngine = innerLineGameEngine;
        return this;
    }

    public double getCoastDelimiterLineGameEngine() {
        return coastDelimiterLineGameEngine;
    }

    public SlopeConfig setCoastDelimiterLineGameEngine(double coastDelimiterLineGameEngine) {
        this.coastDelimiterLineGameEngine = coastDelimiterLineGameEngine;
        return this;
    }

    public double getHorizontalSpace() {
        return horizontalSpace;
    }

    public SlopeConfig setHorizontalSpace(double horizontalSpace) {
        this.horizontalSpace = horizontalSpace;
        return this;
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

    @Deprecated
    public SlopeConfig setSlopeNodes(SlopeNode[][] slopeNodes) {
        throw new UnsupportedOperationException("...Deprecated...");
    }

    public boolean isInterpolateNorm() {
        return interpolateNorm;
    }

    public SlopeConfig setInterpolateNorm(boolean interpolateNorm) {
        this.interpolateNorm = interpolateNorm;
        return this;
    }

//    public Integer getSlopeTextureId() {
//        return slopeTextureId;
//    }
//
//    public SlopeConfig setSlopeTextureId(Integer slopeTextureId) {
//        this.slopeTextureId = slopeTextureId;
//        return this;
//    }
//
//    public double getSlopeTextureScale() {
//        return slopeTextureScale;
//    }
//
//    public SlopeConfig setSlopeTextureScale(double slopeTextureScale) {
//        this.slopeTextureScale = slopeTextureScale;
//        return this;
//    }
//
//    public Integer getSlopeBumpMapId() {
//        return slopeBumpMapId;
//    }
//
//    public SlopeConfig setSlopeBumpMapId(Integer slopeBumpMapId) {
//        this.slopeBumpMapId = slopeBumpMapId;
//        return this;
//    }

    public Double getWaterLevel() {
        return waterLevel;
    }

    public SlopeConfig setWaterLevel(Double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

//    public Double getWaterFresnelOffset() {
//        return waterFresnelOffset;
//    }
//
//    public SlopeConfig setWaterFresnelOffset(Double waterFresnelOffset) {
//        this.waterFresnelOffset = waterFresnelOffset;
//        return this;
//    }
//
//    public Double getWaterFresnelDelta() {
//        return waterFresnelDelta;
//    }
//
//    public SlopeConfig setWaterFresnelDelta(Double waterFresnelDelta) {
//        this.waterFresnelDelta = waterFresnelDelta;
//        return this;
//    }
//
//    public Double getWaterShininess() {
//        return waterShininess;
//    }
//
//    public SlopeConfig setWaterShininess(Double waterShininess) {
//        this.waterShininess = waterShininess;
//        return this;
//    }
//
//    public Double getWaterSpecularStrength() {
//        return waterSpecularStrength;
//    }
//
//    public SlopeConfig setWaterSpecularStrength(Double waterSpecularStrength) {
//        this.waterSpecularStrength = waterSpecularStrength;
//        return this;
//    }
//
//    public Integer getWaterReflectionId() {
//        return waterReflectionId;
//    }
//
//    public SlopeConfig setWaterReflectionId(Integer waterReflectionId) {
//        this.waterReflectionId = waterReflectionId;
//        return this;
//    }
//
//    public Double getWaterReflectionScale() {
//        return waterReflectionScale;
//    }
//
//    public SlopeConfig setWaterReflectionScale(Double waterReflectionScale) {
//        this.waterReflectionScale = waterReflectionScale;
//        return this;
//    }
//
//    public Double getWaterMapScale() {
//        return waterMapScale;
//    }
//
//    public SlopeConfig setWaterMapScale(Double waterMapScale) {
//        this.waterMapScale = waterMapScale;
//        return this;
//    }
//
//    public Integer getWaterDistortionId() {
//        return waterDistortionId;
//    }
//
//    public SlopeConfig setWaterDistortionId(Integer waterDistortionId) {
//        this.waterDistortionId = waterDistortionId;
//        return this;
//    }
//
//    public Double getWaterDistortionStrength() {
//        return waterDistortionStrength;
//    }
//
//    public SlopeConfig setWaterDistortionStrength(Double waterDistortionStrength) {
//        this.waterDistortionStrength = waterDistortionStrength;
//        return this;
//    }
//
//    public Integer getWaterBumpMapId() {
//        return waterBumpMapId;
//    }
//
//    public SlopeConfig setWaterBumpMapId(Integer waterBumpMapId) {
//        this.waterBumpMapId = waterBumpMapId;
//        return this;
//    }
//
//    public Double getWaterBumpMapDepth() {
//        return waterBumpMapDepth;
//    }
//
//    public SlopeConfig setWaterBumpMapDepth(Double waterBumpMapDepth) {
//        this.waterBumpMapDepth = waterBumpMapDepth;
//        return this;
//    }
//
//    public Double getWaterTransparency() {
//        return waterTransparency;
//    }
//
//    public SlopeConfig setWaterTransparency(Double waterTransparency) {
//        this.waterTransparency = waterTransparency;
//        return this;
//    }
//
//    public Double getWaterAnimationDuration() {
//        return waterAnimationDuration;
//    }
//
//    public SlopeConfig setWaterAnimationDuration(Double waterAnimationDuration) {
//        this.waterAnimationDuration = waterAnimationDuration;
//        return this;
//    }
//
//    public Integer getShallowWaterTextureId() {
//        return shallowWaterTextureId;
//    }
//
//    public SlopeConfig setShallowWaterTextureId(Integer shallowWaterTextureId) {
//        this.shallowWaterTextureId = shallowWaterTextureId;
//        return this;
//    }
//
//    public Double getShallowWaterTextureScale() {
//        return shallowWaterTextureScale;
//    }
//
//    public SlopeConfig setShallowWaterTextureScale(Double shallowWaterTextureScale) {
//        this.shallowWaterTextureScale = shallowWaterTextureScale;
//        return this;
//    }
//
//    public Double getShallowWaterDistortionStrength() {
//        return shallowWaterDistortionStrength;
//    }
//
//    public SlopeConfig setShallowWaterDistortionStrength(Double shallowWaterDistortionStrength) {
//        this.shallowWaterDistortionStrength = shallowWaterDistortionStrength;
//        return this;
//    }
//
//    public Double getShallowWaterAnimation() {
//        return shallowWaterAnimation;
//    }
//
//    public SlopeConfig setShallowWaterAnimation(Double shallowWaterAnimation) {
//        this.shallowWaterAnimation = shallowWaterAnimation;
//        return this;
//    }
//
//    public Integer getShallowWaterStencilId() {
//        return shallowWaterStencilId;
//    }
//
//    public SlopeConfig setShallowWaterStencilId(Integer shallowWaterStencilId) {
//        this.shallowWaterStencilId = shallowWaterStencilId;
//        return this;
//    }
//
//    public Integer getShallowWaterDistortionId() {
//        return shallowWaterDistortionId;
//    }
//
//    public SlopeConfig setShallowWaterDistortionId(Integer shallowWaterDistortionId) {
//        this.shallowWaterDistortionId = shallowWaterDistortionId;
//        return this;
//    }

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

    public SlopeConfig groundConfigId(Integer groundConfigId) {
        setGroundConfigId(groundConfigId);
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
