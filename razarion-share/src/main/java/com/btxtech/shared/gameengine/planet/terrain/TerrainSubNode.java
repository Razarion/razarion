package com.btxtech.shared.gameengine.planet.terrain;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 30.06.2017.
 */
@JsType(isNative = true, name = "TerrainSubNode", namespace = "com.btxtech.shared.nativejs")
public class TerrainSubNode {
    public native TerrainSubNode[][] getTerrainSubNodes();

    public native boolean isLand();

    public native double getHeight();
}
