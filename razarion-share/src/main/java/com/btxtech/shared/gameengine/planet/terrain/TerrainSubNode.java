package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 30.06.2017.
 */
@JsType(isNative = true, name = "TerrainSubNode", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainSubNode {
    public native TerrainSubNode[][] getTerrainSubNodes();

    public native void setTerrainSubNodes(TerrainSubNode[][] terrainSubNodes);

    public native Boolean isLand();

    public native void setLand(Boolean land);

    public native double getHeight();

    public native void setHeight(double height);
}
