package com.btxtech.shared.gameengine.datatypes.workerdto;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 12.01.2018.
 */
@JsType(name = "NativeSyncBaseItemTickInfo", isNative = true, namespace = "com.btxtech.shared.nativejs.workerdto")
public class NativeSyncBaseItemTickInfo {
    public int id;
    public int itemTypeId;
    public double x;
    public double y;
    public double z;
    public double angle;
    public int baseId;
    public double turretAngle;
    public double spawning;
    public double buildup;
    public double health;
    public double constructing;
    public int constructingBaseItemTypeId; // Id or > 0. Integer is not possible
    public NativeDecimalPosition harvestingResourcePosition;
    public NativeDecimalPosition buildingPosition;
    public int containingItemCount;
    public double maxContainingRadius;
    public boolean contained;
    public boolean idle;
}
