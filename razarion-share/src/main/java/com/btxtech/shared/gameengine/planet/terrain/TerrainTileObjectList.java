package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.nativejs.NativeMatrix;
import jsinterop.annotations.JsType;

import java.util.List;

/**
 * Created by Beat
 * on 18.01.2018.
 */
@JsType
public class TerrainTileObjectList {
    @Deprecated
    private int terrainObjectConfigId;
    public NativeMatrix[] models;

    public int getTerrainObjectConfigId() {
        return terrainObjectConfigId;
    }

    public void setTerrainObjectConfigId(int terrainObjectConfigId) {
        this.terrainObjectConfigId = terrainObjectConfigId;
    }

    public void setModel(NativeMatrix[] models) {
        this.models = models;
    }

    public NativeMatrix[] getModels() {
        return models;
    }
}
