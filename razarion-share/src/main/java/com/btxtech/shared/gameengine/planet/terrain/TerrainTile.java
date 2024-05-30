package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 28.03.2017.
 */
@JsType
public class TerrainTile {
    private Index index;
    private TerrainTileObjectList[] terrainTileObjectLists;
    private int groundConfigId;
    private int waterConfigId;
    private Uint16ArrayEmu groundHeightMap;

    public Index getIndex() {
        return index;
    }

    @JsIgnore
    public void setIndex(Index index) {
        this.index = index;
    }

    @SuppressWarnings("unused") // Used ba angular
    public TerrainTileObjectList[] getTerrainTileObjectLists() {
        return terrainTileObjectLists;
    }

    public void setTerrainTileObjectLists(TerrainTileObjectList[] terrainTileObjectLists) {
        this.terrainTileObjectLists = terrainTileObjectLists;
    }

    @SuppressWarnings("unused") // Used ba angular
    public int getGroundConfigId() {
        return groundConfigId;
    }

    public void setGroundConfigId(int groundConfigId) {
        this.groundConfigId = groundConfigId;
    }

    public TerrainTile groundConfigId(int groundConfigId) {
        setGroundConfigId(groundConfigId);
        return this;
    }

    @SuppressWarnings("unused") // Used ba angular
    public int getWaterConfigId() {
        return waterConfigId;
    }

    public void setWaterConfigId(int waterConfigId) {
        this.waterConfigId = waterConfigId;
    }

    public TerrainTile waterConfigId(int waterConfigId) {
        setWaterConfigId(waterConfigId);
        return this;
    }

    @SuppressWarnings("unused") // Used ba angular
    public Uint16ArrayEmu getGroundHeightMap() {
        return groundHeightMap;
    }

    public void setGroundHeightMap(Uint16ArrayEmu groundHeightMap) {
        this.groundHeightMap = groundHeightMap;
    }

    @SuppressWarnings("unused") // Used ba angular
    public TerrainTile groundHeightMap(Uint16ArrayEmu groundHeightMap) {
        setGroundHeightMap(groundHeightMap);
        return this;
    }
}
