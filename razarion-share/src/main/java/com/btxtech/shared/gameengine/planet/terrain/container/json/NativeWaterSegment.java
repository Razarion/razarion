package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@JsType(name = "NativeWaterSegment", isNative = true, namespace = "com.btxtech.shared.json")
public class NativeWaterSegment {
    public int groundConfigId;
    public NativeVertex[][] polygons;
}
