package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 30.06.2017.
 */
@JsType(isNative = true, name = "TerrainNode", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainNode {
    public native void initTerrainSubNodeField(int terrainSubNodeEdgeCount);

    public native TerrainSubNode[][] getTerrainSubNodes();

    public native void insertTerrainSubNode(int x, int y, TerrainSubNode terrainSubNode);

    public native boolean isLand();

    public native void setLand(Boolean land);

    public native double getHeight();

    public native void setHeight(double height);

    public native Integer getTerrainType();

    public native void setTerrainType(Integer terrainType);
}
