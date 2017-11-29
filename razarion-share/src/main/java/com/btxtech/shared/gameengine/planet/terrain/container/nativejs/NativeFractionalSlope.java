package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 27.06.2017.
 */
@JsType(name = "NativeFractionalSlope", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeFractionalSlope {
    public int slopeSkeletonConfigId;
    public double groundHeight;
    public boolean inverted;
    public NativeFractionalSlopeSegment[] fractionalSlopeSegments;
}
