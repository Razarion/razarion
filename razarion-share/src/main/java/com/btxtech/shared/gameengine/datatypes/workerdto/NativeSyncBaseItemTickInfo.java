package com.btxtech.shared.gameengine.datatypes.workerdto;

import com.btxtech.shared.nativejs.NativeMatrixDto;
import com.btxtech.shared.nativejs.NativeVertexDto;
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
    public NativeMatrixDto model;
    public int baseId;
    public double turretAngle;
    public double spawning;
    public double buildup;
    public double health;
    public double constructing;
    public NativeVertexDto harvestingResourcePosition;
    public NativeVertexDto buildingPosition;
    public NativeVertexDto interpolatableVelocity;
    public int containingItemCount;
    public double maxContainingRadius;
    public boolean contained;
}
