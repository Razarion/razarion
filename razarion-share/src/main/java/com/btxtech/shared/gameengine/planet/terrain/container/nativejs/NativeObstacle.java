package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 27.06.2017.
 */
@JsType(name = "NativeObstacle", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeObstacle {
    public Double x1;
    public Double y1;
    public Double x2;
    public Double y2;
    public Double xC;
    public Double yC;
    public Double r;
}
