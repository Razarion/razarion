package com.btxtech.shared.dto;


/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeSkeletonConfig implements ObjectNameIdProvider{
    public enum Type {
        LAND,
        WATER
    }

    private int id;
    private String internalName;
    private int segments;
    private int rows;
    private double width;
    private double height;
    private double verticalSpace;
    private Type type;
    private SlopeNode[][] slopeNodes;
    private LightConfig lightConfig;
    private boolean slopeOriented;
    private Integer textureId;
    private double textureScale;
    private Integer bmId;
    private double bmScale;
    private double bmDepth;

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

    public double getBmDepth() {
        return bmDepth;
    }

    public SlopeSkeletonConfig setBmDepth(double bmDepth) {
        this.bmDepth = bmDepth;
        return this;
    }

    public double getVerticalSpace() {
        return verticalSpace;
    }

    public SlopeSkeletonConfig setVerticalSpace(double verticalSpace) {
        this.verticalSpace = verticalSpace;
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

    public LightConfig getLightConfig() {
        return lightConfig;
    }

    public SlopeSkeletonConfig setLightConfig(LightConfig lightConfig) {
        this.lightConfig = lightConfig;
        return this;
    }

    // Errai can not handle is-getter
    public boolean getSlopeOriented() {
        return slopeOriented;
    }

    public SlopeSkeletonConfig setSlopeOriented(boolean slopeOriented) {
        this.slopeOriented = slopeOriented;
        return this;
    }

    public Integer getTextureId() {
        return textureId;
    }

    public SlopeSkeletonConfig setTextureId(Integer textureId) {
        this.textureId = textureId;
        return this;
    }

    public double getTextureScale() {
        return textureScale;
    }

    public SlopeSkeletonConfig setTextureScale(double textureScale) {
        this.textureScale = textureScale;
        return this;
    }

    public Integer getBmId() {
        return bmId;
    }

    public SlopeSkeletonConfig setBmId(Integer bmId) {
        this.bmId = bmId;
        return this;
    }

    public double getBmScale() {
        return bmScale;
    }

    public SlopeSkeletonConfig setBmScale(double bmScale) {
        this.bmScale = bmScale;
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
