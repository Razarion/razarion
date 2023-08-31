package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.system.Nullable;

/**
 * Created by Beat
 * on 28.07.2017.
 */
public class StartRegionConfig {
    private int id;
    private String internalName;
    private Integer minimalLevelId;
    private PlaceConfig region;
    private DecimalPosition noBaseViewPosition;

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
}
