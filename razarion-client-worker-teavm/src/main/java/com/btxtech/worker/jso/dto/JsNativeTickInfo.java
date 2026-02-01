package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeTickInfo
 * Used for worker-to-main thread communication
 */
public interface JsNativeTickInfo extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeTickInfo create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSProperty
    int getResources();

    @JSProperty
    void setResources(int resources);

    @JSProperty
    int getXpFromKills();

    @JSProperty
    void setXpFromKills(int xpFromKills);

    @JSProperty
    int getHouseSpace();

    @JSProperty
    void setHouseSpace(int houseSpace);

    @JSProperty
    JsNativeSyncBaseItemTickInfo[] getUpdatedNativeSyncBaseItemTickInfos();

    @JSProperty
    void setUpdatedNativeSyncBaseItemTickInfos(JsNativeSyncBaseItemTickInfo[] infos);

    @JSProperty
    JsNativeSimpleSyncBaseItemTickInfo[] getKilledSyncBaseItems();

    @JSProperty
    void setKilledSyncBaseItems(JsNativeSimpleSyncBaseItemTickInfo[] killed);

    @JSProperty
    int[] getRemoveSyncBaseItemIds();

    @JSProperty
    void setRemoveSyncBaseItemIds(int[] ids);
}
