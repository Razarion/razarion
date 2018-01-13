package com.btxtech.shared.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 24.03.2017.
 */
@JsType(name = "NativeMatrixFactory", isNative = true, namespace = "com.btxtech.shared.nativejs")
public abstract class NativeMatrixFactory {
    public native NativeMatrix createFromColumnMajorArray(double[] array);

    public native NativeMatrix createTranslation(double x, double y, double z);

    public native NativeMatrix createScale(double x, double y, double z);

    public native NativeMatrix createXRotation(double rad);

    public native NativeMatrix createYRotation(double rad);

    public native NativeMatrix createZRotation(double rad);

    public native NativeMatrix createFromNativeMatrixDto(NativeMatrixDto nativeMatrixDto);

    public native NativeMatrixDto createNativeMatrixDtoColumnMajorArray(double array[]);

    public native int[] intArrayConverter(int[] ints);
}
