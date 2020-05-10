package com.btxtech.shared.nativejs;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 12.01.2018.
 */
@JsType(name = "NativeMatrixDto", isNative = true, namespace = "com.btxtech.shared.json")
public class NativeMatrixDto {
    public double[] numbers;
}
