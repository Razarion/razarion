package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType
public class TerrainTile {
    private Index index;
    private TerrainTileObjectList[] terrainTileObjectLists;
    private int groundConfigId;
    private Uint16ArrayEmu groundHeightMap;
    private BabylonDecal[] babylonDecals;
    private BotGround[] botGrounds;

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
    public Uint16ArrayEmu getGroundHeightMap() {
        return groundHeightMap;
    }

    public void setGroundHeightMap(Uint16ArrayEmu groundHeightMap) {
        this.groundHeightMap = groundHeightMap;
    }

    public TerrainTile groundHeightMap(Uint16ArrayEmu groundHeightMap) {
        setGroundHeightMap(groundHeightMap);
        return this;
    }

    @SuppressWarnings("unused") // Used ba angular
    public BabylonDecal[] getBabylonDecals() {
        return babylonDecals;
    }

    public void setBabylonDecals(BabylonDecal[] babylonDecals) {
        this.babylonDecals = babylonDecals;
    }

    @SuppressWarnings("unused") // Used ba angular
    public BotGround[] getBotGrounds() {
        return botGrounds;
    }

    public void setBotGrounds(BotGround[] botGrounds) {
        this.botGrounds = botGrounds;
    }
}