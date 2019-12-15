package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.nativejs.NativeMatrixFactory;
import jsinterop.annotations.JsType;

import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 28.03.2017.
 */
@JsType(isNative = true, name = "TerrainTile", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainTile {
    private Map<Integer, double[]> groundSlopeVertices; // TODO remove if used in Javascript Interop GWT
    private Map<Integer, double[]> groundSlopeNorms; // TODO remove if used in Javascript Interop GWT
    private List<TerrainWaterTile> terrainWaterTiles;

    public native void init(int indexX, int indexY);

    public native int getIndexX();

    public native int getIndexY();

    public native void setGroundPositions(double[] groundPositions);

    public native double[] getGroundPositions();

    public native void setGroundNorms(double[] groundNorms);

    public native double[] getGroundNorms();

    public Map<Integer, double[]> getGroundSlopeVertices() {
        return groundSlopeVertices;
    }

    public void setGroundSlopeVertices(Map<Integer, double[]> groundSlopeVertices) {
        this.groundSlopeVertices = groundSlopeVertices;
    }

    public Map<Integer, double[]> getGroundSlopeNorms() {
        return groundSlopeNorms;
    }

    public void setGroundSlopeNorms(Map<Integer, double[]> groundSlopeNorms) {
        this.groundSlopeNorms = groundSlopeNorms;
    }

    public native void addTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile);

    public native TerrainSlopeTile[] getTerrainSlopeTiles();

    public void setTerrainWaterTiles(List<TerrainWaterTile> terrainWaterTiles) {
        this.terrainWaterTiles = terrainWaterTiles;
    }

    public List<TerrainWaterTile> getTerrainWaterTiles() {
        return terrainWaterTiles;
    }

    public native double getLandWaterProportion();

    public native void setLandWaterProportion(double landWaterProportion);

    public native void setHeight(double height);

    public native double getHeight();

    public native void initTerrainNodeField(int terrainTileNodesEdgeCount);

    public native void insertTerrainNode(int x, int y, TerrainNode terrainNode);

    public native TerrainNode[][] getTerrainNodes();

    public native Object toArray();

    public native int fromArray(Object object, NativeMatrixFactory nativeMatrixFactory);

    public native TerrainTileObjectList[] getTerrainTileObjectLists();

    public native void addTerrainTileObjectList(TerrainTileObjectList terrainTileObjectList);
}
