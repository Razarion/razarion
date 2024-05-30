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
    @Deprecated
    private GroundTerrainTile[] groundTerrainTiles;
    private Index index;
    @Deprecated
    private TerrainWaterTile[] terrainWaterTiles;
    @Deprecated
    private TerrainSlopeTile[] terrainSlopeTiles;
    @Deprecated
    private double landWaterProportion;
    @Deprecated
    private TerrainNode[][] terrainNodes;
    @Deprecated
    private double height;
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

    @Deprecated
    public GroundTerrainTile[] getGroundTerrainTiles() {
        return groundTerrainTiles;
    }

    @Deprecated
    public void setGroundTerrainTiles(GroundTerrainTile[] groundTerrainTiles) {
        this.groundTerrainTiles = groundTerrainTiles;
    }

    @Deprecated
    public TerrainSlopeTile[] getTerrainSlopeTiles() {
        return terrainSlopeTiles;
    }

    @Deprecated
    public void setTerrainSlopeTiles(TerrainSlopeTile[] terrainSlopeTiles) {
        this.terrainSlopeTiles = terrainSlopeTiles;
    }

    @Deprecated
    public TerrainWaterTile[] getTerrainWaterTiles() {
        return terrainWaterTiles;
    }

    @Deprecated
    public void setTerrainWaterTiles(TerrainWaterTile[] terrainWaterTiles) {
        this.terrainWaterTiles = terrainWaterTiles;
    }

    @Deprecated
    public double getLandWaterProportion() {
        return landWaterProportion;
    }

    @Deprecated
    public void setLandWaterProportion(double landWaterProportion) {
        this.landWaterProportion = landWaterProportion;
    }

    @Deprecated
    public void initTerrainNodeField(int terrainTileNodesEdgeCount) {
        terrainNodes = new TerrainNode[terrainTileNodesEdgeCount][terrainTileNodesEdgeCount];
    }

    @Deprecated
    public void insertTerrainNode(int x, int y, TerrainNode terrainNode) {
        terrainNodes[x][y] = terrainNode;
    }

    @Deprecated
    public TerrainNode[][] getTerrainNodes() {
        return terrainNodes;
    }

    @Deprecated
    public void setTerrainNodes(TerrainNode[][] terrainNodes) {
        this.terrainNodes = terrainNodes;
    }

    @Deprecated
    public double getHeight() {
        return height;
    }

    @Deprecated
    public void setHeight(double height) {
        this.height = height;
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
