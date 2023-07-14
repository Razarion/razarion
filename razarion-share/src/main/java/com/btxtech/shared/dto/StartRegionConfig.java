package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;

/**
 * Created by Beat
 * on 28.07.2017.
 */
public class StartRegionConfig {
    private int id;
    private String internalName;
    private Integer minimalLevelId;
    private Polygon2D region;
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

    public Integer getMinimalLevelId() {
        return minimalLevelId;
    }

    public void setMinimalLevelId(Integer minimalLevelId) {
        this.minimalLevelId = minimalLevelId;
    }

    public Polygon2D getRegion() {
        return region;
    }

    public void setRegion(Polygon2D region) {
        this.region = region;
    }

    public DecimalPosition getNoBaseViewPosition() {
        return noBaseViewPosition;
    }

    public void setNoBaseViewPosition(DecimalPosition noBaseViewPosition) {
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

    public StartRegionConfig region(Polygon2D region) {
        setRegion(region);
        return this;
    }

    public StartRegionConfig noBaseViewPosition(DecimalPosition noBaseViewPosition) {
        setNoBaseViewPosition(noBaseViewPosition);
        return this;
    }
}
