package com.btxtech.shared.gameengine.planet.terrain.container.json;


import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 27.06.2017.
 */
@JsType(name = "NativeTerrainShapeTile", isNative = true, namespace = "com.btxtech.shared.json")
public class NativeTerrainShapeTile {
    public NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists;
    public NativeBabylonDecal[] nativeBabylonDecals;
}
