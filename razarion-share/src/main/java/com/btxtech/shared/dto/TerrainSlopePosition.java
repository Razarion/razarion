package com.btxtech.shared.dto;

import java.util.List;
import java.util.Objects;

/**
 * Created by Beat
 * 06.05.2016.
 */
public class TerrainSlopePosition {
    private Integer id;
    private int slopeConfigId;
    private boolean inverted;
    private List<TerrainSlopeCorner> polygon;
    private List<TerrainSlopePosition> children;
    private Integer editorParentIdIfCreated; // Filled in Angular slope editor

    public Integer getId() {
        return id;
    }

    public int getSlopeConfigId() {
        return slopeConfigId;
    }

    public void setSlopeConfigId(int slopeConfigId) {
        this.slopeConfigId = slopeConfigId;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public List<TerrainSlopeCorner> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<TerrainSlopeCorner> polygon) {
        this.polygon = polygon;
    }

    public List<TerrainSlopePosition> getChildren() {
        return children;
    }

    public void setChildren(List<TerrainSlopePosition> children) {
        this.children = children;
    }

    public Integer getEditorParentIdIfCreated() {
        return editorParentIdIfCreated;
    }

    public void setEditorParentIdIfCreated(Integer editorParentIdIfCreated) {
        this.editorParentIdIfCreated = editorParentIdIfCreated;
    }

    public TerrainSlopePosition id(Integer id) {
        this.id = id;
        return this;
    }

    public TerrainSlopePosition slopeConfigId(int slopeConfigId) {
        setSlopeConfigId(slopeConfigId);
        return this;
    }

    public TerrainSlopePosition inverted(boolean inverted) {
        setInverted(inverted);
        return this;
    }

    public TerrainSlopePosition polygon(List<TerrainSlopeCorner> polygon) {
        setPolygon(polygon);
        return this;
    }

    public TerrainSlopePosition children(List<TerrainSlopePosition> children) {
        setChildren(children);
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

        TerrainSlopePosition that = (TerrainSlopePosition) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
