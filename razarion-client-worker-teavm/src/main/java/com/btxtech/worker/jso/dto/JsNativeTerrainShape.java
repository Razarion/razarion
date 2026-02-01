package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeTerrainShape
 * Represents the terrain shape data received from server
 */
public interface JsNativeTerrainShape extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeTerrainShape create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    // Use JSBody for 2D array access since @JSProperty doesn't work well with 2D arrays
    @JSBody(script = "return this.nativeTerrainShapeTiles ? this.nativeTerrainShapeTiles.length : 0;")
    int getTilesXLength();

    @JSBody(params = {"x"}, script = "return this.nativeTerrainShapeTiles && this.nativeTerrainShapeTiles[x] ? this.nativeTerrainShapeTiles[x].length : 0;")
    int getTilesYLength(int x);

    @JSBody(params = {"x", "y"}, script = "return this.nativeTerrainShapeTiles && this.nativeTerrainShapeTiles[x] ? this.nativeTerrainShapeTiles[x][y] : null;")
    JsNativeTerrainShapeTile getTile(int x, int y);
}
