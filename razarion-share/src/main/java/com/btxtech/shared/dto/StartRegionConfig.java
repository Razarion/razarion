package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Polygon2D;

/**
 * Created by Beat
 * on 28.07.2017.
 */
public class StartRegionConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private Integer minimalLevelId;
    private Polygon2D region;

    public int getId() {
        return id;
    }

    public StartRegionConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public StartRegionConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Integer getMinimalLevelId() {
        return minimalLevelId;
    }

    public StartRegionConfig setMinimalLevelId(Integer minimalLevelId) {
        this.minimalLevelId = minimalLevelId;
        return this;
    }

    public Polygon2D getRegion() {
        return region;
    }

    public StartRegionConfig setRegion(Polygon2D region) {
        this.region = region;
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
