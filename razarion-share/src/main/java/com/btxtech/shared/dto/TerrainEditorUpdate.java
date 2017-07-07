package com.btxtech.shared.dto;

import java.util.List;

/**
 * Created by Beat
 * on 07.07.2017.
 */
public class TerrainEditorUpdate {
    private List<TerrainSlopePosition> createdSlopes;
    private List<TerrainSlopePosition> updatedSlopes;
    private List<Integer> deletedSlopeIds;

    public List<TerrainSlopePosition> getCreatedSlopes() {
        return createdSlopes;
    }

    public void setCreatedSlopes(List<TerrainSlopePosition> createdSlopes) {
        this.createdSlopes = createdSlopes;
    }

    public List<TerrainSlopePosition> getUpdatedSlopes() {
        return updatedSlopes;
    }

    public void setUpdatedSlopes(List<TerrainSlopePosition> updatedSlopes) {
        this.updatedSlopes = updatedSlopes;
    }

    public List<Integer> getDeletedSlopeIds() {
        return deletedSlopeIds;
    }

    public void setDeletedSlopeIds(List<Integer> deletedSlopeIds) {
        this.deletedSlopeIds = deletedSlopeIds;
    }
}
