package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.system.Nullable;

/**
 * Created by Beat
 * on 28.07.2017.
 */
public class StartRegionConfig {
    private Integer id;
    private String internalName;
    private Integer minimalLevelId;
    private PlaceConfig region;
    private DecimalPosition noBaseViewPosition;
    private boolean findFreePosition;
    private Double positionRadius;
    private Integer positionMaxItems;

    public @Nullable Integer getId() {
        return id;
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public @Nullable Integer getMinimalLevelId() {
        return minimalLevelId;
    }

    public void setMinimalLevelId(@Nullable Integer minimalLevelId) {
        this.minimalLevelId = minimalLevelId;
    }

    public @Nullable PlaceConfig getRegion() {
        return region;
    }

    public void setRegion(@Nullable PlaceConfig region) {
        this.region = region;
    }

    public @Nullable DecimalPosition getNoBaseViewPosition() {
        return noBaseViewPosition;
    }

    public void setNoBaseViewPosition(@Nullable DecimalPosition noBaseViewPosition) {
        this.noBaseViewPosition = noBaseViewPosition;
    }

    public boolean isFindFreePosition() {
        return findFreePosition;
    }

    public void setFindFreePosition(boolean findFreePosition) {
        this.findFreePosition = findFreePosition;
    }

    public @Nullable Double getPositionRadius() {
        return positionRadius;
    }

    public void setPositionRadius(@Nullable Double positionRadius) {
        this.positionRadius = positionRadius;
    }

    public @Nullable Integer getPositionMaxItems() {
        return positionMaxItems;
    }

    public void setPositionMaxItems(@Nullable Integer positionMaxItems) {
        this.positionMaxItems = positionMaxItems;
    }

    public StartRegionConfig id(int id) {
        setId(id);
        return this;
    }

    public StartRegionConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public StartRegionConfig minimalLevelId(Integer minimalLevelId) {
        setMinimalLevelId(minimalLevelId);
        return this;
    }

    public StartRegionConfig region(PlaceConfig region) {
        setRegion(region);
        return this;
    }

    public StartRegionConfig noBaseViewPosition(DecimalPosition noBaseViewPosition) {
        setNoBaseViewPosition(noBaseViewPosition);
        return this;
    }

    public StartRegionConfig findFreePosition(boolean findFreePosition) {
        setFindFreePosition(findFreePosition);
        return this;
    }

    public StartRegionConfig positionRadius(Double positionRadius) {
        setPositionRadius(positionRadius);
        return this;
    }

    public StartRegionConfig positionMaxItems(Integer positionMaxItems) {
        setPositionMaxItems(positionMaxItems);
        return this;
    }
}
