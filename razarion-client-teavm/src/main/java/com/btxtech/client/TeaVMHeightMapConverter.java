package com.btxtech.client;

import com.btxtech.client.jso.JsUint16ArrayWrapper;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.uiservice.terrain.HeightMapConverter;
import org.teavm.jso.JSObject;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * TeaVM-specific implementation that safely converts JSObject-based Uint16Arrays
 * to plain Java int arrays, avoiding WASM-GC illegal cast errors.
 */
@Singleton
public class TeaVMHeightMapConverter implements HeightMapConverter {

    @Inject
    public TeaVMHeightMapConverter() {
    }

    @Override
    public int[] convert(Uint16ArrayEmu heightMap) {
        // Cast to JSObject to bypass interface type checking
        if (heightMap instanceof JSObject) {
            JSObject jsObj = (JSObject) heightMap;
            return JsUint16ArrayWrapper.convertToJavaArray(jsObj);
        }

        // Fallback for non-JSObject implementations (tests, etc.)
        return heightMap.toJavaArray();
    }
}
