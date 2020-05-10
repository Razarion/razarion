package com.btxtech.shared.gameengine.datatypes.workerdto;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 10.01.2018.
 */
@JsType(name = "NativeTickInfo", isNative = true, namespace = "com.btxtech.shared.json.workerdto")
public class NativeTickInfo {
    public int resources;
    public int xpFromKills;
    public int houseSpace;
    public NativeSyncBaseItemTickInfo[] updatedNativeSyncBaseItemTickInfos;
    public NativeSimpleSyncBaseItemTickInfo[] killedSyncBaseItems;
    public int[] removeSyncBaseItemIds;
}
