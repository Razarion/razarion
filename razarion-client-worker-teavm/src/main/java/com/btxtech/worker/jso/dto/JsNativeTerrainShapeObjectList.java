package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeTerrainShapeObjectList
 */
public interface JsNativeTerrainShapeObjectList extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeTerrainShapeObjectList create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSBody(script = "return this.terrainObjectConfigId || 0;")
    int getTerrainObjectConfigId();

    // Positions - use JSBody for safe array access
    @JSBody(script = "return this.terrainShapeObjectPositions ? this.terrainShapeObjectPositions.length : 0;")
    int getPositionsLength();

    @JSBody(params = {"index"}, script = "return this.terrainShapeObjectPositions ? this.terrainShapeObjectPositions[index] : null;")
    JsNativeTerrainShapeObjectPosition getPosition(int index);
}
