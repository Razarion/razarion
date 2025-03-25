package com.btxtech.shared.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 07.07.2017.
 */
public class TerrainEditorUpdate {
    private List<TerrainObjectPosition> createdTerrainObjects = new ArrayList<>();
    private List<TerrainObjectPosition> updatedTerrainObjects = new ArrayList<>();
    private List<Integer> deletedTerrainObjectsIds = new ArrayList<>();


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

    public boolean hasAnyChanged() {
        return !createdTerrainObjects.isEmpty() || !updatedTerrainObjects.isEmpty() || !deletedTerrainObjectsIds.isEmpty();
    }

}
