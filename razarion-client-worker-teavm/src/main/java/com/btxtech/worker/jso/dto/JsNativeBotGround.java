package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeBotGround
 */
public interface JsNativeBotGround extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeBotGround create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSBody(script = "return this.model3DId || 0;")
    int getModel3DId();

    @JSBody(script = "return this.height || 0;")
    double getHeight();

    // Positions - use JSBody for safe array access
    @JSBody(script = "return this.positions ? this.positions.length : 0;")
    int getPositionsLength();

    @JSBody(params = {"index"}, script = "return this.positions ? this.positions[index] : null;")
    JsNativeDecimalPosition getPosition(int index);

    // Slope boxes - use JSBody for safe array access
    @JSBody(script = "return this.botGroundSlopeBoxes ? this.botGroundSlopeBoxes.length : 0;")
    int getSlopeBoxesLength();

    @JSBody(params = {"index"}, script = "return this.botGroundSlopeBoxes ? this.botGroundSlopeBoxes[index] : null;")
    JsNativeBotGroundSlopeBox getSlopeBox(int index);
}
