package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeTerrainShapeTile
 */
public interface JsNativeTerrainShapeTile extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeTerrainShapeTile create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    // Object lists - use JSBody for safe array access
    @JSBody(script = "return this.nativeTerrainShapeObjectLists ? this.nativeTerrainShapeObjectLists.length : 0;")
    int getObjectListsLength();

    @JSBody(params = {"index"}, script = "return this.nativeTerrainShapeObjectLists ? this.nativeTerrainShapeObjectLists[index] : null;")
    JsNativeTerrainShapeObjectList getObjectList(int index);

    // Babylon decals - use JSBody for safe array access
    @JSBody(script = "return this.nativeBabylonDecals ? this.nativeBabylonDecals.length : 0;")
    int getDecalsLength();

    @JSBody(params = {"index"}, script = "return this.nativeBabylonDecals ? this.nativeBabylonDecals[index] : null;")
    JsNativeBabylonDecal getDecal(int index);

    // Bot grounds - use JSBody for safe array access
    @JSBody(script = "return this.nativeBotGrounds ? this.nativeBotGrounds.length : 0;")
    int getBotGroundsLength();

    @JSBody(params = {"index"}, script = "return this.nativeBotGrounds ? this.nativeBotGrounds[index] : null;")
    JsNativeBotGround getBotGround(int index);
}
