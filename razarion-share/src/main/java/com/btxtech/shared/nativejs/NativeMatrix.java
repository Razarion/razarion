package com.btxtech.shared.nativejs;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 24.03.2017.
 */
@JsType(name = "NativeMatrix", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeMatrix {
    public native NativeMatrix multiply(NativeMatrix other);

    public native NativeVertexDto multiplyVertex(NativeVertexDto other, double w);

    public native Float32ArrayEmu getColumnMajorFloat32Array();

    public native NativeMatrix invert();

    public native NativeMatrix transpose();

    public native NativeMatrixFactory getNativeMatrixFactory();

    @Override
    public native String toString();

}
