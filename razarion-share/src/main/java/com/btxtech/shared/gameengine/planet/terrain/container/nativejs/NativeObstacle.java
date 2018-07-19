package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import com.btxtech.shared.datatypes.DecimalPosition;
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
    public Double pDx;
    public Double pDy;
    public Boolean p1C;
    public Double p1Dx;
    public Double p1Dy;
    public Boolean p2C;
    public Double p2Dx;
    public Double p2Dy;
    public Double xC;
    public Double yC;
    public Double r;
}
