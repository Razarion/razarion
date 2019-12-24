package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SpecularLightConfig;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeConfig implements ObjectNameIdProvider {
    public enum Type {
        LAND,
        WATER
    }

    private int id;
    private String internalName;
    private List<SlopeShape> slopeShapes;
    private int segments;
    private double horizontalSpace;
    private int rows;
    private double width;
    private double height;
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    private Type type;
    private SlopeNode[][] slopeNodes;
    private boolean interpolateNorm;
    @Deprecated
    private SpecularLightConfig specularLightConfig;
    private Integer slopeTextureId;
    private double slopeTextureScale;
    private Integer slopeBumpMapId;
    private double slopeBumpMapDepth;
    private double slopeShininess;
    private double slopeSpecularStrength;
    private Integer slopeFoamTextureId;
    private Integer slopeFoamDistortionId;
    private Double slopeFoamDistortionStrength;
    private Double slopeFoamAnimationDuration;
    private GroundSkeletonConfig groundSkeletonConfig;
    private Integer slopeSplattingId;
    private Double slopeSplattingScale1;
    private Double slopeSplattingScale2;
    private Double slopeSplattingFadeThreshold;
    private Double slopeSplattingOffset;
    private Double waterLevel;
    private Double waterFresnelOffset;
    private Double waterFresnelDelta;
    private Double waterShininess;
    private Integer waterReflectionId;
    private Double waterSpecularStrength;
    private Double waterReflectionScale;
    private Double waterMapScale;
    private Integer waterDistortionId;
    private Double waterDistortionStrength;
    private Integer waterBumpMapId;
    private Double waterBumpMapDepth;
    private Double waterTransparency;
    private Double waterAnimationDuration;
    private Double shallowWaterTextureId;
    private Double shallowWaterTextureScale;
    private Double shallowWaterDistortionStrength;
    private Double shallowWaterAnimation;
    @Deprecated
    private Integer slopeWaterSplattingId;
    @Deprecated
    private double slopeWaterSplattingScale;
    @Deprecated
    private double slopeWaterSplattingFactor;
    @Deprecated
    private double slopeWaterSplattingFadeThreshold;
    @Deprecated
    private double slopeWaterSplattingHeight;

    public int getId() {
        return id;
    }

    public SlopeConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public SlopeConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
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

    public int getSegments() {
        return segments;
    }

    public SlopeConfig setSegments(int segments) {
        this.segments = segments;
        return this;
    }

    public SlopeConfig setRows(int rows) {
        this.rows = rows;
        return this;
    }

    public SlopeConfig setWidth(double width) {
        this.width = width;
        return this;
    }

    public SlopeConfig setHeight(double height) {
        this.height = height;
        return this;
    }

    public int getRows() {
        return rows;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getSlopeBumpMapDepth() {
        return slopeBumpMapDepth;
    }

    public SlopeConfig setSlopeBumpMapDepth(double slopeBumpMapDepth) {
        this.slopeBumpMapDepth = slopeBumpMapDepth;
        return this;
    }

    public double getSlopeShininess() {
        return slopeShininess;
    }

    public SlopeConfig setSlopeShininess(double slopeShininess) {
        this.slopeShininess = slopeShininess;
        return this;
    }

    public double getSlopeSpecularStrength() {
        return slopeSpecularStrength;
    }

    public SlopeConfig setSlopeSpecularStrength(double slopeSpecularStrength) {
        this.slopeSpecularStrength = slopeSpecularStrength;
        return this;
    }

    public Integer getSlopeFoamTextureId() {
        return slopeFoamTextureId;
    }

    public SlopeConfig setSlopeFoamTextureId(Integer slopeFoamTextureId) {
        this.slopeFoamTextureId = slopeFoamTextureId;
        return this;
    }

    public Integer getSlopeFoamDistortionId() {
        return slopeFoamDistortionId;
    }

    public SlopeConfig setSlopeFoamDistortionId(Integer slopeFoamDistortionId) {
        this.slopeFoamDistortionId = slopeFoamDistortionId;
        return this;
    }

    public Double getSlopeFoamDistortionStrength() {
        return slopeFoamDistortionStrength;
    }

    public SlopeConfig setSlopeFoamDistortionStrength(Double slopeFoamDistortionStrength) {
        this.slopeFoamDistortionStrength = slopeFoamDistortionStrength;
        return this;
    }

    public Double getSlopeFoamAnimationDuration() {
        return slopeFoamAnimationDuration;
    }

    public SlopeConfig setSlopeFoamAnimationDuration(Double slopeFoamAnimationDuration) {
        this.slopeFoamAnimationDuration = slopeFoamAnimationDuration;
        return this;
    }

    public GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public SlopeConfig setGroundSkeletonConfig(GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
        return this;
    }

    public Integer getSlopeSplattingId() {
        return slopeSplattingId;
    }

    public SlopeConfig setSlopeSplattingId(Integer slopeSplattingId) {
        this.slopeSplattingId = slopeSplattingId;
        return this;
    }

    public Double getSlopeSplattingScale1() {
        return slopeSplattingScale1;
    }

    public SlopeConfig setSlopeSplattingScale1(Double slopeSplattingScale1) {
        this.slopeSplattingScale1 = slopeSplattingScale1;
        return this;
    }

    public Double getSlopeSplattingScale2() {
        return slopeSplattingScale2;
    }

    public SlopeConfig setSlopeSplattingScale2(Double slopeSplattingScale2) {
        this.slopeSplattingScale2 = slopeSplattingScale2;
        return this;
    }

    public Double getSlopeSplattingFadeThreshold() {
        return slopeSplattingFadeThreshold;
    }

    public SlopeConfig setSlopeSplattingFadeThreshold(Double slopeSplattingFadeThreshold) {
        this.slopeSplattingFadeThreshold = slopeSplattingFadeThreshold;
        return this;
    }

    public Double getSlopeSplattingOffset() {
        return slopeSplattingOffset;
    }

    public SlopeConfig setSlopeSplattingOffset(Double slopeSplattingOffset) {
        this.slopeSplattingOffset = slopeSplattingOffset;
        return this;
    }

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

    public Type getType() {
        return type;
    }

    public SlopeConfig setType(Type type) {
        this.type = type;
        return this;
    }

    public SlopeNode[][] getSlopeNodes() {
        return slopeNodes;
    }

    public SlopeConfig setSlopeNodes(SlopeNode[][] slopeNodes) {
        this.slopeNodes = slopeNodes;
        return this;
    }

    public boolean isInterpolateNorm() {
        return interpolateNorm;
    }

    public SlopeConfig setInterpolateNorm(boolean interpolateNorm) {
        this.interpolateNorm = interpolateNorm;
        return this;
    }

    @Deprecated
    public SpecularLightConfig getSpecularLightConfig() {
        return specularLightConfig;
    }

    @Deprecated
    public SlopeConfig setSpecularLightConfig(SpecularLightConfig specularLightConfig) {
        this.specularLightConfig = specularLightConfig;
        return this;
    }

    public Integer getSlopeTextureId() {
        return slopeTextureId;
    }

    public SlopeConfig setSlopeTextureId(Integer slopeTextureId) {
        this.slopeTextureId = slopeTextureId;
        return this;
    }

    public double getSlopeTextureScale() {
        return slopeTextureScale;
    }

    public SlopeConfig setSlopeTextureScale(double slopeTextureScale) {
        this.slopeTextureScale = slopeTextureScale;
        return this;
    }

    public Integer getSlopeBumpMapId() {
        return slopeBumpMapId;
    }

    public SlopeConfig setSlopeBumpMapId(Integer slopeBumpMapId) {
        this.slopeBumpMapId = slopeBumpMapId;
        return this;
    }

    public SlopeNode getSlopeNode(int column, int row) {
        return slopeNodes[column % segments][row];
    }

    public boolean hasWater() {
        return waterLevel != null;
    }

    public Double getWaterLevel() {
        return waterLevel;
    }

    public SlopeConfig setWaterLevel(Double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

    public Double getWaterFresnelOffset() {
        return waterFresnelOffset;
    }

    public SlopeConfig setWaterFresnelOffset(Double waterFresnelOffset) {
        this.waterFresnelOffset = waterFresnelOffset;
        return this;
    }

    public Double getWaterFresnelDelta() {
        return waterFresnelDelta;
    }

    public SlopeConfig setWaterFresnelDelta(Double waterFresnelDelta) {
        this.waterFresnelDelta = waterFresnelDelta;
        return this;
    }

    public Double getWaterShininess() {
        return waterShininess;
    }

    public SlopeConfig setWaterShininess(Double waterShininess) {
        this.waterShininess = waterShininess;
        return this;
    }

    public Double getWaterSpecularStrength() {
        return waterSpecularStrength;
    }

    public SlopeConfig setWaterSpecularStrength(Double waterSpecularStrength) {
        this.waterSpecularStrength = waterSpecularStrength;
        return this;
    }

    public Integer getWaterReflectionId() {
        return waterReflectionId;
    }

    public SlopeConfig setWaterReflectionId(Integer waterReflectionId) {
        this.waterReflectionId = waterReflectionId;
        return this;
    }

    public Double getWaterReflectionScale() {
        return waterReflectionScale;
    }

    public SlopeConfig setWaterReflectionScale(Double waterReflectionScale) {
        this.waterReflectionScale = waterReflectionScale;
        return this;
    }

    public Double getWaterMapScale() {
        return waterMapScale;
    }

    public SlopeConfig setWaterMapScale(Double waterMapScale) {
        this.waterMapScale = waterMapScale;
        return this;
    }

    public Integer getWaterDistortionId() {
        return waterDistortionId;
    }

    public SlopeConfig setWaterDistortionId(Integer waterDistortionId) {
        this.waterDistortionId = waterDistortionId;
        return this;
    }

    public Double getWaterDistortionStrength() {
        return waterDistortionStrength;
    }

    public SlopeConfig setWaterDistortionStrength(Double waterDistortionStrength) {
        this.waterDistortionStrength = waterDistortionStrength;
        return this;
    }

    public Integer getWaterBumpMapId() {
        return waterBumpMapId;
    }

    public SlopeConfig setWaterBumpMapId(Integer waterBumpMapId) {
        this.waterBumpMapId = waterBumpMapId;
        return this;
    }

    public Double getWaterBumpMapDepth() {
        return waterBumpMapDepth;
    }

    public SlopeConfig setWaterBumpMapDepth(Double waterBumpMapDepth) {
        this.waterBumpMapDepth = waterBumpMapDepth;
        return this;
    }

    public Double getWaterTransparency() {
        return waterTransparency;
    }

    public SlopeConfig setWaterTransparency(Double waterTransparency) {
        this.waterTransparency = waterTransparency;
        return this;
    }

    public Double getWaterAnimationDuration() {
        return waterAnimationDuration;
    }

    public SlopeConfig setWaterAnimationDuration(Double waterAnimationDuration) {
        this.waterAnimationDuration = waterAnimationDuration;
        return this;
    }

    public Double getShallowWaterTextureId() {
        return shallowWaterTextureId;
    }

    public SlopeConfig setShallowWaterTextureId(Double shallowWaterTextureId) {
        this.shallowWaterTextureId = shallowWaterTextureId;
        return this;
    }

    public Double getShallowWaterTextureScale() {
        return shallowWaterTextureScale;
    }

    public SlopeConfig setShallowWaterTextureScale(Double shallowWaterTextureScale) {
        this.shallowWaterTextureScale = shallowWaterTextureScale;
        return this;
    }

    public Double getShallowWaterDistortionStrength() {
        return shallowWaterDistortionStrength;
    }

    public SlopeConfig setShallowWaterDistortionStrength(Double shallowWaterDistortionStrength) {
        this.shallowWaterDistortionStrength = shallowWaterDistortionStrength;
        return this;
    }

    public Double getShallowWaterAnimation() {
        return shallowWaterAnimation;
    }

    public SlopeConfig setShallowWaterAnimation(Double shallowWaterAnimation) {
        this.shallowWaterAnimation = shallowWaterAnimation;
        return this;
    }

    public Integer getSlopeWaterSplattingId() {
        return slopeWaterSplattingId;
    }

    public SlopeConfig setSlopeWaterSplattingId(Integer slopeWaterSplattingId) {
        this.slopeWaterSplattingId = slopeWaterSplattingId;
        return this;
    }

    public double getSlopeWaterSplattingScale() {
        return slopeWaterSplattingScale;
    }

    public SlopeConfig setSlopeWaterSplattingScale(double slopeWaterSplattingScale) {
        this.slopeWaterSplattingScale = slopeWaterSplattingScale;
        return this;
    }

    public double getSlopeWaterSplattingFactor() {
        return slopeWaterSplattingFactor;
    }

    public SlopeConfig setSlopeWaterSplattingFactor(double slopeWaterSplattingFactor) {
        this.slopeWaterSplattingFactor = slopeWaterSplattingFactor;
        return this;
    }

    public double getSlopeWaterSplattingFadeThreshold() {
        return slopeWaterSplattingFadeThreshold;
    }

    public SlopeConfig setSlopeWaterSplattingFadeThreshold(double slopeWaterSplattingFadeThreshold) {
        this.slopeWaterSplattingFadeThreshold = slopeWaterSplattingFadeThreshold;
        return this;
    }

    public double getSlopeWaterSplattingHeight() {
        return slopeWaterSplattingHeight;
    }

    public SlopeConfig setSlopeWaterSplattingHeight(double slopeWaterSplattingHeight) {
        this.slopeWaterSplattingHeight = slopeWaterSplattingHeight;
        return this;
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
