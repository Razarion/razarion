package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.List;

/**
 * Created by Beat
 * 06.05.2016.
 */
public class TerrainSlopePosition {
    private Integer id;
    private int slopeConfigEntity;
    private List<DecimalPosition> polygon;

    public Integer getId() {
        return id;
    }

    public TerrainSlopePosition setId(int id) {
        this.id = id;
        return this;
    }

    public int getSlopeConfigEntity() {
        return slopeConfigEntity;
    }

    public TerrainSlopePosition setSlopeConfigEntity(int slopeConfigEntity) {
        this.slopeConfigEntity = slopeConfigEntity;
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
