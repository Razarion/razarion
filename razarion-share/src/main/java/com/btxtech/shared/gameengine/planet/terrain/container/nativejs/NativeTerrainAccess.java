package com.btxtech.shared.gameengine.planet.terrain.container.nativejs;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * on 27.06.2017.
 */
@JsType(name = "NativeTerrainAccess", isNative = true, namespace = "com.btxtech.shared.nativejs")
public class NativeTerrainAccess {
    @JsFunction
    public interface LoadedCallback {
        void onLoaded(NativeTerrainShape nativeTerrainShape);
    }

    public native void load(LoadedCallback loadedCallback);
}
