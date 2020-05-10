package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@JsType(name = "NativeTerrainShape", isNative = true, namespace = "com.btxtech.shared.json")
public class NativeTerrainShape {
    public int tileXCount;

    public int tileYCount;

    public int tileXOffset;

    public int tileYOffset;

    public NativeTerrainShapeTile[][] nativeTerrainShapeTiles;

}
