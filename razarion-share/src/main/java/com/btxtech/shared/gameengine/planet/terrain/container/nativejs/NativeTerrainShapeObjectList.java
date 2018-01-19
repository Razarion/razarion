package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 18.01.2018.
 */
@JsType(name = "NativeTerrainShapeObjectList", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainShapeObjectList {
    public int terrainObjectId;
    public NativeTerrainShapeObjectPosition[] positions;
}
