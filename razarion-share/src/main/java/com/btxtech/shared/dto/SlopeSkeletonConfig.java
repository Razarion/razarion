package com.btxtech.shared.dto;


/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeSkeletonConfig {
    public enum Type {
        LAND,
        WATER
    }

    private int id;
    private int segments;
    private int rows;
    private double width;
    private double height;
    private double verticalSpace;
    private double bumpMapDepth;
    private Type type;
    private SlopeNode[][] slopeNodes;
    private LightConfig lightConfig;
    private boolean slopeOriented;
    private int imageId;
    private double imageScale;
    private int bumpImageId;
    private double bumpImageScale;

    public int getId() {
        return id;
    }

    public SlopeSkeletonConfig setId(int id) {
        this.id = id;
        return this;
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

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public SlopeSkeletonConfig setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
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

    public int getImageId() {
        return imageId;
    }

    public SlopeSkeletonConfig setImageId(int imageId) {
        this.imageId = imageId;
        return this;
    }

    public double getImageScale() {
        return imageScale;
    }

    public SlopeSkeletonConfig setImageScale(double imageScale) {
        this.imageScale = imageScale;
        return this;
    }

    public int getBumpImageId() {
        return bumpImageId;
    }

    public SlopeSkeletonConfig setBumpImageId(int bumpImageId) {
        this.bumpImageId = bumpImageId;
        return this;
    }

    public double getBumpImageScale() {
        return bumpImageScale;
    }

    public SlopeSkeletonConfig setBumpImageScale(double bumpImageScale) {
        this.bumpImageScale = bumpImageScale;
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
