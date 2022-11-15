package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.nativejs.NativeMatrix;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 18.01.2018.
 */
@JsType
public class TerrainTileObjectList {
    public int terrainObjectConfigId;
    public TerrainObjectModel[] terrainObjectModels;

    public int getTerrainObjectConfigId() {
        return terrainObjectConfigId;
    }

    public void setTerrainObjectConfigId(int terrainObjectConfigId) {
        this.terrainObjectConfigId = terrainObjectConfigId;
    }

    public void setTerrainObjectModels(TerrainObjectModel[] terrainObjectModels) {
        this.terrainObjectModels = terrainObjectModels;
    }

    public TerrainObjectModel[] getTerrainObjectModels() {
        return terrainObjectModels;
    }
}
