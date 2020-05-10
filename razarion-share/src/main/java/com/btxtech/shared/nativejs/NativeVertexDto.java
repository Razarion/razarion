package com.btxtech.shared.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 12.01.2018.
 */
@JsType(name = "NativeVertexDto", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeVertexDto {
    public double x;
    public double y;
    public double z;
}
