package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 30.06.2017.
 */
@JsType(isNative = true, name = "TerrainSubNode", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainSubNode {
    public native void initTerrainSubNodeField(int terrainSubNodeEdgeCount);

    public native TerrainSubNode[][] getTerrainSubNodes();

    public native void insertTerrainSubNode(int x, int y, TerrainSubNode terrainSubNode);

    public native Boolean isLand();

    public native void setLand(Boolean land);

    public native double getHeight();

    public native void setHeight(double height);

    public native int getTerrainType(); // Integer is not working here because Integer.intValue() is not defined

    public native void setTerrainType(int terrainType); // Integer is not working here because Integer.intValue() is not defined
}
