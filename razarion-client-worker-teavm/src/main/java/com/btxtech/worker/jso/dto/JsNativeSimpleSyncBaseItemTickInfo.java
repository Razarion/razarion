package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeSimpleSyncBaseItemTickInfo
 * Simplified tick info for killed items
 */
public interface JsNativeSimpleSyncBaseItemTickInfo extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeSimpleSyncBaseItemTickInfo create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSProperty
    int getId();

    @JSProperty
    void setId(int id);

    @JSProperty
    int getItemTypeId();

    @JSProperty
    void setItemTypeId(int itemTypeId);

    @JSProperty
    double getX();

    @JSProperty
    void setX(double x);

    @JSProperty
    double getY();

    @JSProperty
    void setY(double y);

    @JSProperty
    boolean isContained();

    @JSProperty
    void setContained(boolean contained);
}
