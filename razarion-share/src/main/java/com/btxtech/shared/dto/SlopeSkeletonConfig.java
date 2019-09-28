package com.btxtech.shared.dto;


/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeSkeletonConfig implements ObjectNameIdProvider {
    public enum Type {
        LAND,
        WATER
    }

    private int id;
    private String internalName;
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
    private Integer groundTextureId;
    private Double groundTextureScale;
    private Integer groundBumpMapId;
    private Double groundBumpMapDepth;
    private Double groundShininess;
    private Double groundSpecularStrength;
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

    public SlopeSkeletonConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public SlopeSkeletonConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    public int getSegments() {
        return segments;
    }

    public SlopeSkeletonConfig setSegments(int segments) {
        this.segments = segments;
        return this;
    }

    public SlopeSkeletonConfig setRows(int rows) {
        this.rows = rows;
        return this;
    }

    public SlopeSkeletonConfig setWidth(double width) {
        this.width = width;
        return this;
    }

    public SlopeSkeletonConfig setHeight(double height) {
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

    public SlopeSkeletonConfig setSlopeBumpMapDepth(double slopeBumpMapDepth) {
        this.slopeBumpMapDepth = slopeBumpMapDepth;
        return this;
    }

    public double getSlopeShininess() {
        return slopeShininess;
    }

    public SlopeSkeletonConfig setSlopeShininess(double slopeShininess) {
        this.slopeShininess = slopeShininess;
        return this;
    }

    public double getSlopeSpecularStrength() {
        return slopeSpecularStrength;
    }

    public SlopeSkeletonConfig setSlopeSpecularStrength(double slopeSpecularStrength) {
        this.slopeSpecularStrength = slopeSpecularStrength;
        return this;
    }

    public Integer getSlopeFoamTextureId() {
        return slopeFoamTextureId;
    }

    public SlopeSkeletonConfig setSlopeFoamTextureId(Integer slopeFoamTextureId) {
        this.slopeFoamTextureId = slopeFoamTextureId;
        return this;
    }

    public Integer getSlopeFoamDistortionId() {
        return slopeFoamDistortionId;
    }

    public SlopeSkeletonConfig setSlopeFoamDistortionId(Integer slopeFoamDistortionId) {
        this.slopeFoamDistortionId = slopeFoamDistortionId;
        return this;
    }

    public Double getSlopeFoamDistortionStrength() {
        return slopeFoamDistortionStrength;
    }

    public SlopeSkeletonConfig setSlopeFoamDistortionStrength(Double slopeFoamDistortionStrength) {
        this.slopeFoamDistortionStrength = slopeFoamDistortionStrength;
        return this;
    }

    public Double getSlopeFoamAnimationDuration() {
        return slopeFoamAnimationDuration;
    }

    public SlopeSkeletonConfig setSlopeFoamAnimationDuration(Double slopeFoamAnimationDuration) {
        this.slopeFoamAnimationDuration = slopeFoamAnimationDuration;
        return this;
    }

    public Integer getGroundTextureId() {
        return groundTextureId;
    }

    public SlopeSkeletonConfig setGroundTextureId(Integer groundTextureId) {
        this.groundTextureId = groundTextureId;
        return this;
    }

    public Double getGroundTextureScale() {
        return groundTextureScale;
    }

    public SlopeSkeletonConfig setGroundTextureScale(Double groundTextureScale) {
        this.groundTextureScale = groundTextureScale;
        return this;
    }

    public Integer getGroundBumpMapId() {
        return groundBumpMapId;
    }

    public SlopeSkeletonConfig setGroundBumpMapId(Integer groundBumpMapId) {
        this.groundBumpMapId = groundBumpMapId;
        return this;
    }

    public Double getGroundBumpMapDepth() {
        return groundBumpMapDepth;
    }

    public SlopeSkeletonConfig setGroundBumpMapDepth(Double groundBumpMapDepth) {
        this.groundBumpMapDepth = groundBumpMapDepth;
        return this;
    }

    public Double getGroundShininess() {
        return groundShininess;
    }

    public SlopeSkeletonConfig setGroundShininess(Double groundShininess) {
        this.groundShininess = groundShininess;
        return this;
    }

    public Double getGroundSpecularStrength() {
        return groundSpecularStrength;
    }

    public SlopeSkeletonConfig setGroundSpecularStrength(Double groundSpecularStrength) {
        this.groundSpecularStrength = groundSpecularStrength;
        return this;
    }

    public double getOuterLineGameEngine() {
        return outerLineGameEngine;
    }

    public SlopeSkeletonConfig setOuterLineGameEngine(double outerLineGameEngine) {
        this.outerLineGameEngine = outerLineGameEngine;
        return this;
    }

    public double getInnerLineGameEngine() {
        return innerLineGameEngine;
    }

    public SlopeSkeletonConfig setInnerLineGameEngine(double innerLineGameEngine) {
        this.innerLineGameEngine = innerLineGameEngine;
        return this;
    }

    public double getCoastDelimiterLineGameEngine() {
        return coastDelimiterLineGameEngine;
    }

    public SlopeSkeletonConfig setCoastDelimiterLineGameEngine(double coastDelimiterLineGameEngine) {
        this.coastDelimiterLineGameEngine = coastDelimiterLineGameEngine;
        return this;
    }

    public double getHorizontalSpace() {
        return horizontalSpace;
    }

    public SlopeSkeletonConfig setHorizontalSpace(double horizontalSpace) {
        this.horizontalSpace = horizontalSpace;
        return this;
    }

    public Type getType() {
        return type;
    }

    public SlopeSkeletonConfig setType(Type type) {
        this.type = type;
        return this;
    }

    public SlopeNode[][] getSlopeNodes() {
        return slopeNodes;
    }

    public SlopeSkeletonConfig setSlopeNodes(SlopeNode[][] slopeNodes) {
        this.slopeNodes = slopeNodes;
        return this;
    }

    public boolean isInterpolateNorm() {
        return interpolateNorm;
    }

    public SlopeSkeletonConfig setInterpolateNorm(boolean interpolateNorm) {
        this.interpolateNorm = interpolateNorm;
        return this;
    }

    @Deprecated
    public SpecularLightConfig getSpecularLightConfig() {
        return specularLightConfig;
    }

    @Deprecated
    public SlopeSkeletonConfig setSpecularLightConfig(SpecularLightConfig specularLightConfig) {
        this.specularLightConfig = specularLightConfig;
        return this;
    }

    public Integer getSlopeTextureId() {
        return slopeTextureId;
    }

    public SlopeSkeletonConfig setSlopeTextureId(Integer slopeTextureId) {
        this.slopeTextureId = slopeTextureId;
        return this;
    }

    public double getSlopeTextureScale() {
        return slopeTextureScale;
    }

    public SlopeSkeletonConfig setSlopeTextureScale(double slopeTextureScale) {
        this.slopeTextureScale = slopeTextureScale;
        return this;
    }

    public Integer getSlopeBumpMapId() {
        return slopeBumpMapId;
    }

    public SlopeSkeletonConfig setSlopeBumpMapId(Integer slopeBumpMapId) {
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

    public SlopeSkeletonConfig setWaterLevel(Double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

    public Double getWaterFresnelOffset() {
        return waterFresnelOffset;
    }

    public SlopeSkeletonConfig setWaterFresnelOffset(Double waterFresnelOffset) {
        this.waterFresnelOffset = waterFresnelOffset;
        return this;
    }

    public Double getWaterFresnelDelta() {
        return waterFresnelDelta;
    }

    public SlopeSkeletonConfig setWaterFresnelDelta(Double waterFresnelDelta) {
        this.waterFresnelDelta = waterFresnelDelta;
        return this;
    }

    public Double getWaterShininess() {
        return waterShininess;
    }

    public SlopeSkeletonConfig setWaterShininess(Double waterShininess) {
        this.waterShininess = waterShininess;
        return this;
    }

    public Double getWaterSpecularStrength() {
        return waterSpecularStrength;
    }

    public SlopeSkeletonConfig setWaterSpecularStrength(Double waterSpecularStrength) {
        this.waterSpecularStrength = waterSpecularStrength;
        return this;
    }

    public Integer getWaterReflectionId() {
        return waterReflectionId;
    }

    public SlopeSkeletonConfig setWaterReflectionId(Integer waterReflectionId) {
        this.waterReflectionId = waterReflectionId;
        return this;
    }

    public Double getWaterReflectionScale() {
        return waterReflectionScale;
    }

    public SlopeSkeletonConfig setWaterReflectionScale(Double waterReflectionScale) {
        this.waterReflectionScale = waterReflectionScale;
        return this;
    }

    public Double getWaterMapScale() {
        return waterMapScale;
    }

    public SlopeSkeletonConfig setWaterMapScale(Double waterMapScale) {
        this.waterMapScale = waterMapScale;
        return this;
    }

    public Integer getWaterDistortionId() {
        return waterDistortionId;
    }

    public SlopeSkeletonConfig setWaterDistortionId(Integer waterDistortionId) {
        this.waterDistortionId = waterDistortionId;
        return this;
    }

    public Double getWaterDistortionStrength() {
        return waterDistortionStrength;
    }

    public SlopeSkeletonConfig setWaterDistortionStrength(Double waterDistortionStrength) {
        this.waterDistortionStrength = waterDistortionStrength;
        return this;
    }

    public Integer getWaterBumpMapId() {
        return waterBumpMapId;
    }

    public SlopeSkeletonConfig setWaterBumpMapId(Integer waterBumpMapId) {
        this.waterBumpMapId = waterBumpMapId;
        return this;
    }

    public Double getWaterBumpMapDepth() {
        return waterBumpMapDepth;
    }

    public SlopeSkeletonConfig setWaterBumpMapDepth(Double waterBumpMapDepth) {
        this.waterBumpMapDepth = waterBumpMapDepth;
        return this;
    }

    public Double getWaterTransparency() {
        return waterTransparency;
    }

    public SlopeSkeletonConfig setWaterTransparency(Double waterTransparency) {
        this.waterTransparency = waterTransparency;
        return this;
    }

    public Double getWaterAnimationDuration() {
        return waterAnimationDuration;
    }

    public SlopeSkeletonConfig setWaterAnimationDuration(Double waterAnimationDuration) {
        this.waterAnimationDuration = waterAnimationDuration;
        return this;
    }

    public Double getShallowWaterTextureId() {
        return shallowWaterTextureId;
    }

    public SlopeSkeletonConfig setShallowWaterTextureId(Double shallowWaterTextureId) {
        this.shallowWaterTextureId = shallowWaterTextureId;
        return this;
    }

    public Double getShallowWaterTextureScale() {
        return shallowWaterTextureScale;
    }

    public SlopeSkeletonConfig setShallowWaterTextureScale(Double shallowWaterTextureScale) {
        this.shallowWaterTextureScale = shallowWaterTextureScale;
        return this;
    }

    public Double getShallowWaterDistortionStrength() {
        return shallowWaterDistortionStrength;
    }

    public SlopeSkeletonConfig setShallowWaterDistortionStrength(Double shallowWaterDistortionStrength) {
        this.shallowWaterDistortionStrength = shallowWaterDistortionStrength;
        return this;
    }

    public Double getShallowWaterAnimation() {
        return shallowWaterAnimation;
    }

    public SlopeSkeletonConfig setShallowWaterAnimation(Double shallowWaterAnimation) {
        this.shallowWaterAnimation = shallowWaterAnimation;
        return this;
    }

    public Integer getSlopeWaterSplattingId() {
        return slopeWaterSplattingId;
    }

    public SlopeSkeletonConfig setSlopeWaterSplattingId(Integer slopeWaterSplattingId) {
        this.slopeWaterSplattingId = slopeWaterSplattingId;
        return this;
    }

    public double getSlopeWaterSplattingScale() {
        return slopeWaterSplattingScale;
    }

    public SlopeSkeletonConfig setSlopeWaterSplattingScale(double slopeWaterSplattingScale) {
        this.slopeWaterSplattingScale = slopeWaterSplattingScale;
        return this;
    }

    public double getSlopeWaterSplattingFactor() {
        return slopeWaterSplattingFactor;
    }

    public SlopeSkeletonConfig setSlopeWaterSplattingFactor(double slopeWaterSplattingFactor) {
        this.slopeWaterSplattingFactor = slopeWaterSplattingFactor;
        return this;
    }

    public double getSlopeWaterSplattingFadeThreshold() {
        return slopeWaterSplattingFadeThreshold;
    }

    public SlopeSkeletonConfig setSlopeWaterSplattingFadeThreshold(double slopeWaterSplattingFadeThreshold) {
        this.slopeWaterSplattingFadeThreshold = slopeWaterSplattingFadeThreshold;
        return this;
    }

    public double getSlopeWaterSplattingHeight() {
        return slopeWaterSplattingHeight;
    }

    public SlopeSkeletonConfig setSlopeWaterSplattingHeight(double slopeWaterSplattingHeight) {
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

        SlopeSkeletonConfig that = (SlopeSkeletonConfig) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
