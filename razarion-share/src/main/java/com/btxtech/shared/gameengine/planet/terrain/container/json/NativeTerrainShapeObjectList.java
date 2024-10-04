package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 18.01.2018.
 */
@JsType(name = "NativeTerrainShapeObjectList", namespace = "com.btxtech.shared.json")
public class NativeTerrainShapeObjectList {
    public int terrainObjectConfigId;
    public NativeTerrainShapeObjectPosition[] terrainShapeObjectPositions;
}
