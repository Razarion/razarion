package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 19.01.2018.
 */
@JsType(name = "NativeTerrainShapeObjectPosition", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainShapeObjectPosition {
    public double x;
    public double y;
    public double scale;
    public double rotationZ;
}
