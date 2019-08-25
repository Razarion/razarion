package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.nativejs.NativeMatrixFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 31.03.2017.
 */
public class DevToolTerrainTile extends TerrainTile {
    private int indexX;
    private int indexY;
    private int groundVertexCount;
    private double[] groundVertices;
    private double[] groundNorms;
    private double[] groundTangents;
    private double[] groundSplattings;
    private Collection<TerrainSlopeTile> terrainSlopeTiles;
    private TerrainWaterTile terrainWaterTile;
    private double landWaterProportion;
    private TerrainNode[][] terrainNodes;
    private double height;
    private List<DevToolTerrainTileObjectList> terrainTileObjectLists;

    @Override
    public void init(int indexX, int indexY) {
        this.indexX = indexX;
        this.indexY = indexY;
    }

    @Override
    public int getIndexX() {
        return indexX;
    }

    @Override
    public int getIndexY() {
        return indexY;
    }

    @Override
    public double[] getGroundVertices() {
        return groundVertices;
    }

    @Override
    public double[] getGroundNorms() {
        return groundNorms;
    }

    @Override
    public double[] getGroundSplattings() {
        return groundSplattings;
    }

    @Override
    public void initTerrainNodeField(int terrainTileNodesEdgeCount) {
        terrainNodes = new TerrainNode[terrainTileNodesEdgeCount][terrainTileNodesEdgeCount];
    }

    @Override
    public void insertTerrainNode(int x, int y, TerrainNode terrainNode) {
        terrainNodes[x][y] = terrainNode;
    }

    @Override
    public TerrainNode[][] getTerrainNodes() {
        return terrainNodes;
    }

    @Override
    public void setGroundVertexCount(int groundVertexCount) {
        this.groundVertexCount = groundVertexCount;
    }

    @Override
    public int getGroundVertexCount() {
        return groundVertexCount;
    }

    @Override
    public void addTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        if (terrainSlopeTiles == null) {
            terrainSlopeTiles = new ArrayList<>();
        }
        terrainSlopeTiles.add(terrainSlopeTile);
    }

    @Override
    public TerrainSlopeTile[] getTerrainSlopeTiles() {
        if (terrainSlopeTiles == null) {
            return null;
        }
        return terrainSlopeTiles.toArray(new TerrainSlopeTile[terrainSlopeTiles.size()]);
    }

    @Override
    public void setTerrainWaterTile(TerrainWaterTile terrainWaterTile) {
        this.terrainWaterTile = terrainWaterTile;
    }

    @Override
    public TerrainWaterTile getTerrainWaterTile() {
        return terrainWaterTile;
    }

    @Override
    public double getLandWaterProportion() {
        return landWaterProportion;
    }

    @Override
    public void setLandWaterProportion(double landWaterProportion) {
        this.landWaterProportion = landWaterProportion;
    }

    @Override
    public Object toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int fromArray(Object object, NativeMatrixFactory nativeMatrixFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public TerrainTileObjectList[] getTerrainTileObjectLists() {
        if(terrainTileObjectLists == null) {
            return null;
        }
        return terrainTileObjectLists.toArray(new TerrainTileObjectList[terrainTileObjectLists.size()]);
    }

    @Override
    public void addTerrainTileObjectList(TerrainTileObjectList terrainTileObjectList) {
        if(terrainTileObjectLists == null) {
            terrainTileObjectLists = new ArrayList<>();
        }
        terrainTileObjectLists.add((DevToolTerrainTileObjectList) terrainTileObjectList);
    }
}
