package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.system.Nullable;

/**
 * Created by Beat
 * on 15.09.2017.
 */
public class BoxRegionConfig implements ObjectNameIdProvider {
    private Integer id;
    private String internalName;
    private Integer boxItemTypeId;
    private int minInterval; // seconds
    private int maxInterval; // seconds
    private int count;
    private double minDistanceToItems;
    private PlaceConfig region;

    public @Nullable Integer getId() {
        return id;
    }

    public BoxRegionConfig id(@Nullable Integer id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public BoxRegionConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public @Nullable Integer getBoxItemTypeId() {
        return boxItemTypeId;
    }

    public BoxRegionConfig boxItemTypeId(Integer boxItemTypeId) {
        setBoxItemTypeId(boxItemTypeId);
        return this;
    }

    public void setBoxItemTypeId(@Nullable Integer boxItemTypeId) {
        this.boxItemTypeId = boxItemTypeId;
    }

    public int getMinInterval() {
        return minInterval;
    }

    public BoxRegionConfig minInterval(int minInterval) {
        setMinInterval(minInterval);
        return this;
    }

    public void setMinInterval(int minInterval) {
        this.minInterval = minInterval;
    }

    public int getMaxInterval() {
        return maxInterval;
    }

    public BoxRegionConfig maxInterval(int maxInterval) {
        setMaxInterval(maxInterval);
        return this;
    }

    public void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }

    public int getCount() {
        return count;
    }

    public BoxRegionConfig count(int count) {
        setCount(count);
        return this;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getMinDistanceToItems() {
        return minDistanceToItems;
    }

    public BoxRegionConfig minDistanceToItems(double minDistanceToItems) {
        setMinDistanceToItems(minDistanceToItems);
        return this;
    }

    public void setMinDistanceToItems(double minDistanceToItems) {
        this.minDistanceToItems = minDistanceToItems;
    }

    public @Nullable PlaceConfig getRegion() {
        return region;
    }

    public BoxRegionConfig region(PlaceConfig region) {
        setRegion(region);
        return this;
    }

    public void setRegion(@Nullable PlaceConfig region) {
        this.region = region;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
