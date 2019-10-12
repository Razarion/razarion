package com.btxtech.shared;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 31.03.2017.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class TestTerrainTile extends TerrainTile {
    private int indexX;
    private int indexY;
    private double[] groundVertices;
    private double[] groundNorms;
    private Collection<TestTerrainSlopeTile> terrainSlopeTiles;
    private double landWaterProportion;
    private TestTerrainNode[][] testTerrainNodes;
    private double height;
    private List<TestTerrainTileObjectList> terrainTileObjectLists;

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
    public double[] getGroundPositions() {
        return groundVertices;
    }

    @Override
    public void setGroundPositions(double[] groundPositions) {
        this.groundVertices = groundPositions;
    }

    @Override
    public double[] getGroundNorms() {
        return groundNorms;
    }

    @Override
    public void setGroundNorms(double[] groundNorms) {
        this.groundNorms = groundNorms;
    }

    @Override
    public void addTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        if (terrainSlopeTiles == null) {
            terrainSlopeTiles = new ArrayList<>();
        }
        terrainSlopeTiles.add((TestTerrainSlopeTile) terrainSlopeTile);
    }

    @Override
    public TerrainSlopeTile[] getTerrainSlopeTiles() {
        if (terrainSlopeTiles == null) {
            return null;
        }
        return terrainSlopeTiles.toArray(new TerrainSlopeTile[terrainSlopeTiles.size()]);
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
    public void initTerrainNodeField(int terrainTileNodesEdgeCount) {
        testTerrainNodes = new TestTerrainNode[terrainTileNodesEdgeCount][terrainTileNodesEdgeCount];
    }

    @Override
    public void insertTerrainNode(int x, int y, TerrainNode terrainNode) {
        testTerrainNodes[x][y] = (TestTerrainNode) terrainNode;
    }

    @Override
    public TerrainNode[][] getTerrainNodes() {
        return testTerrainNodes;
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
        if (terrainTileObjectLists == null) {
            return null;
        }
        return terrainTileObjectLists.toArray(new TestTerrainTileObjectList[terrainTileObjectLists.size()]);
    }

    @Override
    public void addTerrainTileObjectList(TerrainTileObjectList terrainTileObjectList) {
        if (terrainTileObjectLists == null) {
            terrainTileObjectLists = new ArrayList<>();
        }
        terrainTileObjectLists.add((TestTerrainTileObjectList)terrainTileObjectList);
    }
}
