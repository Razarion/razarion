package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 28.03.2017.
 */
@JsType(isNative = true, name = "TerrainTile", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainTile {
    public native void init(int indexX, int indexY);

    public native void initGroundArrays(int groundSizeVec, int groundSizeScalar, int nodes);

    public native void setGroundTriangleCorner(int triangleCornerIndex, double vertexX, double vertexY, double vertexZ, double normX, double normY, double normZ, double tangentX, double tangentY, double tangentZ, double splatting);

    public native int getIndexX();

    public native int getIndexY();

    public native double[] getGroundVertices();

    public native double[] getGroundNorms();

    public native double[] getGroundTangents();

    public native double[] getGroundSplattings();

    public native void setGroundVertexCount(int groundVertexCount);

    public native int getGroundVertexCount();

    public native void addTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile);

    public native TerrainSlopeTile[] getTerrainSlopeTiles();

    public native void setTerrainWaterTile(TerrainWaterTile terrainWaterTile);

    public native TerrainWaterTile getTerrainWaterTile();

    public native double getLandWaterProportion();

    public native void setLandWaterProportion(double landWaterProportion);

    public native void setHeight(double height);

    public native double getHeight();

    public native void initTerrainNodeField(int terrainTileNodesEdgeCount);

    public native void insertTerrainNode(int x, int y, TerrainNode terrainNode);

    public native TerrainNode[][] getTerrainNodes();

    public native Object toArray();

    public native int fromArray(Object object);

}
