package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 30.06.2017.
 */
@JsType(isNative = true, name = "TerrainNode", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainNode {
    public native TerrainSubNode[][] getTerrainSubNodes();

    public native void setTerrainSubNode(TerrainSubNode[][] terrainSubNodes);

    public native boolean isLand();

    public native void setLand(Boolean land);

    public native double getHeight();
}
