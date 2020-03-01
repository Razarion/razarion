package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 28.03.2017.
 */
public class TerrainTile {
    private Index index;
    private Float32ArrayEmu groundPositions;
    private Float32ArrayEmu groundNorms;
    private Map<Integer, Float32ArrayEmu> groundSlopePositions;
    private Map<Integer, Float32ArrayEmu> groundSlopeNorms;
    private Collection<TerrainWaterTile> terrainWaterTiles;
    private Collection<TerrainSlopeTile> terrainSlopeTiles;
    private double landWaterProportion;
    private TerrainNode[][] terrainNodes;
    private double height;
    private Collection<TerrainTileObjectList> terrainTileObjectLists;


    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    public Float32ArrayEmu getGroundPositions() {
        return groundPositions;
    }

    public void setGroundPositions(Float32ArrayEmu groundPositions) {
        this.groundPositions = groundPositions;
    }

    public Float32ArrayEmu getGroundNorms() {
        return groundNorms;
    }

    public void setGroundNorms(Float32ArrayEmu groundNorms) {
        this.groundNorms = groundNorms;
    }

    public Map<Integer, Float32ArrayEmu> getGroundSlopePositions() {
        return groundSlopePositions;
    }

    public void setGroundSlopePositions(Map<Integer, Float32ArrayEmu> groundSlopePositions) {
        this.groundSlopePositions = groundSlopePositions;
    }

    public Map<Integer, Float32ArrayEmu> getGroundSlopeNorms() {
        return groundSlopeNorms;
    }

    public void setGroundSlopeNorms(Map<Integer, Float32ArrayEmu> groundSlopeNorms) {
        this.groundSlopeNorms = groundSlopeNorms;
    }

    public void setTerrainWaterTiles(List<TerrainWaterTile> terrainWaterTiles) {
        this.terrainWaterTiles = terrainWaterTiles;
    }

    public Collection<TerrainWaterTile> getTerrainWaterTiles() {
        return terrainWaterTiles;
    }

    public void addTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        if (terrainSlopeTiles == null) {
            terrainSlopeTiles = new ArrayList<>();
        }
        terrainSlopeTiles.add(terrainSlopeTile);
    }

    public TerrainSlopeTile[] getTerrainSlopeTiles() {
        if (terrainSlopeTiles == null) {
            return null;
        }
        return terrainSlopeTiles.toArray(new TerrainSlopeTile[0]);
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

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }

    public TerrainTileObjectList[] getTerrainTileObjectLists() {
        if (terrainTileObjectLists == null) {
            return null;
        }
        return terrainTileObjectLists.toArray(new TerrainTileObjectList[0]);
    }

    public void addTerrainTileObjectList(TerrainTileObjectList terrainTileObjectList) {
        if (terrainTileObjectLists == null) {
            terrainTileObjectLists = new ArrayList<>();
        }
        terrainTileObjectLists.add(terrainTileObjectList);
    }
}
