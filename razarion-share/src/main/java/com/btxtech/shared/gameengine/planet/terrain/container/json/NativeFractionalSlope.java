package com.btxtech.shared.gameengine.planet.terrain.container.json;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 27.06.2017.
 */
@JsType(name = "NativeFractionalSlope", isNative = true, namespace = "com.btxtech.shared.json")
@Deprecated
public class NativeFractionalSlope {
    public int slopeConfigId;
    public double groundHeight;
    public boolean inverted;
    public NativeFractionalSlopeSegment[] fractionalSlopeSegments;
}
