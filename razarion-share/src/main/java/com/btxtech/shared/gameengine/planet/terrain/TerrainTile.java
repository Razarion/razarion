package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import java.util.Map;

/**
 * Created by Beat
 * 28.03.2017.
 */
@JsType
public class TerrainTile {
    private GroundTerrainTile[] groundTerrainTiles;
    private Index index;
    private TerrainWaterTile[] terrainWaterTiles;
    private TerrainSlopeTile[] terrainSlopeTiles;
    private double landWaterProportion;
    private TerrainNode[][] terrainNodes;
    private double height;
    private TerrainTileObjectList[] terrainTileObjectLists;

    public Index getIndex() {
        return index;
    }

    @JsIgnore
    public void setIndex(Index index) {
        this.index = index;
    }

    public GroundTerrainTile[] getGroundTerrainTiles() {
        return groundTerrainTiles;
    }

    public void setGroundTerrainTiles(GroundTerrainTile[] groundTerrainTiles) {
        this.groundTerrainTiles = groundTerrainTiles;
    }

    public TerrainSlopeTile[] getTerrainSlopeTiles() {
        return terrainSlopeTiles;
    }

    public void setTerrainSlopeTiles(TerrainSlopeTile[] terrainSlopeTiles) {
        this.terrainSlopeTiles = terrainSlopeTiles;
    }

    public TerrainWaterTile[] getTerrainWaterTiles() {
        return terrainWaterTiles;
    }

    public void setTerrainWaterTiles(TerrainWaterTile[] terrainWaterTiles) {
        this.terrainWaterTiles = terrainWaterTiles;
    }

    public double getLandWaterProportion() {
        return landWaterProportion;
    }

    public void setLandWaterProportion(double landWaterProportion) {
        this.landWaterProportion = landWaterProportion;
    }

    public void initTerrainNodeField(int terrainTileNodesEdgeCount) {
        terrainNodes = new TerrainNode[terrainTileNodesEdgeCount][terrainTileNodesEdgeCount];
    }

    public void insertTerrainNode(int x, int y, TerrainNode terrainNode) {
        terrainNodes[x][y] = terrainNode;
    }

    public TerrainNode[][] getTerrainNodes() {
        return terrainNodes;
    }

    public void setTerrainNodes(TerrainNode[][] terrainNodes) {
        this.terrainNodes = terrainNodes;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public TerrainTileObjectList[] getTerrainTileObjectLists() {
        return terrainTileObjectLists;
    }

    public void setTerrainTileObjectLists(TerrainTileObjectList[] terrainTileObjectLists) {
        this.terrainTileObjectLists = terrainTileObjectLists;
    }
}
