package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 27.06.2017.
 */
@JsType(name = "NativeFractionalSlopeSegment", isNative = true, namespace = "com.btxtech.shared.json")
@Deprecated
public class NativeFractionalSlopeSegment {
    public double xI;
    public double yI;
    public double xO;
    public double yO;
    public int index;
    public Double drivewayHeightFactor;
    public double uvY;
    public Double uvYTermination;
}
