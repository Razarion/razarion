package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 03.07.2017.
 */
@JsType(name = "NativeTerrainShapeSubNode", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainShapeSubNode {
    public Boolean notLand;
    public Double height;
    public NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes;
}
