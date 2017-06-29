package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;


import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 27.06.2017.
 */
@JsType(name = "NativeTerrainShapeTile", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainShapeTile {
    public Double fullWaterLevel;
    public NativeFractionalSlope[] fractionalSlopes;
    public Double uniformGroundHeight;
    public NativeTerrainShapeNode[][] nativeTerrainShapeNodes;
}
