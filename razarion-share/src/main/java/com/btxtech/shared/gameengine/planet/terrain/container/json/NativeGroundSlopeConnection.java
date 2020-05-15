package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

@JsType(name = "NativeGroundSlopeConnection", isNative = true, namespace = "com.btxtech.shared.json")
public class NativeGroundSlopeConnection {
    public int groundConfigId;
    public boolean defaultGround;
    public NativeVertex[][] polygons;
}
