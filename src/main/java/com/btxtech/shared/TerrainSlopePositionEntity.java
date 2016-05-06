package com.btxtech.shared;

import com.btxtech.game.jsre.client.common.Index;

import java.util.List;

/**
 * Created by Beat
 * 06.05.2016.
 */
public class TerrainSlopePositionEntity {
    private int id;
    private int slopeId;
    private List<Index> polygon;

    public TerrainSlopePositionEntity(int id, int slopeId, List<Index> polygon) {
        this.id = id;
        this.slopeId = slopeId;
        this.polygon = polygon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSlopeId() {
        return slopeId;
    }

    public void setSlopeId(int slopeId) {
        this.slopeId = slopeId;
    }

    public List<Index> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<Index> polygon) {
        this.polygon = polygon;
    }
}
