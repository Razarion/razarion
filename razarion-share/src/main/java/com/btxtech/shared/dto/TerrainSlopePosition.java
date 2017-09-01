package com.btxtech.shared.dto;

import java.util.List;

/**
 * Created by Beat
 * 06.05.2016.
 */
public class TerrainSlopePosition {
    private Integer id;
    private int slopeConfigId;
    private List<TerrainSlopeCorner> polygon;
    private List<TerrainSlopePosition> children;

    public Integer getId() {
        return id;
    }

    public TerrainSlopePosition setId(int id) {
        this.id = id;
        return this;
    }

    public int getSlopeConfigId() {
        return slopeConfigId;
    }

    public TerrainSlopePosition setSlopeConfigId(int slopeConfigId) {
        this.slopeConfigId = slopeConfigId;
        return this;
    }

    public List<TerrainSlopeCorner> getPolygon() {
        return polygon;
    }

    public TerrainSlopePosition setPolygon(List<TerrainSlopeCorner> polygon) {
        this.polygon = polygon;
        return this;
    }

    public List<TerrainSlopePosition> getChildren() {
        return children;
    }

    public void setChildren(List<TerrainSlopePosition> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainSlopePosition that = (TerrainSlopePosition) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
