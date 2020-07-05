package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.nativejs.NativeMatrix;

import java.util.List;

/**
 * Created by Beat
 * on 18.01.2018.
 */
public class TerrainTileObjectList {
    private int terrainObjectConfigId;
    private List<NativeMatrix> models;

    public int getTerrainObjectConfigId() {
        return terrainObjectConfigId;
    }

    public void setTerrainObjectConfigId(int terrainObjectConfigId) {
        this.terrainObjectConfigId = terrainObjectConfigId;
    }

    public void setModel(List<NativeMatrix> models) {
        this.models = models;
    }

    public List<NativeMatrix> getModels() {
        return models;
    }
}
