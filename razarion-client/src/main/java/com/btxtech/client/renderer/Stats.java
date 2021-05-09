package com.btxtech.client.renderer;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * https://github.com/mrdoob/stats.js
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Stats {
    public native void begin();

    public native void end();
}
