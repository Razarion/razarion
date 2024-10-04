package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 03.07.2017.
 */
@JsType(name = "NativeTerrainShapeSubNode", isNative = true, namespace = "com.btxtech.shared.json")
@Deprecated
public class NativeTerrainShapeSubNode {
    public int terrainTypeOrdinal; // Integer is not working here because Integer.intValue() is not defined
    public Double height;
    public NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes;
    public double[] drivewayHeights; // bl, br, tr, tl
}
