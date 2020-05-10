package com.btxtech.shared.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 24.03.2017.
 */
@JsType(name = "NativeMatrix", isNative = true, namespace = "com.btxtech.shared.json")
public class NativeMatrix {
    public native NativeMatrix multiply(NativeMatrix other);

    public native NativeVertexDto multiplyVertex(NativeVertexDto other, double w);

    public native double[] toColumnMajorArray();

    public native NativeMatrix invert();

    public native NativeMatrix transpose();

    public native NativeMatrixFactory getNativeMatrixFactory();

    @Override
    public native String toString();

}
