package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * on 15.09.2017.
 */
public class BoxRegionConfig {
    private int id;
    private String internalName;
    private Integer boxItemTypeId;
    private int minInterval; // seconds
    private int maxInterval; // seconds
    private int count;
    private double minDistanceToItems;
    private PlaceConfig region;

    public int getId() {
        return id;
    }

    public BoxRegionConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public BoxRegionConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Integer getBoxItemTypeId() {
        return boxItemTypeId;
    }

    public BoxRegionConfig setBoxItemTypeId(Integer boxItemTypeId) {
        this.boxItemTypeId = boxItemTypeId;
        return this;
    }

    public int getMinInterval() {
        return minInterval;
    }

    public BoxRegionConfig setMinInterval(int minInterval) {
        this.minInterval = minInterval;
        return this;
    }

    public int getMaxInterval() {
        return maxInterval;
    }

    public BoxRegionConfig setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
        return this;
    }

    public int getCount() {
        return count;
    }

    public BoxRegionConfig setCount(int count) {
        this.count = count;
        return this;
    }

    public double getMinDistanceToItems() {
        return minDistanceToItems;
    }

    public BoxRegionConfig setMinDistanceToItems(double minDistanceToItems) {
        this.minDistanceToItems = minDistanceToItems;
        return this;
    }

    public PlaceConfig getRegion() {
        return region;
    }

    public BoxRegionConfig setRegion(PlaceConfig region) {
        this.region = region;
        return this;
    }
}
