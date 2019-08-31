package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import com.btxtech.shared.datatypes.Vertex;
import jsinterop.annotations.JsType;

import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@JsType(name = "NativeTerrainShapeNode", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainShapeNode {
    public double[] fullDrivewayHeights; // bl, br, tr, tl
    public double[] renderWaterOffsetToOuter; // bl, br, tr, tl
    public Double innerGroundHeight;
    public Map<Integer, List<List<Vertex>>> groundSlopeConnections; // TODO Map not working here
    public Map<Integer, List<List<Vertex>>> waterSegments; // TODO Map not working here
    public Integer renderInnerSlopeId; // TODO  Integer is not working here because Integer.intValue() is not defined
    public boolean renderHideGround;
    public Integer renderInnerWaterSlopeId; // TODO  Integer is not working here because Integer.intValue() is not defined
    public Double fullWaterLevel;
    public NativeObstacle[] obstacles;
    public NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes; // bl, br, tr, tl
    public Boolean drivewayBreakingLine;
    public int terrainTypeOrdinal; // Integer is not working here because Integer.intValue() is not defined
    public Boolean fullGameEngineDriveway;
    public Boolean fullRenderEngineDriveway;
}
