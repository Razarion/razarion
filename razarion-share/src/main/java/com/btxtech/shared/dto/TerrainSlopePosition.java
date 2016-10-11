package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;

import java.util.List;

/**
 * Created by Beat
 * 06.05.2016.
 */
public class TerrainSlopePosition {
    private Integer id;
    private int slopeId;
    private List<DecimalPosition> polygon;

    /**
     * Used by errai
     */
    public TerrainSlopePosition() {
    }

    @Deprecated
    public TerrainSlopePosition(Integer id, int slopeId, List<DecimalPosition> polygon) {
        this.id = id;
        this.slopeId = slopeId;
        this.polygon = polygon;
    }

    public Integer getId() {
        return id;
    }

    public boolean hasId() {
        return id != null;
    }

    public TerrainSlopePosition setId(int id) {
        this.id = id;
        return this;
    }

    public int getSlopeId() {
        return slopeId;
    }

    public TerrainSlopePosition setSlopeId(int slopeId) {
        this.slopeId = slopeId;
        return this;
    }

    public List<DecimalPosition> getPolygon() {
        return polygon;
    }

    public TerrainSlopePosition setPolygon(List<DecimalPosition> polygon) {
        this.polygon = polygon;
        return this;
    }
}
