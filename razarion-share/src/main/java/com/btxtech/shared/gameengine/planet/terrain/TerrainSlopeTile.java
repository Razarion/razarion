package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 03.04.2017.
 */
@JsType(isNative = true, name = "TerrainSlopeTile", namespace = "com.btxtech.shared.nativejs")
public abstract class TerrainSlopeTile {
//    private int slopeConfigId;
//    private SlopeGeometry outerSlopeGeometry;
//    private SlopeGeometry centerSlopeGeometry;
//    private SlopeGeometry innerSlopeGeometry;

    public native int getSlopeConfigId();

    public native void setSlopeConfigId(int slopeConfigId);

    public native SlopeGeometry getOuterSlopeGeometry();

    public native void setOuterSlopeGeometry(SlopeGeometry outerSlopeGeometry);

    public native SlopeGeometry getCenterSlopeGeometry();

    public native void setCenterSlopeGeometry(SlopeGeometry centerSlopeGeometry);

    public native SlopeGeometry getInnerSlopeGeometry();

    public native void setInnerSlopeGeometry(SlopeGeometry innerSlopeGeometry);
}
