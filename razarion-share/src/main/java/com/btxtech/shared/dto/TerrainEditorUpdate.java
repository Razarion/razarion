package com.btxtech.shared.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 07.07.2017.
 */
public class TerrainEditorUpdate {
    private List<TerrainSlopePosition> createdSlopes;
    private List<TerrainSlopePosition> updatedSlopes;
    private List<Integer> deletedSlopeIds;
    private List<TerrainObjectPosition> createdTerrainObjects = new ArrayList<>();
    private List<TerrainObjectPosition> updatedTerrainObjects = new ArrayList<>();
    private List<Integer> deletedTerrainObjectsIds = new ArrayList<>();


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

    public List<TerrainObjectPosition> getCreatedTerrainObjects() {
        return createdTerrainObjects;
    }

    public void setCreatedTerrainObjects(List<TerrainObjectPosition> createdTerrainObjects) {
        this.createdTerrainObjects = createdTerrainObjects;
    }

    public List<TerrainObjectPosition> getUpdatedTerrainObjects() {
        return updatedTerrainObjects;
    }

    public void setUpdatedTerrainObjects(List<TerrainObjectPosition> updatedTerrainObjects) {
        this.updatedTerrainObjects = updatedTerrainObjects;
    }

    public List<Integer> getDeletedTerrainObjectsIds() {
        return deletedTerrainObjectsIds;
    }

    public void setDeletedTerrainObjectsIds(List<Integer> deletedTerrainObjectsIds) {
        this.deletedTerrainObjectsIds = deletedTerrainObjectsIds;
    }

    public boolean hasAnyChanged() {
        return !createdSlopes.isEmpty() || !updatedSlopes.isEmpty() || !deletedSlopeIds.isEmpty() || !createdTerrainObjects.isEmpty() || !updatedTerrainObjects.isEmpty() || !deletedTerrainObjectsIds.isEmpty();
    }

    public TerrainEditorUpdate createdSlopes(List<TerrainSlopePosition> createdSlopes) {
        setCreatedSlopes(createdSlopes);
        return this;
    }

    public TerrainEditorUpdate updatedSlopes(List<TerrainSlopePosition> updatedSlopes) {
        setUpdatedSlopes(updatedSlopes);
        return this;
    }

    public TerrainEditorUpdate deletedSlopeIds(List<Integer> deletedSlopeIds) {
        setDeletedSlopeIds(deletedSlopeIds);
        return this;
    }

    public TerrainEditorUpdate createdTerrainObjects(List<TerrainObjectPosition> createdTerrainObjects) {
        setCreatedTerrainObjects(createdTerrainObjects);
        return this;
    }

    public TerrainEditorUpdate updatedTerrainObjects(List<TerrainObjectPosition> updatedTerrainObjects) {
        setUpdatedTerrainObjects(updatedTerrainObjects);
        return this;
    }

    public TerrainEditorUpdate deletedTerrainObjectsIds(List<Integer> deletedTerrainObjectsIds) {
        setDeletedTerrainObjectsIds(deletedTerrainObjectsIds);
        return this;
    }
}
