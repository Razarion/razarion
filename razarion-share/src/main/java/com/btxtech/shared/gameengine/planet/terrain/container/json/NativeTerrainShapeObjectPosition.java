package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 19.01.2018.
 */
@JsType(name = "NativeTerrainShapeObjectPosition", isNative = true, namespace = "com.btxtech.shared.json")
public class NativeTerrainShapeObjectPosition {
    public double x;
    public double y;
    public NativeVertex scale;
    public NativeVertex rotation;
    public NativeVertex offset;
}