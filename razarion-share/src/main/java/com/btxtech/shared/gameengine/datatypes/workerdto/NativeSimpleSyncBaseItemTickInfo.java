package com.btxtech.shared.gameengine.datatypes.workerdto;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 12.01.2018.
 */
@JsType(name = "NativeSimpleSyncBaseItemTickInfo", isNative = true, namespace = "com.btxtech.shared.nativejs.workerdto")
public class NativeSimpleSyncBaseItemTickInfo {
    public int id;
    public int itemTypeId;
    public boolean contained;
    public double x;
    public double y;
}
