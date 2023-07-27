package com.btxtech.shared.dto;

import java.util.List;

public class SlopeTerrainEditorUpdate {
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

//    public boolean hasAnyChanged() {
//        return !createdSlopes.isEmpty() || !updatedSlopes.isEmpty() || !deletedSlopeIds.isEmpty() || !createdTerrainObjects.isEmpty() || !updatedTerrainObjects.isEmpty() || !deletedTerrainObjectsIds.isEmpty();
//    }

    public SlopeTerrainEditorUpdate createdSlopes(List<TerrainSlopePosition> createdSlopes) {
        setCreatedSlopes(createdSlopes);
        return this;
    }

    public SlopeTerrainEditorUpdate updatedSlopes(List<TerrainSlopePosition> updatedSlopes) {
        setUpdatedSlopes(updatedSlopes);
        return this;
    }

    public SlopeTerrainEditorUpdate deletedSlopeIds(List<Integer> deletedSlopeIds) {
        setDeletedSlopeIds(deletedSlopeIds);
        return this;
    }

}
