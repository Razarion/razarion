package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@JsType(name = "NativeTerrainShapeNode", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainShapeNode {
    public double[] fullDrivewayHeights; // bl, br, tr, tl
    //public TerrainShapeSubNode[][] terrainShapeSubNodes;
    public Double uniformGroundHeight;
    public NativeVertex[][] groundSlopeConnections;
    public NativeVertex[][] waterSegments;
    public Double fullWaterLevel;
    public NativeObstacle[] obstacles;
    public Boolean hiddenUnderSlope;
    public NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes;
    public Boolean drivewayBreakingLine;
    public Integer terrainTypeOrdinal;
}
