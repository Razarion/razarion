package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@JsType(name = "NativeTerrainShapeNode", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainShapeNode {
    public double[] fullDrivewayHeights; // bl, br, tr, tl
    public double[] offsetToOuter; // bl, br, tr, tl
    public Double gameEngineHeight;
    public Double renderEngineHeight;
    public NativeVertex[][] groundSlopeConnections;
    public NativeVertex[][] waterSegments;
    public Double fullWaterLevel;
    public NativeObstacle[] obstacles;
    public Boolean doNotRenderGround;
    public NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes; // bl, br, tr, tl
    public Boolean drivewayBreakingLine;
    public int terrainTypeOrdinal; // Integer is not working here because Integer.intValue() is not defined
    public Boolean fullGameEngineDriveway;
    public Boolean fullRenderEngineDriveway;
}
